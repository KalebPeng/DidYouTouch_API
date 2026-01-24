package com.example.springboot002.demos.web.Service.AuthService;


import com.example.springboot002.demos.web.Util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务
 */
@Service
public class SmsCodeService {

    @Autowired
    private RedisUtil redisUtil;

    // Redis key 前缀
    private static final String CODE_PREFIX = "sms:code:";           // 验证码
    private static final String SEND_TIME_PREFIX = "sms:send:";      // 发送时间
    private static final String RETRY_PREFIX = "sms:retry:";         // 重试次数

    @Value("${sms.code.expireTime:300}")
    private long codeExpireTime; // 验证码过期时间（秒），默认5分钟

    @Value("${sms.code.sendInterval:60}")
    private long sendInterval; // 发送间隔（秒），默认60秒

    @Value("${sms.code.maxRetryTimes:5}")
    private int maxRetryTimes; // 最大重试次数，默认5次

    @Value("${sms.code.length:6}")
    private int codeLength; // 验证码长度，默认6位

    /**
     * 生成随机验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 保存验证码到 Redis
     */
    public void saveCode(String phoneNumber, String code) {
        String codeKey = CODE_PREFIX + phoneNumber;
        String sendTimeKey = SEND_TIME_PREFIX + phoneNumber;
        String retryKey = RETRY_PREFIX + phoneNumber;

        // 保存验证码，设置过期时间
        redisUtil.set(codeKey, code, codeExpireTime, TimeUnit.SECONDS);

        // 记录发送时间（用于控制发送频率）
        redisUtil.set(sendTimeKey, String.valueOf(System.currentTimeMillis()),
                sendInterval, TimeUnit.SECONDS);

        // 初始化重试次数为0
        redisUtil.set(retryKey, "0", codeExpireTime, TimeUnit.SECONDS);

        System.out.println("=== 验证码已保存到Redis ===");
        System.out.println("手机号: " + phoneNumber);
        System.out.println("验证码: " + code);
        System.out.println("过期时间: " + codeExpireTime + "秒");
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phoneNumber, String code) {
        String codeKey = CODE_PREFIX + phoneNumber;
        String retryKey = RETRY_PREFIX + phoneNumber;

        // 检查验证码是否存在
        if (!redisUtil.hasKey(codeKey)) {
            System.out.println("验证码不存在或已过期: " + phoneNumber);
            return false;
        }

        // 检查重试次数
        String retryCountStr = redisUtil.get(retryKey);
        int retryCount = retryCountStr != null ? Integer.parseInt(retryCountStr) : 0;

        if (retryCount >= maxRetryTimes) {
            System.out.println("验证次数超过限制: " + phoneNumber);
            // 删除验证码
            redisUtil.delete(codeKey);
            redisUtil.delete(retryKey);
            return false;
        }

        // 获取保存的验证码
        String savedCode = redisUtil.get(codeKey);

        if (code.equals(savedCode)) {
            // 验证成功，删除相关key
            redisUtil.delete(codeKey);
            redisUtil.delete(retryKey);
            System.out.println("验证码验证成功: " + phoneNumber);
            return true;
        } else {
            // 验证失败，增加重试次数
            redisUtil.increment(retryKey);
            int remainingTimes = maxRetryTimes - retryCount - 1;
            System.out.println("验证码错误，剩余尝试次数: " + remainingTimes);
            return false;
        }
    }

    /**
     * 检查是否可以发送（防止频繁发送）
     */
    public boolean canSend(String phoneNumber) {
        String sendTimeKey = SEND_TIME_PREFIX + phoneNumber;
        return !redisUtil.hasKey(sendTimeKey);
    }

    /**
     * 获取剩余发送等待时间（秒）
     */
    public long getRemainingSendTime(String phoneNumber) {
        String sendTimeKey = SEND_TIME_PREFIX + phoneNumber;
        if (!redisUtil.hasKey(sendTimeKey)) {
            return 0;
        }
        Long expire = redisUtil.getExpire(sendTimeKey);
        return expire != null ? expire : 0;
    }

    /**
     * 获取剩余验证次数
     */
    public int getRemainingRetryTimes(String phoneNumber) {
        String retryKey = RETRY_PREFIX + phoneNumber;
        String retryCountStr = redisUtil.get(retryKey);
        int retryCount = retryCountStr != null ? Integer.parseInt(retryCountStr) : 0;
        return maxRetryTimes - retryCount;
    }

    /**
     * 删除验证码（手动清除）
     */
    public void deleteCode(String phoneNumber) {
        String codeKey = CODE_PREFIX + phoneNumber;
        String retryKey = RETRY_PREFIX + phoneNumber;
        redisUtil.delete(codeKey);
        redisUtil.delete(retryKey);
        System.out.println("已删除验证码: " + phoneNumber);
    }
}