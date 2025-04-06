package co.edu.uniquindio.dto;

public record PasswordResetRequest(
        String email,
        String code,
        String newPassword
) {
}
