package code.vanilson.marketplace.exception;

import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends BaseException {
    public IncorrectPasswordException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
