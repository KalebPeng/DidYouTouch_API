package com.example.springboot002.demos.web.Service.AuthService;

import com.aliyun.credentials.models.Config;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AliSmsService {

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.signName}")
    private String signName;

    @Value("${aliyun.sms.templateCode}")
    private String templateCode;

    /**
     * 创建阿里云客户端
     */
    private Client createClient() throws Exception {
        Config credentialConfig = new Config()
                .setType("access_key")
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);

        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client(credentialConfig);

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setCredential(credential)
                .setEndpoint("dypnsapi.aliyuncs.com");

        return new Client(config);
    }

    /**
     * 发送短信验证码
     */
    public boolean sendVerifyCode(String phoneNumber, String code) {
        try {
            Client client = createClient();

            // 构建模板参数
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"5\"}", code);

            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            RuntimeOptions runtime = new RuntimeOptions();

            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);

            System.out.println("=== 阿里云短信发送成功 ===");
            System.out.println("手机号: " + phoneNumber);
            System.out.println("验证码: " + code);
            System.out.println("响应: " + response.getBody());

            return true;

        } catch (TeaException e) {
            System.err.println("=== 短信发送失败 ===");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误码: " + e.getCode());
            return false;

        } catch (Exception e) {
            System.err.println("=== 系统错误 ===");
            e.printStackTrace();
            return false;
        }
    }
}
