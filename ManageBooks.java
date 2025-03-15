package jframe;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.Year;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author HS
/**
 *
 * @author HS
 */

public class ManageBooks extends javax.swing.JFrame {

    /**
     * Creates new form ManageBooksee
     */
    public ManageBooks() {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        AddPanel = new rojeru_san.complementos.RSButtonHover();
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        searchAreaPanel = new javax.swing.JPanel();
        AddBooksPanel = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        cmbCriteria = new javax.swing.JComboBox<>();
        cmbCategory = new javax.swing.JComboBox<>();
        cmbYear = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        txtQuantity = new javax.swing.JTextField();
        txtISBN = new javax.swing.JTextField();
        txtTitle = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        txtPublisher = new javax.swing.JTextField();
        txtPublishedYear = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtCategory = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        btnDelete = new rojeru_san.complementos.RSButtonHover();
        btnEdit = new rojeru_san.complementos.RSButtonHover();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(0, 51, 51));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AddPanel.setBackground(new java.awt.Color(0, 51, 51));
        AddPanel.setText("Add Books");
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
        jPanel4.add(AddPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, -1, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setText("Search Books");
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
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, -1, -1));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 700));

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel5.setText("Edit Details");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel6.setText("Delete Book");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addGap(30, 30, 30)
                .addComponent(jLabel6)
                .addContainerGap(1035, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 1300, 50));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AddBooksPanel.setBackground(new java.awt.Color(51, 255, 102));
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
            .addGap(0, 30, Short.MAX_VALUE)
        );

        searchAreaPanel.add(AddBooksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 1300, 30));

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
        searchAreaPanel.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 310, 40));

        btnSearch.setBackground(new java.awt.Color(255, 255, 255));
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/searcher_magnifyng_glass_search_locate_find_icon_123813.png"))); // NOI18N
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        searchAreaPanel.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 110, 60));

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

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 210, 1180, 410));

        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "Author", "Published Year" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, 200, 40));

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Category", "Fiction", "History", "Philosophy", "Non-fiction", "Thriller", "Science Fiction", "Classics", "Self-help", "Mystery", "Fantasy", "Drama", "Magical Realism", "Biography", "Horror", "Young Adult", " " }));
        cmbCategory.setToolTipText("");
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 30, 200, 40));

        cmbYear.setToolTipText("");
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 30, 200, 40));

        jPanel5.setBackground(new java.awt.Color(0, 51, 51));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 153, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtQuantity.setText("jTextField1");
        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });
        jPanel5.add(txtQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, -2, 150, 40));

        txtISBN.setText("jTextField1");
        txtISBN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtISBNActionPerformed(evt);
            }
        });
        jPanel5.add(txtISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -2, 150, 40));

        txtTitle.setText("jTextField1");
        txtTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTitleActionPerformed(evt);
            }
        });
        jPanel5.add(txtTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, -2, 150, 40));

        txtAuthor.setText("jTextField1");
        txtAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAuthorActionPerformed(evt);
            }
        });
        jPanel5.add(txtAuthor, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, -2, 150, 40));

        txtPublisher.setText("jTextField1");
        txtPublisher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherActionPerformed(evt);
            }
        });
        jPanel5.add(txtPublisher, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, -2, 150, 40));

        txtPublishedYear.setText("jTextField1");
        txtPublishedYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublishedYearActionPerformed(evt);
            }
        });
        jPanel5.add(txtPublishedYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, -2, 150, 40));
        jPanel5.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 230, -1, -1));

        txtCategory.setText("jTextField1");
        txtCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCategoryActionPerformed(evt);
            }
        });
        jPanel5.add(txtCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, -2, 150, 40));

        txtPrice.setText("jTextField1");
        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });
        jPanel5.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, -2, 150, 40));

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/18x25trash.png"))); // NOI18N
        btnDelete.setText("Delete");
        jPanel5.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 50, 120, -1));

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24 edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel5.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 50, 120, -1));

        searchAreaPanel.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, 1180, 110));

        jPanel1.add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 650));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1500, 700));

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(102, 102, 0));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(59, 6, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE BOOKS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 10, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/li.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 50, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1500, 60));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchBooks();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void AddPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPanelActionPerformed
        AddBooksPanel.setVisible(true);
        searchAreaPanel.setVisible(false);
    }//GEN-LAST:event_AddPanelActionPerformed

    private void AddPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddPanelMouseClicked
        AddBooksPanel.setVisible(true);
        searchAreaPanel.setVisible(false);
    }//GEN-LAST:event_AddPanelMouseClicked

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        searchAreaPanel.setVisible(true);
        AddBooksPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        searchAreaPanel.setVisible(true);
        AddBooksPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelActionPerformed

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

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCategoryActionPerformed

    private void cmbYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbYearActionPerformed

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void txtTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTitleActionPerformed

    private void txtAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAuthorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAuthorActionPerformed

    private void txtPublisherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublisherActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublisherActionPerformed

    private void txtPublishedYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublishedYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublishedYearActionPerformed

    private void txtCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCategoryActionPerformed

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPriceActionPerformed

    private void txtISBNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtISBNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtISBNActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        
    }
    }//GEN-LAST:event_btnEditActionPerformed

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
        java.util.logging.Logger.getLogger(ManageBooks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
        java.util.logging.Logger.getLogger(ManageBooks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
        java.util.logging.Logger.getLogger(ManageBooks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        java.util.logging.Logger.getLogger(ManageBooks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            new ManageBooks().setVisible(true);
        }
    });
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AddBooksPanel;
    private rojeru_san.complementos.RSButtonHover AddPanel;
    private rojeru_san.complementos.RSButtonHover btnDelete;
    private rojeru_san.complementos.RSButtonHover btnEdit;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
