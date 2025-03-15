/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
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
        AddPanel = new rojeru_san.complementos.RSButtonHover();
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        searchAreaPanel = new javax.swing.JPanel();
        AddBooksPanel = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        cmbCriteria = new javax.swing.JComboBox<>();
        cmbYear = new javax.swing.JComboBox<>();
        cmbCategory = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        txtISBN = new javax.swing.JTextField();
        txtTitle = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        txtPublisher = new javax.swing.JTextField();
        txtQuantity = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtCategory = new javax.swing.JTextField();
        txtPublishedYear = new javax.swing.JTextField();
        btnEdit = new rojerusan.RSButtonHover();
        btnDelete = new rojerusan.RSButtonHover();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        rSButtonHover1 = new rojerusan.RSButtonHover();

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

        btnBack.setBackground(new java.awt.Color(51, 255, 0));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/back_main_page_icon_124174.png"))); // NOI18N
        btnBack.setColorHover(new java.awt.Color(204, 0, 51));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        jPanel2.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(1360, 0, 140, 60));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1500, 60));

        jPanel4.setBackground(new java.awt.Color(0, 51, 51));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AddPanel.setBackground(new java.awt.Color(0, 51, 51));
        AddPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24business_application_addthedatabase_add_insert_database_db_2313.png"))); // NOI18N
        AddPanel.setText("     Add Books");
        AddPanel.setColorHover(new java.awt.Color(102, 153, 255));
        AddPanel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        AddPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AddPanelMouseClicked(evt);
            }
        });
        AddPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddPanelActionPerformed(evt);
            }
        });
        jPanel4.add(AddPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(-40, 130, 240, -1));

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
        jLabel3.setText("FEATURES");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 50));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/shelving.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, -1, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 200, 720));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AddBooksPanel.setBackground(new java.awt.Color(255, 255, 255));
        AddBooksPanel.setAlignmentX(1.0F);
        AddBooksPanel.setAlignmentY(1.0F);

        javax.swing.GroupLayout AddBooksPanelLayout = new javax.swing.GroupLayout(AddBooksPanel);
        AddBooksPanel.setLayout(AddBooksPanelLayout);
        AddBooksPanelLayout.setHorizontalGroup(
            AddBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        AddBooksPanelLayout.setVerticalGroup(
            AddBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        searchAreaPanel.add(AddBooksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 720));

        txtSearch.setBackground(new java.awt.Color(255, 255, 255));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
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
        searchAreaPanel.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 310, 40));

        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "Author", "Published Year" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 50, 200, 40));

        cmbYear.setToolTipText("");
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 50, 200, 40));

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Category", "Fiction", "History", "Philosophy", "Non-fiction", "Thriller", "Science Fiction", "Classics", "Self-help", "Mystery", "Fantasy", "Drama", "Magical Realism", "Biography", "Horror", "Young Adult", " " }));
        cmbCategory.setToolTipText("");
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 50, 200, 40));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtISBN.setText("ISBN");
        jPanel1.add(txtISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 150, -1));

        txtTitle.setText("Title");
        jPanel1.add(txtTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 0, 160, -1));

        txtAuthor.setText("Author");
        jPanel1.add(txtAuthor, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 0, 160, -1));

        txtPublisher.setText("Publisher");
        txtPublisher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherActionPerformed(evt);
            }
        });
        jPanel1.add(txtPublisher, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 0, 160, -1));

        txtQuantity.setText("Quantity");
        jPanel1.add(txtQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 0, 150, -1));

        txtPrice.setText("Price");
        jPanel1.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 0, 140, -1));

        txtCategory.setText("Category");
        jPanel1.add(txtCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 0, 150, -1));

        txtPublishedYear.setText("Published year");
        jPanel1.add(txtPublishedYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 0, 150, -1));

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24 edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel1.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 50, 130, -1));

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/18x25trash.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel1.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 50, 130, -1));

        searchAreaPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, 1220, 100));

        tblBooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Title", "Author", "Publisher", "Published year ", "Category", "Price", "Quantity"
            }
        ));
        tblBooks.setPreferredSize(new java.awt.Dimension(600, 2000));
        tblBooks.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblBooks.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBooksMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblBooks);

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 220, 1180, 400));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("CATEGORY");
        searchAreaPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 20, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("CRITERIA ");
        searchAreaPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("PUBLISHED YEAR");
        searchAreaPanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 20, -1, -1));

        rSButtonHover1.setBackground(new java.awt.Color(255, 255, 255));
        rSButtonHover1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24_searcher_magnifyng_glass_search_locate_find_icon_123813.png"))); // NOI18N
        rSButtonHover1.setColorHover(new java.awt.Color(51, 255, 0));
        rSButtonHover1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSButtonHover1ActionPerformed(evt);
            }
        });
        searchAreaPanel.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 70, -1));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 60, 1300, 720));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddPanelMouseClicked
        AddBooksPanel.setVisible(true);
        searchAreaPanel.setVisible(false);
    }//GEN-LAST:event_AddPanelMouseClicked

    private void AddPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPanelActionPerformed
        AddBooksPanel.setVisible(true);
        searchAreaPanel.setVisible(false);
    }//GEN-LAST:event_AddPanelActionPerformed

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
    private rojeru_san.complementos.RSButtonHover AddPanel;
    private rojerusan.RSButtonHover btnBack;
    private rojerusan.RSButtonHover btnDelete;
    private rojerusan.RSButtonHover btnEdit;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private rojerusan.RSButtonHover rSButtonHover1;
    private javax.swing.JPanel searchAreaPanel;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private javax.swing.JTable tblBooks;
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
