package com.hanu.AiEcommerce.common.outbox;

public enum OutboxStatus {

    PENDING,
    PROCESSING,
    PUBLISHED,
    DEAD
}
