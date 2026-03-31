package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.CollectionPoint;
import dto.Deposit;

public class DepositDAO {
    public List<Deposit> findAll() {
        List<Deposit> list = new ArrayList<>();
        String query = "SELECT * FROM deposit where collecte = false;";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Deposit(
                    rs.getInt("id"),
                    rs.getInt("userId"),
                    rs.getInt("pointId"),
                    rs.getInt("wastetypeid"),
                    rs.getInt("poids"),
                    rs.getDate("datedepot"),
                    rs.getBoolean("collecte")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Deposit findById(int id) {
        String query = "SELECT * FROM deposit WHERE id = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Deposit(
                    rs.getInt("id"),
                    rs.getInt("userId"),
                    rs.getInt("pointId"),
                    rs.getInt("wastetypeid"),
                    rs.getInt("poids"),
                    rs.getDate("datedepot"),
                    rs.getBoolean("collecte")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean add(Deposit dep) {
        String query = "INSERT INTO deposit(userid, pointid, wastetypeid, poids, datedepot) VALUES (?, ?, ?, ?, ?)";
        AcceptsDAO adao = new AcceptsDAO();
        if(adao.isAccepted(dep.pointid(), dep.wastetypeid())) {
            try(Connection con = DS.instance.getConnection();
                PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, dep.userid());
                ps.setInt(2, dep.pointid());
                ps.setInt(3, dep.wastetypeid());
                ps.setInt(4, dep.poids());
                ps.setDate(5, dep.datedepot());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    public boolean update(Deposit dep, boolean isPatch) {
        Deposit cp = this.findById(dep.id());
        String query = "UPDATE deposit SET userid = ?, pointid = ?, wastetypeid = ?, poids = ?, datedepot = ?, collecte = ? WHERE id = ?";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setObject(1, (dep.userid() == null && isPatch) ? cp.userid() : dep.userid());
            ps.setObject(2, (dep.pointid() == null && isPatch) ? cp.pointid() : dep.pointid());
            ps.setObject(3, (dep.wastetypeid() == null && isPatch) ? cp.wastetypeid() : dep.wastetypeid());
            ps.setObject(4, (dep.poids() == null && isPatch) ? cp.poids() : dep.poids());
            ps.setObject(5, (dep.datedepot() == null && isPatch) ? cp.datedepot() : dep.datedepot());
            ps.setObject(6, (dep.collecte() == null && isPatch) ? cp.collecte() : dep.collecte());
            ps.setObject(7, dep.id());
            ps.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public int collectionPointSpaceLeft(int pointId) {
        String query = "SELECT SUM(poids) AS somme FROM deposit where pointid = ? AND collecte = false";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, pointId);
            ResultSet rs = ps.executeQuery();
            CollectionPointDAO cpdao = new CollectionPointDAO();
            CollectionPoint cp = cpdao.findById(pointId);
            if(cp != null && rs.next()) {
                return cp.capaciteMax() - rs.getInt("somme");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
