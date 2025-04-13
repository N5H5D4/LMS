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
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;
import static jframe.DBConnection.con;
import javax.swing.border.MatteBorder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;

import DAO.*;
import UI_Helper.RoundedPanel;

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

        styleTable(tblBooks);
        styleTable(tblBooks1);

        jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    private void styleTable(JTable table) {
        // Tạo renderer cho tiêu đề cột
        MatteBorder matteBorder = new MatteBorder(0, 0, 3, 0, new Color(255, 255, 255));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(0, 51, 51));
                label.setBorder(matteBorder);
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Căn giữa nội dung trong các ô
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
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

        BookDAO dao = new BookDAO();
        List<Object[]> list = dao.searchBooks(keyword, yearFilter, categoryFilter, searchType);

        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();
        model.setRowCount(0);
        for (Object[] row : list) {
            model.addRow(row);
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
            JOptionPane.showMessageDialog(this, "No books to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Object[]> booksToSave = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[8];
            boolean valid = true;
            for (int j = 0; j < 8; j++) {
                Object val = model.getValueAt(i, j);
                if (val == null || val.toString().trim().isEmpty()) {
                    valid = false;
                    break;
                }
                row[j] = val.toString().trim();
            }
            if (valid) {
                booksToSave.add(row);
            }
        }

        if (!booksToSave.isEmpty()) {
            BookDAO dao = new BookDAO();
            int added = dao.saveBooks(booksToSave);
            if (added > 0) {
                JOptionPane.showMessageDialog(this, added + " books added!");
                model.setRowCount(0);
                model.addRow(new Object[]{"", "", "", "", "", "", "", ""});
            }
        } else {
            JOptionPane.showMessageDialog(this, "No valid books to save!");
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

        AddBooksPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBooks1 = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        btnAddRow = new rojerusan.RSButtonHover();
        btnSave = new rojerusan.RSButtonHover();
        searchAreaPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        btnEdit = new rojerusan.RSButtonHover();
        btnDelete = new rojerusan.RSButtonHover();
        jPanel1 = new javax.swing.JPanel();
        txtTitle = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        txtISBN = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtPublisher = new javax.swing.JTextField();
        txtQuantity = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtPublishedYear = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txtCategory = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        rSButtonHover1 = new rojerusan.RSButtonHover();
        cmbYear = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        cmbCriteria = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
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
        jLabel20 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AddBooksPanel.setBackground(new java.awt.Color(255, 255, 255));
        AddBooksPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        AddBooksPanel.setAlignmentX(1.0F);
        AddBooksPanel.setAlignmentY(1.0F);
        AddBooksPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBackground(new java.awt.Color(0, 51, 51));

        tblBooks1.setBackground(new java.awt.Color(0, 51, 51));
        tblBooks1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tblBooks1.setForeground(new java.awt.Color(255, 255, 255));
        tblBooks1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Title", "Author", "Publisher", "Published year ", "Category", "Price/10000", "Quantity"
            }
        ));
        tblBooks1.setGridColor(new java.awt.Color(102, 153, 255));
        tblBooks1.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblBooks1.setRowHeight(40);
        tblBooks1.setRowMargin(3);
        tblBooks1.setSelectionBackground(new java.awt.Color(255, 255, 255));
        tblBooks1.setSelectionForeground(new java.awt.Color(0, 51, 51));
        tblBooks1.setShowGrid(true);
        tblBooks1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBooks1MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblBooks1);

        AddBooksPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 180, 1290, 510));

        jPanel9.setBackground(new java.awt.Color(0, 51, 51));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel9 = new RoundedPanel(30);
        jPanel9.setBackground(new Color(0, 51, 51));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("ADD BOOKS");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 30, -1, -1));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/digital-library.png"))); // NOI18N
        jPanel9.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 170, 140));

        jPanel10.setBackground(new java.awt.Color(0, 51, 51));
        jPanel10.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(102, 153, 255)));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddRow.setBackground(new java.awt.Color(0, 51, 51));
        btnAddRow.setBorder(null);
        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/add.png"))); // NOI18N
        btnAddRow.setText("Add row");
        btnAddRow.setColorHover(new java.awt.Color(204, 0, 0));
        btnAddRow.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAddRow.setIconTextGap(10);
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });
        jPanel10.add(btnAddRow, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 10, 130, 50));

        btnSave.setBackground(new java.awt.Color(0, 51, 51));
        btnSave.setBorder(null);
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/save.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.setColorHover(new java.awt.Color(204, 0, 0));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSave.setIconTextGap(10);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel10.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, 130, 50));

        jPanel9.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 1110, 70));

        AddBooksPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 10, 1290, 160));

        getContentPane().add(AddBooksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(0, 51, 51));
        RoundedPanel jPanel11 = new RoundedPanel(30);
        jPanel11.setBackground(new Color(0, 51, 51));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEdit.setBackground(new java.awt.Color(0, 51, 51));
        btnEdit.setBorder(null);
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel11.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 5, 100, 40));

        btnDelete.setBackground(new java.awt.Color(0, 51, 51));
        btnDelete.setBorder(null);
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel11.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 5, 100, 40));

        searchAreaPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 1280, 50));

        jPanel1.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel1 = new RoundedPanel(30);
        jPanel1.setBackground(new Color(0, 51, 51));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtTitle.setBackground(new java.awt.Color(0, 51, 51));
        txtTitle.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtTitle.setForeground(new java.awt.Color(255, 255, 255));
        txtTitle.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTitle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTitleActionPerformed(evt);
            }
        });
        jPanel1.add(txtTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 110, 170, 40));

        txtAuthor.setBackground(new java.awt.Color(0, 51, 51));
        txtAuthor.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtAuthor.setForeground(new java.awt.Color(255, 255, 255));
        txtAuthor.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtAuthor.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAuthorActionPerformed(evt);
            }
        });
        jPanel1.add(txtAuthor, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 110, 170, 40));

        txtISBN.setBackground(new java.awt.Color(0, 51, 51));
        txtISBN.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtISBN.setForeground(new java.awt.Color(255, 255, 255));
        txtISBN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtISBN.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        jPanel1.add(txtISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 170, 40));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/isbn2_32x32.png"))); // NOI18N
        jLabel12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 20, 40, 40));

        txtPublisher.setBackground(new java.awt.Color(0, 51, 51));
        txtPublisher.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtPublisher.setForeground(new java.awt.Color(255, 255, 255));
        txtPublisher.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPublisher.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtPublisher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherActionPerformed(evt);
            }
        });
        jPanel1.add(txtPublisher, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 170, 40));

        txtQuantity.setBackground(new java.awt.Color(0, 51, 51));
        txtQuantity.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtQuantity.setForeground(new java.awt.Color(255, 255, 255));
        txtQuantity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtQuantity.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });
        jPanel1.add(txtQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 30, 170, 40));

        txtPrice.setBackground(new java.awt.Color(0, 51, 51));
        txtPrice.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtPrice.setForeground(new java.awt.Color(255, 255, 255));
        txtPrice.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPrice.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPriceActionPerformed(evt);
            }
        });
        jPanel1.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 110, 170, 40));

        txtPublishedYear.setBackground(new java.awt.Color(0, 51, 51));
        txtPublishedYear.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtPublishedYear.setForeground(new java.awt.Color(255, 255, 255));
        txtPublishedYear.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPublishedYear.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        txtPublishedYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublishedYearActionPerformed(evt);
            }
        });
        jPanel1.add(txtPublishedYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 200, 170, 40));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, 3, 240));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        RoundedPanel jPanel7 = new RoundedPanel(30);
        jPanel7.setBackground(new Color(255, 255, 255));

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/Book_Avatar3.png"))); // NOI18N
        jPanel7.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 190, -1));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 153, 255));
        jLabel22.setText("Book details");
        jLabel22.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(102, 153, 255)));
        jPanel7.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 150, 150, 70));

        jPanel1.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 190, 240));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/tag.png"))); // NOI18N
        jLabel13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 40, 40));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/writer.png"))); // NOI18N
        jLabel14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 110, 40, 40));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/publishing-house.png"))); // NOI18N
        jLabel15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, 40, 40));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        jLabel16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 200, 40, 40));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/category.png"))); // NOI18N
        jLabel17.setText("Category:");
        jLabel17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, 40, 40));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/price-tag.png"))); // NOI18N
        jLabel18.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 110, 40, 40));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/bookshelves.png"))); // NOI18N
        jLabel19.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 30, 40, 40));

        txtCategory.setBackground(new java.awt.Color(0, 51, 51));
        txtCategory.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtCategory.setForeground(new java.awt.Color(255, 255, 255));
        txtCategory.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCategory.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(102, 153, 255)));
        jPanel1.add(txtCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 200, 170, 40));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 10, 3, 240));

        searchAreaPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 10, 900, 260));

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

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 1280, 350));

        jPanel8.setBackground(new java.awt.Color(0, 51, 51));
        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel8 = new RoundedPanel(30);
        jPanel8.setBackground(new Color(0, 51, 51));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtSearch.setBackground(new java.awt.Color(0, 51, 51));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setCaretColor(new java.awt.Color(0, 0, 0));
        txtSearch.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSearch.setPhColor(new java.awt.Color(255, 255, 255));
        txtSearch.setPlaceholder("                   ENTER KEYWORD");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanel8.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 310, 40));

        rSButtonHover1.setBackground(new java.awt.Color(0, 51, 51));
        rSButtonHover1.setBorder(null);
        rSButtonHover1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/search.png"))); // NOI18N
        rSButtonHover1.setColorHover(new java.awt.Color(51, 255, 0));
        rSButtonHover1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSButtonHover1ActionPerformed(evt);
            }
        });
        jPanel8.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 50, 40));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setForeground(new java.awt.Color(0, 0, 0));
        cmbYear.setToolTipText("");
        cmbYear.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbYear.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        SwingUtilities.invokeLater(() -> {
            try {
                JPopupMenu popup = (JPopupMenu) cmbYear.getUI().getAccessibleChild(cmbYear, 0);
                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

                verticalScrollBar.setPreferredSize(new Dimension(0, 0)); 
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        jPanel8.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 140, 160, 40));

        jLabel6.setBackground(new java.awt.Color(0, 51, 51));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        jLabel6.setText("PUBLISHED YEAR");
        jPanel8.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 160, 40));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "Author", "Published Year" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel8.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 80, 160, 40));

        jLabel5.setBackground(new java.awt.Color(0, 51, 51));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/filter.png"))); // NOI18N
        jLabel5.setText("CRITERIA ");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 160, 40));

        cmbCategory.setBackground(new java.awt.Color(255, 255, 255));
        cmbCategory.setForeground(new java.awt.Color(0, 0, 0));
        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Category", "Fiction", "History", "Philosophy", "Non-fiction", "Thriller", "Science Fiction", "Classics", "Self-help", "Mystery", "Fantasy", "Drama", "Magical Realism", "Biography", "Horror", "Young Adult", " " }));
        cmbCategory.setToolTipText("");
        cmbCategory.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbCategory.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        SwingUtilities.invokeLater(() -> {
            try {
                JPopupMenu popup = (JPopupMenu) cmbCategory.getUI().getAccessibleChild(cmbCategory, 0);
                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

                verticalScrollBar.setPreferredSize(new Dimension(0, 0)); 
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });
        jPanel8.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 160, 40));

        jLabel4.setBackground(new java.awt.Color(0, 51, 51));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/category.png"))); // NOI18N
        jLabel4.setText("CATEGORY");
        jPanel8.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 160, 40));

        searchAreaPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 370, 260));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE BOOKS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/li4.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 50, -1));

        btnBack.setBackground(new java.awt.Color(0, 51, 51));
        btnBack.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(0, 51, 51)));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/undo.png"))); // NOI18N
        btnBack.setColorHover(new java.awt.Color(204, 0, 51));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        jPanel2.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(1440, 0, 60, 50));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1500, 50));

        jPanel4.setBackground(new java.awt.Color(0, 51, 51));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddBook.setBackground(new java.awt.Color(0, 51, 51));
        btnAddBook.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        btnAddBook.setText("Add Books");
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
        jPanel4.add(btnAddBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
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
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 180, -1));

        jPanel5.setBackground(new java.awt.Color(102, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("FEATURES");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 0, -1, 50));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 50));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/shelving.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, -1, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

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
        Object[] book = new Object[]{
            txtISBN.getText(),
            txtTitle.getText(),
            txtAuthor.getText(),
            txtPublisher.getText(),
            txtPublishedYear.getText(),
            txtCategory.getText(),
            txtPrice.getText(),
            txtQuantity.getText()
        };

        BookDAO dao = new BookDAO();
        if (dao.updateBook(book)) {
            JOptionPane.showMessageDialog(this, "Book updated successfully.");
            searchBooks();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update the book.");
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
            BookDAO dao = new BookDAO();
            if (dao.deleteBookByISBN(isbn)) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                searchBooks(); // Refresh
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the book.");
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

    private void txtPublishedYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublishedYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublishedYearActionPerformed

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
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
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
