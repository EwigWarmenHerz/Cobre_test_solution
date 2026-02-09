package com.taller.cobre.domain.use_case;

import com.taller.cobre.domain.model.client.ClientRouting;
import com.taller.cobre.domain.model.enums.NotificationStatus;
import com.taller.cobre.domain.model.event.EventDomain;
import com.taller.cobre.domain.model.exceptions.BusinessException;
import com.taller.cobre.domain.model.exceptions.TechnicalException;
import com.taller.cobre.domain.model.mappers.DomainMapper;
import com.taller.cobre.domain.model.notification.AuditLog;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.reactive_web.dtos.NotificationResponse;
import com.taller.cobre.infrastructure.output_adapters.cache_adapter.CacheRepository;
import com.taller.cobre.infrastructure.output_adapters.notification_registry.repository.NotificationLogsRepository;
import com.taller.cobre.infrastructure.output_adapters.queue_producer.QueueProducer;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.dtos.NotificationWithEvent;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Client;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.ClientRepository;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.EventRepository;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.NotificationRepository;
import com.taller.cobre.infrastructure.output_adapters.web_client.NotificationWebhookConnector;
import com.taller.cobre.domain.model.client.WebhookResponse;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationUseCase implements NotificationGateway {
    private final ClientRepository clientRepository;
    private final CacheRepository cacheAdapterRepository;
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogsRepository logsRepository;
    private final QueueProducer queueProducer;
    private final NotificationWebhookConnector webClientConnector;
    private final DomainMapper mapper;

    public NotificationUseCase(ClientRepository clientRepository, CacheRepository cacheAdapterRepository, EventRepository eventRepository, NotificationRepository notificationRepository, NotificationLogsRepository logsRepository, QueueProducer queueProducer, NotificationWebhookConnector webClientConnector, DomainMapper mapper) {
        this.clientRepository = clientRepository;
        this.cacheAdapterRepository = cacheAdapterRepository;
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
        this.logsRepository = logsRepository;
        this.queueProducer = queueProducer;
        this.webClientConnector = webClientConnector;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> sendNotification(EventDomain eventDomain) {
        return Mono.just(eventDomain)
            .flatMap(this::saveEvent)
            .flatMap(savedEventDomain -> findClientRoutingData(savedEventDomain.clientId())
                .flatMap(client -> {
                    if (client.isSubscribedTo(savedEventDomain.eventType())) {
                        return processNotificationDelivery(savedEventDomain, client);
                    }
                    log.info("Cliente {} no suscrito al evento {}", client.id(), savedEventDomain.eventType());
                    return Mono.empty();
                }))
            .onErrorResume(e -> {
                log.error("Fallo crítico en el flujo de notificación para evento: {}", eventDomain.eventType(), e);
                return Mono.empty();
            });
    }

    @Override
    public Mono<NotificationResponse> getNotificationById(String secretKey, String notificationId) {
        return checkIfSecretKeyBelongsToClient(secretKey)
            .flatMap(client -> notificationRepository.findByNotificationIdWithEventData(Long.valueOf(notificationId))
                .map(NotificationUseCase::mapTpNotificationResponse));
    }

    @Override
    public Flux<NotificationResponse> getAllNotifications(String secretKey) {
        return clientRepository.findBySecretKey(secretKey)
            .switchIfEmpty(Mono.error(new BusinessException("Credenciales inválidas")))
            .flatMapMany(client ->
                notificationRepository.findAllWithEventDataByClientId((long)client.getId())
                    .map(NotificationUseCase::mapTpNotificationResponse)
            );
    }

    @Override
    public Mono<Boolean> retryNotification(String secretKey, long eventId) {
        return checkIfSecretKeyBelongsToClient(secretKey)
            .flatMap(client -> getOriginalEvent(eventId)
                .flatMap(this::sendNotification)
                .map(unused -> true)
                .onErrorResume(e -> {
                    log.error("Fallo en el reintento de notificación para evento {}: {}", eventId, e.getMessage());
                    return Mono.just(false);
                })
            );
    }

    private static NotificationResponse mapTpNotificationResponse(NotificationWithEvent notificationWithEvent) {
        return NotificationResponse.builder()
            .id(notificationWithEvent.id())
            .status(notificationWithEvent.status())
            .tries(notificationWithEvent.tries())
            .updatedAt(notificationWithEvent.updatedAt())
            .jsonNode(notificationWithEvent.details())
            .build();
    }


    @Nonnull
    private Mono<Client> checkIfSecretKeyBelongsToClient(String secretKey) {
        return clientRepository.findBySecretKey(secretKey)
            .switchIfEmpty(Mono.error(new BusinessException("Client does not exist or secret key is wrong")));
    }




    private Mono<EventDomain> saveEvent(EventDomain eventDomain) {
        var eventEntity = mapper.toEventEntity(eventDomain).withCreatedAt(LocalDateTime.now());
        return eventRepository.save(eventEntity)
            .map(savedEvent -> eventDomain.withId(savedEvent.getId()))
            .onErrorMap(e -> new TechnicalException("Database connection exception", e));
    }

    private Mono<ClientRouting> findClientRoutingData(long clientId) {
        return cacheAdapterRepository.getClientRouting(clientId)
            .onErrorResume(e -> {
                log.warn("Error reading client data from cache {}, checking on disk database", clientId, e);
                return Mono.empty();
            })
            .switchIfEmpty(clientRepository.findById(clientId)
                .map(mapper::toClientRouting)
                .flatMap(routing -> cacheAdapterRepository.cacheClient(routing)
                    .onErrorResume(e -> {
                        log.warn("Could not update client's data to cache db {}", clientId);
                        return Mono.empty();
                    })
                    .thenReturn(routing)))
            .switchIfEmpty(Mono.error(new BusinessException("Could not find client in the database " + clientId)));
    }

    private Mono<Void> processNotificationDelivery(EventDomain eventDomain, ClientRouting clientRouting) {
        var initialNotification = createInitialNotification(eventDomain, clientRouting);

        return notificationRepository.save(initialNotification)
            .onErrorMap(e -> new TechnicalException("Failed to save notification", e))
            .flatMap(notification -> webClientConnector.sendNotification(clientRouting.url(), clientRouting.secretKey(), eventDomain.details())
                .flatMap(webhookResponse -> {
                    var nextStatus = webhookResponse.isSuccess() ? NotificationStatus.SENT : NotificationStatus.RETRYING;
                    if (webhookResponse.isSuccess()) {
                        return updateStatus(notification, webhookResponse, nextStatus)
                            .onErrorResume(e -> {
                                log.error("Fail to update success notification on DB {}", notification.getId(), e);
                                return Mono.empty();
                            });
                    } else {
                        var updatedNotification = notification.withTries(notification.getTries() + 1);
                        return updateStatus(updatedNotification, webhookResponse, nextStatus)
                            .onErrorResume(e -> {
                                log.error("Failed to update notification retry status on database", e);
                                return Mono.empty();
                            })
                            .then(sendToAuxQueue(mapper.toNotificationDomain(updatedNotification)));
                    }
                }));
    }

    private Mono<Void> updateStatus(Notification notification, WebhookResponse response, NotificationStatus nextStatus) {
        var updatedNotification = notification
            .withStatus(nextStatus)
            .withUpdatedAt(LocalDateTime.now());

        return notificationRepository.save(updatedNotification)
            .flatMap(savedNotification -> {
                var auditLog = createAuditLog(notification, response, savedNotification, nextStatus);
                return logsRepository.saveLog(auditLog)
                    .onErrorResume(e -> {
                        log.error("Error persistiendo auditoría en Dynamo para notif {}", savedNotification.getId(), e);
                        return Mono.empty();
                    });
            })
            .then();
    }

    private Mono<Void> sendToAuxQueue(NotificationDomain notification) {
        return Mono.defer(() -> {
            if (notification.canRetry()) {
                return queueProducer.sendToRetry(notification);
            } else {
                return queueProducer.sendToDLQ(notification.updateForDLQ());
            }
        }).onErrorResume(e -> {
            log.error("FALLO CRÍTICO: No se pudo enviar notificación {} a ninguna cola de SQS", notification.id(), e);
            return Mono.empty();
        });
    }

    private Notification createInitialNotification(EventDomain event, ClientRouting client) {
        return Notification.builder()
            .eventId(event.id())
            .clientId(client.id())
            .status(NotificationStatus.PENDING)
            .tries(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private AuditLog createAuditLog(Notification notification, WebhookResponse response, Notification savedNotification, NotificationStatus nextStatus) {
        return new AuditLog(
            String.valueOf(savedNotification.getId()),
            String.valueOf(notification.getClientId()),
            nextStatus,
            String.valueOf(response.statusCode()),
            response.responseBody(),
            response.isSuccess() ? null : "Webhook failed with status " + response.statusCode(),
            LocalDateTime.now()
        );
    }

    private Mono<EventDomain> getOriginalEvent(Long notificationId){
        return notificationRepository.findById(notificationId)
            .flatMap(notification -> eventRepository.findById(notification.getEventId())
                .map(mapper::toEventDomain)
                .switchIfEmpty(Mono.error(new BusinessException("El evento original " + notification.getEventId() + " no existe.")))
                .doOnNext(event -> log.debug("Evento original recuperado: {}", event.id())));
    }
}
