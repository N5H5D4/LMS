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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import DAO.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author HS
 */
public class BooksBorrowing extends javax.swing.JFrame {

    /**
     * Creates new form BooksBorrowing
     */
    public BooksBorrowing() {
        initComponents();
        singleSlipPanel.setVisible(false);
        multiSlipPanel.setVisible(false);
        addAreaPanel.setVisible(false);
         searchAreaPanel.setVisible(false);
        initBookTitleSearch();
        initTableEvents();
    }

    private void initBookTitleSearch() {
        listModel = new DefaultListModel<>();
        bookList = new JList<>(listModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bookList.setFixedCellHeight(30);
        bookList.setBackground(Color.WHITE);
        bookList.setSelectionBackground(new Color(135, 206, 250));
        bookList.setSelectionForeground(Color.BLACK);

        bookList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(new Color(0, 120, 215));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });

        suggestionPopup = new JPopupMenu();
        suggestionPopup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        suggestionPopup.add(new JScrollPane(bookList));

        txtTitle.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (searchTimer != null) {
                    searchTimer.cancel(); // Hủy bộ đếm trước đó nếu còn tồn tại
                }

                String keyword = txtTitle.getText().trim();
                if (keyword.length() > 1) { // Chỉ tìm kiếm khi có ít nhất 2 ký tự
                    searchTimer = new Timer();
                    searchTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> updateSuggestionList(keyword));
                        }
                    }, 350); //350ms delay
                } else {
                    suggestionPopup.setVisible(false);
                }
            }
        });

        bookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = bookList.getSelectedIndex();
                    if (index != -1) {
                        String selectedTitle = bookList.getSelectedValue();
                        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(selectedTitle);
                        if (!results.isEmpty()) {
                            String selectedISBN = results.get(0)[0];
                            txtISBN.setText(selectedISBN);
                            txtTitle.setText(selectedTitle);
                            suggestionPopup.setVisible(false);
                        }
                    }
                }
            }
        });

        bookList.addMouseMotionListener(new MouseMotionAdapter() {
            int lastIndex = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = bookList.locationToIndex(e.getPoint());
                if (index != lastIndex) {
                    bookList.setSelectedIndex(index);
                    lastIndex = index;
                }
            }
        });
    }

    private void updateSuggestionList(String keyword) {
        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(keyword);
        listModel.clear();
        for (String[] book : results) {
            listModel.addElement(book[1]);
        }

        if (!results.isEmpty()) {

            int txtWidth = txtTitle.getWidth();
            int popupWidth = txtWidth > 300 ? txtWidth : 300;

            suggestionPopup.setPopupSize(new Dimension(popupWidth, 150));
            suggestionPopup.show(txtTitle, (txtTitle.getWidth() - popupWidth) / 2, txtTitle.getHeight());
        } else {
            suggestionPopup.setVisible(false);
        }
    }

    private void updateReaderNameAndBookTitle(int row) {
        try {
            // Lấy dữ liệu từ bảng
            String readerIdText = (String) tblBorrowSlips.getValueAt(row, 2);
            String isbn = (String) tblBorrowSlips.getValueAt(row, 0);

            // Cập nhật Reader Name nếu Reader ID tồn tại
            if (readerIdText != null && !readerIdText.isEmpty()) {
                try {
                    int readerId = Integer.parseInt(readerIdText);
                    List<String[]> readerResult = ReaderDAO.searchReadersById(readerIdText);

                    if (!readerResult.isEmpty()) {
                        tblBorrowSlips.setValueAt(readerResult.get(0)[1], row, 3);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "ID độc giả phải là số nguyên hợp lệ.");
                }
            }

            // Cập nhật Book Title nếu ISBN tồn tại
            if (isbn != null && !isbn.isEmpty()) {
                List<String[]> bookResult = BookDAO.searchBooksByISBN(isbn);

                if (!bookResult.isEmpty()) {
                    tblBorrowSlips.setValueAt(bookResult.get(0)[1], row, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTableWithBookTitle(int row, String isbn) {
        List<String[]> results = BookDAO.searchBooksByISBN(isbn);
        if (!results.isEmpty()) {
            String title = results.get(0)[1];
            tblBorrowSlips.setValueAt(title, row, 1);
        }
    }

    private void updateTableWithReaderName(int row, String readerId) {
        List<String[]> results = ReaderDAO.searchReadersById(readerId);
        if (!results.isEmpty()) {
            String name = results.get(0)[1];
            tblBorrowSlips.setValueAt(name, row, 3);
        }
    }

    private void initTableEvents() {
        tblBorrowSlips.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 0) { // ISBN cột đầu tiên
                String isbn = (String) tblBorrowSlips.getValueAt(row, column);
                if (isbn != null && !isbn.trim().isEmpty()) {
                    updateTableWithBookTitle(row, isbn);
                }
            } else if (column == 2) { // Reader ID cột thứ ba
                String readerId = (String) tblBorrowSlips.getValueAt(row, column);
                if (readerId != null && !readerId.trim().isEmpty()) {
                    updateTableWithReaderName(row, readerId);
                }
            } else if (column == 1) { // Title cột thứ hai
                String titleKeyword = (String) tblBorrowSlips.getValueAt(row, column);
                if (titleKeyword != null && titleKeyword.length() > 1) {
                    updateSuggestionListMulti(titleKeyword, row);
                }
            }
        });

        listModelMulti = new DefaultListModel<>();
        bookListMulti = new JList<>(listModelMulti);
        bookListMulti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        bookListMulti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblBorrowSlips.getSelectedRow();
                    int index = bookListMulti.getSelectedIndex();
                    if (index != -1 && row != -1) {
                        String selectedTitle = bookListMulti.getSelectedValue();
                        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(selectedTitle);
                        if (!results.isEmpty()) {
                            String isbn = results.get(0)[0];
                            tblBorrowSlips.setValueAt(isbn, row, 0); // Điền ISBN vào cột ISBN
                            tblBorrowSlips.setValueAt(selectedTitle, row, 1); // Xác nhận lại tiêu đề
                            suggestionPopupMulti.setVisible(false);
                        }
                    }
                }
            }
        });

        suggestionPopupMulti = new JPopupMenu();
        suggestionPopupMulti.add(new JScrollPane(bookListMulti));
    }

    private void updateSuggestionListMulti(String keyword, int row) {
        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(keyword);
        SwingUtilities.invokeLater(() -> {
            String isbn = (String) tblBorrowSlips.getValueAt(row, 0); // Lấy ISBN của hàng hiện tại

            if (isbn != null && !isbn.trim().isEmpty()) {
                suggestionPopupMulti.setVisible(false);
                return;
            }

            listModelMulti.clear();
            for (String[] book : results) {
                listModelMulti.addElement(book[1]); // Chỉ hiển thị tiêu đề sách
            }

            if (!results.isEmpty()) {
                int columnWidth = tblBorrowSlips.getColumnModel().getColumn(1).getWidth();
                int x = tblBorrowSlips.getCellRect(row, 1, true).x;
                int y = tblBorrowSlips.getCellRect(row, 1, true).y + tblBorrowSlips.getRowHeight();

                suggestionPopupMulti.setPopupSize(new Dimension(columnWidth, 150));
                suggestionPopupMulti.show(tblBorrowSlips, x, y);
            } else {
                suggestionPopupMulti.setVisible(false);
            }
        });
    }

    private void saveMultipleSlips() {
        int rowCount = tblBorrowSlips.getRowCount();
        boolean hasError = false;
        List<String[]> bookDetails = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String isbn = (String) tblBorrowSlips.getValueAt(i, 0);
            String title = (String) tblBorrowSlips.getValueAt(i, 1);
            String readerIdText = (String) tblBorrowSlips.getValueAt(i, 2);
            String readerName = (String) tblBorrowSlips.getValueAt(i, 3);
            String borrowDate = (String) tblBorrowSlips.getValueAt(i, 4);

            // Kiểm tra hàng trống (không có dữ liệu nào)
            if (isbn.isEmpty() && readerIdText.isEmpty() && borrowDate.isEmpty()) {
                continue; // Bỏ qua hàng trống
            }

            // Kiểm tra dữ liệu thiếu
            if (isbn.isEmpty() || readerIdText.isEmpty() || borrowDate.isEmpty()) {
                hasError = true;
                StringBuilder errorMessage = new StringBuilder("Dòng " + (i + 1) + " có lỗi:\n");

                if (isbn.isEmpty()) {
                    errorMessage.append(" - ISBN không được để trống.\n");
                }
                if (readerIdText.isEmpty()) {
                    errorMessage.append(" - Reader ID không được để trống.\n");
                }
                if (borrowDate.isEmpty()) {
                    errorMessage.append(" - Borrow Date không được để trống.\n");
                }

                JOptionPane.showMessageDialog(null, errorMessage.toString(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return; // Dừng quá trình lưu và thông báo lỗi
            }

            try {
                int readerId = Integer.parseInt(readerIdText);
                bookDetails.add(new String[]{isbn, title});

                // Ghi dữ liệu vào CSDL (Nếu tất cả các dòng hợp lệ)
                BorrowSlipDAO.insertBorrowSlip(readerId, borrowDate, bookDetails);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID độc giả phải là số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!hasError) {
            JOptionPane.showMessageDialog(null, "Lưu thành công!");
        }
    }

    private void addNewRowToTable() {
        DefaultTableModel tableModel = (DefaultTableModel) tblBorrowSlips.getModel();
        tableModel.addRow(new Object[]{"", "", "", "", ""});
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
        btnAdd = new rojeru_san.complementos.RSButtonHover();
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        searchAreaPanel = new javax.swing.JPanel();
        addAreaPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnSingleSlip = new rojeru_san.complementos.RSButtonHover();
        btnMultiSlip = new rojerusan.RSButtonHover();
        multiSlipPanel = new javax.swing.JPanel();
        btnAddRow = new rojeru_san.complementos.RSButtonHover();
        btnAddRow1 = new rojeru_san.complementos.RSButtonHover();
        btnSaveMultiple = new rojeru_san.complementos.RSButtonHover();
        jPanel9 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBorrowSlips = new javax.swing.JTable();
        singleSlipPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtTitle = new app.bolivia.swing.JCTextField();
        lblBookTitle = new javax.swing.JLabel();
        txtReaderID = new app.bolivia.swing.JCTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblReaderName = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtISBN = new app.bolivia.swing.JCTextField();
        btnSingleSave = new rojeru_san.complementos.RSButtonHover();
        txtDATE = new app.bolivia.swing.JCTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 7, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE BORROWING BOOKS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 380, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/li4.png"))); // NOI18N
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

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanel4.setBackground(new java.awt.Color(0, 51, 51));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAdd.setBackground(new java.awt.Color(0, 51, 51));
        btnAdd.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        btnAdd.setText("Add Borrow Slips");
        btnAdd.setColorHover(new java.awt.Color(102, 153, 255));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        jPanel4.add(btnAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 180, -1));

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        searchPanel.setText("Search Borrow Slips");
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
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/contact-form.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, -1, -1));

        jPanel5.setBackground(new java.awt.Color(102, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("FEATURES");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 0, -1, 50));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        searchAreaPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        addAreaPanel.setBackground(new java.awt.Color(0, 51, 51));
        addAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        addAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel6.setLayout(new java.awt.GridLayout(1, 0));

        btnSingleSlip.setBackground(new java.awt.Color(102, 153, 255));
        btnSingleSlip.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        btnSingleSlip.setForeground(new java.awt.Color(0, 51, 51));
        btnSingleSlip.setText("Single Form");
        btnSingleSlip.setColorHover(new java.awt.Color(255, 0, 51));
        btnSingleSlip.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSingleSlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSingleSlipActionPerformed(evt);
            }
        });
        jPanel6.add(btnSingleSlip);

        btnMultiSlip.setBackground(new java.awt.Color(102, 153, 255));
        btnMultiSlip.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 255, 255)));
        btnMultiSlip.setText("Mutiple Form");
        btnMultiSlip.setColorHover(new java.awt.Color(255, 0, 51));
        btnMultiSlip.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnMultiSlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMultiSlipActionPerformed(evt);
            }
        });
        jPanel6.add(btnMultiSlip);

        addAreaPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        multiSlipPanel.setBackground(new java.awt.Color(0, 51, 51));
        multiSlipPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(255, 255, 255)));
        multiSlipPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddRow.setBackground(new java.awt.Color(255, 255, 255));
        btnAddRow.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 153, 255)));
        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24X24add-1_icon-icons.com_65127.png"))); // NOI18N
        btnAddRow.setText("ADD ROW");
        btnAddRow.setColorHover(new java.awt.Color(255, 0, 51));
        btnAddRow.setColorText(new java.awt.Color(0, 0, 0));
        btnAddRow.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });
        multiSlipPanel.add(btnAddRow, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 130, 130, 40));

        btnAddRow1.setBackground(new java.awt.Color(255, 255, 255));
        btnAddRow1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 153, 255)));
        btnAddRow1.setText("CLEAR ALL");
        btnAddRow1.setColorHover(new java.awt.Color(255, 0, 51));
        btnAddRow1.setColorText(new java.awt.Color(0, 0, 0));
        btnAddRow1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAddRow1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRow1ActionPerformed(evt);
            }
        });
        multiSlipPanel.add(btnAddRow1, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 130, 130, 40));

        btnSaveMultiple.setBackground(new java.awt.Color(255, 255, 255));
        btnSaveMultiple.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 153, 255)));
        btnSaveMultiple.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/24X24save_78935.png"))); // NOI18N
        btnSaveMultiple.setText("SAVE");
        btnSaveMultiple.setColorHover(new java.awt.Color(255, 0, 51));
        btnSaveMultiple.setColorText(new java.awt.Color(0, 0, 0));
        btnSaveMultiple.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSaveMultiple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveMultipleActionPerformed(evt);
            }
        });
        multiSlipPanel.add(btnSaveMultiple, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 130, 130, 40));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/stamp32.png"))); // NOI18N
        jPanel9.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, -1, -1));

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("BOOKS BORROWING FORM");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 10, -1, -1));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/library11.png"))); // NOI18N
        jPanel9.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 170, 140));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(0, 51, 51)));
        jPanel9.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 68, 1100, 85));

        multiSlipPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 1280, 160));

        tblBorrowSlips.setBackground(new java.awt.Color(255, 255, 255));
        tblBorrowSlips.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tblBorrowSlips.setForeground(new java.awt.Color(0, 0, 0));
        tblBorrowSlips.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Title", "ReaderID", "Reader name", "Borrow date"
            }
        ));
        tblBorrowSlips.setGridColor(new java.awt.Color(0, 51, 51));
        tblBorrowSlips.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tblBorrowSlips.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblBorrowSlips.setRowHeight(30);
        tblBorrowSlips.setSelectionBackground(new java.awt.Color(255, 204, 255));
        tblBorrowSlips.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblBorrowSlips.setShowGrid(true);
        tblBorrowSlips.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tblBorrowSlipsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tblBorrowSlipsFocusLost(evt);
            }
        });
        tblBorrowSlips.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBorrowSlipsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblBorrowSlips);

        multiSlipPanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 1280, 450));

        addAreaPanel.add(multiSlipPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        singleSlipPanel.setBackground(new java.awt.Color(255, 255, 255));
        singleSlipPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        singleSlipPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("BOOK BORROWING FORM");
        singleSlipPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, 500, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Date borrow: ");
        singleSlipPanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 370, -1, -1));

        txtTitle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtTitle.setForeground(new java.awt.Color(0, 0, 0));
        txtTitle.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTitle.setToolTipText("");
        txtTitle.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtTitle.setBackground(new Color(0, 0, 0, 0));
        txtTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTitleActionPerformed(evt);
            }
        });
        txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtTitleKeyPressed(evt);
            }
        });
        singleSlipPanel.add(txtTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 300, 700, 40));

        lblBookTitle.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblBookTitle.setForeground(new java.awt.Color(0, 0, 0));
        lblBookTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBookTitle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        singleSlipPanel.add(lblBookTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 300, 700, 40));

        txtReaderID.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtReaderID.setForeground(new java.awt.Color(0, 0, 0));
        txtReaderID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID.setToolTipText("");
        txtReaderID.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtReaderID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderIDActionPerformed(evt);
            }
        });
        singleSlipPanel.add(txtReaderID, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 120, 680, 40));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Reader's ID:");
        singleSlipPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 130, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Reader's name:");
        singleSlipPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 190, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("ISBN: ");
        singleSlipPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, -1, -1));

        lblReaderName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblReaderName.setForeground(new java.awt.Color(0, 0, 0));
        lblReaderName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReaderName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        singleSlipPanel.add(lblReaderName, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 180, 660, 40));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("Book title: ");
        singleSlipPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 310, -1, -1));

        txtISBN.setBackground(new java.awt.Color(255, 255, 255));
        txtISBN.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtISBN.setForeground(new java.awt.Color(0, 0, 0));
        txtISBN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtISBN.setToolTipText("");
        txtISBN.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtISBN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtISBNActionPerformed(evt);
            }
        });
        singleSlipPanel.add(txtISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 240, 740, 40));

        btnSingleSave.setBackground(new java.awt.Color(102, 153, 255));
        btnSingleSave.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        btnSingleSave.setText("SAVE");
        btnSingleSave.setColorHover(new java.awt.Color(255, 0, 51));
        btnSingleSave.setColorText(new java.awt.Color(0, 0, 0));
        btnSingleSave.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnSingleSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSingleSaveActionPerformed(evt);
            }
        });
        singleSlipPanel.add(btnSingleSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 530, 800, 40));

        txtDATE.setBackground(new java.awt.Color(255, 255, 255));
        txtDATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtDATE.setForeground(new java.awt.Color(0, 0, 0));
        txtDATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDATE.setToolTipText("");
        txtDATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtDATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDATEActionPerformed(evt);
            }
        });
        singleSlipPanel.add(txtDATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 360, 680, 40));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/stamp.png"))); // NOI18N
        singleSlipPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        addAreaPanel.add(singleSlipPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        getContentPane().add(addAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        HomePage home = new HomePage();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        addAreaPanel.setVisible(true);


    }//GEN-LAST:event_btnAddMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addAreaPanel.setVisible(true);
        searchAreaPanel.setVisible(false);
    }//GEN-LAST:event_btnAddActionPerformed

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(true);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(true);
    }//GEN-LAST:event_searchPanelActionPerformed

    private void btnSingleSlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSingleSlipActionPerformed
        singleSlipPanel.setVisible(true);
        multiSlipPanel.setVisible(false);
    }//GEN-LAST:event_btnSingleSlipActionPerformed

    private void btnMultiSlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMultiSlipActionPerformed
        singleSlipPanel.setVisible(false);
        multiSlipPanel.setVisible(true);
    }//GEN-LAST:event_btnMultiSlipActionPerformed

    private void txtTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTitleActionPerformed

    private void txtReaderIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderIDActionPerformed
        String readerIDText = txtReaderID.getText().trim();
        if (!readerIDText.isEmpty()) {
            try {
                int readerID = Integer.parseInt(readerIDText);
                String readerName = ReaderDAO.getReaderNameByID(readerID);
                lblReaderName.setText(readerName.isEmpty() ? "Không tìm thấy độc giả" : readerName);
            } catch (NumberFormatException ex) {
                lblReaderName.setText("Reader ID không hợp lệ");
            }
        }
    }//GEN-LAST:event_txtReaderIDActionPerformed

    private void txtISBNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtISBNActionPerformed
        String isbn = txtISBN.getText().trim();
        if (!isbn.isEmpty()) {
            String bookTitle = BookDAO.getBookTitleByISBN(isbn);
            lblBookTitle.setText(bookTitle.isEmpty() ? "Không tìm thấy sách" : bookTitle);
        }
        if (isbn.isEmpty()) {
            String bookTitle = "";
            lblBookTitle.setText(bookTitle);
        }
    }//GEN-LAST:event_txtISBNActionPerformed

    private void btnSingleSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSingleSaveActionPerformed

        try {
            int readerID = Integer.parseInt(txtReaderID.getText().trim());
            String isbn = txtISBN.getText().trim();
            String borrowDateText = txtDATE.getText().trim();

            if (readerID <= 0 || isbn.isEmpty() || borrowDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Chuyển đổi chuỗi ngày thành đối tượng Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date borrowDate = dateFormat.parse(borrowDateText);

            // Lưu phiếu mượn (Borrow Slip)
            int borrowId = BorrowSlipDAO.saveBorrowSlip(readerID, borrowDate);

            if (borrowId != -1) {
                // Lưu chi tiết phiếu mượn (Borrow Detail)
                boolean detailSaved = BorrowSlipDAO.saveBorrowDetail(borrowId, isbn);

                if (detailSaved) {
                    JOptionPane.showMessageDialog(this, "Lưu phiếu mượn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                    // Reset các trường nhập liệu
                    txtReaderID.setText("");
                    txtISBN.setText("");
                    txtDATE.setText("");
                    lblReaderName.setText("");
                    lblBookTitle.setText("");
                    txtTitle.setText("");

                } else {
                    JOptionPane.showMessageDialog(this, "Không thể lưu chi tiết phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Reader ID không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày mượn không hợp lệ! Vui lòng nhập theo định dạng yyyy-MM-dd", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_btnSingleSaveActionPerformed

    private void tblBorrowSlipsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBorrowSlipsMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBorrowSlipsMouseClicked

    private void txtDATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDATEActionPerformed

    private void txtTitleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTitleKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTitleKeyPressed

    private void tblBorrowSlipsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblBorrowSlipsFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBorrowSlipsFocusGained

    private void tblBorrowSlipsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblBorrowSlipsFocusLost
        int row = tblBorrowSlips.getSelectedRow();
        if (row >= 0) {
            updateReaderNameAndBookTitle(row);
        }
    }//GEN-LAST:event_tblBorrowSlipsFocusLost

    private void btnSaveMultipleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveMultipleActionPerformed
        saveMultipleSlips();
    }//GEN-LAST:event_btnSaveMultipleActionPerformed

    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
        addNewRowToTable();
    }//GEN-LAST:event_btnAddRowActionPerformed

    private void btnAddRow1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRow1ActionPerformed

        DefaultTableModel model = (DefaultTableModel) tblBorrowSlips.getModel();
        model.setRowCount(0);

    }//GEN-LAST:event_btnAddRow1ActionPerformed

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
            java.util.logging.Logger.getLogger(BooksBorrowing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BooksBorrowing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BooksBorrowing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BooksBorrowing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BooksBorrowing().setVisible(true);
            }
        });
    }

    private Timer searchTimer;
    private DefaultListModel<String> listModel;
    private JList<String> bookList;
    private JPopupMenu suggestionPopup;

    private JPopupMenu suggestionPopupMulti;
    private JList<String> bookListMulti;
    private DefaultListModel<String> listModelMulti;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addAreaPanel;
    private rojeru_san.complementos.RSButtonHover btnAdd;
    private rojeru_san.complementos.RSButtonHover btnAddRow;
    private rojeru_san.complementos.RSButtonHover btnAddRow1;
    private rojerusan.RSButtonHover btnBack;
    private rojerusan.RSButtonHover btnMultiSlip;
    private rojeru_san.complementos.RSButtonHover btnSaveMultiple;
    private rojeru_san.complementos.RSButtonHover btnSingleSave;
    private rojeru_san.complementos.RSButtonHover btnSingleSlip;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblBookTitle;
    private javax.swing.JLabel lblReaderName;
    private javax.swing.JPanel multiSlipPanel;
    private javax.swing.JPanel searchAreaPanel;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private javax.swing.JPanel singleSlipPanel;
    private javax.swing.JTable tblBorrowSlips;
    private app.bolivia.swing.JCTextField txtDATE;
    private app.bolivia.swing.JCTextField txtISBN;
    private app.bolivia.swing.JCTextField txtReaderID;
    private app.bolivia.swing.JCTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}
