package com.hanu.AiEcommerce.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
public class KafkaDltConfig {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, Object> template
    ) {

        return new DeadLetterPublishingRecoverer(template);
    }
}
