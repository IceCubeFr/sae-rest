package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.UserDAO;
import dao.VerifToken;
import dto.Role;
import dto.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/users/*")
public class UserRestAPI extends HttpServlet{
    UserDAO udao = new UserDAO();


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        String info = req.getPathInfo();

        if (info == null || info.equals("/")) {
            res.sendError(400);
            return;
        }
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String action = split[1];
            if(!"leaderboard".equals(action)) {
                res.sendError(400);
                return;
            }
            Map<Integer,Integer> leaderboard = udao.getLeaderboard();
            if(leaderboard == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            out.println(om.writeValueAsString(leaderboard));
        } catch(NumberFormatException ex) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        out.close();
    }

    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if(VerifToken.checkToken(req.getHeader("Authorization")) != Role.ADMIN) {
            res.sendError(401);
            return;
        }
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        BufferedReader br = req.getReader();
        StringBuilder data = new StringBuilder();
        String line = br.readLine();
        while(line != null) {
            data.append(line);
            line = br.readLine();
        }

        String info = req.getPathInfo();
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(data.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            User user = om.readValue(data.toString(), User.class);
            if(this.udao.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(user.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.udao.update(user, false)) out.println(om.writeValueAsString(this.udao.findById(user.id())));
            else res.sendError(400);
            return;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            res.sendError(500);
            return;
        }
    }

    public void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if(VerifToken.checkToken(req.getHeader("Authorization")) != Role.ADMIN) {
            res.sendError(401);
            return;
        }
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        BufferedReader br = req.getReader();
        StringBuilder data = new StringBuilder();
        String line = br.readLine();
        while(line != null) {
            data.append(line);
            line = br.readLine();
        }

        String info = req.getPathInfo();
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(data.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            User user = om.readValue(data.toString(), User.class);
            if(this.udao.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(user.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.udao.update(user, true)) out.println(om.writeValueAsString(this.udao.findById(user.id())));
            else res.sendError(400);
            return;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            res.sendError(500);
            return;
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if("PATCH".equals(req.getMethod())) this.doPatch(req, res);
        else if("GET".equals(req.getMethod())) this.doGet(req, res);
        else if("PUT".equals(req.getMethod())) this.doPut(req, res);
    }
}
