package com.github.ivacoronel.server.services.impl;

import java.util.HashSet;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.github.ivacoronel.server.services.KafkaAgentService;
import com.github.ivacoronel.server.web.dtos.ChallengeDto;
import com.github.rozidan.springboot.logger.Loggable;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

@Service
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class KafkaAgentServiceImpl implements KafkaAgentService {

    private final KafkaTemplate<String, ChallengeDto> kafkaTemplate;
    private final AdminClient adminClient;
    private final HashSet<String> openTopics = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(KafkaAgentServiceImpl.class);

    public KafkaAgentServiceImpl(
            @Value("${kafka.bootstrap.servers}") String bootstrapServers,
            KafkaTemplate<String, ChallengeDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;

        // Configurar AdminClient
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.adminClient = AdminClient.create(config);
    }

    @Loggable
    @Override
    public void send(String topic, ChallengeDto chal) {
        ListenableFuture<SendResult<String, ChallengeDto>> future = kafkaTemplate.send(topic, chal);
        future.addCallback(new ListenableFutureCallback<SendResult<String, ChallengeDto>>() {
            @Override
            public void onSuccess(SendResult<String, ChallengeDto> result) {
                logger.info("Published message to kafka...");
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Failed publishing message to kafka...", ex);
            }
        });
    }

    @Loggable
    @Override
    public void openTopic(String topic) {
        if (openTopics.contains(topic)) return;

        try {
            // Comprobar si el topic existe
            boolean exists = adminClient.listTopics().names().get().contains(topic);
            if (!exists) {
                // Crear topic: 1 partición, 1 réplica
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
                adminClient.createTopics(java.util.Collections.singleton(newTopic)).all().get();
            }
            openTopics.add(topic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open/create topic: " + topic, e);
        }
    }

    @Loggable
    @Override
    public void closeTopic(String topic) {
        try {
            adminClient.deleteTopics(java.util.Collections.singleton(topic)).all().get();
            openTopics.remove(topic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete topic: " + topic, e);
        }
    }
}