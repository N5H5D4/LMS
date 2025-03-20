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
    }

    private void populateYearComboBox() {
        int currentYear = Year.now().getValue();

        cmbBirthDate.addItem("All years");

        for (int year = currentYear; year >= 1900; year--) {
            cmbBirthDate.addItem(String.valueOf(year));
        }
    }

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

    private void searchReaders() {
        

        String keyword = txtSearch.getText().trim();
        String yearFilter = cmbBirthDate.getSelectedItem().toString();
        String searchType = cmbCriteria.getSelectedItem().toString();
        String genderFilter = cmbGender.getSelectedItem().toString();

        String query = "SELECT * FROM readers WHERE 1=1";
        List<Object> params = new ArrayList<>();

        // Thêm điều kiện tìm kiếm nếu có từ khóa
        if (!keyword.isEmpty()) {
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
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    break;
            }
        }

        // Thêm điều kiện lọc năm nếu không phải là "All year"
        if (!yearFilter.equals("All years")) {
            query += " AND YEAR(birth_date)  = ?";
            try {
                int year = Integer.parseInt(yearFilter);
                params.add(year);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Năm không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!genderFilter.equals("All")) {
            query += " AND gender LIKE ?";
            params.add(genderFilter);
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
            DefaultTableModel model = (DefaultTableModel) tblReaders.getModel();
            model.setRowCount(0);

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
                    rs.getString("card_expired_at"),};
                model.addRow(row);
            }

            rs.close();
            pst.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm người đọc: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtIdentity.setText("");
        txtBirthDate.setText("");
        txtGender.setText("");
        txtEmail.setText("");
        txtCCD.setText("");
        txtCED.setText("");
        txtAdress.setText("");
    }

    private void saveReaders() {
        DefaultTableModel model = (DefaultTableModel) tblReaders1.getModel();
        int rowCount = tblReaders1.getRowCount();

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "Không có thông tin nào để lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String insertQuery = "INSERT INTO readers (name, identity_card, birth_date, gender, email, address, card_created_at, card_expired_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertQuery);

            con.setAutoCommit(false);

            int addedCount = 0;
            for (int i = 0; i < rowCount; i++) {
                try {
                    Object nameObj = tblReaders1.getValueAt(i, 0);
                    Object ICObj = tblReaders1.getValueAt(i, 1);
                    Object BDObj = tblReaders1.getValueAt(i, 2);
                    Object GenderObj = tblReaders1.getValueAt(i, 3);
                    Object EmailObj = tblReaders1.getValueAt(i, 4);
                    Object AddressObj = tblReaders1.getValueAt(i, 5);
                    Object CCDObj = tblReaders1.getValueAt(i, 6);
                    Object CEDObj = tblReaders1.getValueAt(i, 7);

                    if (nameObj == null && ICObj == null && BDObj == null && GenderObj == null
                            && EmailObj == null && AddressObj == null && CCDObj == null && CEDObj == null) {
                        continue;
                    }

                    if (nameObj == null || ICObj == null || BDObj == null || GenderObj == null
                            || EmailObj == null || AddressObj == null || CCDObj == null || CEDObj == null
                            || nameObj.toString().trim().isEmpty() || ICObj.toString().trim().isEmpty()
                            || BDObj.toString().trim().isEmpty() || GenderObj.toString().trim().isEmpty()
                            || EmailObj.toString().trim().isEmpty() || AddressObj.toString().trim().isEmpty()
                            || CCDObj.toString().trim().isEmpty() || CEDObj.toString().trim().isEmpty()) {
                        continue;
                    }

                    String name = nameObj.toString().trim();
                    String IC = ICObj.toString().trim();
                    String BD = BDObj.toString().trim();
                    String Gender = GenderObj.toString().trim();
                    String Email = EmailObj.toString().trim();
                    String Address = AddressObj.toString().trim();
                    String CCD = CCDObj.toString().trim();
                    String CED = CEDObj.toString().trim();

                    pst.setString(1, name);
                    pst.setString(2, IC);
                    pst.setDate(3, java.sql.Date.valueOf(BD));
                    pst.setString(4, Gender);
                    pst.setString(5, Email);
                    pst.setString(6, Address);
                    pst.setDate(7, java.sql.Date.valueOf(CCD));
                    pst.setDate(8, java.sql.Date.valueOf(CED));

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
                JOptionPane.showMessageDialog(this, addedCount + "Đã thêm!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                model.setRowCount(0); // Xóa bảng sau khi lưu thành công
                model.addRow(new Object[]{"", "", "", "", "", "", "", ""}); // Tạo một dòng trống mới để nhập thêm
            } else {
                con.rollback(); // Hoàn tác nếu không có sách nào được thêm
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sách: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        searchAreaPanel = new javax.swing.JPanel();
        cmbBirthDate = new javax.swing.JComboBox<>();
        cmbCriteria = new javax.swing.JComboBox<>();
        cmbGender = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        txtIdentity = new javax.swing.JTextField();
        txtBirthDate = new javax.swing.JTextField();
        txtGender = new javax.swing.JTextField();
        txtCED = new javax.swing.JTextField();
        txtCCD = new javax.swing.JTextField();
        txtAdress = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        btnDelete = new rojerusan.RSButtonHover();
        btnEdit = new rojerusan.RSButtonHover();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblReaders = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtSearch = new app.bolivia.swing.JCTextField();
        rSButtonHover1 = new rojerusan.RSButtonHover();
        jLabel10 = new javax.swing.JLabel();
        AddReadersPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblReaders1 = new javax.swing.JTable();
        btnAddRow = new rojerusan.RSButtonHover();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnSave = new rojerusan.RSButtonHover();
        jLabel21 = new javax.swing.JLabel();

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
        jLabel2.setText("MANAGE READERS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/li.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 50, -1));

        btnBack.setBackground(new java.awt.Color(255, 255, 255));
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

        btnAddReader.setBackground(new java.awt.Color(0, 51, 51));
        btnAddReader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24business_application_addthedatabase_add_insert_database_db_2313.png"))); // NOI18N
        btnAddReader.setText("     Add Readers");
        btnAddReader.setColorHover(new java.awt.Color(102, 153, 255));
        btnAddReader.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
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
        jPanel4.add(btnAddReader, new org.netbeans.lib.awtextra.AbsoluteConstraints(-40, 130, 240, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24x24search_find_database_16703.png"))); // NOI18N
        searchPanel.setText("    Search Readers");
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

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/reader.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, -1, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cmbBirthDate.setBackground(new java.awt.Color(255, 255, 255));
        cmbBirthDate.setForeground(new java.awt.Color(0, 0, 0));
        cmbBirthDate.setToolTipText("");
        cmbBirthDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBirthDateActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 50, 200, 40));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "name", "identity card (CCCD/CMND)", "email", "address", "birth date" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, 200, 40));

        cmbGender.setBackground(new java.awt.Color(255, 255, 255));
        cmbGender.setForeground(new java.awt.Color(0, 0, 0));
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Male", "Female", "Other" }));
        cmbGender.setToolTipText("");
        cmbGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenderActionPerformed(evt);
            }
        });
        searchAreaPanel.add(cmbGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 50, 200, 40));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });
        jPanel1.add(txtName, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 40, 210, -1));

        txtIdentity.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtIdentity.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtIdentity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdentityActionPerformed(evt);
            }
        });
        jPanel1.add(txtIdentity, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 40, 210, -1));

        txtBirthDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBirthDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBirthDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBirthDateActionPerformed(evt);
            }
        });
        jPanel1.add(txtBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 40, 190, -1));

        txtGender.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtGender.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGenderActionPerformed(evt);
            }
        });
        jPanel1.add(txtGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 40, 190, -1));

        txtCED.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCED.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCED.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCEDActionPerformed(evt);
            }
        });
        jPanel1.add(txtCED, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 110, 190, -1));

        txtCCD.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCCD.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCCD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCCDActionPerformed(evt);
            }
        });
        jPanel1.add(txtCCD, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 110, 190, -1));

        txtAdress.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAdress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(txtAdress, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 110, 210, -1));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 110, 210, -1));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/reading.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        txtID.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtID.setForeground(new java.awt.Color(0, 51, 51));
        txtID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtID.setText("ID");
        txtID.setCaretColor(new java.awt.Color(255, 255, 255));
        txtID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDActionPerformed(evt);
            }
        });
        jPanel1.add(txtID, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 50, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Card expiration date:");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 90, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Gender: ");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 20, -1, -1));

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
        jPanel1.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 90, 130, -1));

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
        jPanel1.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, 130, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Card creation date:");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 90, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Birth date:");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 20, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Adress:");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 90, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Identity card:");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Email:");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, -1, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Reader name:");
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, -1, -1));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("Reader ID:");
        jPanel1.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, -1, -1));

        jPanel6.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 3, 130));

        jPanel7.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 10, 3, 130));

        searchAreaPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 1280, 150));

        tblReaders.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblReaders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Identity card (CCCD)", "Birth date", "Gender", "Email", "Adress", "Card creation date", "Card expiration date"
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

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 1280, 410));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("BIRTH YEAR");
        searchAreaPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 20, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("GENDER");
        searchAreaPanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 20, -1, -1));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("CRITERIA ");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 10, -1, -1));

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
        jPanel8.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 50, -1));

        searchAreaPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 100));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book-wall-1151405_1920.jpg"))); // NOI18N
        searchAreaPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        AddReadersPanel.setBackground(new java.awt.Color(255, 255, 255));
        AddReadersPanel.setAlignmentX(1.0F);
        AddReadersPanel.setAlignmentY(1.0F);
        AddReadersPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setOpaque(true);
        jScrollPane3.setWheelScrollingEnabled(false);

        tblReaders1.setBackground(new java.awt.Color(255, 255, 255));
        tblReaders1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblReaders1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Identity card", "Birth date", "Gender", "Email", "Address", " Card created at ", " Card expired at "
            }
        ));
        tblReaders1.setGridColor(new java.awt.Color(0, 51, 51));
        tblReaders1.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblReaders1.setRowHeight(30);
        tblReaders1.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblReaders1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblReaders1.setShowGrid(true);
        tblReaders1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReaders1MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblReaders1);

        AddReadersPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 1300, -1));

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
        AddReadersPanel.add(btnAddRow, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 580, 130, 50));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("ADD READERS");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, -10, 310, 90));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("*Enter complete information per line to add reader information");
        jPanel9.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, -1, -1));

        AddReadersPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 650, 100));

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
        AddReadersPanel.add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 580, 130, 50));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book-wall-1151405_1920.jpg"))); // NOI18N
        AddReadersPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

        getContentPane().add(AddReadersPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

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

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void cmbGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbGenderActionPerformed

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

    private void txtGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGenderActionPerformed
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
            Connection con = DBConnection.getConnection();
            String query = "UPDATE readers SET name = ?, identity_card = ?, birth_date = ?, gender = ?, email = ?, address = ?, card_created_at = ?, card_expired_at = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, name);
            pst.setString(2, IC);
            pst.setDate(3, convertToDate(BD));
            pst.setString(4, Gender);
            pst.setString(5, Email);
            pst.setString(6, Adress);
            pst.setDate(7, convertToDate(CCD));
            pst.setDate(8, convertToDate(CED));
            pst.setInt(9, Integer.parseInt(ID));

            int rowCount = pst.executeUpdate();

            if (rowCount > 0) {
                JOptionPane.showMessageDialog(this, "Reader's infor updated successfully.");
                searchReaders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update.");
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
        searchReaders();
    }//GEN-LAST:event_rSButtonHover1ActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String identityCard = txtIdentity.getText();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this reader?", "Delete Reader", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection con = DBConnection.getConnection();
                String query = "DELETE FROM readers WHERE identity_card = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, identityCard);

                int rowCount = pst.executeUpdate();

                if (rowCount > 0) {
                    JOptionPane.showMessageDialog(this, "Deleted successfully.");
                    searchReaders();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete.");
                }

                pst.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tblReaders1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblReaders1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblReaders1MouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveReaders();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblReaders1.getModel();
        model.addRow(new Object[]{"", "", "", "", "", "", "", ""}); // ✅ ĐÚNG

    }//GEN-LAST:event_btnAddRowActionPerformed

    private void cmbBirthDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBirthDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbBirthDateActionPerformed

    private void txtIdentityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdentityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdentityActionPerformed

    private void txtBirthDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBirthDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBirthDateActionPerformed

    private void txtCCDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCCDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCCDActionPerformed

    private void txtIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDActionPerformed

    private void txtCEDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCEDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCEDActionPerformed

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEmailActionPerformed

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
