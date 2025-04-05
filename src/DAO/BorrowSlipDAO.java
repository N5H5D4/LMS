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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    public Integer getBorrowId(int readerId, String borrowDate) {
        Integer borrowId = null;
        String query = "SELECT id FROM borrow_slips WHERE reader_id = ? AND borrow_date = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, readerId);
            pst.setString(2, borrowDate);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                borrowId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowId;
    }

    public boolean deleteBorrowRecord(int borrowId, String isbn) {
        boolean isDeleted = false;
        String deleteDetailQuery = "DELETE FROM borrow_details WHERE borrow_id = ? AND isbn = ?";
        String deleteSlipQuery = "DELETE FROM borrow_slips WHERE id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // Bắt đầu transaction

            // Xóa chi tiết mượn trước
            try (PreparedStatement pstDetail = con.prepareStatement(deleteDetailQuery)) {
                pstDetail.setInt(1, borrowId);
                pstDetail.setString(2, isbn);
                int detailRows = pstDetail.executeUpdate();

                if (detailRows > 0) {
                    // Kiểm tra xem phiếu mượn còn sách nào không
                    try (PreparedStatement checkStmt = con.prepareStatement(
                            "SELECT COUNT(*) FROM borrow_details WHERE borrow_id = ?")) {
                        checkStmt.setInt(1, borrowId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement pstSlip = con.prepareStatement(deleteSlipQuery)) {
                                pstSlip.setInt(1, borrowId);
                                pstSlip.executeUpdate();
                            }
                        }
                    }
                    con.commit();
                    isDeleted = true;
                } else {
                    con.rollback();
                }
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isDeleted;

    }

    public boolean updateBorrowRecord(int borrowId, String oldIsbn, String newIsbn, int readerId, Date dueDate, Date returnDate, String status) {
        Connection con = DBConnection.getConnection();
        try {
            con.setAutoCommit(false);

            // Update borrow_slips first
            String updateSlipQuery = "UPDATE borrow_slips SET reader_id = ?, due_date = ?, return_date = ? WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(updateSlipQuery)) {
                pst.setInt(1, readerId);
                pst.setDate(2, new java.sql.Date(dueDate.getTime()));
                pst.setDate(3, returnDate != null ? new java.sql.Date(returnDate.getTime()) : null);
                pst.setInt(4, borrowId);
                System.out.println("Executing: " + pst);
                pst.executeUpdate();
            }

            // If ISBN changes, delete old and insert new in borrow_details
            if (!oldIsbn.equals(newIsbn)) {
                String deleteQuery = "DELETE FROM borrow_details WHERE borrow_id = ? AND isbn = ?";
                try (PreparedStatement pst = con.prepareStatement(deleteQuery)) {
                    pst.setInt(1, borrowId);
                    pst.setString(2, oldIsbn);
                    System.out.println("Executing: " + pst);
                    pst.executeUpdate();
                }

                String insertQuery = "INSERT INTO borrow_details (borrow_id, isbn, status) VALUES (?, ?, ?)";
                try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
                    pst.setInt(1, borrowId);
                    pst.setString(2, newIsbn);
                    pst.setString(3, status);
                    System.out.println("Executing: " + pst);
                    pst.executeUpdate();
                }
            } else {
                // If ISBN doesn't change, just update status
                String updateDetailsQuery = "UPDATE borrow_details SET status = ? WHERE borrow_id = ? AND isbn = ?";
                try (PreparedStatement pst = con.prepareStatement(updateDetailsQuery)) {
                    pst.setString(1, status);
                    pst.setInt(2, borrowId);
                    pst.setString(3, newIsbn);
                    System.out.println("Executing: " + pst);
                    pst.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static List<Map<String, Object>> getUnreturnedBorrowSlips(int readerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String query = """
        SELECT bs.id, bs.borrow_date, bs.due_date
        FROM borrow_slips bs
        JOIN borrow_details bd ON bs.id = bd.borrow_id
        WHERE bs.reader_id = ? AND bd.status != 'Returned'
        GROUP BY bs.id
    """;

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, readerId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("borrow_date", rs.getDate("borrow_date"));
                row.put("due_date", rs.getDate("due_date"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<Map<String, Object>> getBorrowDetailsBySlipId(int slipId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String query = """
        SELECT bd.isbn, b.title, bd.status
        FROM borrow_details bd
        JOIN books b ON bd.isbn = b.isbn
        WHERE bd.borrow_id = ?;
    """;

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, slipId);
            ResultSet rs = pst.executeQuery();

            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("isbn", rs.getString("isbn"));
                row.put("title", rs.getString("title"));
                row.put("status", rs.getString("status"));
                result.add(row);

                
                //System.out.println("Row: " + row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getBookStatus(int slipId, String isbn) {
        String sql = "SELECT status FROM borrow_details WHERE borrow_id = ? AND isbn = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, slipId);
            stmt.setString(2, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateBookStatus(int slipId, String isbn, String status) {
        String sql = "UPDATE borrow_details SET status = ? WHERE borrow_id = ? AND isbn = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, slipId);
            stmt.setString(3, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateReturnDate(int slipId, java.sql.Date returnDate) {
        String sql = "UPDATE borrow_slips SET return_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, returnDate);
            stmt.setInt(2, slipId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BigDecimal getBookPriceByISBN(String isbn) {
        String sql = "SELECT price FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public static int getReaderIdBySlip(int slipId) {
        String sql = "SELECT reader_id FROM borrow_slips WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, slipId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("reader_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void insertPenalty(int readerId, int borrowId, BigDecimal amount, String reason) {
        String sql = "INSERT INTO penalties (reader_id, borrow_id, amount, reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, readerId);
            stmt.setInt(2, borrowId);
            stmt.setBigDecimal(3, amount);
            stmt.setString(4, reason);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
