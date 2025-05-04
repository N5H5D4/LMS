/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import java.awt.Color;
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
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import DAO.*;
import UI_Helper.RoundedPanel;
import java.awt.Component;
import java.awt.Font;
import javax.swing.border.MatteBorder;

/**
 *
 * @author HS
 */
public class ManageReaders extends javax.swing.JFrame {

    /**
     * Creates new form ManageBookss
     */
    public ManageReaders() {
        initComponents();
        searchAreaPanel.setVisible(false);
        AddReadersPanel.setVisible(false);
        populateYearComboBox();
        enableRightClickCopy(tblReaders);

        styleTable(tblReaders);
        styleTable(tblReaders1);
        jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        //Tự động điền ccd cà ced, hõ trợ thêm reader
        DefaultTableModel model = (DefaultTableModel) tblReaders1.getModel();
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >= 0 && column >= 0 && column <= 5) {
                        checkAndFillDates(row, model);
                    }
                }
            }
        });

        this.setLocationRelativeTo(null);
    }

    private void styleTable(JTable table) {
        // Tạo renderer cho tittle cột
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

    private void populateYearComboBox() {
        int currentYear = Year.now().getValue();

        cmbBirthDate.addItem("All years");

        for (int year = currentYear; year >= 1900; year--) {
            cmbBirthDate.addItem(String.valueOf(year));
        }
    }

    /*
    private void filterTableByYear() {
        String selectedYear = (String) cmbBirthDate.getSelectedItem();

        if (selectedYear.equals("All years")) {
            return;
        }

        try {
            int year = Integer.parseInt(selectedYear);
            int currentYear = Year.now().getValue();

            if (year > currentYear) {
                cmbBirthDate.setSelectedItem("All years");
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
     */
    //Find reader
    private void searchReaders() {
        String keyword = txtSearch.getText().trim();
        String yearFilter = cmbBirthDate.getSelectedItem().toString();
        String searchType = cmbCriteria.getSelectedItem().toString();
        String genderFilter = cmbGender.getSelectedItem().toString();

        try {
            List<Object[]> results = ReaderDAO.searchReaders(keyword, yearFilter, searchType, genderFilter);

            DefaultTableModel model = (DefaultTableModel) tblReaders.getModel();
            model.setRowCount(0);

            for (Object[] row : results) {
                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error while searching for reader: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //cho btn clear
    private void clearFields() {
        txtName.setText("");
        txtIdentity.setText("");
        txtBirthDate.setText("");
        txtGender.setText("");
        txtEmail.setText("");
        txtCCD.setText("");
        txtCED.setText("");
        txtAdress.setText("");
        txtID.setText("");
    }

    //Kiểm tra và tự động điền ngày tạo thẻ, ngày hét hạn
    private void checkAndFillDates(int row, DefaultTableModel model) {
        boolean allPreviousFilled = true;
        for (int col = 0; col <= 5; col++) {
            Object cellValue = model.getValueAt(row, col);
            if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                allPreviousFilled = false;
                break;
            }
        }

        // Nếu tất cả các ô trước đó đã được điền
        if (allPreviousFilled) {
            LocalDate currentDate = LocalDate.now();
            // Tính ngày hết hạn (48 tháng sau)
            LocalDate expiryDate = currentDate.plusMonths(48);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            Object createdAtValue = model.getValueAt(row, 6);
            Object expiredAtValue = model.getValueAt(row, 7);

            // Chỉ cập nhật nếu ô ngày tháng còn trống
            boolean needsUpdate = false;
            if (createdAtValue == null || createdAtValue.toString().trim().isEmpty()) {
                // Tạm thời gỡ listener 
                TableModelListener[] listeners = model.getTableModelListeners();
                for (TableModelListener l : listeners) {
                    model.removeTableModelListener(l);
                }

                model.setValueAt(currentDate.format(formatter), row, 6); // Điền ngày tạo

                for (TableModelListener l : listeners) {
                    model.addTableModelListener(l);
                }
                needsUpdate = true;
            }

            // Nếu cột hết hạn trống or cột tạo vừa được cập nhật
            if (expiredAtValue == null || expiredAtValue.toString().trim().isEmpty() || needsUpdate) {
                TableModelListener[] listeners = model.getTableModelListeners();
                for (TableModelListener l : listeners) {
                    model.removeTableModelListener(l);
                }

                model.setValueAt(expiryDate.format(formatter), row, 7);

                for (TableModelListener l : listeners) {
                    model.addTableModelListener(l);
                }
            }
        }
    }

    //Cho btn save
    private void saveReaders() {
        DefaultTableModel model = (DefaultTableModel) tblReaders1.getModel();
        int rowCount = tblReaders1.getRowCount();

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "No information to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Object[]> readerList = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Object[] row = new Object[8];
            boolean allEmpty = true;

            for (int j = 0; j < 8; j++) {
                row[j] = tblReaders1.getValueAt(i, j);
                if (row[j] != null && !row[j].toString().trim().isEmpty()) {
                    allEmpty = false;
                }
            }

            if (!allEmpty) {
                readerList.add(row);
            }
        }

        try {
            int addedCount = ReaderDAO.addReadersBatch(readerList);

            if (addedCount > 0) {
                JOptionPane.showMessageDialog(this, addedCount + " readers added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                model.setRowCount(0);
                model.addRow(new Object[]{"", "", "", "", "", "", "", ""});
            } else {
                JOptionPane.showMessageDialog(this, "No valid rows to add.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding readers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //
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
        btnAddReader = new rojeru_san.complementos.RSButtonHover();
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        AddReadersPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblReaders1 = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        btnAddRow = new rojerusan.RSButtonHover();
        btnSave = new rojerusan.RSButtonHover();
        jLabel23 = new javax.swing.JLabel();
        searchAreaPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        btnDelete = new rojerusan.RSButtonHover();
        btnEdit = new rojerusan.RSButtonHover();
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        txtIdentity = new javax.swing.JTextField();
        txtBirthDate = new javax.swing.JTextField();
        txtGender = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtCED = new javax.swing.JTextField();
        txtCCD = new javax.swing.JTextField();
        txtAdress = new javax.swing.JTextField();
        txtID = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblReaders = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        rSButtonHover1 = new rojerusan.RSButtonHover();
        txtSearch = new app.bolivia.swing.JCTextField();
        cmbCriteria = new javax.swing.JComboBox<>();
        cmbBirthDate = new javax.swing.JComboBox<>();
        cmbGender = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE READERS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/li4.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 50, -1));

        btnBack.setBackground(new java.awt.Color(0, 51, 51));
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

        btnAddReader.setBackground(new java.awt.Color(0, 51, 51));
        btnAddReader.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        btnAddReader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/add_user_24x24.png"))); // NOI18N
        btnAddReader.setText("Add Readers");
        btnAddReader.setColorHover(new java.awt.Color(102, 153, 255));
        btnAddReader.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddReader.setIconTextGap(10);
        btnAddReader.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddReaderMouseClicked(evt);
            }
        });
        btnAddReader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddReaderActionPerformed(evt);
            }
        });
        jPanel4.add(btnAddReader, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        searchPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/search_3.png"))); // NOI18N
        searchPanel.setText("Search Readers");
        searchPanel.setColorHover(new java.awt.Color(102, 153, 255));
        searchPanel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        searchPanel.setIconTextGap(10);
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
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 0, -1, 50));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 50));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/reader.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, -1, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        AddReadersPanel.setBackground(new java.awt.Color(255, 255, 255));
        AddReadersPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        AddReadersPanel.setAlignmentX(1.0F);
        AddReadersPanel.setAlignmentY(1.0F);
        AddReadersPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBackground(new java.awt.Color(0, 51, 51));
        jScrollPane3.setOpaque(true);
        jScrollPane3.setWheelScrollingEnabled(false);

        tblReaders1.setBackground(new java.awt.Color(0, 51, 51));
        tblReaders1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tblReaders1.setForeground(new java.awt.Color(255, 255, 255));
        tblReaders1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Identity card", "Birth date", "Gender", "Email", "Address", " Card created at ", " Card expired at "
            }
        ));
        tblReaders1.setGridColor(new java.awt.Color(102, 153, 255));
        tblReaders1.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblReaders1.setRowHeight(30);
        tblReaders1.setSelectionBackground(new java.awt.Color(255, 255, 255));
        tblReaders1.setSelectionForeground(new java.awt.Color(0, 51, 51));
        tblReaders1.setShowGrid(true);
        tblReaders1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReaders1MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblReaders1);

        AddReadersPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 180, 1290, 510));

        jPanel9.setBackground(new java.awt.Color(0, 51, 51));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel9 = new RoundedPanel(30);
        jPanel9.setBackground(new Color(0, 51, 51));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("ADD READERS");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 0, 310, 90));

        jPanel10.setBackground(new java.awt.Color(0, 51, 51));
        jPanel10.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(102, 153, 255)));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddRow.setBackground(new java.awt.Color(0, 51, 51));
        btnAddRow.setBorder(null);
        btnAddRow.setForeground(new java.awt.Color(0, 0, 0));
        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/add.png"))); // NOI18N
        btnAddRow.setText("Add row");
        btnAddRow.setColorHover(new java.awt.Color(204, 0, 0));
        btnAddRow.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });
        jPanel10.add(btnAddRow, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 10, 130, 50));

        btnSave.setBackground(new java.awt.Color(0, 51, 51));
        btnSave.setBorder(null);
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/save.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.setColorHover(new java.awt.Color(204, 0, 0));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel10.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, 130, 50));

        jPanel9.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 1110, 70));

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/reader2.png"))); // NOI18N
        jPanel9.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 170, 140));

        AddReadersPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 10, 1290, 160));

        getContentPane().add(AddReadersPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(0, 51, 51));
        RoundedPanel jPanel11 = new RoundedPanel(30);
        jPanel11.setBackground(new Color(0, 51, 51));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnDelete.setBackground(new java.awt.Color(0, 51, 51));
        btnDelete.setBorder(null);
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDelete.setIconTextGap(10);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel11.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 5, 130, 40));

        btnEdit.setBackground(new java.awt.Color(0, 51, 51));
        btnEdit.setBorder(null);
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEdit.setIconTextGap(10);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel11.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 5, 110, 40));

        searchAreaPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 1280, 50));

        jPanel1.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel1 = new RoundedPanel(30);
        jPanel1.setBackground(new Color(0, 51, 51));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtName.setBackground(new java.awt.Color(0, 51, 51));
        txtName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtName.setForeground(new java.awt.Color(255, 255, 255));
        txtName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });
        jPanel1.add(txtName, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 110, 170, 40));

        txtIdentity.setBackground(new java.awt.Color(0, 51, 51));
        txtIdentity.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtIdentity.setForeground(new java.awt.Color(255, 255, 255));
        txtIdentity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtIdentity.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtIdentity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdentityActionPerformed(evt);
            }
        });
        jPanel1.add(txtIdentity, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 200, 170, 40));

        txtBirthDate.setBackground(new java.awt.Color(0, 51, 51));
        txtBirthDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBirthDate.setForeground(new java.awt.Color(255, 255, 255));
        txtBirthDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBirthDate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtBirthDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBirthDateActionPerformed(evt);
            }
        });
        jPanel1.add(txtBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 110, 170, 40));

        txtGender.setBackground(new java.awt.Color(0, 51, 51));
        txtGender.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtGender.setForeground(new java.awt.Color(255, 255, 255));
        txtGender.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtGender.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGenderActionPerformed(evt);
            }
        });
        jPanel1.add(txtGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 170, 40));

        txtEmail.setBackground(new java.awt.Color(0, 51, 51));
        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(255, 255, 255));
        txtEmail.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEmail.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 20, 170, 40));

        txtCED.setBackground(new java.awt.Color(0, 51, 51));
        txtCED.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCED.setForeground(new java.awt.Color(255, 255, 255));
        txtCED.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCED.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtCED.setName(""); // NOI18N
        txtCED.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCEDActionPerformed(evt);
            }
        });
        jPanel1.add(txtCED, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 200, 170, 40));

        txtCCD.setBackground(new java.awt.Color(0, 51, 51));
        txtCCD.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCCD.setForeground(new java.awt.Color(255, 255, 255));
        txtCCD.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCCD.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtCCD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCCDActionPerformed(evt);
            }
        });
        jPanel1.add(txtCCD, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 110, 170, 40));

        txtAdress.setBackground(new java.awt.Color(0, 51, 51));
        txtAdress.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAdress.setForeground(new java.awt.Color(255, 255, 255));
        txtAdress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtAdress.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        jPanel1.add(txtAdress, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 200, 170, 40));

        txtID.setBackground(new java.awt.Color(0, 51, 51));
        txtID.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtID.setForeground(new java.awt.Color(255, 255, 255));
        txtID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtID.setCaretColor(new java.awt.Color(255, 255, 255));
        txtID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDActionPerformed(evt);
            }
        });
        jPanel1.add(txtID, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 20, 170, 40));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/start-date.png"))); // NOI18N
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 200, 40, 40));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/identity-card.png"))); // NOI18N
        jLabel17.setToolTipText("");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 40, 40));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/name2.png"))); // NOI18N
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 110, 40, 40));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id.png"))); // NOI18N
        jPanel1.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 20, 40, 40));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        RoundedPanel jPanel7 = new RoundedPanel(30);
        jPanel7.setBackground(new Color(255, 255, 255));

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/reader_Avatar2.png"))); // NOI18N
        jPanel7.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 180, 180));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(0, 51, 51));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("Reader details");
        jLabel24.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(0, 51, 51)));
        jPanel7.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 160, 40));

        jPanel1.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 180, 240));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/gender.png"))); // NOI18N
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, 40, 40));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/birthday.png"))); // NOI18N
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 110, 40, 40));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/home-address.png"))); // NOI18N
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 200, 40, 40));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        RoundedPanel jPanel6 = new RoundedPanel(30);
        jPanel6.setBackground(new Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(425, 10, 3, 240));
        jPanel6.getAccessibleContext().setAccessibleName("");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/email.png"))); // NOI18N
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, 40, 40));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/start-date.png"))); // NOI18N
        jPanel1.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 110, 40, 40));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        RoundedPanel jPanel12 = new RoundedPanel(30);
        jPanel12.setBackground(new Color(255, 255, 255));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel12.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(665, 10, 3, 240));

        jPanel1.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(665, 10, 3, 240));

        searchAreaPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 10, 900, 260));

        tblReaders.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblReaders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Identity card (CCCD)", "Birth date", "Gender", "Email", "Adress", "CCD", "CED"
            }
        ));
        tblReaders.setGridColor(new java.awt.Color(0, 51, 51));
        tblReaders.setPreferredSize(new java.awt.Dimension(600, 4000));
        tblReaders.setRowHeight(27);
        tblReaders.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblReaders.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblReaders.setShowGrid(true);
        tblReaders.setShowVerticalLines(false);
        tblReaders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReadersMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblReaders);

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 1280, 350));

        jPanel8.setBackground(new java.awt.Color(0, 51, 51));
        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel8 = new RoundedPanel(30);
        jPanel8.setBackground(new Color(0, 51, 51));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/filter.png"))); // NOI18N
        jLabel5.setText("CRITERIA ");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 160, 40));

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

        txtSearch.setBackground(new java.awt.Color(0, 51, 51));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setCaretColor(new java.awt.Color(255, 255, 255));
        txtSearch.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSearch.setPhColor(new java.awt.Color(255, 255, 255));
        txtSearch.setPlaceholder("                          ENTER KEYWORD");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanel8.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 310, 40));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "name", "identity card (CCCD/CMND)", "email", "address", "birth date" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel8.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 80, 160, 40));

        cmbBirthDate.setBackground(new java.awt.Color(255, 255, 255));
        cmbBirthDate.setForeground(new java.awt.Color(0, 0, 0));
        cmbBirthDate.setToolTipText("");
        cmbBirthDate.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbBirthDate.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        SwingUtilities.invokeLater(() -> {
            try {
                JPopupMenu popup = (JPopupMenu) cmbBirthDate.getUI().getAccessibleChild(cmbBirthDate, 0);
                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

                verticalScrollBar.setPreferredSize(new Dimension(0, 0)); 
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        cmbBirthDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBirthDateActionPerformed(evt);
            }
        });
        jPanel8.add(cmbBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 140, 160, 40));

        cmbGender.setBackground(new java.awt.Color(255, 255, 255));
        cmbGender.setForeground(new java.awt.Color(0, 0, 0));
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Male", "Female", "Other" }));
        cmbGender.setToolTipText("");
        cmbGender.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbGender.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        SwingUtilities.invokeLater(() -> {
            try {
                JPopupMenu popup = (JPopupMenu) cmbGender.getUI().getAccessibleChild(cmbGender, 0);
                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

                verticalScrollBar.setPreferredSize(new Dimension(0, 0)); 
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        cmbGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenderActionPerformed(evt);
            }
        });
        jPanel8.add(cmbGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 160, 40));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        jLabel11.setText("BIRTH YEAR");
        jPanel8.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 150, 40));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/gender.png"))); // NOI18N
        jLabel6.setText("GENDER");
        jPanel8.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 160, 40));

        searchAreaPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 370, 260));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddReaderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddReaderMouseClicked
        searchAreaPanel.setVisible(false);
        AddReadersPanel.setVisible(true);

    }//GEN-LAST:event_btnAddReaderMouseClicked

    private void btnAddReaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddReaderActionPerformed
        searchAreaPanel.setVisible(false);
        AddReadersPanel.setVisible(true);

    }//GEN-LAST:event_btnAddReaderActionPerformed

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        searchAreaPanel.setVisible(true);
        AddReadersPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        searchAreaPanel.setVisible(true);
        AddReadersPanel.setVisible(false);
    }//GEN-LAST:event_searchPanelActionPerformed
    //
    private Date convertToDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(dateStr);
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        HomePage home = new HomePage();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void tblReaders1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblReaders1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblReaders1MouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveReaders();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblReaders1.getModel();
        model.addRow(new Object[]{"", "", "", "", "", "", "", ""});

    }//GEN-LAST:event_btnAddRowActionPerformed

    private void cmbGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbGenderActionPerformed

    private void cmbBirthDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBirthDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbBirthDateActionPerformed

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void rSButtonHover1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rSButtonHover1ActionPerformed
        searchReaders();
        clearFields();
    }//GEN-LAST:event_rSButtonHover1ActionPerformed

    private void tblReadersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblReadersMouseClicked

        int selectedRow = tblReaders.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblReaders.getModel();

        txtID.setText(model.getValueAt(selectedRow, 0).toString());
        txtName.setText(model.getValueAt(selectedRow, 1).toString());
        txtIdentity.setText(model.getValueAt(selectedRow, 2).toString());
        txtBirthDate.setText(model.getValueAt(selectedRow, 3).toString());
        txtGender.setText(model.getValueAt(selectedRow, 4).toString());
        txtEmail.setText(model.getValueAt(selectedRow, 5).toString());
        txtAdress.setText(model.getValueAt(selectedRow, 6).toString());
        txtCCD.setText(model.getValueAt(selectedRow, 7).toString());
        txtCED.setText(model.getValueAt(selectedRow, 8).toString());
    }//GEN-LAST:event_tblReadersMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed

        String name = txtName.getText();
        String IC = txtIdentity.getText();
        String BD = txtBirthDate.getText();
        String Gender = txtGender.getText();
        String Email = txtEmail.getText();
        String Adress = txtAdress.getText();
        String CCD = txtCCD.getText();
        String CED = txtCED.getText();
        String ID = txtID.getText();

        try {
            boolean updated = ReaderDAO.updateReader(
                    Integer.parseInt(ID),
                    name,
                    IC,
                    convertToDate(BD),
                    Gender,
                    Email,
                    Adress,
                    convertToDate(CCD),
                    convertToDate(CED)
            );

            if (updated) {
                JOptionPane.showMessageDialog(this, "Reader's information updated successfully.");
                searchReaders();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update reader.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String identityCard = txtIdentity.getText();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this reader?", "Delete Reader", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = ReaderDAO.deleteReader(
                        identityCard
                );

                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Reader's information deleted successfully.");
                    searchReaders();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete reader.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDActionPerformed

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEmailActionPerformed

    private void txtCCDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCCDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCCDActionPerformed

    private void txtCEDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCEDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCEDActionPerformed

    private void txtGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGenderActionPerformed

    private void txtBirthDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBirthDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBirthDateActionPerformed

    private void txtIdentityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdentityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdentityActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

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
            java.util.logging.Logger.getLogger(ManageReaders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageReaders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageReaders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageReaders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManageReaders().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AddReadersPanel;
    private rojeru_san.complementos.RSButtonHover btnAddReader;
    private rojerusan.RSButtonHover btnAddRow;
    private rojerusan.RSButtonHover btnBack;
    private rojerusan.RSButtonHover btnDelete;
    private rojerusan.RSButtonHover btnEdit;
    private rojerusan.RSButtonHover btnSave;
    private javax.swing.JComboBox<String> cmbBirthDate;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
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
    private javax.swing.JTable tblReaders;
    private javax.swing.JTable tblReaders1;
    private javax.swing.JTextField txtAdress;
    private javax.swing.JTextField txtBirthDate;
    private javax.swing.JTextField txtCCD;
    private javax.swing.JTextField txtCED;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtGender;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtIdentity;
    private javax.swing.JTextField txtName;
    private app.bolivia.swing.JCTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
