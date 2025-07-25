package com.ecode.modelevalplat.kafka.producer;

import com.ecode.modelevalplat.kafka.dto.MailMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "mail-send-topic";

    /**
     * 发送邮件消息到 Kafka
     */
    public void sendMail(MailMessageDTO message) {
        kafkaTemplate.send(TOPIC, message);
        log.info("【Kafka】邮件消息已发送，目标邮箱：{}", message.getTo());
    }
}
