package com.tripdog.service.impl;

import com.tripdog.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类（简化版本，使用内存存储验证码）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 使用内存存储验证码（生产环境建议使用Redis）
    // Key: 验证码, Value: 验证码信息（包含邮箱和过期时间）
    private final ConcurrentHashMap<String, CodeInfo> codeStorage = new ConcurrentHashMap<>();

    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Override
    public boolean sendVerificationCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("TripDog验证码");
            message.setText(buildEmailContent(code));
            message.setFrom(fromEmail); // 使用配置的发送邮箱

            mailSender.send(message);
            log.info("验证码邮件发送成功: email={}", email);
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败: email={}, error={}", email, e.getMessage());
            // 开发阶段，即使邮件发送失败也返回true，方便测试
            log.warn("开发模式：邮件发送失败但返回成功，验证码为: {}", code);
            return true;
        }
    }

    @Override
    public String generateAndSendCode(String email) {
        // 生成6位数字验证码，确保唯一性
        String code = generateUniqueCode();

        // 发送邮件
        if (!sendVerificationCode(email, code)) {
            return null;
        }

        // 存储到内存中，以验证码为key，5分钟过期
        codeStorage.put(code, new CodeInfo(email, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CODE_EXPIRE_MINUTES)));

        // 清理过期的验证码
        cleanExpiredCodes();

        return code;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        // 以验证码为key查找
        CodeInfo codeInfo = codeStorage.get(code);

        if (codeInfo == null) {
            log.warn("验证码不存在: code={}", code);
            return false;
        }

        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireTime) {
            codeStorage.remove(code);
            log.warn("验证码已过期: code={}", code);
            return false;
        }

        // 验证邮箱是否匹配
        if (!codeInfo.email.equals(email)) {
            log.warn("验证码对应邮箱不匹配: code={}, expected={}, actual={}", code, codeInfo.email, email);
            return false;
        }

        // 验证成功后删除验证码
        codeStorage.remove(code);
        log.info("验证码验证成功: email={}, code={}", email, code);
        return true;
    }

    private String generateUniqueCode() {
        String code;
        SecureRandom random = new SecureRandom();

        // 确保生成的验证码唯一（避免冲突）
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(random.nextInt(10));
            }
            code = sb.toString();
        } while (codeStorage.containsKey(code));

        return code;
    }

    private String buildEmailContent(String code) {
        return String.format(
            "您好！\n\n" +
            "您正在进行TripDog验证操作，验证码为：%s\n\n" +
            "验证码有效期为%d分钟，请及时使用。如非本人操作，请忽略此邮件。\n\n" +
            "TripDog团队",
            code, CODE_EXPIRE_MINUTES
        );
    }

    private void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        codeStorage.entrySet().removeIf(entry -> now > entry.getValue().expireTime);
    }

    /**
     * 验证码信息类
     */
    private static class CodeInfo {
        final String email;
        final long expireTime;

        CodeInfo(String email, long expireTime) {
            this.email = email;
            this.expireTime = expireTime;
        }
    }

}
