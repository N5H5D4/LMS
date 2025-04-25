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
import UI_Helper.RoundedPanel;
import java.util.Date;
import java.util.Calendar;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import static javax.swing.SwingConstants.CENTER;

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

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.BLACK);

        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            setText((value == null) ? "View" : value.toString());

            if (isSelected) {
                setBackground(Color.RED); // màu nền khi được chọn
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(255, 255, 255)); // màu nền mặc định
                setForeground(Color.BLACK);
            }

            return this;
        }
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
                    label.setBackground(new Color(0, 51, 51));
                    setForeground(Color.WHITE);
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
            tblOverdue.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());

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
            overdueScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            OverduePanel.add(overdueScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 630));
        }
        overdueScrollPane.setVisible(true);
        loadOverdueData();
    }

    private void loadOverdueData() {
        overdueModel.setRowCount(0);

        List<Map<String, Object>> overdueList = PenaltyDAO.getOverdueNotReturned();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();

        for (Map<String, Object> overdue : overdueList) {
            int borrowId = (int) overdue.get("borrow_id");
            int readerId = (int) overdue.get("reader_id");
            String readerName = (String) overdue.get("reader_name");
            String bookListStr = (String) overdue.get("book_list");
            List<Map<String, String>> books = new ArrayList<>();
            for (String bookStr : bookListStr.split(", ")) {
                String[] parts = bookStr.split(" - ");
                Map<String, String> book = new HashMap<>();
                book.put("isbn", parts.length > 0 ? parts[0] : "");
                book.put("title", parts.length > 1 ? parts[1] : "");
                book.put("status", parts.length > 2 ? parts[2] : "Borrowed");
                books.add(book);
            }
            tblOverdue.putClientProperty("bookList_" + borrowId, books);

            Date dueDate = (Date) overdue.get("due_date");

            long diffInMillies = currentDate.getTime() - dueDate.getTime();
            long daysOverdue = diffInMillies / (1000 * 60 * 60 * 24);

            overdueModel.addRow(new Object[]{
                readerId,
                readerName,
                borrowId,
                "View borrowed list",
                sdf.format(dueDate),
                daysOverdue
            });

        }
    }

    private void showOverduePopup(int row, int col) {
        int borrowId = (int) overdueModel.getValueAt(row, 2);

        if (col != 3) {
            return; // Chỉ xử lý khi click cột "Borrow list"
        }
        List<Map<String, String>> bookList = (List<Map<String, String>>) tblOverdue.getClientProperty("bookList_" + borrowId);

        if (bookList == null || bookList.isEmpty()) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(240, 248, 255));
        MatteBorder matteBorder = new MatteBorder(3, 3, 3, 3, new Color(0, 51, 51));
        popup.setBorder(matteBorder);

        JTable booksTable = new JTable();
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ISBN", "Title", "Status"}, 0
        );
        booksTable.setModel(model);
        booksTable.setRowHeight(30);
        booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        for (Map<String, String> book : bookList) {
            model.addRow(new Object[]{
                book.getOrDefault("isbn", ""),
                book.getOrDefault("title", ""),
                book.getOrDefault("status", "")
            });
        }

        // Style cho header/cell
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(Color.WHITE);
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };
        for (int i = 0; i < booksTable.getColumnCount(); i++) {
            booksTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            booksTable.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                {
                    setHorizontalAlignment(JLabel.CENTER);
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int height = Math.min(bookList.size() * 30 + 30, 200);
        scrollPane.setPreferredSize(new Dimension(600, height));

        popup.add(scrollPane);
        popup.pack();

        //Vị trí xuất hiện popup
        Rectangle cellRect = tblOverdue.getCellRect(row, col, true);
        int popupWidth = popup.getPreferredSize().width;
        int cellCenterX = cellRect.x + (cellRect.width / 2);
        int popupX = cellCenterX - (popupWidth / 2);
        int popupY = cellRect.y + tblOverdue.getRowHeight();
        popup.show(tblOverdue, popupX, popupY);
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
        // Show grid
        tblPenalty.setShowGrid(true);
        tblPenalty.setGridColor(Color.BLACK);

        // Căn giữa và giữ bold cho header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBackground(new Color(0, 51, 51));
                label.setForeground(Color.WHITE);
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
            if (i != 3 && i != 5) { // Bỏ qua cột 3 và 5 
                tblPenalty.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
        }

        // Renderer tùy chỉnh cho cột Borrow list và Penalty details (đặt sau để không bị ghi đè)
        tblPenalty.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer()); 
        tblPenalty.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());

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

        PenaltiesPanel.add(scrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 1280, 510));

        // Đảm bảo các JComboBox có giá trị mặc định
        if (cmbYear.getItemCount() == 0) {
            cmbYear.addItem("All");
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 2000; i <= currentYear; i++) {
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
    private void loadPenaltyData(String keyword, String year, String month, String filter) {
        tableModel.setRowCount(0);

        List<Map<String, Object>> penalties = PenaltyDAO.getPenalties(keyword, year, month, filter);
        Map<Integer, Map<String, Object>> borrowData = new HashMap<>();
        Map<Integer, List<String>> bookLists = new HashMap<>();
        Map<Integer, List<String>> penaltyDetails = new HashMap<>();
        Map<Integer, BigDecimal> borrowTotalFines = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Nhóm theo borrow_id
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
                "View borrowed list",
                returnDate != null ? sdf.format(returnDate) : "N/A",
                "View penalty details",
                borrowTotalFines.get(borrowId).toString()
            });
            // Lưu dữ liệu chi tiết vào thuộc tính của hàng
            tblPenalty.putClientProperty("bookList_" + borrowId, bookLists.get(borrowId));
            tblPenalty.putClientProperty("penaltyDetails_" + borrowId, penaltyDetails.get(borrowId));
        }
    }

    private void showPopup(int row, int col) {
        int borrowId = (int) tableModel.getValueAt(row, 2);
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(240, 248, 255));
        popup.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 51), 2));

        JTable innerTable = new JTable();
        innerTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        innerTable.setRowHeight(30);
        //innerTable.setGridColor(new Color(0, 51, 51));
        //innerTable.setShowGrid(true);

        if (col == 3) { // Borrow list
            List<String> bookList = (List<String>) tblPenalty.getClientProperty("bookList_" + borrowId);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ISBN", "Title"}, 0);
            for (String item : bookList) {
                String[] parts = item.split(" - ", 2); // ISBN - Title
                model.addRow(new Object[]{parts[0], parts.length > 1 ? parts[1] : ""});
            }
            innerTable.setModel(model);
        } else if (col == 5) { // Penalty details
            List<String> penalties = (List<String>) tblPenalty.getClientProperty("penaltyDetails_" + borrowId);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Reason", "Amount (VND)"}, 0);
            for (String item : penalties) {
                String[] parts = item.split(" - ", 2); // Reason - Amount
                model.addRow(new Object[]{parts[0], parts.length > 1 ? parts[1] : ""});
            }
            innerTable.setModel(model);
        }

        // Style cho header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(Color.WHITE);
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };
        for (int i = 0; i < innerTable.getColumnCount(); i++) {
            innerTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            innerTable.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                {
                    setHorizontalAlignment(JLabel.CENTER);
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(innerTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(400, Math.min(innerTable.getRowCount() * 30 + 30, 300)));
        popup.add(scrollPane);

        MatteBorder matteBorder = new MatteBorder(3, 3, 3, 3, new Color(0, 51, 51));
        popup.setBorder(matteBorder);

        //Vị trí popup (giữa)
        Rectangle cellRect = tblPenalty.getCellRect(row, col, true);
        int popupWidth = popup.getPreferredSize().width;
        int cellCenterX = cellRect.x + (cellRect.width / 2);
        int popupX = cellCenterX - (popupWidth / 2);
        int popupY = cellRect.y + tblPenalty.getRowHeight();
        popup.show(tblPenalty, popupX, popupY);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SearchPanel = new javax.swing.JPanel();
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
        jPanel6 = new javax.swing.JPanel();
        btnPenalties = new rojeru_san.complementos.RSButtonHover();
        btnUnreturned = new rojerusan.RSButtonHover();
        OverduePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
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
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 51, 51));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SearchPanel.setBackground(new java.awt.Color(255, 255, 255));
        SearchPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        PenaltiesPanel.setBackground(new java.awt.Color(255, 255, 255));
        PenaltiesPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(0, 51, 51));
        jPanel11.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel11 = new RoundedPanel(30);
        jPanel11.setBackground(new Color(0, 51, 51));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtSearch.setBackground(new java.awt.Color(0, 51, 51));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setCaretColor(new java.awt.Color(255, 255, 255));
        txtSearch.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSearch.setPhColor(new java.awt.Color(255, 255, 255));
        txtSearch.setPlaceholder("                       ENTER KEYWORD");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanel11.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 310, 40));

        btnSearch.setBackground(new java.awt.Color(0, 51, 51));
        btnSearch.setBorder(null);
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/search.png"))); // NOI18N
        btnSearch.setColorHover(new java.awt.Color(51, 255, 0));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanel11.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 50, 40));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbYear.setForeground(new java.awt.Color(0, 51, 51));
        cmbYear.setToolTipText("");
        cmbYear.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbYear.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbYear.getUI().getAccessibleChild(cmbYear, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        jPanel11.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 50, 200, 40));

        jLabel26.setBackground(new java.awt.Color(0, 51, 51));
        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        jLabel26.setText("RETURNED YEAR");
        jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel26.setIconTextGap(10);
        jPanel11.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 10, 140, -1));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbCriteria.setForeground(new java.awt.Color(0, 51, 51));
        cmbCriteria.setToolTipText("");
        cmbCriteria.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbCriteria.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbCriteria.getUI().getAccessibleChild(cmbCriteria, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel11.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, 200, 40));

        jLabel27.setBackground(new java.awt.Color(0, 51, 51));
        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/filter.png"))); // NOI18N
        jLabel27.setText("CRITERIA ");
        jLabel27.setIconTextGap(10);
        jPanel11.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 10, -1, -1));

        cmbMonth.setBackground(new java.awt.Color(255, 255, 255));
        cmbMonth.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbMonth.setForeground(new java.awt.Color(0, 51, 51));
        cmbMonth.setToolTipText("");
        cmbMonth.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        cmbMonth.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbMonth.getUI().getAccessibleChild(cmbMonth, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cmbMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMonthActionPerformed(evt);
            }
        });
        jPanel11.add(cmbMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 50, 200, 40));

        jLabel25.setBackground(new java.awt.Color(0, 51, 51));
        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/month.png"))); // NOI18N
        jLabel25.setText("RETURNED MONTH");
        jLabel25.setIconTextGap(10);
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 10, -1, -1));

        PenaltiesPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 110));

        SearchPanel.add(PenaltiesPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        jPanel6.setBackground(new java.awt.Color(0, 51, 51));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel6 = new RoundedPanel(30);
        jPanel6.setBackground(new Color(0, 51, 51));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnPenalties.setBackground(new java.awt.Color(0, 51, 51));
        btnPenalties.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        btnPenalties.setForeground(new java.awt.Color(0, 51, 51));
        btnPenalties.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/penalty_list.png"))); // NOI18N
        btnPenalties.setText("Penalties List");
        btnPenalties.setColorHover(new java.awt.Color(0, 51, 51));
        btnPenalties.setColorTextHover(new java.awt.Color(102, 153, 255));
        btnPenalties.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPenalties.setIconTextGap(20);
        btnPenalties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPenaltiesActionPerformed(evt);
            }
        });
        jPanel6.add(btnPenalties, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 3, 630, 34));

        btnUnreturned.setBackground(new java.awt.Color(0, 51, 51));
        btnUnreturned.setBorder(null);
        btnUnreturned.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/overdue32x32.png"))); // NOI18N
        btnUnreturned.setText("Overdue List");
        btnUnreturned.setColorHover(new java.awt.Color(0, 51, 51));
        btnUnreturned.setColorTextHover(new java.awt.Color(102, 153, 255));
        btnUnreturned.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnUnreturned.setIconTextGap(20);
        btnUnreturned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnreturnedActionPerformed(evt);
            }
        });
        jPanel6.add(btnUnreturned, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 3, 630, 34));

        SearchPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 40));

        OverduePanel.setBackground(new java.awt.Color(255, 255, 255));
        OverduePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        SearchPanel.add(OverduePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/bookPenalt.png"))); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 0, 0)));
        SearchPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 100, -1, -1));

        getContentPane().add(SearchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

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
