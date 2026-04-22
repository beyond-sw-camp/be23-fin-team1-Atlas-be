package com.ozz.atlas.supply.kafka.log;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    Optional<EventLog> findByEventId(String eventId);
}
