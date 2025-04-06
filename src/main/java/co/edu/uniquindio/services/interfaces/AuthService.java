package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.LoginDTO;

public interface AuthService {
    String login(LoginDTO loginDTO);
    boolean activateUser(String email, String code);
    void sendPasswordResetCode(String email);
    boolean resetPassword(String email, String code, String newPassword);

}
