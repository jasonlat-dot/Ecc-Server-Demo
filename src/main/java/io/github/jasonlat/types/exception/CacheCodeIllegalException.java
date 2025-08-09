package io.github.jasonlat.types.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author li--jiaqiang 2024−12−23
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class CacheCodeIllegalException extends AppException {

    public CacheCodeIllegalException(String code) {
        super(code);
    }

    public CacheCodeIllegalException(String code, Throwable cause) {
        super(code, cause);
    }

    public CacheCodeIllegalException(String code, String message) {
        super(code, message);
    }

    public CacheCodeIllegalException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}