package com.ecommerce.common.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@ConditionalOnBean(KafkaOperations.class)
public class KafkaReliabilityConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<String, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxElapsedTime(5_000L);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
