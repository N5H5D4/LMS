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

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

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

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

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

    //
    public List<Object[]> searchBooks(String keyword, String yearFilter, String categoryFilter, String searchType) {
        List<Object[]> bookList = new ArrayList<>();
        String query = "SELECT * FROM books WHERE 1=1";
        List<Object> params = new ArrayList<>();

        if (!keyword.isEmpty()) {
            switch (searchType) {
                case "ISBN":
                    query += " AND isbn LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "Title":
                    query += " AND title LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "Author":
                    query += " AND author LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "All":
                    query += " AND (isbn LIKE ? OR title LIKE ? OR author LIKE ? OR CAST(published_year AS CHAR) LIKE ?)";
                    for (int i = 0; i < 4; i++) {
                        params.add("%" + keyword + "%");
                    }
                    break;
            }
        }

        if (!yearFilter.equals("All years")) {
            query += " AND published_year = ?";
            params.add(Integer.parseInt(yearFilter));
        }

        if (!categoryFilter.equals("All Category")) {
            query += " AND category = ?";
            params.add(categoryFilter);
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pst.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pst.setInt(i + 1, (Integer) param);
                }
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getInt("published_year"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                };
                bookList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookList;
    }

    //
    public int saveBooks(List<Object[]> books) {
        int addedCount = 0;
        String insertQuery = "INSERT INTO books (isbn, title, author, publisher, published_year, category, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(insertQuery)) {
            con.setAutoCommit(false);

            for (Object[] book : books) {
                pst.setString(1, book[0].toString());
                pst.setString(2, book[1].toString());
                pst.setString(3, book[2].toString());
                pst.setString(4, book[3].toString());
                pst.setInt(5, Integer.parseInt(book[4].toString()));
                pst.setString(6, book[5].toString());
                pst.setDouble(7, Double.parseDouble(book[6].toString()));
                pst.setInt(8, Integer.parseInt(book[7].toString()));
                pst.addBatch();
                addedCount++;
            }

            if (addedCount > 0) {
                pst.executeBatch();
                con.commit();
            } else {
                con.rollback();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return addedCount;
    }

    //
    public boolean updateBook(Object[] bookData) {
        String query = "UPDATE books SET title = ?, author = ?, publisher = ?, published_year = ?, category = ?, price = ?, quantity = ? WHERE isbn = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, bookData[1].toString()); // title
            pst.setString(2, bookData[2].toString()); // author
            pst.setString(3, bookData[3].toString()); // publisher
            pst.setInt(4, Integer.parseInt(bookData[4].toString())); // published_year
            pst.setString(5, bookData[5].toString()); // category
            pst.setDouble(6, Double.parseDouble(bookData[6].toString())); // price
            pst.setInt(7, Integer.parseInt(bookData[7].toString())); // quantity
            pst.setString(8, bookData[0].toString()); // isbn

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //
    public boolean deleteBookByISBN(String isbn) {
        String query = "DELETE FROM books WHERE isbn = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, isbn);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
