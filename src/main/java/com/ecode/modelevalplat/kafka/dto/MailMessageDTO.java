package com.ecode.modelevalplat.kafka.dto;

import lombok.Data;

/**
 * Kafka 消息传输对象：封装发送邮件的关键信息
 */
@Data
public class MailMessageDTO {

    private String to;       // 收件人邮箱

    private String subject;  // 邮件标题

    private String content;  // 邮件正文（含验证码等）

}