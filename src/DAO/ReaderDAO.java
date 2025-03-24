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
import java.sql.*;
/**
 *
 * @author HS
 */
public class ReaderDAO {
    public static String getReaderNameByID(int readerID) {
        String name = "";
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM readers WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, readerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    
    // Tìm độc giả theo Reader ID
    public static List<String[]> searchReadersById(String readerId) {
        List<String[]> readers = new ArrayList<>();
        String query = "SELECT id, name FROM readers WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, readerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String[] reader = {rs.getString("id"), rs.getString("name")};
                readers.add(reader);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return readers;
    }

    // Thêm phiếu mượn (Đơn lẻ hoặc nhiều)
    public static void insertBorrowSlip(int readerId, String borrowDate, List<String[]> bookDetails) {
        String insertSlipQuery = "INSERT INTO borrow_slips (reader_id, borrow_date) VALUES (?, ?)";
        String insertDetailQuery = "INSERT INTO borrow_details (borrow_id, isbn, status) VALUES (?, ?, 'Borrowed')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement slipStmt = conn.prepareStatement(insertSlipQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement detailStmt = conn.prepareStatement(insertDetailQuery)) {

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
