package com.hanu.AiEcommerce.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
    SELECT e
    FROM OutboxEvent e
    WHERE e.status = com.hanu.AiEcommerce.common.outbox.OutboxStatus.PENDING
      AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
    ORDER BY e.createdAt ASC
    """)
    List<OutboxEvent> findRetryableEvents(
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
    UPDATE OutboxEvent e
    SET e.status = com.hanu.AiEcommerce.common.outbox.OutboxStatus.PROCESSING,
        e.processingStartedAt = :now
    WHERE e.id = :id
      AND e.status = com.hanu.AiEcommerce.common.outbox.OutboxStatus.PENDING
    """)
    int claimEvent(
            @Param("id") Long id,
            @Param("now") LocalDateTime now
    );

    @Query("""
    SELECT e
    FROM OutboxEvent e
    WHERE e.status = com.hanu.AiEcommerce.common.outbox.OutboxStatus.PROCESSING
      AND e.processingStartedAt <= :cutoff
    ORDER BY e.processingStartedAt ASC
    """)
    List<OutboxEvent> findStuckProcessingEvents(
            @Param("cutoff") LocalDateTime cutoff
    );
}
