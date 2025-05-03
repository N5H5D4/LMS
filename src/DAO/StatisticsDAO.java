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

    //Tổng số đầu sách
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

    //Tổng số lượng sách
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

    //Tổng độc giả
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

    //Tổng sách theo từng thể loại
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

    // số đầu sách theo thể loại
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

    //Thống kê độc giả theo giới tính
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

    //top 10 sách được mượn nhiều 
    public static List<Map<String, Object>> getTopBorrowedBooks(int year, int month) {
        List<Map<String, Object>> topBooks = new ArrayList<>();
        String sql = "SELECT bd.isbn, b.title, COUNT(*) as borrow_count "
                + "FROM borrow_details bd "
                + "JOIN borrow_slips bs ON bd.borrow_id = bs.id "
                + "JOIN books b ON bd.isbn = b.isbn "
                + "WHERE YEAR(bs.borrow_date) = ? AND MONTH(bs.borrow_date) = ? "
                + "GROUP BY bd.isbn, b.title "
                + "ORDER BY borrow_count DESC "
                + "LIMIT 10";
        try (Connection conn = DBConnection.getConnection(); 
                 PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("isbn", rs.getString("isbn"));
                bookData.put("title", rs.getString("title"));
                bookData.put("borrow_count", rs.getInt("borrow_count"));
                topBooks.add(bookData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topBooks;
    }

    // Top sách theo năm
    public static List<Map<String, Object>> getTopBorrowedBooks(int year) {
        List<Map<String, Object>> topBooks = new ArrayList<>();
        String sql = "SELECT bd.isbn, b.title, COUNT(*) as borrow_count "
                + "FROM borrow_details bd "
                + "JOIN borrow_slips bs ON bd.borrow_id = bs.id "
                + "JOIN books b ON bd.isbn = b.isbn "
                + "WHERE YEAR(bs.borrow_date) = ? "
                + "GROUP BY bd.isbn, b.title "
                + "ORDER BY borrow_count DESC "
                + "LIMIT 10";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("isbn", rs.getString("isbn"));
                bookData.put("title", rs.getString("title"));
                bookData.put("borrow_count", rs.getInt("borrow_count"));
                topBooks.add(bookData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topBooks;
    }

    // top thể loại theo tháng/năm
    public static List<Map<String, Object>> getTopBorrowedCategories(int year, int month) {
        List<Map<String, Object>> topCategories = new ArrayList<>();
        String sql = "SELECT b.category, COUNT(*) as borrow_count "
                + "FROM borrow_details bd "
                + "JOIN borrow_slips bs ON bd.borrow_id = bs.id "
                + "JOIN books b ON bd.isbn = b.isbn "
                + "WHERE YEAR(bs.borrow_date) = ? AND MONTH(bs.borrow_date) = ? "
                + "GROUP BY b.category "
                + "ORDER BY borrow_count DESC "
                + "LIMIT 5";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", rs.getString("category"));
                categoryData.put("borrow_count", rs.getInt("borrow_count"));
                topCategories.add(categoryData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topCategories;
    }

    // Top 5 thể loại theo năm
    public static List<Map<String, Object>> getTopBorrowedCategories(int year) {
        List<Map<String, Object>> topCategories = new ArrayList<>();
        String sql = "SELECT b.category, COUNT(*) as borrow_count "
                + "FROM borrow_details bd "
                + "JOIN borrow_slips bs ON bd.borrow_id = bs.id "
                + "JOIN books b ON bd.isbn = b.isbn "
                + "WHERE YEAR(bs.borrow_date) = ? "
                + "GROUP BY b.category "
                + "ORDER BY borrow_count DESC "
                + "LIMIT 5";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", rs.getString("category"));
                categoryData.put("borrow_count", rs.getInt("borrow_count"));
                topCategories.add(categoryData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topCategories;
    }
}
