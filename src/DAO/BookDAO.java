/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jframe.DBConnection;
/**
 *
 * @author HS
 */
public class BookDAO {
    public static String getBookTitleByISBN(String isbn) {
        String title = "";
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT title FROM books WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                title = rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return title;
    }
    
    // Tìm sách theo ISBN
    public static List<String[]> searchBooksByISBN(String isbn) {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT isbn, title FROM books WHERE isbn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String[] book = {rs.getString("isbn"), rs.getString("title")};
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Tìm sách theo Title gần đúng
    public static List<String[]> searchBooksByTitle(String title) {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT isbn, title FROM books WHERE title LIKE ? LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + title + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String[] book = {rs.getString("isbn"), rs.getString("title")};
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
}
