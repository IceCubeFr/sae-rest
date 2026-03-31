package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DS {
    public static DS instance = new DS();

    private DS() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur de driver : " + e.getMessage());
        }
    }



    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://psqlserv:5432/but2", "floriangavoilleetu", "moi");
    }
}
