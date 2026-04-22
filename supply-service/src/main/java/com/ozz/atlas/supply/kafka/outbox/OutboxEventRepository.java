package com.ozz.atlas.supply.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select o
            from OutboxEvent o
            where o.status in :statuses
              and o.nextAttemptAt <= :now
            order by o.id asc
            """)
    List<OutboxEvent> findPublishableEvents(
            @Param("statuses") List<OutboxEventStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
