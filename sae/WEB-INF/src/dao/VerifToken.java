package dao;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import dto.Role;
import dto.User;

public class VerifToken {
    public static Role checkToken(String encoded) {
        if(encoded != null && encoded.startsWith("Basic ")) {
            String token = new String(Base64.getDecoder().decode(encoded.substring("Basic".length()).trim()), StandardCharsets.UTF_8);
            int index = token.indexOf(':');
            if(index < 0) return null;
            String login = token.substring(0, index);
            String password = token.substring(index + 1);
            UserDAO udao = new UserDAO();
            User user = udao.findByLogin(login, password);
            if(user != null) {
                return user.role();
            }
        }
        return null;
    }
    
}
