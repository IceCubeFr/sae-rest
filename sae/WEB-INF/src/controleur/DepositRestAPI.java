package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DepositDAO;
import dao.VerifToken;
import dto.Deposit;
import dto.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/deposits/*")
public class DepositRestAPI extends HttpServlet {
    DepositDAO ddao = new DepositDAO();
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        String info = req.getPathInfo();

        if (info == null || info.equals("/")) {
            out.println(om.writeValueAsString(this.ddao.findAll()));
            return;
        }
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(split[1]);
            Deposit ig = this.ddao.findById(id);
            if(ig == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            out.println(om.writeValueAsString(ig));
        } catch(NumberFormatException ex) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if(VerifToken.checkToken(req.getHeader("Authorization")) == null) {
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
        if(data.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Deposit dep = om.readValue(data.toString(), Deposit.class);
            if(ddao.collectionPointSpaceLeft(dep.pointid()) < dep.poids()) {
                res.sendError(403);
                return;
            }
            if(dep.poids() < 0) {
                res.sendError(400);
                return;
            }
            if(!this.ddao.add(dep)) {
                res.sendError(500, "Impossible d'ajouter le deposit demandé");
                return;
            }
            out.println(om.writeValueAsString(dep));
            return;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            res.sendError(500);
            return;
        }
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
            Deposit dep = om.readValue(data.toString(), Deposit.class);
            if(this.ddao.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(dep.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.ddao.update(dep, false)) out.println(om.writeValueAsString(dep));
            else res.sendError(400);
            return;
        } catch(Exception e) {
            System.out.println(e.getMessage() + "\nDétails : ");
            e.printStackTrace();
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
            Deposit dep = om.readValue(data.toString(), Deposit.class);
            if(this.ddao.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(dep.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.ddao.update(dep, true)) out.println(om.writeValueAsString(this.ddao.findById(dep.id())));
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
        else if("POST".equals(req.getMethod())) this.doPost(req, res);
    }
}
