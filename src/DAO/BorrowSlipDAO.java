/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Date;
import jframe.DBConnection;

import java.util.ArrayList;
import java.util.List;

public class BorrowSlipDAO {

    public static int saveBorrowSlip(int readerID, java.util.Date borrowDate) {
        int borrowId = -1;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO borrow_slips (reader_id, borrow_date) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, readerID);
            stmt.setDate(2, new Date(borrowDate.getTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    borrowId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowId;
    }

    public static boolean saveBorrowDetail(int borrowId, String isbn) {
        boolean success = false;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO borrow_details (borrow_id, isbn) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrowId);
            stmt.setString(2, isbn);

            int affectedRows = stmt.executeUpdate();
            success = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static List<String[]> searchBooksByTitle(String keyword) {
        List<String[]> books = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT isbn, title FROM books WHERE title LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                books.add(new String[]{isbn, title});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Thêm phiếu mượn (Đơn lẻ hoặc nhiều)
    public static void insertBorrowSlip(int readerId, String borrowDate, List<String[]> bookDetails) {
        String insertSlipQuery = "INSERT INTO borrow_slips (reader_id, borrow_date) VALUES (?, ?)";
        String insertDetailQuery = "INSERT INTO borrow_details (borrow_id, isbn, status) VALUES (?, ?, 'Borrowed')";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement slipStmt = conn.prepareStatement(insertSlipQuery, Statement.RETURN_GENERATED_KEYS); PreparedStatement detailStmt = conn.prepareStatement(insertDetailQuery)) {

            // Thêm phiếu mượn vào bảng `borrow_slips`
            slipStmt.setInt(1, readerId);
            slipStmt.setDate(2, Date.valueOf(borrowDate));
            slipStmt.executeUpdate();

            // Lấy `borrow_id` vừa tạo ra
            ResultSet generatedKeys = slipStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int borrowId = generatedKeys.getInt(1);

                // Thêm từng sách vào bảng `borrow_details`
                for (String[] book : bookDetails) {
                    detailStmt.setInt(1, borrowId);
                    detailStmt.setString(2, book[0]);  // ISBN
                    detailStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
