package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.WasteType;

public class WasteTypeDAO {
    public List<WasteType> findAll() {
        List<WasteType> list = new ArrayList<>();
        String query = "SELECT * FROM WasteType";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new WasteType(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getInt("pointsPerKilos")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public WasteType findById(int id) {
        String query = "SELECT * FROM WasteType WHERE id = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new WasteType(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getInt("pointsPerKilos")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean add(WasteType wasteType) {
        String query = "INSERT INTO WasteType VALUES (?, ?, ?)";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, wasteType.id());
            ps.setString(2, wasteType.nom());
            ps.setInt(3, wasteType.pointsPerKilos());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean update(WasteType wasteType) {
        String query = "UPDATE WasteType SET nom = ?, pointsPerKilos = ? WHERE id = ?";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, wasteType.nom());
            ps.setInt(2, wasteType.pointsPerKilos());
            ps.setInt(3, wasteType.id());
            ps.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM WasteType WHERE id = ?";
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
    
}
