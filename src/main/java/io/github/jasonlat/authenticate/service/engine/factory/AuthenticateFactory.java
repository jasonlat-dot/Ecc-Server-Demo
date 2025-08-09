package io.github.jasonlat.authenticate.service.engine.factory;

import io.github.jasonlat.authenticate.service.engine.IMessageLoader;
import io.github.jasonlat.types.utils.Validator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author li--jiaqiang 2024−12−23
 */
@Getter
@Service
public class AuthenticateFactory {

    private final Map<String, IMessageLoader> messageLoaderMap;
    public AuthenticateFactory(Map<String, IMessageLoader> messageLoaderMap) {
        this.messageLoaderMap = messageLoaderMap;
    }


    public static MessageLoaderType getMessageLoaderType(String value) {
        if (Validator.isValidPhoneNumber(value)) {
            return getMessageLoaderTypeHelper(1);
        } else if (Validator.isValidEmail(value)) {
            return getMessageLoaderTypeHelper(2);
        } else {
            throw new IllegalArgumentException();
        }
    }
    public static MessageLoaderType getMessageLoaderType(int value) {
        MessageLoaderType messageLoaderTypeHelper = getMessageLoaderTypeHelper(value);
        if (messageLoaderTypeHelper.equals(MessageLoaderType.UN_KNOWN)) {
            throw new IllegalArgumentException();
        }
        return messageLoaderTypeHelper;
    }
    private static MessageLoaderType getMessageLoaderTypeHelper(int value) {
        switch (value) {
            case 1:
                return MessageLoaderType.PHONE;
            case 2:
                return MessageLoaderType.EMAIL;
            default:
                return MessageLoaderType.UN_KNOWN;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum MessageLoaderType {
        PHONE(1, "phoneMessageLoader"),
        EMAIL(2, "emailMessageLoader"),
        UN_KNOWN(-1, "unknownMessageLoader")
        ;

        private final int code;
        // 执行器名字
        private final String actuatorName;

    }



}