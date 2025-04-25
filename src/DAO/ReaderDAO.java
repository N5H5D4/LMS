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

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

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

    public static int addReadersBatch(List<Object[]> readerList) throws SQLException {
        int addedCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            String insertQuery = "INSERT INTO readers (name, identity_card, birth_date, gender, email, address, card_created_at, card_expired_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertQuery);
            con.setAutoCommit(false);

            for (Object[] reader : readerList) {
                try {
                    if (isValid(reader)) {
                        pst.setString(1, reader[0].toString().trim());
                        pst.setString(2, reader[1].toString().trim());
                        pst.setDate(3, java.sql.Date.valueOf(reader[2].toString().trim()));
                        pst.setString(4, reader[3].toString().trim());
                        pst.setString(5, reader[4].toString().trim());
                        pst.setString(6, reader[5].toString().trim());
                        pst.setDate(7, java.sql.Date.valueOf(reader[6].toString().trim()));
                        pst.setDate(8, java.sql.Date.valueOf(reader[7].toString().trim()));
                        pst.addBatch();
                        addedCount++;
                    }
                } catch (Exception ex) {
                    System.err.println("❌ Error in line: " + ex.getMessage());
                }
            }

            if (addedCount > 0) {
                pst.executeBatch();
                con.commit();
            } else {
                con.rollback();
            }

            return addedCount;
        }
    }

    private static boolean isValid(Object[] row) {
        if (row.length < 8) {
            return false;
        }
        for (Object cell : row) {
            if (cell == null || cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static List<Object[]> searchReaders(String keyword, String yearFilter, String searchType, String genderFilter) throws SQLException {
        List<Object[]> resultList = new ArrayList<>();

        String query = "SELECT * FROM readers WHERE 1=1";
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            switch (searchType) {
                case "name":
                    query += " AND name LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "identity card (CCCD/CMND)":
                    query += " AND identity_card LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "address":
                    query += " AND address LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "email":
                    query += " AND email LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "birth date":
                    query += " AND DATE_FORMAT(birth_date, '%Y-%m-%d') LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "All":
                    query += " AND (name LIKE ? OR address LIKE ? OR email LIKE ? OR identity_card LIKE ?)";
                    for (int i = 0; i < 4; i++) {
                        params.add("%" + keyword + "%");
                    }
                    break;
            }
        }

        if (!yearFilter.equals("All years")) {
            query += " AND YEAR(birth_date) = ?";
            params.add(Integer.parseInt(yearFilter));
        }

        if (!genderFilter.equals("All")) {
            query += " AND gender LIKE ?";
            params.add(genderFilter);
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pst.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pst.setInt(i + 1, (Integer) params.get(i));
                }
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("identity_card"),
                    rs.getString("birth_date"),
                    rs.getString("gender"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("card_created_at"),
                    rs.getString("card_expired_at")
                };
                resultList.add(row);
            }

            rs.close();
        }

        return resultList;
    }

    public static boolean updateReader(int id, String name, String identityCard, Date birthDate, String gender,
            String email, String address, Date cardCreatedAt, Date cardExpiredAt) throws SQLException {
        String query = "UPDATE readers SET name = ?, identity_card = ?, birth_date = ?, gender = ?, email = ?, "
                + "address = ?, card_created_at = ?, card_expired_at = ? WHERE id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, name);
            pst.setString(2, identityCard);
            pst.setDate(3, birthDate);
            pst.setString(4, gender);
            pst.setString(5, email);
            pst.setString(6, address);
            pst.setDate(7, cardCreatedAt);
            pst.setDate(8, cardExpiredAt);
            pst.setInt(9, id);

            return pst.executeUpdate() > 0;
        }
    }

    public static boolean deleteReader(String identityCard) throws SQLException {
        String query = "DELETE FROM readers WHERE identity_card = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, identityCard);
            return pst.executeUpdate() > 0;
        }
    }
}
