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
public class PenaltyDAO {

    //
    public static List<Map<String, Object>> getPenalties(String keyword, String year, String month, String filter) {
        List<Map<String, Object>> penalties = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.reader_id, r.name AS reader_name, p.borrow_id, bs.return_date, p.amount, p.reason, "
                + "(SELECT GROUP_CONCAT(DISTINCT CONCAT(bd.isbn, ' - ', b.title) SEPARATOR ', ') "
                + //
                "FROM borrow_details bd JOIN books b ON bd.isbn = b.isbn WHERE bd.borrow_id = p.borrow_id) AS book_list "
                + "FROM penalties p "
                + "JOIN readers r ON p.reader_id = r.id "
                + "JOIN borrow_slips bs ON p.borrow_id = bs.id "
                + "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();
        if (!keyword.isEmpty() && !"All".equals(filter)) {
            if ("ISBN".equals(filter)) {
                sql.append(" AND EXISTS (SELECT 1 FROM borrow_details bd WHERE bd.borrow_id = p.borrow_id AND bd.isbn LIKE ?)");
                params.add("%" + keyword + "%");
            } else if ("Reader Name".equals(filter)) {
                sql.append(" AND r.name LIKE ?");
                params.add("%" + keyword + "%");
            } else if ("Reader ID".equals(filter)) {
                sql.append(" AND p.reader_id = ?");
                params.add(Integer.parseInt(keyword));
            }
        } else if (!keyword.isEmpty()) { // All
            sql.append(" AND (EXISTS (SELECT 1 FROM borrow_details bd WHERE bd.borrow_id = p.borrow_id AND bd.isbn LIKE ?) "
                    + "OR r.name LIKE ? OR p.reader_id = ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            try {
                params.add(Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                params.add(-1);
            }
        }

        if (!"All".equals(year)) {
            sql.append(" AND YEAR(bs.return_date) = ?");
            params.add(Integer.parseInt(year));
        }
        if (!"All".equals(month)) {
            sql.append(" AND MONTH(bs.return_date) = ?");
            params.add(Integer.parseInt(month));
        }

        sql.append(" ORDER BY p.borrow_id");

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> penalty = new HashMap<>();
                penalty.put("reader_id", rs.getInt("reader_id"));
                penalty.put("reader_name", rs.getString("reader_name"));
                penalty.put("borrow_id", rs.getInt("borrow_id"));
                java.sql.Date returnDateSql = rs.getDate("return_date");
                penalty.put("return_date", returnDateSql != null ? new Date(returnDateSql.getTime()) : null);
                penalty.put("amount", rs.getBigDecimal("amount"));
                penalty.put("reason", rs.getString("reason"));
                penalty.put("book_list", rs.getString("book_list"));
                penalties.add(penalty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return penalties;
    }

    //
    public static List<Map<String, Object>> getOverdueNotReturned() {
        List<Map<String, Object>> overdueList = new ArrayList<>();
        String sql = "SELECT bs.id AS borrow_id, bs.reader_id, r.name AS reader_name, bs.due_date, "
                + "(SELECT GROUP_CONCAT(DISTINCT CONCAT(bd.isbn, ' - ', b.title) SEPARATOR ', ') "
                + "FROM borrow_details bd JOIN books b ON bd.isbn = b.isbn WHERE bd.borrow_id = bs.id) AS book_list "
                + "FROM borrow_slips bs "
                + "JOIN readers r ON bs.reader_id = r.id "
                + "WHERE bs.return_date IS NULL AND bs.due_date < CURDATE() "
                + "ORDER BY bs.due_date";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> overdue = new HashMap<>();
                overdue.put("borrow_id", rs.getInt("borrow_id"));
                overdue.put("reader_id", rs.getInt("reader_id"));
                overdue.put("reader_name", rs.getString("reader_name"));
                java.sql.Date dueDateSql = rs.getDate("due_date");
                overdue.put("due_date", new Date(dueDateSql.getTime()));
                overdue.put("book_list", rs.getString("book_list"));
                overdueList.add(overdue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return overdueList;
    }
}
