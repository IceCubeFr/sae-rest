package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dto.User;
import dto.Role;

public class UserDAO {
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public User findById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public User findByLogin(String login, String password) {
        String query = "SELECT * FROM users WHERE login = ? AND password = ?";
        try (Connection con = DS.instance.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean add(User user) {
        String query = "INSERT INTO users(login,password,role) VALUES (?, ?, ?)";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, user.login());
            ps.setString(2, user.password());
            ps.setString(3, user.role().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean update(User user, boolean isPatch) {
        User cp = this.findById(user.id());
        String query = "UPDATE users SET login = ?, password = ?, role = ? WHERE id = ?";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, (user.login() == null && isPatch) ? cp.login() : user.login());
            ps.setString(2, (user.password() == null && isPatch) ? cp.password() : user.password());
            ps.setString(3, (user.role() == null) ? cp.role().toString() : user.role().toString());
            ps.setInt(4, user.id());
            ps.executeUpdate();
            return true;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM users WHERE id = ?";
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

    public Map<Integer, Integer> getLeaderboard() {
        Map<Integer, Integer> users = new HashMap<>();
        String query = "SELECT u.id AS userid, SUM(d.poids * w.pointsperkilos) AS score from users AS u JOIN deposit AS d ON(u.id = d.userid) JOIN wastetype AS w ON (d.wastetypeid = w.id) GROUP BY u.id ORDER BY SUM(d.poids * w.pointsperkilos) DESC LIMIT 10";
        try(Connection con = DS.instance.getConnection();
            PreparedStatement ps = con.prepareStatement(query))  {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                users.put(rs.getInt("userid"), rs.getInt("score"));
            }
            return users;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
