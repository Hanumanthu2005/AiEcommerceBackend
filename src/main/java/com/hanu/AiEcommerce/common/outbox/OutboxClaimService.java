package com.hanu.AiEcommerce.common.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxClaimService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public boolean claim(Long eventId) {

        return outboxEventRepository.claimEvent(
                eventId,
                LocalDateTime.now()
        ) == 1;
    }
}