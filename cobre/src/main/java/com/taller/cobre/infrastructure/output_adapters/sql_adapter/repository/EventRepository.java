package com.taller.cobre.infrastructure.output_adapters.sql_adapter.repository;

import com.taller.cobre.infrastructure.output_adapters.sql_adapter.entities.Event;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveCrudRepository<Event, Long> {
}
