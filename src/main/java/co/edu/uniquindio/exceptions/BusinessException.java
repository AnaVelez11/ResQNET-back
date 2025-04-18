package co.edu.uniquindio.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {

        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    private String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
