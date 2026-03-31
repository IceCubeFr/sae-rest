package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.WasteType;

public class AcceptsDAO {
    public List<WasteType> findByCollectionPointId(int collectionPointId) {
        List<WasteType> list = new ArrayList<>();
        String query = "SELECT wt.* FROM WasteType wt JOIN Accepts a ON wt.id = a.wastetypeid WHERE a.pointid = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, collectionPointId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WasteType(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("pointsPerKilos")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public boolean isAccepted(int collectionPointId, int wasteTypeId) {
        String query = "SELECT * FROM Accepts WHERE pointid = ? AND wastetypeid = ?";
        try (Connection con = DS.instance.getConnection()) {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, collectionPointId);
            ps.setInt(2, wasteTypeId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean addRelation(int collectionPointId, int wasteTypeId) {
        String query = "INSERT INTO Accepts (pointid, wastetypeid) VALUES (?, ?)";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, collectionPointId);
            ps.setInt(2, wasteTypeId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean deleteRelation(int collectionPointId, int wasteTypeId) {
        String query = "DELETE FROM Accepts WHERE pointid = ? AND wastetypeid = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, collectionPointId);
            ps.setInt(2, wasteTypeId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

}
