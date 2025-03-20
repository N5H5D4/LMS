/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import static jframe.DBConnection.con;

/**
 *
 * @author HS
 */
public class ManageBookss extends javax.swing.JFrame {

    /**
     * Creates new form ManageBookss
     */
    public ManageBookss() {
        initComponents();
        searchAreaPanel.setVisible(false);
        AddBooksPanel.setVisible(false);
        populateYearComboBox();
        enableRightClickCopy(tblBooks);

    }

    private void enableRightClickCopy(JTable table) {

        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem("Copy");

        copyMenuItem.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            int[] selectedColumns = table.getSelectedColumns();

            if (selectedRows.length == 0 || selectedColumns.length == 0) {
                return;
            }

            StringBuilder copiedText = new StringBuilder();

            for (int row : selectedRows) {
                for (int col : selectedColumns) {
                    Object value = table.getValueAt(row, col);
                    copiedText.append(value == null ? "" : value.toString()).append("\t");
                }
                copiedText.setLength(copiedText.length() - 1);
                copiedText.append("\n");
            }

            if (copiedText.length() > 0) {
                copiedText.setLength(copiedText.length() - 1);
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(copiedText.toString()), null
            );

            JOptionPane.showMessageDialog(null, "Copied!");
        });

        popupMenu.add(copyMenuItem);

        table.setComponentPopupMenu(popupMenu);
    }

    private void populateYearComboBox() {
        int currentYear = Year.now().getValue();

        cmbYear.addItem("All years");

        for (int year = currentYear; year >= 1700; year--) {
            cmbYear.addItem(String.valueOf(year));
        }
    }

    private void filterTableByYear() {
        String selectedYear = (String) cmbYear.getSelectedItem();

        if (selectedYear.equals("All years")) {

            return;
        }

        try {
            int year = Integer.parseInt(selectedYear);
            int currentYear = Year.now().getValue();

            if (year > currentYear) {
                cmbYear.setSelectedItem("All years");
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    private void searchBooks() {
        String keyword = txtSearch.getText().trim();
        String yearFilter = cmbYear.getSelectedItem().toString();
        String categoryFilter = cmbCategory.getSelectedItem().toString();
        String searchType = cmbCriteria.getSelectedItem().toString();

        String query = "SELECT * FROM books WHERE 1=1";
        List<Object> params = new ArrayList<>();

        // Thêm điều kiện tìm kiếm nếu có từ khóa
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
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    break;
            }
        }

        // Thêm điều kiện lọc năm nếu không phải là "All year"
        if (!yearFilter.equals("All years")) {
            query += " AND published_year = ?";
            try {
                int year = Integer.parseInt(yearFilter);
                params.add(year);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Năm không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Thêm điều kiện lọc thể loại nếu không phải là "All Category"
        if (!categoryFilter.equals("All Category")) {
            query += " AND category = ?";
            params.add(categoryFilter);
        }

        try {

            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(query);

            // Thiết lập các tham số truy vấn
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pst.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pst.setInt(i + 1, (Integer) params.get(i));
                }
            }

            ResultSet rs = pst.executeQuery();
            DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getInt("published_year"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                };
                model.addRow(row);
            }

            rs.close();
            pst.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm sách: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtISBN.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtPublishedYear.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
        txtCategory.setText("");
    }

    private void saveBooks() {
        DefaultTableModel model = (DefaultTableModel) tblBooks1.getModel();
        int rowCount = tblBooks1.getRowCount();

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "Không có sách nào để lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String insertQuery = "INSERT INTO books (isbn, title, author, publisher, published_year, category, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertQuery);

            con.setAutoCommit(false); // Bắt đầu transaction

            int addedCount = 0;
            for (int i = 0; i < rowCount; i++) {
                try {
                    Object isbnObj = tblBooks1.getValueAt(i, 0);
                    Object titleObj = tblBooks1.getValueAt(i, 1);
                    Object authorObj = tblBooks1.getValueAt(i, 2);
                    Object publisherObj = tblBooks1.getValueAt(i, 3);
                    Object yearObj = tblBooks1.getValueAt(i, 4);
                    Object categoryObj = tblBooks1.getValueAt(i, 5);
                    Object priceObj = tblBooks1.getValueAt(i, 6);
                    Object quantityObj = tblBooks1.getValueAt(i, 7);

                    // Bỏ qua dòng trống hoàn toàn
                    if (isbnObj == null && titleObj == null && authorObj == null && publisherObj == null
                            && yearObj == null && categoryObj == null && priceObj == null && quantityObj == null) {
                        continue;
                    }

                    // Kiểm tra null hoặc chuỗi rỗng
                    if (isbnObj == null || titleObj == null || authorObj == null || publisherObj == null
                            || yearObj == null || categoryObj == null || priceObj == null || quantityObj == null
                            || isbnObj.toString().trim().isEmpty() || titleObj.toString().trim().isEmpty()
                            || authorObj.toString().trim().isEmpty() || publisherObj.toString().trim().isEmpty()
                            || yearObj.toString().trim().isEmpty() || categoryObj.toString().trim().isEmpty()
                            || priceObj.toString().trim().isEmpty() || quantityObj.toString().trim().isEmpty()) {
                        continue; // Bỏ qua dòng chưa đầy đủ dữ liệu
                    }

                    // Lấy dữ liệu và chuyển đổi
                    String isbn = isbnObj.toString().trim();
                    String title = titleObj.toString().trim();
                    String author = authorObj.toString().trim();
                    String publisher = publisherObj.toString().trim();
                    int year = Integer.parseInt(yearObj.toString().trim());
                    String category = categoryObj.toString().trim();
                    double price = Double.parseDouble(priceObj.toString().trim());
                    int quantity = Integer.parseInt(quantityObj.toString().trim());

                    // Thêm dữ liệu vào PreparedStatement
                    pst.setString(1, isbn);
                    pst.setString(2, title);
                    pst.setString(3, author);
                    pst.setString(4, publisher);
                    pst.setInt(5, year);
                    pst.setString(6, category);
                    pst.setDouble(7, price);
                    pst.setInt(8, quantity);

                    pst.addBatch(); // Thêm vào batch
                    addedCount++;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi định dạng số ở dòng " + (i + 1) + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi không xác định ở dòng " + (i + 1) + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (addedCount > 0) {
                pst.executeBatch(); // Thực hiện batch insert
                con.commit(); // Xác nhận transaction
                JOptionPane.showMessageDialog(this, addedCount + " sách đã được thêm!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                model.setRowCount(0); // Xóa bảng sau khi lưu thành công
                model.addRow(new Object[]{"", "", "", "", "", "", "", ""}); // Tạo một dòng trống mới để nhập thêm
            } else {
                con.rollback(); // Hoàn tác nếu không có sách nào được thêm
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sách: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnBack = new rojerusan.RSButtonHover();
        jPanel4 = new javax.swing.JPanel();
        btnAddBook = new rojeru_san.complementos.RSButtonHover();
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        searchAreaPanel = new javax.swing.JPanel();
        cmbCategory = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        txtISBN = new javax.swing.JTextField();
        txtTitle = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        txtPublisher = new javax.swing.JTextField();
        txtQuantity = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtPublishedYear = new javax.swing.JTextField();
        btnEdit = new rojerusan.RSButtonHover();
        btnDelete = new rojerusan.RSButtonHover();
        jLabel11 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtCategory = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        rSButtonHover1 = new rojerusan.RSButtonHover();
        cmbYear = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        cmbCriteria = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        AddBooksPanel = new javax.swing.JPanel();
        btnAddRow = new rojerusan.RSButtonHover();
        btnSave = new rojerusan.RSButtonHover();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBooks1 = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(102, 102, 0));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE BOOKS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/li.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 50, -1));

        btnBack.setBackground(new java.awt.Color(255, 255, 255));
        btnBack.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(0, 51, 51)));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/back_main_page_icon_124174.png"))); // NOI18N
        btnBack.setColorHover(new java.awt.Color(204, 0, 51));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        jPanel2.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(1400, 0, 100, 50));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1500, 50));

        jPanel4.setBackground(new java.awt.Color(0, 51, 51));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddBook.setBackground(new java.awt.Color(0, 51, 51));
        btnAddBook.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24business_application_addthedatabase_add_insert_database_db_2313.png"))); // NOI18N
        btnAddBook.setText("     Add Books");
        btnAddBook.setColorHover(new java.awt.Color(102, 153, 255));
        btnAddBook.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAddBook.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddBookMouseClicked(evt);
            }
        });
        btnAddBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBookActionPerformed(evt);
            }
        });
        jPanel4.add(btnAddBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(-40, 130, 240, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24search_find_database_16703.png"))); // NOI18N
        searchPanel.setText("    Search Books");
        searchPanel.setColorHover(new java.awt.Color(102, 153, 255));
        searchPanel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        searchPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchPanelMouseClicked(evt);
            }
        });
        searchPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPanelActionPerformed(evt);
            }
        });
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(-20, 70, 220, -1));

        jPanel5.setBackground(new java.awt.Color(102, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("FEATURES");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 50));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/shelving.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, -1, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cmbCategory.setBackground(new java.awt.Color(255, 255, 255));
        cmbCategory.setForeground(new java.awt.Color(0, 0, 0));
        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Category", "Fiction", "History", "Philosophy", "Non-fiction", "Thriller", "Science Fiction", "Classics", "Self-help", "Mystery", "Fantasy", "Drama", "Magical Realism", "Biography", "Horror", "Young Adult", " " }));
        cmbCategory.setToolTipText("");
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 50, 200, 40));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtISBN.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtISBN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(txtISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 40, 160, -1));

        txtTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTitle.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTitleActionPerformed(evt);
            }
        });
        jPanel1.add(txtTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 200, -1));

        txtAuthor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAuthor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAuthorActionPerformed(evt);
            }
        });
        jPanel1.add(txtAuthor, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, 200, -1));

        txtPublisher.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPublisher.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPublisher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherActionPerformed(evt);
            }
        });
        jPanel1.add(txtPublisher, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 40, 160, -1));

        txtQuantity.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtQuantity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });
        jPanel1.add(txtQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 110, 150, -1));

        txtPrice.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPrice.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });
        jPanel1.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 40, 150, -1));

        txtPublishedYear.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPublishedYear.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(txtPublishedYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 110, 160, -1));

        btnEdit.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24 edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel1.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 20, 130, -1));

        btnDelete.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/18x25trash.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel1.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 90, 130, -1));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book.png"))); // NOI18N
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        jPanel6.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 3, 130));

        jPanel7.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 10, 3, 130));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Book's ISBN");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Tittle:");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Author:");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Publisher:");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Published year:");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 90, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Category:");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Price:");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, -1, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Quantity:");
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 90, -1, -1));

        txtCategory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCategory.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(txtCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 160, -1));

        searchAreaPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 1280, 150));

        tblBooks.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblBooks.setForeground(new java.awt.Color(0, 0, 0));
        tblBooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ISBN", "Title", "Author", "Publisher", "Published year ", "Category", "Price", "Quantity"
            }
        ));
        tblBooks.setGridColor(new java.awt.Color(0, 51, 51));
        tblBooks.setPreferredSize(new java.awt.Dimension(600, 4000));
        tblBooks.setRowHeight(27);
        tblBooks.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblBooks.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblBooks.setShowGrid(true);
        tblBooks.setShowVerticalLines(false);
        tblBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBooksMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblBooks);

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 1280, 420));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("CATEGORY");
        searchAreaPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 20, -1, -1));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtSearch.setBackground(new java.awt.Color(255, 255, 255));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 0, 0)));
        txtSearch.setForeground(new java.awt.Color(0, 0, 0));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setCaretColor(new java.awt.Color(0, 0, 0));
        txtSearch.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSearch.setPlaceholder("ENTER KEYWORD");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanel8.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 310, 40));

        rSButtonHover1.setBackground(new java.awt.Color(255, 255, 255));
        rSButtonHover1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 0, new java.awt.Color(0, 0, 0)));
        rSButtonHover1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24_searcher_magnifyng_glass_search_locate_find_icon_123813.png"))); // NOI18N
        rSButtonHover1.setColorHover(new java.awt.Color(51, 255, 0));
        rSButtonHover1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSButtonHover1ActionPerformed(evt);
            }
        });
        jPanel8.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 50, 40));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setForeground(new java.awt.Color(0, 0, 0));
        cmbYear.setToolTipText("");
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        jPanel8.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 40, 200, 40));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("PUBLISHED YEAR");
        jPanel8.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, -1, -1));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "Author", "Published Year" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel8.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 40, 200, 40));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("CRITERIA ");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 10, -1, -1));

        searchAreaPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 100));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book-wall-1151405_1920.jpg"))); // NOI18N
        searchAreaPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        AddBooksPanel.setBackground(new java.awt.Color(0, 204, 204));
        AddBooksPanel.setAlignmentX(1.0F);
        AddBooksPanel.setAlignmentY(1.0F);
        AddBooksPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddRow.setBackground(new java.awt.Color(102, 153, 255));
        btnAddRow.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnAddRow.setForeground(new java.awt.Color(0, 0, 0));
        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24X24add-1_icon-icons.com_65127.png"))); // NOI18N
        btnAddRow.setText("Add row");
        btnAddRow.setColorHover(new java.awt.Color(204, 0, 0));
        btnAddRow.setColorText(new java.awt.Color(0, 0, 0));
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });
        AddBooksPanel.add(btnAddRow, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 590, 130, 50));

        btnSave.setBackground(new java.awt.Color(102, 153, 255));
        btnSave.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24X24save_78935.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.setColorHover(new java.awt.Color(204, 0, 0));
        btnSave.setColorText(new java.awt.Color(0, 0, 0));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        AddBooksPanel.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 590, 130, 50));

        tblBooks1.setBackground(new java.awt.Color(255, 255, 255));
        tblBooks1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tblBooks1.setForeground(new java.awt.Color(0, 0, 0));
        tblBooks1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Title", "Author", "Publisher", "Published year ", "Category", "Price", "Quantity"
            }
        ));
        tblBooks1.setGridColor(new java.awt.Color(0, 51, 51));
        tblBooks1.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tblBooks1.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblBooks1.setRowHeight(30);
        tblBooks1.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblBooks1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblBooks1.setShowGrid(true);
        tblBooks1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBooks1MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblBooks1);

        AddBooksPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 1310, 440));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("ADD BOOKS");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("*Enter complete information per line to add book information");
        jPanel9.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 60, -1, -1));

        AddBooksPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 660, 100));

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book-wall-1151405_1920.jpg"))); // NOI18N
        AddBooksPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

        getContentPane().add(AddBooksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddBookMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddBookMouseClicked
        searchAreaPanel.setVisible(false);
        AddBooksPanel.setVisible(true);

    }//GEN-LAST:event_btnAddBookMouseClicked

    private void btnAddBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBookActionPerformed
        searchAreaPanel.setVisible(false);
        AddBooksPanel.setVisible(true);

    }//GEN-LAST:event_btnAddBookActionPerformed

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        searchAreaPanel.setVisible(true);
        AddBooksPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        searchAreaPanel.setVisible(true);
        AddBooksPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void cmbYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbYearActionPerformed

    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCategoryActionPerformed

    private void tblBooksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBooksMouseClicked
        int selectedRow = tblBooks.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();

        txtISBN.setText(model.getValueAt(selectedRow, 0).toString());
        txtTitle.setText(model.getValueAt(selectedRow, 1).toString());
        txtAuthor.setText(model.getValueAt(selectedRow, 2).toString());
        txtPublisher.setText(model.getValueAt(selectedRow, 3).toString());
        txtPublishedYear.setText(model.getValueAt(selectedRow, 4).toString());
        txtCategory.setText(model.getValueAt(selectedRow, 5).toString());
        txtPrice.setText(model.getValueAt(selectedRow, 6).toString());
        txtQuantity.setText(model.getValueAt(selectedRow, 7).toString());
    }//GEN-LAST:event_tblBooksMouseClicked

    private void txtPublisherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublisherActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublisherActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        String isbn = txtISBN.getText();
        String title = txtTitle.getText();
        String author = txtAuthor.getText();
        String publisher = txtPublisher.getText();
        int publishedYear = Integer.parseInt(txtPublishedYear.getText());
        String category = txtCategory.getText();
        double price = Double.parseDouble(txtPrice.getText());
        int quantity = Integer.parseInt(txtQuantity.getText());

        try {
            Connection con = DBConnection.getConnection();
            String query = "UPDATE books SET title = ?, author = ?, publisher = ?, published_year = ?, category = ?, price = ?, quantity = ? WHERE isbn = ?";
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, title);
            pst.setString(2, author);
            pst.setString(3, publisher);
            pst.setInt(4, publishedYear);
            pst.setString(5, category);
            pst.setDouble(6, price);
            pst.setInt(7, quantity);
            pst.setString(8, isbn);

            int rowCount = pst.executeUpdate();

            if (rowCount > 0) {
                JOptionPane.showMessageDialog(this, "Book updated successfully.");
                searchBooks(); // Refresh bảng hiển thị
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update the book.");
            }

            pst.close();
            con.close();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        HomePage home = new HomePage();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void rSButtonHover1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rSButtonHover1ActionPerformed
        searchBooks();
    }//GEN-LAST:event_rSButtonHover1ActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String isbn = txtISBN.getText();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", "Delete Book", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection con = DBConnection.getConnection();
                String query = "DELETE FROM books WHERE isbn = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, isbn);

                int rowCount = pst.executeUpdate();

                if (rowCount > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                    searchBooks(); // Refresh bảng hiển thị
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete the book.");
                }

                pst.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tblBooks1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBooks1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBooks1MouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveBooks();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblBooks1.getModel();
        model.addRow(new Object[]{"", "", "", "", "", "", "", ""}); // ✅ ĐÚNG

    }//GEN-LAST:event_btnAddRowActionPerformed

    private void txtTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTitleActionPerformed

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void txtAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAuthorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAuthorActionPerformed

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPriceActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ManageBookss.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageBookss.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageBookss.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageBookss.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManageBookss().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AddBooksPanel;
    private rojeru_san.complementos.RSButtonHover btnAddBook;
    private rojerusan.RSButtonHover btnAddRow;
    private rojerusan.RSButtonHover btnBack;
    private rojerusan.RSButtonHover btnDelete;
    private rojerusan.RSButtonHover btnEdit;
    private rojerusan.RSButtonHover btnSave;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private rojerusan.RSButtonHover rSButtonHover1;
    private javax.swing.JPanel searchAreaPanel;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private javax.swing.JTable tblBooks;
    private javax.swing.JTable tblBooks1;
    private javax.swing.JTextField txtAuthor;
    private javax.swing.JTextField txtCategory;
    private javax.swing.JTextField txtISBN;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtPublishedYear;
    private javax.swing.JTextField txtPublisher;
    private javax.swing.JTextField txtQuantity;
    private app.bolivia.swing.JCTextField txtSearch;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}
