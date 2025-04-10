/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.border.MatteBorder;
import DAO.*;
import java.util.Date;
import java.util.Calendar;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 *
 * @author HS
 */
public class Penalty extends javax.swing.JFrame {

    /**
     * Creates new form Penalty
     */
    public Penalty() {
        initComponents();
        SearchPanel.setVisible(false);
        OverduePanel.setVisible(false);
        PenaltiesPanel.setVisible(false);
        Penalties();
        Overdue();
    }

    private void Overdue() {
        if (tblOverdue == null) {
            overdueModel = new DefaultTableModel(
                    new Object[]{"Reader ID", "Reader name", "Borrow ID", "Borrow list", "Due date", "Days overdue"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblOverdue = new JTable(overdueModel);
            tblOverdue.setRowHeight(30);
            tblOverdue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
            tblOverdue.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));

            // Show grid lines
            tblOverdue.setShowGrid(true);
            tblOverdue.setGridColor(Color.BLACK);

            // Làm bảng trong suốt
            tblOverdue.setOpaque(false);
            tblOverdue.setBackground(new Color(0, 0, 0, 0));
            tblOverdue.setForeground(Color.BLACK);
            tblOverdue.setSelectionBackground(Color.RED);

            // Căn giữa
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
            };
            for (int i = 0; i < tblOverdue.getColumnCount(); i++) {
                tblOverdue.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }

            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setHorizontalAlignment(JLabel.CENTER);
            cellRenderer.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
            for (int i = 0; i < tblOverdue.getColumnCount(); i++) {
                if (i != 3) {
                    tblOverdue.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
                }
            }

            // Renderer tùy chỉnh cho cột "Borrow list"
            tblOverdue.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("View Borrow List"));

            // Nhấp chuột để hiển thị dropdown
            tblOverdue.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = tblOverdue.rowAtPoint(evt.getPoint());
                    int col = tblOverdue.columnAtPoint(evt.getPoint());
                    if (row >= 0 && col == 3) {
                        showOverduePopup(row, col);
                    }
                }
            });

            // Thêm bảng Overdue vào PenaltiesPanel
            overdueScrollPane = new JScrollPane(tblOverdue);
            MatteBorder matteBorder = new MatteBorder(5, 3, 3, 3, new Color(0, 51, 51));
            overdueScrollPane.setBorder(matteBorder);
            overdueScrollPane.setOpaque(false);
            overdueScrollPane.getViewport().setOpaque(false);
            OverduePanel.add(overdueScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 1280, 640));
        }
        overdueScrollPane.setVisible(true);
        loadOverdueData();
    }

    private void loadOverdueData() {
        overdueModel.setRowCount(0);

        List<Map<String, Object>> overdueList = PenaltyDAO.getOverdueNotReturned();
        Map<Integer, List<String>> bookLists = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();

        for (Map<String, Object> overdue : overdueList) {
            int borrowId = (int) overdue.get("borrow_id");
            int readerId = (int) overdue.get("reader_id");
            String readerName = (String) overdue.get("reader_name");
            String bookListStr = (String) overdue.get("book_list");
            Date dueDate = (Date) overdue.get("due_date");

            long diffInMillies = currentDate.getTime() - dueDate.getTime();
            long daysOverdue = diffInMillies / (1000 * 60 * 60 * 24);

            bookLists.put(borrowId, new ArrayList<>(Arrays.asList(bookListStr.split(", "))));
            overdueModel.addRow(new Object[]{
                readerId,
                readerName,
                borrowId,
                "View",
                sdf.format(dueDate),
                daysOverdue
            });
            tblOverdue.putClientProperty("bookList_" + borrowId, bookLists.get(borrowId));
        }
    }

    private void showOverduePopup(int row, int col) {
        int borrowId = (int) overdueModel.getValueAt(row, 2);
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(240, 248, 255));
        popup.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 51), 2));

        if (col != 3) {
            return; //Borrow list
        }
        List<String> bookList = (List<String>) tblOverdue.getClientProperty("bookList_" + borrowId);
        JList<String> list = new JList<>(bookList.toArray(new String[0]));
        list.setFont(new Font("Segoe UI", Font.BOLD, 16));
        list.setBackground(new Color(240, 248, 255));
        list.setForeground(new Color(0, 51, 51));
        list.setSelectionBackground(new Color(102, 153, 255));
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int itemCount = list.getModel().getSize();
        int itemHeight = 25;
        int height = itemCount * itemHeight + 10;
        int maxHeight = tblOverdue.getHeight() - row * tblOverdue.getRowHeight() - 10;
        int width = 400;

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (height > maxHeight) {
            scrollPane.setPreferredSize(new Dimension(width, maxHeight));
        } else {
            scrollPane.setPreferredSize(new Dimension(width, height));
        }

        popup.add(scrollPane);
        list.revalidate();
        popup.pack();

        Rectangle cellRect = tblOverdue.getCellRect(row, col, true);
        popup.show(tblOverdue, cellRect.x, cellRect.y + tblOverdue.getRowHeight());
    }

    private void Penalties() {
        // Khởi tạo bảng với các cột
        tableModel = new DefaultTableModel(
                new Object[]{"Reader ID", "Reader name", "Borrow ID", "Borrow list", "Return date", "Penalty details", "Total (VND)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPenalty = new JTable(tableModel);
        tblPenalty.setRowHeight(30);
        tblPenalty.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        tblPenalty.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        tblPenalty.setSelectionBackground(Color.RED);
        // Làm bảng trong suốt
        tblPenalty.setOpaque(false);
        tblPenalty.setBackground(new Color(0, 0, 0, 0));
        tblPenalty.setForeground(Color.BLACK);
        // Show grid lines
        tblPenalty.setShowGrid(true);
        tblPenalty.setGridColor(Color.BLACK);

        // Căn giữa và giữ bold cho header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        };
        for (int i = 0; i < tblPenalty.getColumnCount(); i++) {
            tblPenalty.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Căn giữa cho các ô
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        cellRenderer.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));
        for (int i = 0; i < tblPenalty.getColumnCount(); i++) {
            if (i != 3 && i != 5) { // Bỏ qua cột 3 và 5 để tránh ghi đè ButtonRenderer
                tblPenalty.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
        }

        // Renderer tùy chỉnh cho cột "Borrow list" và "Penalty details" (đặt sau để không bị ghi đè)
        tblPenalty.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("View Borrow List"));
        tblPenalty.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("View Details"));

        // Nhấp chuột để hiển thị dropdown
        tblPenalty.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblPenalty.rowAtPoint(evt.getPoint());
                int col = tblPenalty.columnAtPoint(evt.getPoint());
                if (row >= 0 && (col == 3 || col == 5)) {
                    showPopup(row, col);
                }
            }
        });

        // Thêm bảng vào PenaltiesPanel
        JScrollPane scrollPane = new JScrollPane(tblPenalty);
        MatteBorder matteBorder = new MatteBorder(3, 3, 3, 3, new Color(0, 51, 51));
        scrollPane.setBorder(matteBorder);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        PenaltiesPanel.add(scrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 1280, 520));

        // Đảm bảo các JComboBox có giá trị mặc định
        if (cmbYear.getItemCount() == 0) {
            cmbYear.addItem("All");
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 2020; i <= currentYear; i++) {
                cmbYear.addItem(String.valueOf(i));
            }
        }
        if (cmbMonth.getItemCount() == 0) {
            cmbMonth.addItem("All");
            for (int i = 1; i <= 12; i++) {
                cmbMonth.addItem(String.format("%02d", i));
            }
        }
        if (cmbCriteria.getItemCount() == 0) {
            cmbCriteria.addItem("All");
            cmbCriteria.addItem("ISBN");
            cmbCriteria.addItem("Reader Name");
            cmbCriteria.addItem("Reader ID");
        }

        loadPenaltyData("", "All", "All", "All");
    }

// Renderer tùy chỉnh cho cột "Borrow list" và "Penalty details"
    class ButtonRenderer extends JLabel implements TableCellRenderer {

        private String text;

        public ButtonRenderer(String text) {
            this.text = text;
            setOpaque(false);
            setHorizontalAlignment(CENTER);
            setForeground(new Color(0, 102, 204)); // Xanh dương đậm
            setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(text);
            if (isSelected) {
                setBackground(new Color(102, 153, 255, 100));
            } else {
                setBackground(new Color(0, 0, 0, 0));
            }
            return this;
        }
    }

    private void loadPenaltyData(String keyword, String year, String month, String filter) {
        tableModel.setRowCount(0);

        List<Map<String, Object>> penalties = PenaltyDAO.getPenalties(keyword, year, month, filter);
        Map<Integer, Map<String, Object>> borrowData = new HashMap<>();
        Map<Integer, List<String>> bookLists = new HashMap<>();
        Map<Integer, List<String>> penaltyDetails = new HashMap<>();
        Map<Integer, BigDecimal> borrowTotalFines = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Nhóm dữ liệu theo borrow_id
        for (Map<String, Object> penalty : penalties) {
            int borrowId = (int) penalty.get("borrow_id");
            int readerId = (int) penalty.get("reader_id");
            String readerName = (String) penalty.get("reader_name");
            String bookListStr = (String) penalty.get("book_list");
            Date returnDate = (Date) penalty.get("return_date");
            BigDecimal amount = (BigDecimal) penalty.get("amount");
            String reason = (String) penalty.get("reason");

            // Lưu thông tin cơ bản (chỉ lần đầu)
            if (!borrowData.containsKey(borrowId)) {
                Map<String, Object> data = new HashMap<>();
                data.put("reader_id", readerId);
                data.put("reader_name", readerName);
                data.put("return_date", returnDate);
                borrowData.put(borrowId, data);
                // Lưu danh sách mượn (chỉ 1 lần cho mỗi borrow_id)
                bookLists.put(borrowId, new ArrayList<>(Arrays.asList(bookListStr.split(", "))));
            }

            // Lưu chi tiết phạt
            penaltyDetails.computeIfAbsent(borrowId, k -> new ArrayList<>())
                    .add(String.format("%s - %s", reason, amount.toString()));

            // Tính tổng tiền phạt
            borrowTotalFines.put(borrowId, borrowTotalFines.getOrDefault(borrowId, BigDecimal.ZERO).add(amount));
        }

        // Thêm dữ liệu vào bảng
        for (int borrowId : borrowData.keySet()) {
            Map<String, Object> data = borrowData.get(borrowId);
            Date returnDate = (Date) data.get("return_date");
            tableModel.addRow(new Object[]{
                data.get("reader_id"),
                data.get("reader_name"),
                borrowId,
                "View", // Placeholder cho danh sách mượn
                returnDate != null ? sdf.format(returnDate) : "N/A",
                "View details", // Placeholder cho chi tiết phạt
                borrowTotalFines.get(borrowId).toString()
            });
            // Lưu dữ liệu chi tiết vào thuộc tính của hàng
            tblPenalty.putClientProperty("bookList_" + borrowId, bookLists.get(borrowId));
            tblPenalty.putClientProperty("penaltyDetails_" + borrowId, penaltyDetails.get(borrowId));
        }
    }

    // hiện popup khi nhấp vào ô
    private void showPopup(int row, int col) {
        int borrowId = (int) tableModel.getValueAt(row, 2);
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(240, 248, 255));
        popup.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 51), 2));

        JList<String> list = null;
        if (col == 3) {
            List<String> bookList = (List<String>) tblPenalty.getClientProperty("bookList_" + borrowId);
            list = new JList<>(bookList.toArray(new String[0]));
            list.setFont(new Font("Segoe UI", Font.BOLD, 16));
            list.setBackground(Color.WHITE);
            list.setForeground(Color.BLACK);
            list.setSelectionBackground(new Color(102, 153, 255));
            list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            popup.add(new JScrollPane(list));
        } else if (col == 5) { // Chi tiết xuử phạt
            List<String> penaltyDetails = (List<String>) tblPenalty.getClientProperty("penaltyDetails_" + borrowId);
            list = new JList<>(penaltyDetails.toArray(new String[0]));
            list.setFont(new Font("Segoe UI", Font.BOLD, 16));
            list.setBackground(Color.WHITE);
            list.setForeground(new Color(204, 0, 51));
            list.setSelectionBackground(Color.RED);
            list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            popup.add(new JScrollPane(list));
        }

        // SIZE popup
        int itemCount = list.getModel().getSize();
        int itemHeight = 25;

        int height = itemCount * itemHeight;
        int maxHeight = tblPenalty.getHeight() - row * tblPenalty.getRowHeight() - 10;
        height = Math.min(height, maxHeight);
        int width = col == 3 ? 380 : 360;
        list.setPreferredSize(new Dimension(width - 10, height));

        Rectangle cellRect = tblPenalty.getCellRect(row, col, true);
        popup.show(tblPenalty, cellRect.x, cellRect.y + tblPenalty.getRowHeight());
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
        searchPanel = new rojeru_san.complementos.RSButtonHover();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        SearchPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnPenalties = new rojeru_san.complementos.RSButtonHover();
        btnUnreturned = new rojerusan.RSButtonHover();
        OverduePanel = new javax.swing.JPanel();
        PenaltiesPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        btnSearch = new rojerusan.RSButtonHover();
        cmbYear = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        cmbCriteria = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 51, 51));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 7, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE THE PENALTY LIST");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 380, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/li4.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 50, -1));

        btnBack.setBackground(new java.awt.Color(255, 255, 255));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/back_main_page_icon_124174.png"))); // NOI18N
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

        searchPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        searchPanel.setText("View & Search");
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
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 180, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/penalties.png"))); // NOI18N
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

        SearchPanel.setBackground(new java.awt.Color(255, 255, 255));
        SearchPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel6.setLayout(new java.awt.GridLayout());

        btnPenalties.setBackground(new java.awt.Color(102, 153, 255));
        btnPenalties.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 3, new java.awt.Color(255, 255, 255)));
        btnPenalties.setForeground(new java.awt.Color(0, 51, 51));
        btnPenalties.setText("List of penalties");
        btnPenalties.setColorHover(new java.awt.Color(255, 0, 51));
        btnPenalties.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPenalties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPenaltiesActionPerformed(evt);
            }
        });
        jPanel6.add(btnPenalties);

        btnUnreturned.setBackground(new java.awt.Color(102, 153, 255));
        btnUnreturned.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(255, 255, 255)));
        btnUnreturned.setText("List of overdue and unreturned books");
        btnUnreturned.setColorHover(new java.awt.Color(255, 0, 51));
        btnUnreturned.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnUnreturned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnreturnedActionPerformed(evt);
            }
        });
        jPanel6.add(btnUnreturned);

        SearchPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        OverduePanel.setBackground(new java.awt.Color(255, 255, 255));
        OverduePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        SearchPanel.add(OverduePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        PenaltiesPanel.setBackground(new java.awt.Color(255, 255, 255));
        PenaltiesPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
        jPanel11.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 310, 40));

        btnSearch.setBackground(new java.awt.Color(255, 255, 255));
        btnSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 0, new java.awt.Color(0, 0, 0)));
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24x24_searcher_magnifyng_glass_search_locate_find_icon_123813.png"))); // NOI18N
        btnSearch.setColorHover(new java.awt.Color(51, 255, 0));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanel11.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 50, 40));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbYear.setForeground(new java.awt.Color(0, 0, 0));
        cmbYear.setToolTipText("");
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        jPanel11.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 40, 200, 40));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(0, 0, 0));
        jLabel26.setText("RETURNED YEAR");
        jPanel11.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, -1, -1));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setToolTipText("");
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel11.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 40, 200, 40));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(0, 0, 0));
        jLabel27.setText("CRITERIA ");
        jPanel11.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 10, -1, -1));

        cmbMonth.setBackground(new java.awt.Color(255, 255, 255));
        cmbMonth.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbMonth.setForeground(new java.awt.Color(0, 0, 0));
        cmbMonth.setToolTipText("");
        cmbMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMonthActionPerformed(evt);
            }
        });
        jPanel11.add(cmbMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 40, 200, 40));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(0, 0, 0));
        jLabel25.setText("RETURNED MONTH");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 10, -1, -1));

        PenaltiesPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 1280, 110));

        SearchPanel.add(PenaltiesPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/bookPenalt.png"))); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 0, 0)));
        SearchPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 100, -1, -1));

        getContentPane().add(SearchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        HomePage home = new HomePage();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        SearchPanel.setVisible(true);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        SearchPanel.setVisible(true);
    }//GEN-LAST:event_searchPanelActionPerformed

    private void btnPenaltiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPenaltiesActionPerformed
        PenaltiesPanel.setVisible(true);
        OverduePanel.setVisible(false);
    }//GEN-LAST:event_btnPenaltiesActionPerformed

    private void btnUnreturnedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnreturnedActionPerformed
        PenaltiesPanel.setVisible(false);
        OverduePanel.setVisible(true);
    }//GEN-LAST:event_btnUnreturnedActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String keyword = txtSearch.getText().trim();
        String year = (String) cmbYear.getSelectedItem();
        String month = (String) cmbMonth.getSelectedItem();
        String filter = (String) cmbCriteria.getSelectedItem();
        loadPenaltyData(keyword, year, month, filter);
    }//GEN-LAST:event_btnSearchActionPerformed

    private void cmbYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbYearActionPerformed

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void cmbMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbMonthActionPerformed

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
            java.util.logging.Logger.getLogger(Penalty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Penalty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Penalty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Penalty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Penalty().setVisible(true);
            }
        });
    }
    private JTable tblOverdue;
    private JTable tblPenalty;
    private DefaultTableModel tableModel;
    private DefaultTableModel overdueModel;
    private JScrollPane overdueScrollPane;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OverduePanel;
    private javax.swing.JPanel PenaltiesPanel;
    private javax.swing.JPanel SearchPanel;
    private rojerusan.RSButtonHover btnBack;
    private rojeru_san.complementos.RSButtonHover btnPenalties;
    private rojerusan.RSButtonHover btnSearch;
    private rojerusan.RSButtonHover btnUnreturned;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbMonth;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private app.bolivia.swing.JCTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
