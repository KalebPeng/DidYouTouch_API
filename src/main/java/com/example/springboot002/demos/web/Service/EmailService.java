package com.example.springboot002.demos.web.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${sms.code.length:6}")
    private int codeLength; // 验证码长度，默认6位

    @Value("${spring.mail.username}")
    private String fromEmail;

    //发送验证码
    public boolean sendSimpleEmail(String email, String code) {
        try {
            //设置邮箱配置
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("验证码");
            message.setText("您的验证码是：" + code + "\n5分钟内有效");
            mailSender.send(message);

            System.out.println("邮件发送成功：{" + email+"}");
            return true;
        } catch (MailException e) {
            System.out.println("邮件发送失败：{" + e.getMessage()+"}");
            return false;
        }catch (Exception e) {
            System.err.println("=== 系统错误 ===");
            e.printStackTrace();
            return false;
        }
    }
}
