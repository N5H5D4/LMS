/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import jframe.DBConnection;

/**
 *
 * @author HS
 */
public class StatisticsDAO {

    public static int getTotalTitles() {
        int totalBooks = 0;
        String sql = "SELECT COUNT(isbn) AS total FROM books";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalBooks = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalBooks;
    }

    public static int getTotalBooks() {
        int totalBooks = 0;
        String sql = "SELECT SUM(quantity) AS total FROM books";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalBooks = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalBooks;
    }

    public static int getTotalReaders() {
        int totalReaders = 0;
        String sql = "SELECT COUNT(*) AS total FROM readers";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalReaders = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalReaders;
    }

// Thống kê theo Number of Volumes (tổng số lượng sách)
    public static Map<String, Integer> getBooksByCategoryVolumes() {
        Map<String, Integer> booksByCategory = new HashMap<>();
        String sql = "SELECT category, SUM(quantity) AS total FROM books GROUP BY category";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String category = rs.getString("category");
                int total = rs.getInt("total");
                booksByCategory.put(category, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booksByCategory;
    }

    // Thống kê theo Number of Titles (số đầu sách)
    public static Map<String, Integer> getBooksByCategoryTitles() {
        Map<String, Integer> booksByCategory = new HashMap<>();
        String sql = "SELECT category, COUNT(isbn) AS total FROM books GROUP BY category";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String category = rs.getString("category");
                int total = rs.getInt("total");
                booksByCategory.put(category, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booksByCategory;
    }

    public static Map<String, Integer> getReadersByGender() {
        Map<String, Integer> readersByGender = new HashMap<>();
        String sql = "SELECT gender, COUNT(*) AS total FROM readers GROUP BY gender";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String gender = rs.getString("gender");
                int total = rs.getInt("total");
                // Chuẩn hóa dữ liệu gender
                String genderKey = (gender == null || gender.trim().isEmpty()) ? "Other" : gender;
                readersByGender.put(genderKey, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return readersByGender;
    }

    // Số người đang mượn sách (có ít nhất 1 sách chưa trả)
    public static int getReadersBorrowingBooks() {
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT bs.reader_id) AS total "
                + "FROM borrow_slips bs "
                + "WHERE bs.return_date IS NULL";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // Số người quá hạn nhưng vẫn chưa trả
    public static int getReadersOverdue() {
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT bs.reader_id) AS total "
                + "FROM borrow_slips bs "
                + "JOIN borrow_details bd ON bs.id = bd.borrow_id "
                + "WHERE bs.due_date < CURDATE() "
                + "AND bd.status = 'Borrowed'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
