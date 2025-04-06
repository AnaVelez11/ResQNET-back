package co.edu.uniquindio.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException() {
        this("Resource not found");
    }
}
