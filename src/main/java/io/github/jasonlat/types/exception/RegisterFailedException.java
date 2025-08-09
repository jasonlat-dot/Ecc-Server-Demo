package io.github.jasonlat.types.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author li--jiaqiang 2025−01−13
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RegisterFailedException extends AppException {

    public RegisterFailedException(String code) {
        super(code);
    }

    public RegisterFailedException(String code, Throwable cause) {
        super(code, cause);
    }

    public RegisterFailedException(String code, String message) {
        super(code, message);
    }

    public RegisterFailedException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}