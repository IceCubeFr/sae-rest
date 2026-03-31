package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.CollectionPoint;

public class CollectionPointDAO {
    public List<CollectionPoint> findAll() {
        List<CollectionPoint> list = new ArrayList<>();
        String query = "SELECT * FROM CollectionPoint";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CollectionPoint(
                    rs.getInt("id"),
                    rs.getString("adresse"),
                    rs.getInt("capaciteMax"),
                    new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public CollectionPoint findById(int id) {
        String query = "SELECT * FROM CollectionPoint WHERE id = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CollectionPoint(
                    rs.getInt("id"),
                    rs.getString("adresse"),
                    rs.getInt("capaciteMax"),
                    new ArrayList<>()
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean add(CollectionPoint collect) {
        String query = "INSERT INTO CollectionPoint VALUES (?, ?, ?)";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, collect.id());
            ps.setString(2, collect.adresse());
            ps.setInt(3, collect.capaciteMax());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean update(CollectionPoint collect, boolean isPatch) {
        CollectionPoint cp = this.findById(collect.id());
        String query = "UPDATE CollectionPoint SET adresse = ?, capaciteMax = ? WHERE id = ?";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, (collect.adresse() == null && isPatch) ? cp.adresse() : collect.adresse());
            ps.setInt(2, (collect.capaciteMax() == null && isPatch) ? cp.capaciteMax() : collect.capaciteMax());
            ps.setInt(3, collect.id());
            ps.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "UPDATE deposit SET collecte = true WHERE pointid = ?";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query))  {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public List<CollectionPoint> pointsOverloaded() {
        List<CollectionPoint> result = new ArrayList<>();
        DepositDAO ddao = new DepositDAO();
        for(CollectionPoint cp : this.findAll()) {
            if(ddao.collectionPointSpaceLeft(cp.id()) * 100 / cp.capaciteMax() >= 80) {
                result.add(cp);
            }
        }
        return result;
    }
}
