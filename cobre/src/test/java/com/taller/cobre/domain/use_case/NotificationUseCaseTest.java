package com.taller.cobre.domain.use_case;

import com.taller.cobre.domain.model.client.ClientRouting;
import com.taller.cobre.domain.model.client.WebhookResponse;
import com.taller.cobre.domain.model.enums.NotificationStatus;
import com.taller.cobre.domain.model.event.EventDomain;
import com.taller.cobre.domain.model.event.EventType;
import com.taller.cobre.domain.model.mappers.DomainMapper;
import com.taller.cobre.domain.model.notification.NotificationDomain;
import com.taller.cobre.infrastructure.entry_points.sqs_listener.dtos.EventMessage;
import com.taller.cobre.infrastructure.output_adapters.cache_adapter.CacheRepository;
import com.taller.cobre.infrastructure.output_adapters.notification_registry.repository.NotificationLogsRepository;
import com.taller.cobre.infrastructure.output_adapters.queue_producer.QueueProducer;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.dtos.NotificationWithEvent;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Client;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Event;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Notification;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.ClientRepository;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.EventRepository;
import com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository.NotificationRepository;
import com.taller.cobre.infrastructure.output_adapters.web_client.NotificationWebhookConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationUseCaseTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock private CacheRepository cacheAdapterRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationLogsRepository logsRepository;
    @Mock private QueueProducer queueProducer;
    @Mock private NotificationWebhookConnector webClientConnector;
    @Mock private DomainMapper mapper;

    @InjectMocks
    private NotificationUseCase notificationUseCase;

    private EventDomain eventDomain;
    private ClientRouting clientRouting;

    @BeforeEach
    void setUp() {
        eventDomain = new EventDomain(1L, 100L, EventType.CREDIT, null, LocalDateTime.now());
        clientRouting = new ClientRouting(100, "http://webhook.com", "key_123", Set.of(EventType.CREDIT));
    }

    @Test
    void sendNotification_Success() {

        when(mapper.toEventEntity(any())).thenReturn(new Event());
        when(eventRepository.save(any())).thenReturn(Mono.just(new Event().withId(1L)));


        when(cacheAdapterRepository.getClientRouting(anyLong())).thenReturn(Mono.empty());
        when(clientRepository.findById(anyLong())).thenReturn(Mono.just(new Client()));
        when(mapper.toClientRouting(any())).thenReturn(clientRouting);
        when(cacheAdapterRepository.cacheClient(any())).thenReturn(Mono.just(clientRouting).then());


        var initialNotif = Notification.builder().id(500L).clientId(100L).tries(0).build();
        when(notificationRepository.save(any())).thenReturn(Mono.just(initialNotif));


        when(webClientConnector.sendNotification(anyString(), anyString(), any()))
            .thenReturn(Mono.just(new WebhookResponse(200, "OK", true)));
        when(logsRepository.saveLog(any())).thenReturn(Mono.empty());


        var result = notificationUseCase.sendNotification(eventDomain);
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void sendNotification_WebhookFails_SendsToRetryQueue() {
        when(mapper.toEventEntity(any())).thenReturn(new Event());
        when(eventRepository.save(any())).thenReturn(Mono.just(new Event().withId(1L)));

        when(cacheAdapterRepository.getClientRouting(anyLong())).thenReturn(Mono.empty());
        when(clientRepository.findById(anyLong())).thenReturn(Mono.just(new Client()));
        when(mapper.toClientRouting(any())).thenReturn(clientRouting);
        when(cacheAdapterRepository.cacheClient(any())).thenReturn(Mono.just(clientRouting).then());

        var initialNotif = Notification.builder().id(500L).clientId(100L).tries(0).status(NotificationStatus.PENDING).build();
        when(notificationRepository.save(any())).thenReturn(Mono.just(initialNotif));

        when(webClientConnector.sendNotification(anyString(), anyString(), any()))
            .thenReturn(Mono.just(new WebhookResponse(500, "Internal Error", false)));

        when(logsRepository.saveLog(any())).thenReturn(Mono.empty());
        var domainNotif = new NotificationDomain(500L, 100L, 1L, NotificationStatus.RETRYING, 1, null, null);
        when(mapper.toNotificationDomain(any())).thenReturn(domainNotif);

        when(queueProducer.sendToRetry(any())).thenReturn(Mono.empty());

        StepVerifier.create(notificationUseCase.sendNotification(eventDomain))
            .verifyComplete();

        verify(queueProducer, times(1)).sendToRetry(any());
    }

    @Test
    void getNotificationById_Success() {

        Client mockClient = new Client();
        mockClient.setId(100);
        when(clientRepository.findBySecretKey("valid_key")).thenReturn(Mono.just(mockClient));


        NotificationWithEvent projection = new NotificationWithEvent(
            500L, NotificationStatus.SENT, 1, LocalDateTime.now(), "PAYMENT_CREATED", null
        );
        when(notificationRepository.findByNotificationIdWithEventData(500L)).thenReturn(Mono.just(projection));


        StepVerifier.create(notificationUseCase.getNotificationById("valid_key", "500"))
            .expectNextMatches(response -> response.id() == 500L && response.status().equals(NotificationStatus.SENT))
            .verifyComplete();


        verify(clientRepository).findBySecretKey("valid_key");
        verify(notificationRepository).findByNotificationIdWithEventData(500L);
    }

    @Test
    void getAllNotifications_Success() {
        // 1. Mock de validación del cliente
        Client mockClient = new Client();
        mockClient.setId(100);
        when(clientRepository.findBySecretKey("valid_key")).thenReturn(Mono.just(mockClient));

        // 2. Mock de la query de proyección (Simulamos que devuelve 2 notificaciones)
        NotificationWithEvent notif1 = new NotificationWithEvent(1L, NotificationStatus.SENT, 1, LocalDateTime.now(), "TYPE_A", null);
        NotificationWithEvent notif2 = new NotificationWithEvent(2L, NotificationStatus.FAILED, 5, LocalDateTime.now(), "TYPE_B", null);

        when(notificationRepository.findAllWithEventDataByClientId(100L))
            .thenReturn(Flux.just(notif1, notif2));

        // EJECUCIÓN
        var result = notificationUseCase.getAllNotifications("valid_key");

        // THEN
        StepVerifier.create(result)
            .expectNextMatches(res -> res.id() == 1L)
            .expectNextMatches(res -> res.id() == 2L)
            .verifyComplete();

        verify(notificationRepository).findAllWithEventDataByClientId(100L);
    }




}