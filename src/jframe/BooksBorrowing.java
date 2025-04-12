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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import DAO.*;
import UI_Helper.RoundedPanel;

import java.util.Map;

import java.math.BigDecimal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

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
        addAreaPanel.setVisible(false);
        searchAreaPanel.setVisible(false);
        ReturnBookPanel.setVisible(false);
        lblAPPROVED.setVisible(false);
        lblAPPROVED2.setVisible(false);
        initBookTitleSearch();
        initReturnBooks();
        initSearchTableEvents();
        populateYearComboBox();

        setFieldsNonEditable();
        jLabel_Warning.setVisible(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date todayDate = new java.util.Date();
        String today = dateFormat.format(todayDate);
        txtBORROWDATE.setText(today);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(todayDate);
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date dueDate = calendar.getTime();
        String due = dateFormat.format(dueDate);
        txtDUEDATE.setText(due);

    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    SINGLE SLIP FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    // Chức năng tìm kiếm sách theo title để đề xuất danh sách các books phù hợp
    private void initBookTitleSearch() {
        // Khởi tạo danh sách gợi ý
        listModel = new DefaultListModel<>();
        bookList = new JList<>(listModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bookList.setFixedCellHeight(30);
        bookList.setBackground(Color.WHITE);
        bookList.setSelectionBackground(new Color(135, 206, 250));
        bookList.setSelectionForeground(Color.BLACK);

        // Tùy chỉnh hiển thị của danh sách gợi ý
        bookList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                label.setOpaque(true);
                label.setBackground(isSelected ? new Color(0, 120, 215) : Color.WHITE);
                label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
                return label;
            }
        });

        // Tạo popup gợi ý
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        suggestionPopup.add(new JScrollPane(bookList));

        // --- Gán sự kiện cho JTextField ---
        KeyAdapter textFieldKeyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                JTextField source = (JTextField) e.getSource();
                activeTitleField = source;
                activeTableRow = -1;

                String keyword = source.getText().trim();
                if (searchTimer != null) {
                    searchTimer.cancel();
                }

                if (keyword.length() > 1) {
                    searchTimer = new Timer();
                    searchTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> updateSuggestionList(keyword));
                        }
                    }, 350);
                } else {
                    suggestionPopup.setVisible(false);
                }
            }
        };

        // --- Gán sự kiện cho JTable ---
        tblMultiBookBorrow.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int row = tblMultiBookBorrow.getSelectedRow();
                int col = tblMultiBookBorrow.getSelectedColumn();

                if (col == TITLE_COLUMN_INDEX && row != -1) {
                    Object value = tblMultiBookBorrow.getValueAt(row, col);
                    String keyword = (value != null) ? value.toString().trim() : "";

                    activeTitleField = null;
                    activeTableRow = row;

                    if (searchTimer != null) {
                        searchTimer.cancel();
                    }

                    if (keyword.length() > 1) {
                        searchTimer = new Timer();
                        searchTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SwingUtilities.invokeLater(() -> updateSuggestionList(keyword));
                            }
                        }, 350);
                    } else {
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        // --- Xử lý chọn gợi ý từ danh sách ---
        bookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = bookList.getSelectedIndex();
                    if (index != -1) {
                        String selectedTitle = bookList.getSelectedValue();
                        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(selectedTitle);
                        if (!results.isEmpty()) {
                            String selectedISBN = null;
                            for (String[] book : results) {
                                if (book[1].equals(selectedTitle)) {
                                    selectedISBN = book[0]; // ISBN
                                    break;
                                }
                            }

                            if (selectedISBN != null) {
                                if (activeTableRow != -1) {
                                    tblMultiBookBorrow.setValueAt(selectedTitle, activeTableRow, TITLE_COLUMN_INDEX);
                                    tblMultiBookBorrow.setValueAt(selectedISBN, activeTableRow, ISBN_COLUMN_INDEX);
                                }
                                suggestionPopup.setVisible(false);
                            }
                        }

                    }
                }
            }
        });

        // --- Hiệu ứng highlight khi di chuột ---
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

        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };

        for (int i = 0; i < tblMultiBookBorrow.getColumnCount(); i++) {
            tblMultiBookBorrow.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblMultiBookBorrow.getColumnCount(); i++) {
            tblMultiBookBorrow.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void updateSuggestionList(String keyword) {
        List<String[]> results = BorrowSlipDAO.searchBooksByTitle(keyword);
        listModel.clear();

        for (String[] book : results) {
            listModel.addElement(book[1]); // Hiển thị Title
        }

        if (!results.isEmpty()) {
            int popupWidth = 300;
            if (activeTitleField != null) {
                popupWidth = Math.max(activeTitleField.getWidth(), popupWidth);
                suggestionPopup.setPopupSize(new Dimension(popupWidth, 150));
                suggestionPopup.show(activeTitleField, 0, activeTitleField.getHeight());
            } else if (tblMultiBookBorrow != null && activeTableRow != -1) {
                Rectangle cellRect = tblMultiBookBorrow.getCellRect(activeTableRow, TITLE_COLUMN_INDEX, true);
                popupWidth = Math.max(cellRect.width, popupWidth);
                suggestionPopup.setPopupSize(new Dimension(popupWidth, 150));
                suggestionPopup.show(tblMultiBookBorrow, cellRect.x, cellRect.y + cellRect.height);
            }
        } else {
            suggestionPopup.setVisible(false);
        }
    }


    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    FIND SLIPS FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    private static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setText("View Books");
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setBackground(new Color(255, 255, 255));
            setForeground(Color.BLACK);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(new Color(255, 255, 255));
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private JButton button;
        private List<Map<String, String>> books;
        private JPopupMenu popupMenu;
        private JTable booksTable;
        private JScrollPane scrollPane;

        public ButtonEditor() {
            button = new JButton("View Books");
            button.setFont(new Font("Segoe UI", Font.BOLD, 16));
            button.setBackground(new Color(255, 255, 255));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);

            // Khởi tạo JPopupMenu và JTable
            popupMenu = new JPopupMenu();
            booksTable = new JTable();
            scrollPane = new JScrollPane(booksTable);

            // Khi nhấn nút, hiển thị popup
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateBooksTable();
                    // Hiển thị popup tại vị trí nút
                    popupMenu.show(button, -415, button.getHeight());
                }
            });
        }

        private void updateBooksTable() {
            DefaultTableModel tableModel = new DefaultTableModel(
                    new Object[][]{},
                    new String[]{"ISBN", "Title", "Status"}
            );
            booksTable.setModel(tableModel);
            booksTable.setRowHeight(30);

            if (books != null) {
                for (Map<String, String> book : books) {
                    tableModel.addRow(new Object[]{
                        book.getOrDefault("isbn", ""),
                        book.getOrDefault("title", ""),
                        book.getOrDefault("status", "")
                    });
                }
            }
            MatteBorder matteBorder = new MatteBorder(10, 10, 10, 10, new Color(102, 153, 255));
            popupMenu.setBorder(matteBorder);

            int rowCount = books != null ? books.size() : 0;
            int tableHeight = Math.max(50, (rowCount * 30) + 30);
            scrollPane.setPreferredSize(new Dimension(600, tableHeight));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

            booksTable.setPreferredScrollableViewportSize(new Dimension(600, tableHeight));
            booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setForeground(new Color(255, 255, 255));
                    label.setBackground(new Color(0, 51, 51));
                    return label;
                }
            };

            for (int i = 0; i < booksTable.getColumnCount(); i++) {
                booksTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 0; i < booksTable.getColumnCount(); i++) {
                booksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            // Gán scrollPane vào popupMenu
            popupMenu.removeAll();
            popupMenu.add(scrollPane);
            // Tự động đóng popup khi nhấn ra ngoài
            popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                    fireEditingStopped();
                }

                @Override
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> bookList = (List<Map<String, String>>) value;
                books = bookList;
            } else {
                books = null;
            }

            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
                button.setForeground(table.getSelectionForeground());
            } else {
                button.setBackground(Color.RED);
                button.setForeground(Color.WHITE);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return books;
        }
    }

    private void initSearchTableEvents() {
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };

        for (int i = 0; i < tblMANAGE_BORROW_SLIPS.getColumnCount(); i++) {
            tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblMANAGE_BORROW_SLIPS.getColumnCount(); i++) {
            tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        // Thêm sự kiện chọn hàng cho tblMANAGE_BORROW_SLIPS
        tblMANAGE_BORROW_SLIPS.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tblMANAGE_BORROW_SLIPS.getSelectedRow();
                    if (selectedRow != -1) {
                        try {
                            DefaultTableModel model = (DefaultTableModel) tblMANAGE_BORROW_SLIPS.getModel();
                            // Kiểm tra số cột để tránh ArrayIndexOutOfBoundsException
                            if (model.getColumnCount() < 7) {
                                JOptionPane.showMessageDialog(BooksBorrowing.this, "Table structure is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            // Gán dữ liệu vào JTextField
                            txtBorrowID.setText(getValue(model, selectedRow, 0)); // Borrow ID
                            txtReaderID1.setText(getValue(model, selectedRow, 1)); // ReaderID
                            txtReaderName1.setText(getValue(model, selectedRow, 2)); // ReaderName
                            txtBorrowedDate.setText(getValue(model, selectedRow, 3)); // Borrow Date
                            txtDueDate.setText(getValue(model, selectedRow, 4)); // Due Date
                            txtReturnDate.setText(getValue(model, selectedRow, 5)); // Return Date
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            JOptionPane.showMessageDialog(BooksBorrowing.this, "Error accessing table data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private String getValue(DefaultTableModel model, int row, int column) {
        try {
            if (column >= model.getColumnCount()) {
                System.err.println("Invalid column index: " + column + ", total columns: " + model.getColumnCount());
                return "";
            }
            Object value = model.getValueAt(row, column);
            if (value instanceof List) {
                return "";
            }
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            System.err.println("Error at row " + row + ", column " + column + ": " + e.getMessage());
            return "";
        }
    }

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

        List<Map<String, Object>> results = BorrowSlipDAO.searchBorrowSlips(keyword, yearFilter, monthFilter, searchType);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Borrow ID", "ReaderID", "ReaderName", "Borrow Date", "Due Date", "Return Date", "Borrowed Books"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? List.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }

        };

        for (Map<String, Object> row : results) {
            model.addRow(new Object[]{
                String.valueOf(row.get("borrow_id")),
                String.valueOf(row.get("reader_id")),
                row.get("reader_name"),
                row.get("borrow_date"),
                row.get("due_date"),
                row.get("return_date"),
                row.get("books")
            });
        }

        tblMANAGE_BORROW_SLIPS.setModel(model);
        tblMANAGE_BORROW_SLIPS.setRowHeight(30);
        tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor());

        // CĂN GIỮA
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };

        for (int i = 0; i < tblMANAGE_BORROW_SLIPS.getColumnCount(); i++) {
            tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        for (int i = 0; i < tblMANAGE_BORROW_SLIPS.getColumnCount(); i++) {
            if (i != 6) { 
                tblMANAGE_BORROW_SLIPS.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

    }

    private void saveDueDateActionPerformed() {
        String borrowIdText = txtBorrowID.getText().trim();
        String dueDateText = txtDueDate.getText().trim();

        if (borrowIdText.isEmpty() || dueDateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a borrow slip and enter a due date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int borrowId = Integer.parseInt(borrowIdText);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date dueDate = sdf.parse(dueDateText);

            boolean updated = updateDueDate(borrowId, new java.sql.Date(dueDate.getTime()));
            if (updated) {
                JOptionPane.showMessageDialog(this, "Due date updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                searchBorrowInfor();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update due date.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Borrow ID!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean updateDueDate(int borrowId, java.sql.Date dueDate) {
        String query = "UPDATE borrow_slips SET due_date = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDate(1, dueDate);
            pst.setInt(2, borrowId);
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteSlipActionPerformed() {
        String borrowIdText = txtBorrowID.getText().trim();

        if (borrowIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a borrow slip to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this borrow slip?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int borrowId = Integer.parseInt(borrowIdText);
            boolean deleted = deleteBorrowSlip(borrowId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Borrow slip deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtBorrowID.setText("");
                txtReaderID1.setText("");
                txtReaderName1.setText("");
                txtBorrowedDate.setText("");
                txtDueDate.setText("");
                txtReturnDate.setText("");
                searchBorrowInfor();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete borrow slip.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Borrow ID!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean deleteBorrowSlip(int borrowId) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            String detailQuery = "DELETE FROM borrow_details WHERE borrow_id = ?";
            try (PreparedStatement detailPst = con.prepareStatement(detailQuery)) {
                detailPst.setInt(1, borrowId);
                detailPst.executeUpdate();
            }
            String slipQuery = "DELETE FROM borrow_slips WHERE id = ?";
            try (PreparedStatement slipPst = con.prepareStatement(slipQuery)) {
                slipPst.setInt(1, borrowId);
                int rowsAffected = slipPst.executeUpdate();
                con.commit();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setFieldsNonEditable() {
        txtBorrowID.setEditable(false);
        txtReaderID1.setEditable(false);
        txtReaderName1.setEditable(false);
        txtBorrowedDate.setEditable(false);
        txtReturnDate.setEditable(false);
    }

    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   BOOK RETURN FORM
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    private void initReturnBooks() {
        configureTableAppearance(tblBOOK_BORROW_LIST);
    }

    private void configureTableAppearance(JTable table) {
        jScrollPane4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(Color.WHITE);
                label.setBackground(new Color(0, 51, 51));
                return label;
            }
        };
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

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

        // Xử lý  khi click vào một dòng trong bảng
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

    private void loadBorrowDetails(int slipId) {
        List<Map<String, Object>> details = BorrowSlipDAO.getBorrowDetailsBySlipId(slipId);
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ISBN", "Title", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 && !"Returned".equals(getValueAt(row, 2));
            }
        };

        details.forEach(row -> model.addRow(new Object[]{
            row.get("isbn"),
            row.get("title"),
            row.get("status")
        }));

        tblBOOK_BORROW_LIST.setModel(model);
        configureTableAppearance(tblBOOK_BORROW_LIST);

        JComboBox<String> comboBox = new JComboBox<>(new String[]{"Borrowed", "Returned", "Lost"});
        tblBOOK_BORROW_LIST.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));

        // Thiết lập renderer duy nhất
        tblBOOK_BORROW_LIST.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 2);
                c.setBackground("Returned".equals(status) ? new Color(255, 102, 102) : (Color.WHITE));
                return c;
            }
        });
    }

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
                lblAPPROVED2.setVisible(true);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Invalid date format! Please use YYYY-MM-DD.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No changes to save.");
        }

        checkForLostBooksAndWarn();
    }

    private void checkForLostBooksAndWarn() {
        DefaultTableModel model = (DefaultTableModel) tblBOOK_BORROW_LIST.getModel();
        boolean hasLostBook = false;
        boolean isOverdue = false;

        for (int i = 0; i < model.getRowCount(); i++) {
            if ("Lost".equals(model.getValueAt(i, 2))) {
                hasLostBook = true;
                break;
            }
        }

        if (selectedSlipId != -1) {
            Map<String, Object> slip = BorrowSlipDAO.getBorrowSlipById(selectedSlipId);
            if (slip != null && !txtRETURN_DATE.getText().trim().isEmpty()) {
                try {
                    Date dueDate = (Date) slip.get("due_date");
                    Date returnDate = new SimpleDateFormat("yyyy-MM-dd").parse(txtRETURN_DATE.getText().trim());
                    if (calculateOverdueDays(dueDate, returnDate) > 0) {
                        isOverdue = true;
                    }
                } catch (ParseException ignored) {
                    // Bỏ qua nếu ngày không hợp lệ
                }
            }
        }

        if (hasLostBook || isOverdue) {
            StringBuilder warning = new StringBuilder("Warning: ");
            if (hasLostBook) {
                warning.append("There are lost books");
            }
            if (hasLostBook && isOverdue) {
                warning.append(" and ");
            }
            if (isOverdue) {
                warning.append("The return is overdue");
            }
            warning.append("!");
            jLabel_Warning.setText(warning.toString());
            jLabel_Warning.setVisible(true);
        } else {
            jLabel_Warning.setVisible(false);
            jLabel_Warning.setText("");
        }
    }

    private long calculateOverdueDays(Date dueDate, Date returnDate) {
        if (dueDate == null || returnDate == null) {
            return 0;
        }
        long diffInMillies = returnDate.getTime() - dueDate.getTime();
        return Math.max(diffInMillies / (1000 * 60 * 60 * 24), 0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ReturnBookPanel = new javax.swing.JPanel();
        lblAPPROVED2 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblBOOK_BORROW_LIST = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        txtDUE_DATE = new app.bolivia.swing.JCTextField();
        txtBORROW_DATE = new app.bolivia.swing.JCTextField();
        txtRETURN_DATE = new app.bolivia.swing.JCTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        txtReaderID2 = new app.bolivia.swing.JCTextField();
        lblReaderName2 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        btnView = new rojerusan.RSButtonHover();
        jLabel31 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        btnCLEAR = new rojeru_san.complementos.RSButtonHover();
        btnSAVE_RETURN_FORM = new rojeru_san.complementos.RSButtonHover();
        jLabel_Warning = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        addAreaPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        singleSlipPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblAPPROVED = new javax.swing.JLabel();
        btnSingleSave = new rojeru_san.complementos.RSButtonHover();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblMultiBookBorrow = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        txtReaderID = new app.bolivia.swing.JCTextField();
        lblReaderName = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        txtBORROWDATE = new app.bolivia.swing.JCTextField();
        jLabel6 = new javax.swing.JLabel();
        txtDUEDATE = new app.bolivia.swing.JCTextField();
        jLabel13 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        btnCLEAR_BORROW_SLIP = new rojerusan.RSButtonHover();
        jPanel20 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
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
        tblMANAGE_BORROW_SLIPS = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        txtBorrowID = new javax.swing.JTextField();
        txtBorrowedDate = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtReaderName1 = new javax.swing.JTextField();
        txtReturnDate = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtReaderID1 = new javax.swing.JTextField();
        txtDueDate = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnDelete = new rojerusan.RSButtonHover();
        btnEdit = new rojerusan.RSButtonHover();
        jPanel25 = new javax.swing.JPanel();
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
        jLabel4 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ReturnBookPanel.setBackground(new java.awt.Color(255, 255, 255));
        ReturnBookPanel.setBorder(new javax.swing.border.MatteBorder(null));
        ReturnBookPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAPPROVED2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/APPROVED.png"))); // NOI18N
        ReturnBookPanel.add(lblAPPROVED2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

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

        ReturnBookPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 240, 800, 380));

        jPanel12.setBackground(new java.awt.Color(102, 153, 255));
        jPanel12.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ReturnBookPanel.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51), 5));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(0, 0, 0));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/calendar_return.png"))); // NOI18N
        jLabel34.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jPanel13.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 48, 48));

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
        jPanel13.add(txtDUE_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 90, 290, 50));

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
        jPanel13.add(txtBORROW_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 290, 50));

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
        jPanel13.add(txtRETURN_DATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 170, 290, 50));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/due.png"))); // NOI18N
        jLabel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel13.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 90, 48, 48));

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/calendar.png"))); // NOI18N
        jLabel30.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel13.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 48, 48));

        jPanel23.setBackground(new java.awt.Color(102, 153, 255));
        jPanel23.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel23.setForeground(new java.awt.Color(0, 0, 0));
        jPanel23.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel13.add(jPanel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 10, 220));

        ReturnBookPanel.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 440, 420, 240));

        jLabel37.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(0, 0, 0));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("List of borrowed books ");
        jLabel37.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(0, 51, 51)));
        ReturnBookPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 190, 780, 60));

        jPanel21.setBackground(new java.awt.Color(255, 255, 255));
        jPanel21.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 5, true));
        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtReaderID2.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderID2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 51, 51)));
        txtReaderID2.setForeground(new java.awt.Color(0, 51, 51));
        txtReaderID2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID2.setToolTipText("");
        txtReaderID2.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtReaderID2.setPhColor(new java.awt.Color(0, 51, 51));
        txtReaderID2.setPlaceholder("                   Enter readerID");
        txtReaderID2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderID2ActionPerformed(evt);
            }
        });
        jPanel21.add(txtReaderID2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 90, 290, 40));

        lblReaderName2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblReaderName2.setForeground(new java.awt.Color(0, 0, 0));
        lblReaderName2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReaderName2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 51, 51)));
        jPanel21.add(lblReaderName2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 290, 40));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id-card.png"))); // NOI18N
        jPanel21.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 50));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/name.png"))); // NOI18N
        jLabel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jLabel14.setPreferredSize(new java.awt.Dimension(48, 48));
        jPanel21.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 48, 48));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 0, 0));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id.png"))); // NOI18N
        jLabel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jPanel21.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 48, 48));

        jPanel22.setBackground(new java.awt.Color(102, 153, 255));
        jPanel22.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel22.setForeground(new java.awt.Color(0, 0, 0));
        jPanel22.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel21.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 10, 160));

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
        jPanel21.add(btnView, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 10, 40, 50));

        ReturnBookPanel.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 420, 240));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(0, 0, 0));
        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/return_book.png"))); // NOI18N
        ReturnBookPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, -1, 110));

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(0, 51, 51)));
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnCLEAR.setBackground(new java.awt.Color(255, 255, 255));
        btnCLEAR.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        btnCLEAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/18x25trash.png"))); // NOI18N
        btnCLEAR.setText("CLEAR");
        btnCLEAR.setColorHover(new java.awt.Color(255, 0, 51));
        btnCLEAR.setColorText(new java.awt.Color(0, 51, 51));
        btnCLEAR.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCLEAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCLEARActionPerformed(evt);
            }
        });
        jPanel14.add(btnCLEAR, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 20, 90, 30));

        btnSAVE_RETURN_FORM.setBackground(new java.awt.Color(255, 255, 255));
        btnSAVE_RETURN_FORM.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        btnSAVE_RETURN_FORM.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/24X24save_78935.png"))); // NOI18N
        btnSAVE_RETURN_FORM.setText("SAVE");
        btnSAVE_RETURN_FORM.setColorHover(new java.awt.Color(255, 0, 51));
        btnSAVE_RETURN_FORM.setColorText(new java.awt.Color(0, 51, 51));
        btnSAVE_RETURN_FORM.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSAVE_RETURN_FORM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSAVE_RETURN_FORMActionPerformed(evt);
            }
        });
        jPanel14.add(btnSAVE_RETURN_FORM, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 20, 90, 30));

        ReturnBookPanel.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 120, 950, 60));

        jLabel_Warning.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel_Warning.setForeground(new java.awt.Color(0, 0, 0));
        jLabel_Warning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/alert.png"))); // NOI18N
        jLabel_Warning.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(255, 0, 0)));
        ReturnBookPanel.add(jLabel_Warning, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 650, 780, 30));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(0, 0, 0));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("BOOK RETURN FORM");
        jLabel29.setToolTipText("");
        ReturnBookPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 60, 380, -1));

        jPanel24.setBackground(new java.awt.Color(255, 255, 255));
        jPanel24.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(0, 51, 51)));
        jPanel24.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ReturnBookPanel.add(jPanel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 170, 60));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(0, 51, 51)));
        ReturnBookPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        getContentPane().add(ReturnBookPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        addAreaPanel.setBackground(new java.awt.Color(0, 51, 51));
        addAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        addAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel6.setLayout(new java.awt.GridLayout(1, 0));
        addAreaPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        singleSlipPanel.setBackground(new java.awt.Color(255, 255, 255));
        singleSlipPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        singleSlipPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Books List");
        jLabel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(0, 51, 51)));
        singleSlipPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 180, 690, -1));

        lblAPPROVED.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/APPROVED.png"))); // NOI18N
        singleSlipPanel.add(lblAPPROVED, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, -10, -1, -1));

        btnSingleSave.setBackground(new java.awt.Color(255, 255, 255));
        btnSingleSave.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 3, 0, new java.awt.Color(0, 51, 51)));
        btnSingleSave.setForeground(new java.awt.Color(0, 51, 51));
        btnSingleSave.setText("SAVE");
        btnSingleSave.setColorHover(new java.awt.Color(255, 0, 51));
        btnSingleSave.setColorText(new java.awt.Color(0, 51, 51));
        btnSingleSave.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnSingleSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSingleSaveActionPerformed(evt);
            }
        });
        singleSlipPanel.add(btnSingleSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 590, 700, 40));

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 255));

        tblMultiBookBorrow.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tblMultiBookBorrow.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ISBN", "Title"
            }
        ));
        tblMultiBookBorrow.setOpaque(false);
        tblMultiBookBorrow.setRowHeight(40);
        tblMultiBookBorrow.setSelectionBackground(new java.awt.Color(102, 153, 255));
        tblMultiBookBorrow.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblMultiBookBorrow.setShowGrid(true);
        jScrollPane2.setViewportView(tblMultiBookBorrow);

        singleSlipPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 220, 700, 340));

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));
        jPanel15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 5, true));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtReaderID.setBackground(new java.awt.Color(255, 255, 255));
        txtReaderID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 51, 51)));
        txtReaderID.setForeground(new java.awt.Color(0, 51, 51));
        txtReaderID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID.setToolTipText("");
        txtReaderID.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtReaderID.setPhColor(new java.awt.Color(0, 51, 51));
        txtReaderID.setPlaceholder("                   Enter readerID");
        txtReaderID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderIDActionPerformed(evt);
            }
        });
        jPanel15.add(txtReaderID, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 90, 290, 40));

        lblReaderName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblReaderName.setForeground(new java.awt.Color(0, 0, 0));
        lblReaderName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblReaderName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 51, 51)));
        jPanel15.add(lblReaderName, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 290, 40));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id-card.png"))); // NOI18N
        jPanel15.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 50));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/name.png"))); // NOI18N
        jLabel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jLabel10.setPreferredSize(new java.awt.Dimension(48, 48));
        jPanel15.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 48, 48));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id.png"))); // NOI18N
        jLabel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jPanel15.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 48, 48));

        jPanel16.setBackground(new java.awt.Color(102, 153, 255));
        jPanel16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel16.setForeground(new java.awt.Color(0, 0, 0));
        jPanel16.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel15.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 10, 180));

        singleSlipPanel.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 420, 260));

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 5, true));
        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtBORROWDATE.setBackground(new java.awt.Color(255, 255, 255));
        txtBORROWDATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtBORROWDATE.setForeground(new java.awt.Color(0, 0, 0));
        txtBORROWDATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBORROWDATE.setToolTipText("");
        txtBORROWDATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtBORROWDATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBORROWDATEActionPerformed(evt);
            }
        });
        jPanel17.add(txtBORROWDATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 290, 40));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/due.png"))); // NOI18N
        jLabel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel17.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 48, 48));

        txtDUEDATE.setBackground(new java.awt.Color(255, 255, 255));
        txtDUEDATE.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txtDUEDATE.setForeground(new java.awt.Color(0, 0, 0));
        txtDUEDATE.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDUEDATE.setToolTipText("");
        txtDUEDATE.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtDUEDATE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDUEDATEActionPerformed(evt);
            }
        });
        jPanel17.add(txtDUEDATE, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 120, 290, 40));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/calendar.png"))); // NOI18N
        jLabel13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel17.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 48, 48));

        jPanel18.setBackground(new java.awt.Color(102, 153, 255));
        jPanel18.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jPanel18.setForeground(new java.awt.Color(0, 0, 0));
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel17.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 10, 170));

        singleSlipPanel.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 440, 420, 190));

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(0, 0, 0));
        jLabel32.setText("BOOK BORROWING FORM");
        singleSlipPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, 500, -1));

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        singleSlipPanel.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 15, 970, 70));

        btnCLEAR_BORROW_SLIP.setBackground(new java.awt.Color(255, 255, 255));
        btnCLEAR_BORROW_SLIP.setBorder(null);
        btnCLEAR_BORROW_SLIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/18x25trash.png"))); // NOI18N
        btnCLEAR_BORROW_SLIP.setText("CLEAR");
        btnCLEAR_BORROW_SLIP.setColorHover(new java.awt.Color(255, 0, 51));
        btnCLEAR_BORROW_SLIP.setColorText(new java.awt.Color(0, 51, 51));
        btnCLEAR_BORROW_SLIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCLEAR_BORROW_SLIPActionPerformed(evt);
            }
        });
        singleSlipPanel.add(btnCLEAR_BORROW_SLIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(1180, 140, 80, 30));

        jPanel20.setBackground(new java.awt.Color(255, 255, 255));
        jPanel20.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 0, 0, new java.awt.Color(0, 51, 51)));
        jPanel20.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        singleSlipPanel.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 130, 80));

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/library11.png"))); // NOI18N
        singleSlipPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 30, 140, 120));

        addAreaPanel.add(singleSlipPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        getContentPane().add(addAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        searchAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchAreaPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        searchAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        jPanel11.setForeground(new java.awt.Color(255, 255, 255));
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
        txtSearch.setPlaceholder("                      ENTER KEYWORD");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanel11.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 310, 40));

        rSButtonHover1.setBackground(new java.awt.Color(0, 51, 51));
        rSButtonHover1.setBorder(null);
        rSButtonHover1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/search.png"))); // NOI18N
        rSButtonHover1.setColorHover(new java.awt.Color(51, 255, 0));
        rSButtonHover1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSButtonHover1ActionPerformed(evt);
            }
        });
        jPanel11.add(rSButtonHover1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbYear.setForeground(new java.awt.Color(255, 255, 255));
        cmbYear.setToolTipText("");
        cmbYear.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 3, true));
        cmbYear.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbYear.getUI().getAccessibleChild(cmbYear, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        cmbYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbYearActionPerformed(evt);
            }
        });
        jPanel11.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 160, 40));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        jLabel26.setText("BORROWED YEAR");
        jPanel11.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 160, 40));

        cmbCriteria.setBackground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbCriteria.setForeground(new java.awt.Color(255, 255, 255));
        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ISBN", "Title", "ReaderID", "Reader Name" }));
        cmbCriteria.setToolTipText("");
        cmbCriteria.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 3, true));
        cmbCriteria.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbCriteria.getUI().getAccessibleChild(cmbCriteria, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cmbCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCriteriaActionPerformed(evt);
            }
        });
        jPanel11.add(cmbCriteria, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 100, 160, 40));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/filter.png"))); // NOI18N
        jLabel27.setText("CRITERIA ");
        jPanel11.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 160, 40));

        cmbMonth.setBackground(new java.awt.Color(255, 255, 255));
        cmbMonth.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cmbMonth.setForeground(new java.awt.Color(255, 255, 255));
        cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All months", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        cmbMonth.setToolTipText("");
        cmbMonth.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 3, true));
        cmbMonth.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        ((JScrollPane)((JPopupMenu)cmbMonth.getUI().getAccessibleChild(cmbMonth, 0)).getComponent(0)).setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        cmbMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMonthActionPerformed(evt);
            }
        });
        jPanel11.add(cmbMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 150, 160, 40));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/month.png"))); // NOI18N
        jLabel25.setText("BORROWED MONTH");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 160, 40));

        searchAreaPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 370, 260));

        tblMANAGE_BORROW_SLIPS.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblMANAGE_BORROW_SLIPS.setForeground(new java.awt.Color(0, 0, 0));
        tblMANAGE_BORROW_SLIPS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Borrow ID", "ReaderID", "ReaderName", "Borrow Date", "Due Date", "Return Date", "Borrowed Books"
            }
        ));
        tblMANAGE_BORROW_SLIPS.setGridColor(new java.awt.Color(0, 51, 51));
        tblMANAGE_BORROW_SLIPS.setPreferredSize(new java.awt.Dimension(600, 10000));
        tblMANAGE_BORROW_SLIPS.setRowHeight(30);
        tblMANAGE_BORROW_SLIPS.setSelectionBackground(new java.awt.Color(255, 51, 51));
        tblMANAGE_BORROW_SLIPS.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblMANAGE_BORROW_SLIPS.setShowGrid(true);
        tblMANAGE_BORROW_SLIPS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMANAGE_BORROW_SLIPSMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMANAGE_BORROW_SLIPS);

        searchAreaPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 1280, 410));

        jPanel7.setBackground(new java.awt.Color(0, 51, 51));
        jPanel7.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 3, 3, 3, new java.awt.Color(0, 51, 51)));
        RoundedPanel jPanel7 = new RoundedPanel(30);
        jPanel7.setBackground(new Color(0, 51, 51));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtBorrowID.setEditable(false);
        txtBorrowID.setBackground(new java.awt.Color(0, 51, 51));
        txtBorrowID.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtBorrowID.setForeground(new java.awt.Color(255, 255, 255));
        txtBorrowID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBorrowID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        jPanel7.add(txtBorrowID, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 202, 100, 30));

        txtBorrowedDate.setEditable(false);
        txtBorrowedDate.setBackground(new java.awt.Color(0, 51, 51));
        txtBorrowedDate.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtBorrowedDate.setForeground(new java.awt.Color(255, 255, 255));
        txtBorrowedDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtBorrowedDate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtBorrowedDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBorrowedDateActionPerformed(evt);
            }
        });
        jPanel7.add(txtBorrowedDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 30, 220, 30));

        jLabel19.setBackground(new java.awt.Color(0, 51, 51));
        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/name.png"))); // NOI18N
        jLabel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jPanel7.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, 40, 40));

        txtReaderName1.setEditable(false);
        txtReaderName1.setBackground(new java.awt.Color(0, 51, 51));
        txtReaderName1.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtReaderName1.setForeground(new java.awt.Color(255, 255, 255));
        txtReaderName1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderName1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtReaderName1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReaderName1ActionPerformed(evt);
            }
        });
        jPanel7.add(txtReaderName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 220, 30));

        txtReturnDate.setEditable(false);
        txtReturnDate.setBackground(new java.awt.Color(0, 51, 51));
        txtReturnDate.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtReturnDate.setForeground(new java.awt.Color(255, 255, 255));
        txtReturnDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReturnDate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        txtReturnDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtReturnDateActionPerformed(evt);
            }
        });
        jPanel7.add(txtReturnDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 110, 220, 30));

        jLabel16.setBackground(new java.awt.Color(0, 51, 51));
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/check-form_116472.png"))); // NOI18N
        jPanel7.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, -1, -1));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(133, 10, 3, 240));

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, 3, 240));

        jLabel20.setBackground(new java.awt.Color(0, 51, 51));
        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/calendar.png"))); // NOI18N
        jLabel20.setToolTipText("");
        jLabel20.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel7.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 20, 40, 40));

        jLabel21.setBackground(new java.awt.Color(0, 51, 51));
        jLabel21.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 0, 51));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/due.png"))); // NOI18N
        jLabel21.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel7.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 190, 40, 40));

        jLabel22.setBackground(new java.awt.Color(0, 51, 51));
        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/id.png"))); // NOI18N
        jLabel22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 51)));
        jPanel7.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, 40, 40));

        jLabel24.setBackground(new java.awt.Color(0, 51, 51));
        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/calendar_return.png"))); // NOI18N
        jLabel24.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        jPanel7.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 100, 40, 40));

        txtReaderID1.setEditable(false);
        txtReaderID1.setBackground(new java.awt.Color(0, 51, 51));
        txtReaderID1.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtReaderID1.setForeground(new java.awt.Color(255, 255, 255));
        txtReaderID1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtReaderID1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        jPanel7.add(txtReaderID1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 110, 220, 30));

        txtDueDate.setBackground(new java.awt.Color(0, 51, 51));
        txtDueDate.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        txtDueDate.setForeground(new java.awt.Color(255, 255, 255));
        txtDueDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDueDate.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(102, 153, 255)));
        jPanel7.add(txtDueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 190, 220, 40));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        RoundedPanel jPanel1 = new RoundedPanel(30);
        jPanel1.setBackground(new Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnDelete.setBackground(new java.awt.Color(255, 255, 255));
        btnDelete.setBorder(null);
        btnDelete.setForeground(new java.awt.Color(102, 153, 255));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setColorHover(new java.awt.Color(204, 0, 51));
        btnDelete.setColorText(new java.awt.Color(102, 153, 255));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        btnDelete.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel1.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 120, -1));

        btnEdit.setBackground(new java.awt.Color(255, 255, 255));
        btnEdit.setBorder(null);
        btnEdit.setForeground(new java.awt.Color(102, 153, 255));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/edit.png"))); // NOI18N
        btnEdit.setText("Edit Due Date");
        btnEdit.setColorHover(new java.awt.Color(204, 0, 51));
        btnEdit.setColorText(new java.awt.Color(102, 153, 255));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        btnEdit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel1.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 120, -1));

        jPanel7.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 10, 140, 240));

        jPanel25.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.add(jPanel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, 3, 240));

        searchAreaPanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 10, 900, 260));

        getContentPane().add(searchAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

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

        btnBack.setBackground(new java.awt.Color(0, 51, 51));
        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/undo.png"))); // NOI18N
        btnBack.setColorHover(new java.awt.Color(204, 0, 51));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        jPanel2.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(1450, 0, 50, 50));

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

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        jLabel28.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        getContentPane().add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 700));

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
        singleSlipPanel.setVisible(true);
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

    private void txtReaderIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderIDActionPerformed
        String readerIDText = txtReaderID.getText().trim();
        if (!readerIDText.isEmpty()) {
            try {
                int readerID = Integer.parseInt(readerIDText);
                String readerName = ReaderDAO.getReaderNameByID(readerID);
                lblReaderName.setText(readerName.isEmpty() ? "No reader found" : readerName);
            } catch (NumberFormatException ex) {
                lblReaderName.setText("Invalid Reader ID");
            }
        } else {
            lblReaderName.setText("");
        }
    }//GEN-LAST:event_txtReaderIDActionPerformed

    private void btnSingleSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSingleSaveActionPerformed
        try {
            int readerID = Integer.parseInt(txtReaderID.getText().trim());
            String borrowDateText = txtBORROWDATE.getText().trim();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date borrowDate = dateFormat.parse(borrowDateText);
            // 1. Create a loan slip and get the borrow_id
            int borrowId = BorrowSlipDAO.saveBorrowSlip(readerID, borrowDate);
            if (borrowId == -1) {
                JOptionPane.showMessageDialog(this, "Unable to create ticket.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Delete empty lines
            DefaultTableModel model = (DefaultTableModel) tblMultiBookBorrow.getModel();
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                Object isbnObj = model.getValueAt(i, ISBN_COLUMN_INDEX);
                if (isbnObj == null || isbnObj.toString().trim().isEmpty()) {
                    model.removeRow(i); // Delete empty lines
                }
            }

            //Save each book line (only lines with ISBN)
            int savedCount = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                String isbn = model.getValueAt(i, ISBN_COLUMN_INDEX).toString().trim();
                if (!isbn.isEmpty()) {
                    boolean success = BorrowSlipDAO.saveBorrowDetail(borrowId, isbn);
                    if (success) {
                        savedCount++;
                    }
                }
            }

            // Result notification
            if (savedCount > 0) {
                JOptionPane.showMessageDialog(this, "Successfully creating borrow slips!", "SAVED!", JOptionPane.INFORMATION_MESSAGE);
                lblAPPROVED.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No books saved.", "Notification", JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Reader ID!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving loan slip: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }


    }//GEN-LAST:event_btnSingleSaveActionPerformed

    private void txtBORROWDATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBORROWDATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBORROWDATEActionPerformed

    private void cmbMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbMonthActionPerformed

    private void txtReaderName1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderName1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtReaderName1ActionPerformed

    private void txtBorrowedDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBorrowedDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBorrowedDateActionPerformed

    private void txtReturnDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReturnDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtReturnDateActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        saveDueDateActionPerformed();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblMANAGE_BORROW_SLIPS.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hàng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblMANAGE_BORROW_SLIPS.getModel();

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

    private void tblMANAGE_BORROW_SLIPSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMANAGE_BORROW_SLIPSMouseClicked
        int selectedRow = tblMANAGE_BORROW_SLIPS.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblMANAGE_BORROW_SLIPS.getModel();

        txtBorrowID.setText(getValue(model, selectedRow, 0));
        txtReaderID1.setText(getValue(model, selectedRow, 1));
        txtReaderName1.setText(getValue(model, selectedRow, 2));
        txtBorrowedDate.setText(getValue(model, selectedRow, 3));
        txtDueDate.setText(getValue(model, selectedRow, 4));
        txtReturnDate.setText(getValue(model, selectedRow, 5));
    }//GEN-LAST:event_tblMANAGE_BORROW_SLIPSMouseClicked

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
        lblAPPROVED2.setVisible(false);
    }//GEN-LAST:event_btnCLEARActionPerformed

    private void txtDUEDATEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDUEDATEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDUEDATEActionPerformed

    private void btnCLEAR_BORROW_SLIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCLEAR_BORROW_SLIPActionPerformed

        txtReaderID.setText("");
        lblReaderName.setText("");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new java.util.Date();
        txtBORROWDATE.setText(dateFormat.format(today));

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 7);
        txtDUEDATE.setText(dateFormat.format(cal.getTime()));

        // Clear bảng
        DefaultTableModel model = (DefaultTableModel) tblMultiBookBorrow.getModel();
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                model.setValueAt("", row, col);
            }
        }

        lblAPPROVED.setVisible(false);
    }//GEN-LAST:event_btnCLEAR_BORROW_SLIPActionPerformed

    private void txtReaderID2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtReaderID2ActionPerformed
        String readerIDText = txtReaderID2.getText().trim();
        if (!readerIDText.isEmpty()) {
            try {
                int readerID = Integer.parseInt(readerIDText);
                String readerName = ReaderDAO.getReaderNameByID(readerID);
                lblReaderName2.setText(readerName.isEmpty() ? "No reader found" : readerName);
            } catch (NumberFormatException ex) {
                lblReaderName2.setText("Invalid Reader ID");
            }
        } else {
            lblReaderName2.setText("");
        }
    }//GEN-LAST:event_txtReaderID2ActionPerformed

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
    private int activeTableRow = -1;
    private static final int ISBN_COLUMN_INDEX = 0;

    private final int TITLE_COLUMN_INDEX = 1;

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
    private rojerusan.RSButtonHover btnBack;
    private rojeru_san.complementos.RSButtonHover btnCLEAR;
    private rojerusan.RSButtonHover btnCLEAR_BORROW_SLIP;
    private rojerusan.RSButtonHover btnDelete;
    private rojerusan.RSButtonHover btnEdit;
    private rojeru_san.complementos.RSButtonHover btnReturn;
    private rojeru_san.complementos.RSButtonHover btnSAVE_RETURN_FORM;
    private rojeru_san.complementos.RSButtonHover btnSingleSave;
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
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
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
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblAPPROVED;
    private javax.swing.JLabel lblAPPROVED2;
    private javax.swing.JLabel lblReaderName;
    private javax.swing.JLabel lblReaderName2;
    private rojerusan.RSButtonHover rSButtonHover1;
    private javax.swing.JPanel searchAreaPanel;
    private rojeru_san.complementos.RSButtonHover searchPanel;
    private javax.swing.JPanel singleSlipPanel;
    private javax.swing.JTable tblBOOK_BORROW_LIST;
    private javax.swing.JTable tblMANAGE_BORROW_SLIPS;
    private javax.swing.JTable tblMultiBookBorrow;
    private app.bolivia.swing.JCTextField txtBORROWDATE;
    private app.bolivia.swing.JCTextField txtBORROW_DATE;
    private javax.swing.JTextField txtBorrowID;
    private javax.swing.JTextField txtBorrowedDate;
    private app.bolivia.swing.JCTextField txtDUEDATE;
    private app.bolivia.swing.JCTextField txtDUE_DATE;
    private javax.swing.JTextField txtDueDate;
    private app.bolivia.swing.JCTextField txtRETURN_DATE;
    private app.bolivia.swing.JCTextField txtReaderID;
    private javax.swing.JTextField txtReaderID1;
    private app.bolivia.swing.JCTextField txtReaderID2;
    private javax.swing.JTextField txtReaderName1;
    private javax.swing.JTextField txtReturnDate;
    private app.bolivia.swing.JCTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
