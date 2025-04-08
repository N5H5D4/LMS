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
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.ListSelectionModel;
import java.time.Year;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import DAO.*;
import java.util.Map;

import java.math.BigDecimal;

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
        ReturnBookPanel.setVisible(false);
        initBookTitleSearch();
        initTableEvents();

        populateYearComboBox();

        setFieldsNonEditable();
        jLabel_Warning.setVisible(false);
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    SINGLE SLIP FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    // Chức năng tìm kiếm sách theo title để đề xuất danh sách các books phù hợp
    private void initBookTitleSearch() {
        // Khởi tạo mô hình danh sách và danh sách hiển thị.
        listModel = new DefaultListModel<>();
        bookList = new JList<>(listModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bookList.setFixedCellHeight(30);
        bookList.setBackground(Color.WHITE);
        bookList.setSelectionBackground(new Color(135, 206, 250)); // Màu nền khi được chọn.
        bookList.setSelectionForeground(Color.BLACK);

        // giao diện hiển thị của từng mục trong danh sách
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

        //tạo popup chứa danh sách gợi ý
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        suggestionPopup.add(new JScrollPane(bookList));

        KeyAdapter titleKeyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (searchTimer != null) {
                    searchTimer.cancel(); // Hủy bộ đếm thời gian trước đó nếu tồn tại.
                }

                JTextField source = (JTextField) e.getSource(); // Lấy text field nào đang nhập
                String keyword = source.getText().trim();

                if (keyword.length() > 1) { // Chỉ tìm kiếm khi từ khóa có nhiều hơn 1 ký tự
                    searchTimer = new Timer();
                    searchTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> updateSuggestionList(keyword));
                        }
                    }, 350); // Delay 350ms
                } else {
                    suggestionPopup.setVisible(false);
                }
            }
        };

        // Gán KeyListener cho cả hai text field
        txtTitle.addKeyListener(titleKeyListener);
        //txtTitle2.addKeyListener(titleKeyListener);

        FocusListener titleFocusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                activeTitleField = (JTextField) e.getSource();
            }
        };

        txtTitle.addFocusListener(titleFocusListener);
        //txtTitle2.addFocusListener(titleFocusListener);

        bookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Nhấn đúp chuột
                    int index = bookList.getSelectedIndex();
                    if (index != -1) {
                        String selectedTitle = bookList.getSelectedValue();
                        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(selectedTitle);
                        if (!results.isEmpty()) {
                            String selectedISBN = results.get(0)[0]; // Lấy ISBN

                            // Xác định cặp JTextField cần điền
                            if (activeTitleField == txtTitle) {
                                txtISBN.setText(selectedISBN);
                                txtTitle.setText(selectedTitle);
                            }
                            /*else if (activeTitleField == txtTitle2) {
                                txtISBN2.setText(selectedISBN);
                                txtTitle2.setText(selectedTitle);
                            }*/

                            suggestionPopup.setVisible(false);
                        }
                    }
                }
            }
        });

        // Thêm sự kiện di chuyển chuột qua các mục để highlight mục hiện tại.
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
        listModel.clear(); // Xóa danh sách cũ

        for (String[] book : results) {
            listModel.addElement(book[1]); // Chỉ hiển thị Title
        }

        if (!results.isEmpty()) {
            int txtWidth = activeTitleField.getWidth();
            int popupWidth = Math.max(txtWidth, 300);

            suggestionPopup.setPopupSize(new Dimension(popupWidth, 150));
            suggestionPopup.show(activeTitleField, 0, activeTitleField.getHeight()); // Đảm bảo hiện popup đúng chỗ
        } else {
            suggestionPopup.setVisible(false);
        }
    }


    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    MULTIPLE SLIPS FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

 /*
      Cập nhật tên độc giả và tiêu đề sách dựa trên Reader ID và ISBN đã nhập.
     */
    private void updateReaderNameAndBookTitle(int row) {
        try {
            // Lấy dữ liệu từ bảng
            String readerIdText = (String) tblBorrowSlips.getValueAt(row, 2); // Cột Reader ID
            String isbn = (String) tblBorrowSlips.getValueAt(row, 0); // Cột ISBN

            // Cập nhật tên độc giả nếu Reader ID hợp lệ
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

            // Cập nhật tiêu đề sách nếu ISBN hợp lệ
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

    /* Cập nhật tiêu đề sách khi nhập ISBN vào bảng  
     */
    private void updateTableWithBookTitle(int row, String isbn) {
        List<String[]> results = BookDAO.searchBooksByISBN(isbn);
        if (!results.isEmpty()) {
            tblBorrowSlips.setValueAt(results.get(0)[1], row, 1);
        }
    }

    /* Cập nhật tên độc giả khi nhập Reader ID vào bảng.
     */
    private void updateTableWithReaderName(int row, String readerId) {
        List<String[]> results = ReaderDAO.searchReadersById(readerId);
        if (!results.isEmpty()) {
            tblBorrowSlips.setValueAt(results.get(0)[1], row, 3);
        }
    }

    /*
     Thiết lập sự kiện cho bảng Borrow Slips:
     - Khi nhập ISBN, tự động cập nhật Book Title
     - Khi nhập Reader ID, tự động cập nhật Reader Name
     - Khi nhập một phần Title, hiển thị danh sách gợi ý
     */
    private void initTableEvents() {
        tblBorrowSlips.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 0) { // Cột ISBN
                String isbn = (String) tblBorrowSlips.getValueAt(row, column);
                if (isbn != null && !isbn.trim().isEmpty()) {
                    updateTableWithBookTitle(row, isbn);
                }
            } else if (column == 2) { // Cột Reader ID
                String readerId = (String) tblBorrowSlips.getValueAt(row, column);
                if (readerId != null && !readerId.trim().isEmpty()) {
                    updateTableWithReaderName(row, readerId);
                }
            } else if (column == 1) { // Cột Book Title
                String titleKeyword = (String) tblBorrowSlips.getValueAt(row, column);
                if (titleKeyword != null && titleKeyword.length() > 1) {
                    updateSuggestionListMulti(titleKeyword, row);
                }
            }
        });

        // Tạo danh sách gợi ý khi nhập tiêu đề sách
        listModelMulti = new DefaultListModel<>();
        bookListMulti = new JList<>(listModelMulti);
        bookListMulti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        bookListMulti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Nhấn đúp chuột để chọn tiêu đề sách
                    int row = tblBorrowSlips.getSelectedRow();
                    int index = bookListMulti.getSelectedIndex();
                    if (index != -1 && row != -1) {
                        String selectedTitle = bookListMulti.getSelectedValue();
                        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(selectedTitle);
                        if (!results.isEmpty()) {
                            String isbn = results.get(0)[0];
                            tblBorrowSlips.setValueAt(isbn, row, 0);
                            tblBorrowSlips.setValueAt(selectedTitle, row, 1);
                            suggestionPopupMulti.setVisible(false);
                        }
                    }
                }
            }
        });

        suggestionPopupMulti = new JPopupMenu();
        suggestionPopupMulti.add(new JScrollPane(bookListMulti));
    }

    /* Cập nhật danh sách gợi ý cho tiêu đề sách khi nhập vào bảng Borrow Slips.  
     * @param keyword Từ khóa tìm kiếm
     */
    private void updateSuggestionListMulti(String keyword, int row) {
        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(keyword);
        SwingUtilities.invokeLater(() -> {
            String isbn = (String) tblBorrowSlips.getValueAt(row, 0); // Lấy ISBN của hàng hiện tại

            // Nếu ISBN đã có, không cần hiển thị gợi ý
            if (isbn != null && !isbn.trim().isEmpty()) {
                suggestionPopupMulti.setVisible(false);
                return;
            }

            listModelMulti.clear();
            for (String[] book : results) {
                listModelMulti.addElement(book[1]);
            }

            // Hiển thị popup gợi ý nếu có kết quả tìm kiếm
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

    /**
     * - Lưu nhiều phiếu vào CSDL - Bỏ qua các hàng trống - Kiểm tra các cột bắt
     * buộc (ISBN, Reader ID, Borrow Date) trước khi lưu
     */
    private void saveMultipleSlips() {
        int rowCount = tblBorrowSlips.getRowCount();
        boolean hasError = false;
        List<String[]> bookDetails = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String isbn = (String) tblBorrowSlips.getValueAt(i, 0);
            String title = (String) tblBorrowSlips.getValueAt(i, 1);
            String readerIdText = (String) tblBorrowSlips.getValueAt(i, 2);
            String borrowDate = (String) tblBorrowSlips.getValueAt(i, 4);

            // Kiểm tra hàng trống
            if (isbn.isEmpty() && readerIdText.isEmpty() && borrowDate.isEmpty()) {
                continue;
            }

            // Kiểm tra dữ liệu bắt buộc
            if (isbn.isEmpty() || readerIdText.isEmpty() || borrowDate.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Dòng " + (i + 1) + " có dữ liệu trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int readerId = Integer.parseInt(readerIdText);
                bookDetails.add(new String[]{isbn, title});
                BorrowSlipDAO.insertBorrowSlip(readerId, borrowDate, bookDetails);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID độc giả phải là số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        JOptionPane.showMessageDialog(null, "Saved!");
    }

    //Thêm hàng cho bảng
    private void addNewRowToTable() {
        DefaultTableModel tableModel = (DefaultTableModel) tblBorrowSlips.getModel();
        tableModel.addRow(new Object[]{"", "", "", "", ""});
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    FIND SLIPS FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    private void populateYearComboBox() {
        int currentYear = Year.now().getValue();

        cmbYear.addItem("All years");

        for (int year = currentYear; year >= 2000; year--) {
            cmbYear.addItem(String.valueOf(year));
        }
    }

    private void searchBorrowInfor() {
        String keyword = txtSearch.getText().trim();
        String yearFilter = cmbYear.getSelectedItem().toString();
        String monthFilter = cmbMonth.getSelectedItem().toString();
        String searchType = cmbCriteria.getSelectedItem().toString();

        String query = "SELECT b.isbn, b.title, r.id AS readerID, r.name AS readerName, "
                + "bs.borrow_date, bs.due_date, bs.return_date, bd.status FROM borrow_details bd "
                + "JOIN borrow_slips bs ON bd.borrow_id = bs.id JOIN books b ON bd.isbn = b.isbn "
                + "JOIN readers r ON bs.reader_id = r.id WHERE 1=1";

        List<Object> params = new ArrayList<>();

        // Thêm điều kiện tìm kiếm nếu có từ khóa
        if (!keyword.isEmpty()) {
            switch (searchType) {
                case "ISBN":
                    query += " AND b.isbn LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "Title":
                    query += " AND b.title LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "ReaderID":
                    query += " AND CAST(r.id AS CHAR) LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "Reader Name":
                    query += " AND r.name LIKE ?";
                    params.add("%" + keyword + "%");
                    break;
                case "All":
                    query += " AND (b.isbn LIKE ? OR b.title LIKE ? OR CAST(r.id AS CHAR) LIKE ? OR r.name LIKE ?)";
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    params.add("%" + keyword + "%");
                    break;
            }
        }

        if (!yearFilter.equals("All years")) {
            query += " AND YEAR(bs.borrow_date) = ? ";
            try {
                int year = Integer.parseInt(yearFilter);
                params.add(year);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Năm không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Thêm điều kiện lọc thể loại nếu không phải là "All month"
        if (!monthFilter.equals("All months")) {
            query += " AND MONTH(bs.borrow_date) = ? ";
            try {
                int month = Integer.parseInt(monthFilter);
                params.add(month);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "T không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
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
                    rs.getString("readerID"),
                    rs.getString("readerName"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                };
                model.addRow(row);
            }

            rs.close();
            pst.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "ERROR: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private String getValue(DefaultTableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value != null ? value.toString() : "";
    }

    private void setFieldsNonEditable() {
        txtBorrowedDate.setEditable(false);
        txtTitle1.setEditable(false);
        txtISBN1.setEditable(false);
        txtReaderID1.setEditable(false);
        txtReaderName1.setEditable(false);
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   BOOK RETURN FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
// Hiển thị dialog chọn phiếu mượn chưa trả hết
    private void showBorrowSlipSelectionDialog(List<Map<String, Object>> slips) {
        JDialog dialog = new JDialog((Frame) null, "List of unprocessed book borrowing forms", true);
        dialog.setSize(800, 400);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(245, 245, 245));
        dialog.getContentPane().setForeground(Color.DARK_GRAY);
        dialog.setLocation(350, 175);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Borrow Date", "Due Date"}, 0);
        JTable table = new JTable(model);

        // Duyệt qua danh sách phiếu mượn và thêm vào bảng
        for (Map<String, Object> slip : slips) {
            model.addRow(new Object[]{
                slip.get("id"),
                slip.get("borrow_date"),
                slip.get("due_date")
            });
        }

        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setBackground(Color.WHITE);
        table.setForeground(Color.DARK_GRAY);
        table.setSelectionBackground(new Color(135, 206, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(230, 230, 230));
        header.setForeground(Color.BLACK);

        table.setEnabled(true);
        table.setFocusable(true);

        // Xử lý sự kiện khi người dùng click vào một dòng trong bảng
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JTable sourceTable = (JTable) e.getSource();
                int row = sourceTable.rowAtPoint(e.getPoint());

                if (row != -1) {
                    sourceTable.setRowSelectionInterval(row, row);

                    if (e.getClickCount() == 2) {
                        int slipId = (int) sourceTable.getValueAt(row, 0);
                        selectedSlipId = slipId;

                        Date borrowDate = (Date) sourceTable.getValueAt(row, 1);
                        Date dueDate = (Date) sourceTable.getValueAt(row, 2);
                        txtRETURN_DATE.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                        txtBORROW_DATE.setText(borrowDate.toString());
                        txtDUE_DATE.setText(dueDate.toString());

                        loadBorrowDetails(slipId);
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

// Load chi tiết các sách trong phiếu mượn
    private void loadBorrowDetails(int slipId) {
        List<Map<String, Object>> details = BorrowSlipDAO.getBorrowDetailsBySlipId(slipId);

        // Tạo một Map để lưu status ban đầu từ CSDL
        Map<String, String> initialStatusMap = new HashMap<>();
        for (Map<String, Object> row : details) {
            initialStatusMap.put((String) row.get("isbn"), (String) row.get("status"));
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ISBN", "Title", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String isbn = (String) getValueAt(row, 0);
                String initialStatus = initialStatusMap.get(isbn);
                // Chỉ khóa chỉnh sửa nếu status ban đầu từ CSDL là "Returned"
                return column == 2 && !"Returned".equals(initialStatus);
            }
        };

        for (Map<String, Object> row : details) {
            model.addRow(new Object[]{
                row.get("isbn"),
                row.get("title"),
                row.get("status")
            });
        }

        tblBOOK_BORROW_LIST.setModel(model);

        // Gán comboBox cho cột Status
        String[] statusOptions = {"Borrowed", "Returned", "Lost"};
        JComboBox<String> comboBox = new JComboBox<>(statusOptions);
        TableColumn statusColumn = tblBOOK_BORROW_LIST.getColumnModel().getColumn(2);
        statusColumn.setCellEditor(new DefaultCellEditor(comboBox));

        // Renderer đổi màu chỉ khi status ban đầu từ CSDL là "Returned"
        tblBOOK_BORROW_LIST.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String isbn = (String) table.getModel().getValueAt(row, 0);
                String initialStatus = initialStatusMap.get(isbn);

                // Chỉ set màu đỏ nếu status ban đầu từ CSDL là "Returned"
                if ("Returned".equals(initialStatus)) {
                    c.setBackground(new Color(255, 102, 102));
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                }

                return c;
            }
        });
    }

// Lưu phiếu trả sách và cập nhật trạng thái các sách
    private void btnSaveReturnActionPerformed() {
        int slipId = selectedSlipId;
        if (selectedSlipId == -1) {
            JOptionPane.showMessageDialog(this, "You have not selected a borrow slip to return.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblBOOK_BORROW_LIST.getModel();
        boolean hasChanges = false;
        boolean allReturnedOrLost = true;

        // Cập nhật trạng thái sách
        for (int i = 0; i < model.getRowCount(); i++) {
            String isbn = model.getValueAt(i, 0).toString();
            String status = model.getValueAt(i, 2).toString();

            String oldStatus = BorrowSlipDAO.getBookStatus(slipId, isbn);
            if (!status.equals(oldStatus)) {
                hasChanges = true;
                BorrowSlipDAO.updateBookStatus(slipId, isbn, status);
            }

            if (!"Returned".equals(status) && !"Lost".equals(status)) {
                allReturnedOrLost = false;
            }
        }

        // Xử lý ngày trả và phạt
        if (hasChanges || allReturnedOrLost) {
            try {
                String returnDateStr = txtRETURN_DATE.getText().trim();
                java.sql.Date returnDate = java.sql.Date.valueOf(returnDateStr);

                // Cập nhật ngày trả và tính phạt trong DAO
                BorrowSlipDAO.updateReturnDate(slipId, returnDate);
                BorrowSlipDAO.processPenalties(slipId, returnDate);

                JOptionPane.showMessageDialog(null, "Return slip saved successfully!");
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Invalid date format! Please use YYYY-MM-DD.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No changes to save.");
        }

        checkForLostBooksAndWarn();
    }

// Kiểm tra và cảnh báo phatj nếu có sách bị mất
    private void checkForLostBooksAndWarn() {
        boolean hasLostBook = false;
        boolean isOverdue = false;
        DefaultTableModel model = (DefaultTableModel) tblBOOK_BORROW_LIST.getModel();

        // Kiểm tra sách bị mất
        for (int i = 0; i < model.getRowCount(); i++) {
            String status = (String) model.getValueAt(i, 2);
            if ("Lost".equalsIgnoreCase(status)) {
                hasLostBook = true;
                break;
            }
        }

        // Kiểm tra trả muộn
        if (selectedSlipId != -1) {
            Map<String, Object> slip = BorrowSlipDAO.getBorrowSlipById(selectedSlipId);
            if (slip != null) {
                Date dueDate = (Date) slip.get("due_date"); // java.util.Date từ DAO
                String returnDateStr = txtRETURN_DATE.getText().trim();

                if (!returnDateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date returnDate = sdf.parse(returnDateStr); // java.util.Date
                        long overdueDays = calculateOverdueDays(dueDate, returnDate);
                        if (overdueDays > 0) {
                            isOverdue = true;
                        }
                    } catch (Exception e) {
                        // Bỏ qua nếu định dạng ngày không hợp lệ
                    }
                }
            }
        }

        // Hiển thị cảnh báo
        if (hasLostBook || isOverdue) {
            jLabel_Warning.setVisible(true);
            String warningText = "";
            if (hasLostBook && isOverdue) {
                warningText = "Warning: There are lost books and the return is overdue!";
            } else if (hasLostBook) {
                warningText = "Warning: There are lost books!";
            } else if (isOverdue) {
                warningText = "Warning: The return is overdue!";
            }
            jLabel_Warning.setText(warningText);
        } else {
            jLabel_Warning.setVisible(false);
            jLabel_Warning.setText("");
        }
    }

// Hàm tính số ngày quá hạn với java.util.Date
    private long calculateOverdueDays(Date dueDate, Date returnDate) {
        long diffInMillies = returnDate.getTime() - dueDate.getTime();
        long days = diffInMillies / (1000 * 60 * 60 * 24); // Chuyển từ milliseconds sang ngày
        return Math.max(days, 0); // Nếu trả sớm, không phạt
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        ReturnBookPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblBOOK_BORROW_LIST = new javax.swing.JTable();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        txtReaderID2 = new app.bolivia.swing.JCTextField();
        jLabel31 = new javax.swing.JLabel();
        lblReaderName2 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        txtDUE_DATE = new app.bolivia.swing.JCTextField();
        txtBORROW_DATE = new app.bolivia.swing.JCTextField();
        txtRETURN_DATE = new app.bolivia.swing.JCTextField();
        jLabel37 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        btnSAVE_RETURN_FORM = new rojeru_san.complementos.RSButtonHover();
        btnView = new rojerusan.RSButtonHover();
        btnCLEAR = new rojeru_san.complementos.RSButtonHover();
        jLabel_Warning = new javax.swing.JLabel();
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
        btnReturn = new rojeru_san.complementos.RSButtonHover();
        searchAreaPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        txtSearch = new app.bolivia.swing.JCTextField();
        rSButtonHover1 = new rojerusan.RSButtonHover();
        cmbYear = new javax.swing.JComboBox<>();
        jLabel26 = new javax.swing.JLabel();
        cmbCriteria = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        txtISBN1 = new javax.swing.JTextField();
        txtTitle1 = new javax.swing.JTextField();
        txtReaderName1 = new javax.swing.JTextField();
        txtBorrowedDate = new javax.swing.JTextField();
        txtReturnDate = new javax.swing.JTextField();
        txtStatus = new javax.swing.JTextField();
        btnEdit = new rojerusan.RSButtonHover();
        btnDelete = new rojerusan.RSButtonHover();
        jLabel16 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtReaderID1 = new javax.swing.JTextField();
        txtDueDate = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24X24add-1_icon-icons.com_65127.png"))); // NOI18N
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
        btnSaveMultiple.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24X24save_78935.png"))); // NOI18N
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

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/stamp32.png"))); // NOI18N
        jPanel9.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, -1, -1));

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("BOOKS BORROWING FORM");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 10, -1, -1));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/library11.png"))); // NOI18N
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
        tblBorrowSlips.setSelectionForeground(new java.awt.Color(0, 0, 0));
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

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/stamp.png"))); // NOI18N
        singleSlipPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        addAreaPanel.add(singleSlipPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        getContentPane().add(addAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        ReturnBookPanel.setBackground(new java.awt.Color(255, 255, 255));
        ReturnBookPanel.setBorder(new javax.swing.border.MatteBorder(null));
        ReturnBookPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblBOOK_BORROW_LIST.setBackground(new java.awt.Color(255, 255, 255));
        tblBOOK_BORROW_LIST.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tblBOOK_BORROW_LIST.setForeground(new java.awt.Color(0, 0, 0));
        tblBOOK_BORROW_LIST.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ISBN", "Title", "Status"
            }
        ));
        tblBOOK_BORROW_LIST.setGridColor(new java.awt.Color(0, 51, 51));
        tblBOOK_BORROW_LIST.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tblBOOK_BORROW_LIST.setPreferredSize(new java.awt.Dimension(800, 2000));
        tblBOOK_BORROW_LIST.setRowHeight(30);
        tblBOOK_BORROW_LIST.setSelectionBackground(new java.awt.Color(255, 204, 255));
        tblBOOK_BORROW_LIST.setSelectionForeground(new java.awt.Color(0, 0, 0));
        tblBOOK_BORROW_LIST.setShowGrid(true);
        tblBOOK_BORROW_LIST.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tblBOOK_BORROW_LISTFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tblBOOK_BORROW_LISTFocusLost(evt);
            }
        });
        tblBOOK_BORROW_LIST.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBOOK_BORROW_LISTMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblBOOK_BORROW_LIST);

        ReturnBookPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 270, 800, 350));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(0, 0, 0));
        jLabel29.setText("BOOK RETURN FORM");
        jLabel29.setToolTipText("");
        ReturnBookPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 390, -1));

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setText("Reader's ID:");
        ReturnBookPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        txtReaderID2.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderID2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtReaderID2.setForeground(new java.awt.Color(0, 0, 0));
        txtReaderID2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID2.setToolTipText("");
        txtReaderID2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtReaderID2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderID2ActionPerformed(evt);
            }
        });
        ReturnBookPanel.add(txtReaderID2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 170, 310, 40));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(0, 0, 0));
        jLabel31.setText("Reader's name:");
        ReturnBookPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, -1));

        lblReaderName2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblReaderName2.setForeground(new java.awt.Color(0, 0, 0));
        lblReaderName2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReaderName2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        ReturnBookPanel.add(lblReaderName2, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 280, 40));

        jPanel12.setBackground(new java.awt.Color(102, 153, 255));
        jPanel12.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ReturnBookPanel.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(0, 0, 0)));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(0, 0, 0));
        jLabel34.setText("Return date: ");
        jPanel13.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(0, 0, 0));
        jLabel35.setText("Due date: ");
        jPanel13.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(0, 0, 0));
        jLabel36.setText("Date borrow: ");
        jPanel13.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        txtDUE_DATE.setEditable(false);
        txtDUE_DATE.setBackground(new java.awt.Color(255, 255, 255));
        txtDUE_DATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtDUE_DATE.setForeground(new java.awt.Color(0, 0, 0));
        txtDUE_DATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDUE_DATE.setToolTipText("");
        txtDUE_DATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtDUE_DATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDUE_DATEActionPerformed(evt);
            }
        });
        jPanel13.add(txtDUE_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 90, 300, 50));

        txtBORROW_DATE.setEditable(false);
        txtBORROW_DATE.setBackground(new java.awt.Color(255, 255, 255));
        txtBORROW_DATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtBORROW_DATE.setForeground(new java.awt.Color(0, 0, 0));
        txtBORROW_DATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBORROW_DATE.setToolTipText("");
        txtBORROW_DATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtBORROW_DATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBORROW_DATEActionPerformed(evt);
            }
        });
        jPanel13.add(txtBORROW_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, 300, 50));

        txtRETURN_DATE.setBackground(new java.awt.Color(255, 255, 255));
        txtRETURN_DATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtRETURN_DATE.setForeground(new java.awt.Color(0, 0, 0));
        txtRETURN_DATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtRETURN_DATE.setToolTipText("");
        txtRETURN_DATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtRETURN_DATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRETURN_DATEActionPerformed(evt);
            }
        });
        jPanel13.add(txtRETURN_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 170, 300, 50));

        ReturnBookPanel.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 570, 230));

        jLabel37.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(0, 0, 0));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("List of borrowed books ");
        ReturnBookPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 230, 800, -1));

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(0, 51, 51)));
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSAVE_RETURN_FORM.setBackground(new java.awt.Color(102, 153, 255));
        btnSAVE_RETURN_FORM.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        btnSAVE_RETURN_FORM.setText("SAVE");
        btnSAVE_RETURN_FORM.setColorHover(new java.awt.Color(255, 0, 51));
        btnSAVE_RETURN_FORM.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnSAVE_RETURN_FORM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSAVE_RETURN_FORMActionPerformed(evt);
            }
        });
        jPanel14.add(btnSAVE_RETURN_FORM, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 20, 140, 50));

        btnView.setBackground(new java.awt.Color(255, 255, 255));
        btnView.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(255, 255, 255)));
        btnView.setForeground(new java.awt.Color(0, 0, 0));
        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/search.png"))); // NOI18N
        btnView.setColorHover(new java.awt.Color(255, 255, 255));
        btnView.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        jPanel14.add(btnView, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 40, 50));

        btnCLEAR.setBackground(new java.awt.Color(102, 153, 255));
        btnCLEAR.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        btnCLEAR.setText("CLEAR ALL");
        btnCLEAR.setColorHover(new java.awt.Color(255, 0, 51));
        btnCLEAR.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnCLEAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCLEARActionPerformed(evt);
            }
        });
        jPanel14.add(btnCLEAR, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, 140, 50));

        ReturnBookPanel.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1260, 85));

        jLabel_Warning.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel_Warning.setForeground(new java.awt.Color(0, 0, 0));
        jLabel_Warning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/alert.png"))); // NOI18N
        ReturnBookPanel.add(jLabel_Warning, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 640, -1, -1));

        getContentPane().add(ReturnBookPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 7, 3, 35));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MANAGE BOOK BORROWING");
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
        jPanel4.add(searchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 180, -1));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/contact-form.png"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, -1, -1));

        jPanel5.setBackground(new java.awt.Color(102, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("FEATURES");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 0, -1, 50));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, -1));

        btnReturn.setBackground(new java.awt.Color(0, 51, 51));
        btnReturn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        btnReturn.setText("Return Book Slips");
        btnReturn.setColorHover(new java.awt.Color(102, 153, 255));
        btnReturn.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnReturn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnReturnMouseClicked(evt);
            }
        });
        btnReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnActionPerformed(evt);
            }
        });
        jPanel4.add(btnReturn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        searchAreaPanel.setBackground(new java.awt.Color(0, 51, 51));
        searchAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        rSButtonHover1.setBackground(new java.awt.Color(255, 255, 255));
        rSButtonHover1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 0, new java.awt.Color(0, 0, 0)));
        rSButtonHover1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24x24_searcher_magnifyng_glass_search_locate_find_icon_123813.png"))); // NOI18N
        rSButtonHover1.setColorHover(new java.awt.Color(51, 255, 0));
        rSButtonHover1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSButtonHover1ActionPerformed(evt);
            }
        });
        jPanel11.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 50, 40));

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
        jLabel26.setText("BORROWED YEAR");
        jPanel11.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 10, -1, -1));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbCriteria.setForeground(new java.awt.Color(0, 0, 0));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "ReaderID", "Reader Name" }));
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
        cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All months", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        cmbMonth.setToolTipText("");
        cmbMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMonthActionPerformed(evt);
            }
        });
        jPanel11.add(cmbMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 40, 200, 40));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(0, 0, 0));
        jLabel25.setText("BORROWED MONTH");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 10, -1, -1));

        searchAreaPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 100));

        tblBooks.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblBooks.setForeground(new java.awt.Color(0, 0, 0));
        tblBooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ISBN", "Title", "Reader ID", "Reader Name", "Borrowed date", "Due date", "Return date", "Status"
            }
        ));
        tblBooks.setGridColor(new java.awt.Color(0, 51, 51));
        tblBooks.setPreferredSize(new java.awt.Dimension(600, 8000));
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

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtISBN1.setEditable(false);
        txtISBN1.setBackground(new java.awt.Color(255, 255, 255));
        txtISBN1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtISBN1.setForeground(new java.awt.Color(0, 0, 0));
        txtISBN1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtISBN1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        jPanel7.add(txtISBN1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 40, 160, -1));

        txtTitle1.setEditable(false);
        txtTitle1.setBackground(new java.awt.Color(255, 255, 255));
        txtTitle1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTitle1.setForeground(new java.awt.Color(0, 0, 0));
        txtTitle1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTitle1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtTitle1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTitle1ActionPerformed(evt);
            }
        });
        jPanel7.add(txtTitle1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 200, -1));

        txtReaderName1.setEditable(false);
        txtReaderName1.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderName1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtReaderName1.setForeground(new java.awt.Color(0, 0, 0));
        txtReaderName1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderName1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtReaderName1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderName1ActionPerformed(evt);
            }
        });
        jPanel7.add(txtReaderName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, 200, -1));

        txtBorrowedDate.setEditable(false);
        txtBorrowedDate.setBackground(new java.awt.Color(255, 255, 255));
        txtBorrowedDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBorrowedDate.setForeground(new java.awt.Color(0, 0, 0));
        txtBorrowedDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBorrowedDate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtBorrowedDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBorrowedDateActionPerformed(evt);
            }
        });
        jPanel7.add(txtBorrowedDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 40, 160, -1));

        txtReturnDate.setEditable(false);
        txtReturnDate.setBackground(new java.awt.Color(255, 255, 255));
        txtReturnDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtReturnDate.setForeground(new java.awt.Color(0, 0, 0));
        txtReturnDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReturnDate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtReturnDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReturnDateActionPerformed(evt);
            }
        });
        jPanel7.add(txtReturnDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 110, 160, -1));

        txtStatus.setEditable(false);
        txtStatus.setBackground(new java.awt.Color(255, 255, 255));
        txtStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtStatus.setForeground(new java.awt.Color(0, 0, 0));
        txtStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtStatus.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStatusActionPerformed(evt);
            }
        });
        jPanel7.add(txtStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 40, 150, -1));

        btnEdit.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24x24 edit.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel7.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 20, 130, -1));

        btnDelete.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(102, 255, 0)));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/18x25trash.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel7.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 90, 130, -1));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/check-form_116472.png"))); // NOI18N
        jPanel7.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        jPanel8.setBackground(new java.awt.Color(0, 51, 51));
        jPanel7.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 3, 130));

        jPanel10.setBackground(new java.awt.Color(0, 51, 51));
        jPanel7.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 10, 3, 130));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("ISBN");
        jPanel7.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 20, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Title:");
        jPanel7.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, -1, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Reader Name:");
        jPanel7.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, -1, -1));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("Borrow date:");
        jPanel7.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, -1, -1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 0, 51));
        jLabel21.setText("Due date:");
        jPanel7.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 90, -1, -1));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("ReaderID:");
        jPanel7.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, -1, -1));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("Status:");
        jPanel7.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, -1, -1));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("Return Date:");
        jPanel7.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 90, -1, -1));

        txtReaderID1.setEditable(false);
        txtReaderID1.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderID1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtReaderID1.setForeground(new java.awt.Color(0, 0, 0));
        txtReaderID1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        jPanel7.add(txtReaderID1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 160, 30));

        txtDueDate.setBackground(new java.awt.Color(255, 255, 255));
        txtDueDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDueDate.setForeground(new java.awt.Color(0, 0, 0));
        txtDueDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDueDate.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(102, 153, 255)));
        jPanel7.add(txtDueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 110, 160, -1));

        searchAreaPanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 1280, 150));

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        jLabel28.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
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
        ReturnBookPanel.setVisible(false);
    }//GEN-LAST:event_btnAddActionPerformed

    private void searchPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPanelMouseClicked
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(true);
    }//GEN-LAST:event_searchPanelMouseClicked

    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(true);
        ReturnBookPanel.setVisible(false);
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

    private void cmbMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbMonthActionPerformed

    private void txtTitle1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTitle1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTitle1ActionPerformed

    private void txtReaderName1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderName1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtReaderName1ActionPerformed

    private void txtBorrowedDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBorrowedDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBorrowedDateActionPerformed

    private void txtReturnDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReturnDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtReturnDateActionPerformed

    private void txtStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStatusActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed

        int selectedRow = tblBooks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để chỉnh sửa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();

        BorrowSlipDAO dao = new BorrowSlipDAO();

        int readerId = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
        String borrowDate = model.getValueAt(selectedRow, 4).toString();
        Integer borrowId = dao.getBorrowId(readerId, borrowDate);

        String oldIsbn = getValue(model, selectedRow, 0);
        String newIsbn = txtISBN1.getText().trim();
        String new_DueDate = txtDueDate.getText();
        String new_ReturnDate = txtReturnDate.getText();
        String new_Status = txtStatus.getText();

        try {
            Date utilDueDate = new SimpleDateFormat("yyyy-MM-dd").parse(new_DueDate);
            Date utilReturnDate = null;
            if (!new_ReturnDate.isEmpty()) {
                utilReturnDate = new SimpleDateFormat("yyyy-MM-dd").parse(new_ReturnDate);
            }

            java.sql.Date dueDate = new java.sql.Date(utilDueDate.getTime());
            java.sql.Date returnDate = (utilReturnDate != null) ? new java.sql.Date(utilReturnDate.getTime()) : null;

            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn cập nhật?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean isUpdated = dao.updateBorrowRecord(borrowId, oldIsbn, newIsbn, readerId, dueDate, returnDate, new_Status);

                if (isUpdated) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    // Update the table model
                    model.setValueAt(newIsbn, selectedRow, 0);
                    model.setValueAt(txtTitle1.getText(), selectedRow, 1);
                    model.setValueAt(txtReaderID1.getText(), selectedRow, 2);
                    model.setValueAt(txtReaderName1.getText(), selectedRow, 3);
                    model.setValueAt(txtBorrowedDate.getText(), selectedRow, 4);
                    model.setValueAt(txtDueDate.getText(), selectedRow, 5);
                    model.setValueAt(txtReturnDate.getText(), selectedRow, 6);
                    model.setValueAt(txtStatus.getText(), selectedRow, 7);
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblBooks.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hàng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();

        String isbn = model.getValueAt(selectedRow, 0).toString();
        int readerId = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
        String borrowDate = model.getValueAt(selectedRow, 4).toString();

        BorrowSlipDAO dao = new BorrowSlipDAO();
        Integer borrowId = dao.getBorrowId(readerId, borrowDate);

        if (borrowId == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn tương ứng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isDeleted = dao.deleteBorrowRecord(borrowId, isbn);
            if (isDeleted) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tblBooksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBooksMouseClicked
        int selectedRow = tblBooks.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();

        txtISBN1.setText(getValue(model, selectedRow, 0));
        txtTitle1.setText(getValue(model, selectedRow, 1));
        txtReaderID1.setText(getValue(model, selectedRow, 2));
        txtReaderName1.setText(getValue(model, selectedRow, 3));
        txtBorrowedDate.setText(getValue(model, selectedRow, 4));
        txtDueDate.setText(getValue(model, selectedRow, 5));
        txtReturnDate.setText(getValue(model, selectedRow, 6));
        txtStatus.setText(getValue(model, selectedRow, 7));
    }//GEN-LAST:event_tblBooksMouseClicked

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void rSButtonHover1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rSButtonHover1ActionPerformed
        searchBorrowInfor();
    }//GEN-LAST:event_rSButtonHover1ActionPerformed

    private void cmbYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbYearActionPerformed

    private void cmbCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCriteriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCriteriaActionPerformed

    private void btnReturnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReturnMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnReturnMouseClicked

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(false);
        ReturnBookPanel.setVisible(true);
    }//GEN-LAST:event_btnReturnActionPerformed

    private void txtReaderID2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderID2ActionPerformed
        String readerIDText = txtReaderID2.getText().trim();
        if (!readerIDText.isEmpty()) {
            try {
                int readerID = Integer.parseInt(readerIDText);
                String readerName = ReaderDAO.getReaderNameByID(readerID);
                lblReaderName2.setText(readerName.isEmpty() ? "Không tìm thấy độc giả" : readerName);
            } catch (NumberFormatException ex) {
                lblReaderName2.setText("Reader ID không hợp lệ");
            }
        }
    }//GEN-LAST:event_txtReaderID2ActionPerformed

    private void txtDUE_DATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDUE_DATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDUE_DATEActionPerformed

    private void btnSAVE_RETURN_FORMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSAVE_RETURN_FORMActionPerformed
        btnSaveReturnActionPerformed();
    }//GEN-LAST:event_btnSAVE_RETURN_FORMActionPerformed

    private void txtBORROW_DATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBORROW_DATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBORROW_DATEActionPerformed

    private void txtRETURN_DATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRETURN_DATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRETURN_DATEActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        String readerIdText = txtReaderID2.getText().trim();
        if (!readerIdText.isEmpty()) {
            int readerId = Integer.parseInt(readerIdText);
            List<Map<String, Object>> slips = BorrowSlipDAO.getUnreturnedBorrowSlips(readerId);
            if (!slips.isEmpty()) {
                showBorrowSlipSelectionDialog(slips);
            } else {
                JOptionPane.showMessageDialog(null, "No active borrow slips found for this reader.");
            }
        }
    }//GEN-LAST:event_btnViewActionPerformed

    private void tblBOOK_BORROW_LISTFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblBOOK_BORROW_LISTFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBOOK_BORROW_LISTFocusGained

    private void tblBOOK_BORROW_LISTFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblBOOK_BORROW_LISTFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBOOK_BORROW_LISTFocusLost

    private void tblBOOK_BORROW_LISTMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBOOK_BORROW_LISTMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblBOOK_BORROW_LISTMouseClicked

    private void btnCLEARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCLEARActionPerformed
        txtReaderID2.setText("");
        txtBORROW_DATE.setText("");
        txtDUE_DATE.setText("");
        txtRETURN_DATE.setText("");
        lblReaderName2.setText("");
        jLabel_Warning.setVisible(false);
        selectedSlipId = -1;
        // Xóa dữ liệu trong bảng danh sách sách mượn
        DefaultTableModel model = (DefaultTableModel) tblBOOK_BORROW_LIST.getModel();
        model.setRowCount(0);
        TableColumn statusColumn = tblBOOK_BORROW_LIST.getColumnModel().getColumn(2);
        statusColumn.setCellEditor(null);
    }//GEN-LAST:event_btnCLEARActionPerformed

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
    private JTextField activeTitleField;
    private Timer searchTimer;
    private DefaultListModel<String> listModel;
    private JList<String> bookList;
    private JPopupMenu suggestionPopup;
    private JTable borrowDetailsTable;
    private JPopupMenu suggestionPopupMulti;
    private JList<String> bookListMulti;
    private DefaultListModel<String> listModelMulti;
    private int selectedSlipId = -1;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ReturnBookPanel;
    private javax.swing.JPanel addAreaPanel;
    private rojeru_san.complementos.RSButtonHover btnAdd;
    private rojeru_san.complementos.RSButtonHover btnAddRow;
    private rojeru_san.complementos.RSButtonHover btnAddRow1;
    private rojerusan.RSButtonHover btnBack;
    private rojeru_san.complementos.RSButtonHover btnCLEAR;
    private rojerusan.RSButtonHover btnDelete;
    private rojerusan.RSButtonHover btnEdit;
    private rojerusan.RSButtonHover btnMultiSlip;
    private rojeru_san.complementos.RSButtonHover btnReturn;
    private rojeru_san.complementos.RSButtonHover btnSAVE_RETURN_FORM;
    private rojeru_san.complementos.RSButtonHover btnSaveMultiple;
    private rojeru_san.complementos.RSButtonHover btnSingleSave;
    private rojeru_san.complementos.RSButtonHover btnSingleSlip;
    private rojerusan.RSButtonHover btnView;
    private javax.swing.JComboBox<String> cmbCriteria;
    private javax.swing.JComboBox<String> cmbMonth;
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
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_Warning;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
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
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblBookTitle;
    private javax.swing.JLabel lblReaderName;
    private javax.swing.JLabel lblReaderName2;
    private javax.swing.JPanel multiSlipPanel;
    private rojerusan.RSButtonHover rSButtonHover1;
    private javax.swing.JPanel searchAreaPanel;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private javax.swing.JPanel singleSlipPanel;
    private javax.swing.JTable tblBOOK_BORROW_LIST;
    private javax.swing.JTable tblBooks;
    private javax.swing.JTable tblBorrowSlips;
    private app.bolivia.swing.JCTextField txtBORROW_DATE;
    private javax.swing.JTextField txtBorrowedDate;
    private app.bolivia.swing.JCTextField txtDATE;
    private app.bolivia.swing.JCTextField txtDUE_DATE;
    private javax.swing.JTextField txtDueDate;
    private app.bolivia.swing.JCTextField txtISBN;
    private javax.swing.JTextField txtISBN1;
    private app.bolivia.swing.JCTextField txtRETURN_DATE;
    private app.bolivia.swing.JCTextField txtReaderID;
    private javax.swing.JTextField txtReaderID1;
    private app.bolivia.swing.JCTextField txtReaderID2;
    private javax.swing.JTextField txtReaderName1;
    private javax.swing.JTextField txtReturnDate;
    private app.bolivia.swing.JCTextField txtSearch;
    private javax.swing.JTextField txtStatus;
    private app.bolivia.swing.JCTextField txtTitle;
    private javax.swing.JTextField txtTitle1;
    // End of variables declaration//GEN-END:variables
}
