package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.AcceptsDAO;
import dao.CollectionPointDAO;
import dao.DepositDAO;
import dao.VerifToken;
import dto.CollectionPoint;
import dto.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/points/*")
public class CollectionPointRestAPI extends HttpServlet {
    CollectionPointDAO collectionPointDAO = new CollectionPointDAO();
    AcceptsDAO acceptsDAO = new AcceptsDAO();

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();
        ObjectMapper om = new ObjectMapper();
        String info = req.getPathInfo();

        if (info == null || info.equals("/")) {
            out.println(om.writeValueAsString(this.collectionPointDAO.findAll()));
            return;
        }
        String[] split = info.split("/");
        if(split.length != 2 && split.length != 3) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            String param1 = split[1];
            if("overloaded".equals(param1)) {
                if(VerifToken.checkToken(req.getHeader("Authorization")) != Role.ADMIN) {
                    res.sendError(401);
                    return;
                }
                List<CollectionPoint> overloaded = collectionPointDAO.pointsOverloaded();
                out.println(om.writeValueAsString(overloaded));
                return;
            }
            int id = Integer.parseInt(split[1]);
            CollectionPoint ig = this.collectionPointDAO.findById(id);
            if(split.length == 3 && ig != null) {
                String action = split[2];
                if("status".equals(action)) {
                    DepositDAO ddao = new DepositDAO();
                    CollectionPoint cp = collectionPointDAO.findById(id);
                    int score = ddao.collectionPointSpaceLeft(id) * 100 / ig.capaciteMax();
                    boolean full = score >= 100;
                    String json = """
                            {
                        "id": %d,
                        "adresse": \"%s\",
                        "remplissage": %d,
                        "full": %b
                    }
                            """.formatted(id, cp.adresse(), score, full);
                    out.println(json);
                    return;
                }
            }
            if(ig == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if(ig.adresse() == null || ig.capaciteMax() == null) res.sendError(500);
            ig.wasteTypes().addAll(this.acceptsDAO.findByCollectionPointId(id));
            out.println(om.writeValueAsString(ig));
        } catch(NumberFormatException ex) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        out.close();
    }

    public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
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
            CollectionPoint collect = om.readValue(data.toString(), CollectionPoint.class);
            if(this.collectionPointDAO.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(collect.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.collectionPointDAO.update(collect, false)) out.println(om.writeValueAsString(collect));
            else res.sendError(400);
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
        if(split.length != 3) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(split[1]);
            String type = split[2];
            if(!type.equals("clear")) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            CollectionPoint ig = this.collectionPointDAO.findById(id);
            if(ig == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if(!this.collectionPointDAO.deleteById(id)) {
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
            CollectionPoint collect = om.readValue(data.toString(), CollectionPoint.class);
            if(this.collectionPointDAO.findById(Integer.parseInt(split[1])) == null) {
                res.sendError(404);
            }
            if(collect.id() != Integer.parseInt(split[1])) res.sendError(400);
            if(this.collectionPointDAO.update(collect, true)) out.println(om.writeValueAsString(collect));
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
        else if("DELETE".equals(req.getMethod())) this.doDelete(req, res);
    }
}
