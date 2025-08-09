package io.github.jasonlat.authenticate.service.engine.impl;

import io.github.jasonlat.authenticate.service.engine.IMessageLoader;
import io.github.jasonlat.types.utils.Validator;
import io.github.wppli.email.domain.IEmailService;
import org.springframework.stereotype.Service;

/**
 * @author li--jiaqiang 2024−12−23
 */
@Service("emailMessageLoader")
public class EmailMessageLoader implements IMessageLoader {

    private final IEmailService emailService;
    public EmailMessageLoader(IEmailService emailService) {
        this.emailService = emailService;
    }


    @Override
    public void sendCode(String code, String email) {
        boolean validEmail = Validator.isValidEmail(email);
        if (!validEmail) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        emailService.sendText(email, "您的动态验证码为: " + code + " , 请勿泄露给他人, 五分钟内有效");
    }


}