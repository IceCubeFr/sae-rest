package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.VerifToken;
import dao.WasteTypeDAO;
import dto.Role;
import dto.WasteType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/wasteTypes/*")
public class WasteTypesRestAPI extends HttpServlet {
    WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        String info = req.getPathInfo();

        if (info == null || info.equals("/")) {
            out.println(om.writeValueAsString(this.wasteTypeDAO.findAll()));
            return;
        }
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(split[1]);
            WasteType ig = this.wasteTypeDAO.findById(id);
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
        if(data.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            WasteType wasteType = om.readValue(data.toString(), WasteType.class);
            if(this.wasteTypeDAO.findById(wasteType.id()) != null) {
                res.sendError(409);
            }
            this.wasteTypeDAO.add(wasteType);
            out.println(om.writeValueAsString(wasteType));
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
        if(data.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            WasteType wasteType = om.readValue(data.toString(), WasteType.class);
            if(this.wasteTypeDAO.findById(wasteType.id()) == null) {
                res.sendError(404);
            }
            this.wasteTypeDAO.update(wasteType);
            out.println(om.writeValueAsString(wasteType));
            return;
        } catch(Exception e) {  
            System.out.println(e.getMessage());
            res.sendError(500);
            return;
        }

    }

    public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if(VerifToken.checkToken(req.getHeader("Authorization")) != Role.ADMIN) {
            res.sendError(401);
            return;
        }
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        String info = req.getPathInfo();

        if (info == null || info.equals("/")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String[] split = info.split("/");
        if(split.length != 2) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(split[1]);
            WasteType ig = this.wasteTypeDAO.findById(id);
            if(ig == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if(!this.wasteTypeDAO.deleteById(id)) {
                res.sendError(409);
                return;
            }
            out.println(om.writeValueAsString(ig));
        } catch(NumberFormatException ex) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        out.close();
    }
    
}
