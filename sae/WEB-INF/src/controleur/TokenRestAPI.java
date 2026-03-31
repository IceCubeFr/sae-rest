package controleur;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auth/token")
public class TokenRestAPI extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        UserDAO udao = new UserDAO();
        if(udao.findByLogin(login, password) != null) {
            String logPwd = login + ":" + password;
            String token = Base64.getEncoder().encodeToString(logPwd.getBytes(StandardCharsets.UTF_8));
            out.println(token);
        } else {
            out.println("Inconnu au bataillon!");
        }
    }
}
