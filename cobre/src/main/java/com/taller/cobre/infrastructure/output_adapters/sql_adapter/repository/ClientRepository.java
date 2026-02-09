package com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository;

import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Client;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {
    Mono<Client>findBySecretKey(String secretKey);
}
