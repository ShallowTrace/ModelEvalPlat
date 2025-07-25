package com.ecode.modelevalplat.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendUtils {

    private final JavaMailSender mailSender;

    /**
     * 发送 HTML 邮件
     */
    public void sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            // 配置或从 application.yml 中读取
            String from = "jingyaling52000@gmail.com";
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true 表示 HTML

            mailSender.send(message);
            log.info("邮件发送成功，目标邮箱：{}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败：{}", e.getMessage(), e);
        } catch (javax.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}