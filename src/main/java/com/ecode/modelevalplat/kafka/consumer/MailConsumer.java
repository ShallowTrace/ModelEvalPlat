package com.ecode.modelevalplat.kafka.consumer;

import com.ecode.modelevalplat.kafka.dto.MailMessageDTO;
import com.ecode.modelevalplat.util.EmailSendUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailConsumer {

    private final EmailSendUtils emailSendUtils;

    /**
     * 监听并消费邮件发送主题
     */
    @KafkaListener(topics = "mail-send-topic", groupId = "email_group")
    public void consume(MailMessageDTO message) {
        log.info("【Kafka】收到邮件消息：{}", message);

        emailSendUtils.sendHtmlMail(message.getTo(), message.getSubject(), message.getContent());
    }
}