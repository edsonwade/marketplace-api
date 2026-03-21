package code.vanilson.marketplace.exception;

import org.springframework.http.HttpStatus;

public class EmailNotFoundException extends BaseException {
    public EmailNotFoundException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
