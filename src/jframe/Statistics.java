/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import DAO.StatisticsDAO;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import javax.swing.border.MatteBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.ui.RectangleInsets;
import UI_Helper.*;

/**
 *
 * @author HS
 */
public class Statistics extends javax.swing.JFrame {

    /**
     * Creates new form Statistics
     */
    public Statistics() {
        initComponents();
        updateTotalBooks();
        updateTotalReaders();
        updateTotalTitles();
        updateTotalReadersOverdue();
        updateTotalReadersBorrowingBooks();
        BooksbyGenrePanel.setVisible(false);
        GenderPanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);
        TopBorrowed();
        BooksByGenre();
        updateBooksByGenre();

        ReadersByGender();
        updateReadersByGender();

        TableStyle(tblTopBooks);
        TableStyle(tblTopCategories);

        scrollPaneTopBooks.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPaneTopCategories.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        this.setLocationRelativeTo(null);

    }

    private void updateTotalBooks() {
        int totalBooks = StatisticsDAO.getTotalBooks();
        NumberFormat formatter = NumberFormat.getInstance();
        lblNoB.setText(formatter.format(totalBooks));
    }

    private void updateTotalTitles() {
        int totalBooks = StatisticsDAO.getTotalTitles();
        NumberFormat formatter = NumberFormat.getInstance();
        lblNoISBN.setText(formatter.format(totalBooks));
    }

    private void updateTotalReaders() {
        int totalBooks = StatisticsDAO.getTotalReaders();
        NumberFormat formatter = NumberFormat.getInstance();
        lblNoR.setText(formatter.format(totalBooks));
    }

    private void updateTotalReadersOverdue() {
        int totalBooks = StatisticsDAO.getReadersOverdue();
        NumberFormat formatter = NumberFormat.getInstance();
        lblNoOver.setText(formatter.format(totalBooks));
    }

    private void updateTotalReadersBorrowingBooks() {
        int totalBooks = StatisticsDAO.getReadersBorrowingBooks();
        NumberFormat formatter = NumberFormat.getInstance();
        lblNoBR.setText(formatter.format(totalBooks));
    }

    //thống kê sách theo thể loại
    private void BooksByGenre() {
        BooksbyGenrePanel.setLayout(new BorderLayout(10, 10));

        lblTitle_Genre = new JLabel("Books by Category", JLabel.CENTER);
        lblTitle_Genre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle_Genre.setForeground(Color.WHITE);
        BooksbyGenrePanel.add(lblTitle_Genre, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Pie Chart
        chartPanel = new ChartPanel(null);
        chartPanel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.75;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(chartPanel, gbc);

        // Bảng chi tiết
        tableModel = new DefaultTableModel(new Object[]{"Category", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCategoryDetails = new JTable(tableModel);
        tblCategoryDetails.setRowHeight(30);
        tblCategoryDetails.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tblCategoryDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        tblCategoryDetails.setOpaque(false);
        tblCategoryDetails.setBackground(new Color(0, 51, 51));
        tblCategoryDetails.setForeground(Color.WHITE);
        tblCategoryDetails.getTableHeader().setOpaque(false);
        tblCategoryDetails.getTableHeader().setBackground(new Color(0, 51, 51));
        tblCategoryDetails.getTableHeader().setForeground(Color.BLACK);
        tblCategoryDetails.setSelectionBackground(Color.RED);
        MatteBorder matteBorder = new MatteBorder(0, 1, 0, 0, new Color(255, 255, 255));
        tblCategoryDetails.setBorder(matteBorder);
        tblCategoryDetails.setShowGrid(true);
        tblCategoryDetails.setGridColor(Color.WHITE);

        //
        JScrollPane tableScrollPane = new JScrollPane(tblCategoryDetails);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        MatteBorder matteBorder2 = new MatteBorder(5, 3, 14, 3, new Color(0, 51, 51));
        tableScrollPane.setBorder(matteBorder2);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(tableScrollPane, gbc);
        chartPanel.setPreferredSize(new Dimension(1040, 600));
        tableScrollPane.setPreferredSize(new Dimension(260, 600));

        BooksbyGenrePanel.add(mainPanel, BorderLayout.CENTER);

        // Panel chứa các btn
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false);

        //btn Refresh
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setBackground(new Color(255, 255, 255));
        btnRefresh.setPreferredSize(new Dimension(140, 30));
        btnRefresh.addActionListener(e -> updateBooksByGenre());
        southPanel.add(btnRefresh);

        //btn Switch Mode
        JButton btnSwitchMode = new JButton("Switch to Titles");
        btnSwitchMode.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSwitchMode.setForeground(Color.BLACK);
        btnSwitchMode.setBackground(new Color(255, 255, 255));
        btnSwitchMode.setPreferredSize(new Dimension(180, 30));
        btnSwitchMode.addActionListener(e -> {
            isVolumeMode = !isVolumeMode; // Chuyển đổi chế độ
            btnSwitchMode.setText(isVolumeMode ? "Switch to Titles" : "Switch to Volumes");
            updateBooksByGenre();
            btnSwitchMode.setBackground(isVolumeMode ? new Color(255, 255, 255) : new Color(102, 153, 255));
            btnSwitchMode.setForeground(isVolumeMode ? Color.BLACK : Color.WHITE);
        });
        southPanel.add(btnSwitchMode);

        BooksbyGenrePanel.add(southPanel, BorderLayout.SOUTH);
    }

    private void updateBooksByGenre() {
        Map<String, Integer> booksByCategory = isVolumeMode
                ? StatisticsDAO.getBooksByCategoryVolumes() : StatisticsDAO.getBooksByCategoryTitles();

        lblTitle_Genre.setText(isVolumeMode ? "Books by Category (Volumes)" : "Books by Category (Titles)");

        // Cập nhật Pie Chart
        DefaultPieDataset dataset = new DefaultPieDataset();
        int total = booksByCategory.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : booksByCategory.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            dataset.setValue(entry.getKey() + " (" + String.format("%.1f%%", percentage) + ")", entry.getValue());
        }
        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        chart.setBackgroundPaint(new Color(0, 51, 51));
        org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0, 51, 51, 1));
        plot.setOutlinePaint(new Color(0, 51, 51));
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));

        chart.getLegend().setBackgroundPaint(new Color(0, 51, 51, 0));
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 18));
        chart.getLegend().setItemPaint(Color.WHITE);

        chartPanel.setChart(chart);

        tableModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : booksByCategory.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        tableModel.addRow(new Object[]{"Total", total});

        tableModel.setColumnIdentifiers(new Object[]{"Category", isVolumeMode ? "Quantity" : "Titles"});

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblCategoryDetails.getColumnCount(); i++) {
            tblCategoryDetails.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(102, 153, 255));
                return label;
            }
        };
        for (int i = 0; i < tblCategoryDetails.getColumnCount(); i++) {
            tblCategoryDetails.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

    }

    //Độc giả theo giới tính
    private void ReadersByGender() {
        GenderPanel.setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Readers by Gender", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        GenderPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // Pie Chart
        genderChartPanel = new ChartPanel(null);
        genderChartPanel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.75;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(genderChartPanel, gbc);

        // Bảng chi tiết
        genderTableModel = new DefaultTableModel(new Object[]{"Gender", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblGenderDetails = new JTable(genderTableModel);
        tblGenderDetails.setRowHeight(30);
        tblGenderDetails.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tblGenderDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        tblGenderDetails.setOpaque(false);
        tblGenderDetails.setBackground(new Color(0, 51, 51));
        tblGenderDetails.setForeground(Color.WHITE);
        tblGenderDetails.getTableHeader().setOpaque(false);
        tblGenderDetails.getTableHeader().setBackground(new Color(0, 51, 51));
        tblGenderDetails.getTableHeader().setForeground(Color.BLACK);
        tblGenderDetails.setSelectionBackground(Color.RED);
        MatteBorder matteBorder = new MatteBorder(0, 1, 0, 0, new Color(255, 255, 255));
        tblGenderDetails.setBorder(matteBorder);

        tblGenderDetails.setShowGrid(true);
        tblGenderDetails.setGridColor(Color.WHITE);

        // Căn giữa dữ liệu trong ô
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblGenderDetails.getColumnCount(); i++) {
            tblGenderDetails.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Căn giữa và tùy chỉnh header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 18));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(new Color(255, 255, 255));
                label.setBackground(new Color(102, 153, 255));
                label.setOpaque(true);
                return label;
            }
        };
        for (int i = 0; i < tblGenderDetails.getColumnCount(); i++) {
            tblGenderDetails.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(tblGenderDetails);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);
        MatteBorder matteBorder2 = new MatteBorder(5, 3, 400, 3, new Color(0, 51, 51));
        tableScrollPane.setBorder(matteBorder2);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.25;

        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(tableScrollPane, gbc);

        genderChartPanel.setPreferredSize(new Dimension(1040, 600));
        tableScrollPane.setPreferredSize(new Dimension(260, 600));

        GenderPanel.add(mainPanel, BorderLayout.CENTER);

        //btn Refresh
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setBackground(new Color(255, 255, 255));
        btnRefresh.setPreferredSize(new Dimension(140, 30));
        btnRefresh.addActionListener(e -> updateReadersByGender());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false);
        southPanel.add(btnRefresh);

        GenderPanel.add(southPanel, BorderLayout.SOUTH);
    }

    private void updateReadersByGender() {
        Map<String, Integer> readersByGender = StatisticsDAO.getReadersByGender();

        // Cập nhật Pie Chart
        DefaultPieDataset dataset = new DefaultPieDataset();
        int totalReaders = readersByGender.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : readersByGender.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalReaders;
            dataset.setValue(entry.getKey() + " (" + String.format("%.1f%%", percentage) + ")", entry.getValue());
        }
        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        chart.setBackgroundPaint(new Color(0, 51, 51));
        org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0, 51, 51, 1));
        plot.setOutlinePaint(new Color(0, 51, 51));
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));

        chart.getLegend().setBackgroundPaint(new Color(0, 51, 51, 0));
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 18));
        chart.getLegend().setItemPaint(Color.WHITE);

        genderChartPanel.setChart(chart);

        genderTableModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : readersByGender.entrySet()) {
            genderTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        genderTableModel.addRow(new Object[]{"Total", totalReaders});
    }

    //top mượn theo sách/tháng
    private void TopBorrowed() {
        populateYearComboBox();
        populateMonthComboBox();

        lblMonth.setVisible(false);
        cmbMonth.setVisible(false);
        cmbPeriodType.setSelectedItem("By Year");

        topBooksTableModel = new DefaultTableModel(new Object[]{"Rank", "ISBN", "Title", "Borrow Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTopBooks.setModel(topBooksTableModel);

        topCategoriesTableModel = new DefaultTableModel(new Object[]{"Rank", "Category", "Borrow Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTopCategories.setModel(topCategoriesTableModel);

        cmbPeriodType.addActionListener(e -> isMonthSelection());
        btnRefreshTopBorrowed.addActionListener(e -> updateTopBorrowedStatistics());

    }

    //
    private void TableStyle(JTable table) {

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
        int currentYear = java.time.Year.now().getValue();
        cmbYear.removeAllItems();
        for (int year = currentYear; year >= currentYear - 10; year--) {
            cmbYear.addItem(year);
        }
    }

    private void populateMonthComboBox() {
        cmbMonth.removeAllItems();
        for (int month = 1; month <= 12; month++) {
            cmbMonth.addItem(month);
        }
    }

    private void isMonthSelection() {
        boolean isMonthSelected = "By Month".equals(cmbPeriodType.getSelectedItem());
        lblMonth.setVisible(isMonthSelected);
        cmbMonth.setVisible(isMonthSelected);
    }

    private void updateTopBorrowedStatistics() {
        String periodType = (String) cmbPeriodType.getSelectedItem();
        Integer selectedYear = (Integer) cmbYear.getSelectedItem();
        Integer selectedMonth = (Integer) cmbMonth.getSelectedItem();

        if (selectedYear == null) {
            JOptionPane.showMessageDialog(this, "Please select a year.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("By Month".equals(periodType) && selectedMonth == null) {
            JOptionPane.showMessageDialog(this, "Please select a month.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear data
        topBooksTableModel.setRowCount(0);
        topCategoriesTableModel.setRowCount(0);

        try {
            java.util.List<Map<String, Object>> topBooks;
            java.util.List<Map<String, Object>> topCategories;

            if ("By Month".equals(periodType)) {
                topBooks = StatisticsDAO.getTopBorrowedBooks(selectedYear, selectedMonth);
                topCategories = StatisticsDAO.getTopBorrowedCategories(selectedYear, selectedMonth);
            } else { // By Year
                topBooks = StatisticsDAO.getTopBorrowedBooks(selectedYear);
                topCategories = StatisticsDAO.getTopBorrowedCategories(selectedYear);
            }

            // Top Books tbl
            int rank = 1;
            for (Map<String, Object> bookData : topBooks) {
                topBooksTableModel.addRow(new Object[]{
                    rank++,
                    bookData.get("isbn"),
                    bookData.get("title"),
                    bookData.get("borrow_count")
                });
            }

            // Top Categories Table
            rank = 1;
            for (Map<String, Object> categoryData : topCategories) {
                topCategoriesTableModel.addRow(new Object[]{
                    rank++,
                    categoryData.get("category"),
                    categoryData.get("borrow_count")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching statistics data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnBack = new rojerusan.RSButtonHover();
        jPanel4 = new javax.swing.JPanel();
        genrePanel = new rojeru_san.complementos.RSButtonHover();
        jLabel7 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        topBorrowedPanel = new rojeru_san.complementos.RSButtonHover();
        genderPanel = new rojeru_san.complementos.RSButtonHover();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        lblNoR = new javax.swing.JLabel();
        lblNoISBN = new javax.swing.JLabel();
        lblNoB = new javax.swing.JLabel();
        lblNoOver = new javax.swing.JLabel();
        lblNoBR = new javax.swing.JLabel();
        TopBorrowedPanel = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cmbPeriodType = new javax.swing.JComboBox<>();
        lblYear = new javax.swing.JLabel();
        cmbYear = new javax.swing.JComboBox<>();
        lblMonth = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        btnRefreshTopBorrowed = new rojerusan.RSButtonHover();
        resultPanel = new javax.swing.JPanel();
        topBooksPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        scrollPaneTopBooks = new javax.swing.JScrollPane();
        tblTopBooks = new javax.swing.JTable();
        topCategoriesPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        scrollPaneTopCategories = new javax.swing.JScrollPane();
        tblTopCategories = new javax.swing.JTable();
        GenderPanel = new javax.swing.JPanel();
        BooksbyGenrePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

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
        jLabel2.setText("STATISTICS");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 150, -1));

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

        genrePanel.setBackground(new java.awt.Color(0, 51, 51));
        genrePanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        genrePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/data-classification_24x24.png"))); // NOI18N
        genrePanel.setText("Genre/Catagory");
        genrePanel.setColorHover(new java.awt.Color(102, 153, 255));
        genrePanel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        genrePanel.setIconTextGap(10);
        genrePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                genrePanelMouseClicked(evt);
            }
        });
        genrePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genrePanelActionPerformed(evt);
            }
        });
        jPanel4.add(genrePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 180, -1));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/Statistics.jpg"))); // NOI18N
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 550, 200, 150));

        jPanel5.setBackground(new java.awt.Color(102, 153, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("VIEW");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 50));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, -1));

        topBorrowedPanel.setBackground(new java.awt.Color(0, 51, 51));
        topBorrowedPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        topBorrowedPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/ranking_24x24.png"))); // NOI18N
        topBorrowedPanel.setText("Top Borrowed");
        topBorrowedPanel.setColorHover(new java.awt.Color(102, 153, 255));
        topBorrowedPanel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        topBorrowedPanel.setIconTextGap(10);
        topBorrowedPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                topBorrowedPanelMouseClicked(evt);
            }
        });
        topBorrowedPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topBorrowedPanelActionPerformed(evt);
            }
        });
        jPanel4.add(topBorrowedPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 180, -1));

        genderPanel.setBackground(new java.awt.Color(0, 51, 51));
        genderPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        genderPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/gender-pay-gap.png"))); // NOI18N
        genderPanel.setText("Reader's Gender");
        genderPanel.setColorHover(new java.awt.Color(102, 153, 255));
        genderPanel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        genderPanel.setIconTextGap(10);
        genderPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                genderPanelMouseClicked(evt);
            }
        });
        genderPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genderPanelActionPerformed(evt);
            }
        });
        jPanel4.add(genderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 200, 700));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(102, 153, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(3, 0, 0, 3, new java.awt.Color(255, 255, 255)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblNoR.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNoR.setForeground(new java.awt.Color(255, 255, 255));
        lblNoR.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/reading32x32.png"))); // NOI18N
        lblNoR.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        jPanel6.add(lblNoR, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 10, 100, -1));

        lblNoISBN.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNoISBN.setForeground(new java.awt.Color(255, 255, 255));
        lblNoISBN.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoISBN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/isbn.png"))); // NOI18N
        lblNoISBN.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        jPanel6.add(lblNoISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 100, -1));

        lblNoB.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNoB.setForeground(new java.awt.Color(255, 255, 255));
        lblNoB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-stack.png"))); // NOI18N
        lblNoB.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        jPanel6.add(lblNoB, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, 100, -1));

        lblNoOver.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNoOver.setForeground(new java.awt.Color(255, 255, 255));
        lblNoOver.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoOver.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/overdue32x32.png"))); // NOI18N
        lblNoOver.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        jPanel6.add(lblNoOver, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 10, 100, -1));

        lblNoBR.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNoBR.setForeground(new java.awt.Color(255, 255, 255));
        lblNoBR.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoBR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/borrow32x32.png"))); // NOI18N
        lblNoBR.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 2, new java.awt.Color(255, 255, 255)));
        jPanel6.add(lblNoBR, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 10, 100, -1));

        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1300, 50));

        TopBorrowedPanel.setBackground(new java.awt.Color(255, 255, 255));
        TopBorrowedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        filterPanel.setBackground(new java.awt.Color(0, 51, 51));
        RoundedPanel filterPanel = new RoundedPanel(30);
        filterPanel.setBackground(new Color(0, 51, 51));

        filterPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/filter.png"))); // NOI18N
        filterPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 80));

        cmbPeriodType.setBackground(new java.awt.Color(255, 255, 255));
        cmbPeriodType.setForeground(new java.awt.Color(0, 0, 0));
        cmbPeriodType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "By Month", "By Year" }));
        cmbPeriodType.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        filterPanel.add(cmbPeriodType, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 160, 40));

        lblYear.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblYear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/year.png"))); // NOI18N
        filterPanel.add(lblYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 0, -1, 80));

        cmbYear.setBackground(new java.awt.Color(255, 255, 255));
        cmbYear.setForeground(new java.awt.Color(0, 0, 0));
        cmbYear.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        filterPanel.add(cmbYear, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 160, 40));

        lblMonth.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMonth.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/month.png"))); // NOI18N
        filterPanel.add(lblMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 0, -1, 80));

        cmbMonth.setBackground(new java.awt.Color(255, 255, 255));
        cmbMonth.setForeground(new java.awt.Color(0, 0, 0));
        cmbMonth.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 153, 255), 5, true));
        filterPanel.add(cmbMonth, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 20, 160, 40));

        btnRefreshTopBorrowed.setText("View/Referesh");
        RoundedButton btnRefreshTopBorrowed = new RoundedButton("View/Refresh");
        ((RoundedButton) btnRefreshTopBorrowed).setBackgroundColor(new java.awt.Color(102, 153, 255));
        ((RoundedButton) btnRefreshTopBorrowed).setHoverColor(new java.awt.Color(255, 0, 51));
        ((RoundedButton) btnRefreshTopBorrowed).setCornerRadius(50);

        btnRefreshTopBorrowed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTopBorrowedActionPerformed(evt);
            }
        });
        filterPanel.add(btnRefreshTopBorrowed, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 20, 200, 40));

        TopBorrowedPanel.add(filterPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1280, 80));

        resultPanel.setBackground(new java.awt.Color(255, 255, 255));
        resultPanel.setForeground(new java.awt.Color(255, 255, 255));
        resultPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topBooksPanel.setBackground(new java.awt.Color(255, 255, 255));
        topBooksPanel.setForeground(new java.awt.Color(255, 255, 255));
        topBooksPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 51, 51));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Top 10 most borrowed books");
        jLabel8.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 3, 3, 3, new java.awt.Color(255, 255, 255)));
        topBooksPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, -1));

        scrollPaneTopBooks.setBackground(new java.awt.Color(0, 51, 51));
        scrollPaneTopBooks.setForeground(new java.awt.Color(255, 255, 255));
        scrollPaneTopBooks.setOpaque(true);

        tblTopBooks.setBackground(new java.awt.Color(0, 51, 51));
        tblTopBooks.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        tblTopBooks.setForeground(new java.awt.Color(255, 255, 255));
        tblTopBooks.setGridColor(new java.awt.Color(102, 153, 255));
        tblTopBooks.setName(""); // NOI18N
        tblTopBooks.setOpaque(false);
        tblTopBooks.setRowHeight(48);
        tblTopBooks.setSelectionBackground(new java.awt.Color(102, 153, 255));
        tblTopBooks.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblTopBooks.setShowGrid(false);
        tblTopBooks.setShowHorizontalLines(true);
        scrollPaneTopBooks.setViewportView(tblTopBooks);

        topBooksPanel.add(scrollPaneTopBooks, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 28, 640, 512));

        resultPanel.add(topBooksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, 540));

        topCategoriesPanel.setBackground(new java.awt.Color(255, 255, 255));
        topCategoriesPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 51, 51));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Top 5 most borrowed genres");
        jLabel10.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 3, 3, 0, new java.awt.Color(255, 255, 255)));
        topCategoriesPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 640, -1));

        scrollPaneTopCategories.setBackground(new java.awt.Color(0, 51, 51));
        scrollPaneTopCategories.setForeground(new java.awt.Color(255, 255, 255));
        scrollPaneTopCategories.setOpaque(true);

        tblTopCategories.setBackground(new java.awt.Color(0, 51, 51));
        tblTopCategories.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        tblTopCategories.setForeground(new java.awt.Color(255, 255, 255));
        tblTopCategories.setRowHeight(100);
        tblTopCategories.setSelectionBackground(new java.awt.Color(102, 153, 255));
        tblTopCategories.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrollPaneTopCategories.setViewportView(tblTopCategories);

        topCategoriesPanel.add(scrollPaneTopCategories, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 28, 640, 512));

        resultPanel.add(topCategoriesPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 0, 640, 540));

        TopBorrowedPanel.add(resultPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 1280, 540));

        jPanel1.add(TopBorrowedPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        GenderPanel.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.add(GenderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        BooksbyGenrePanel.setBackground(new java.awt.Color(0, 51, 51));
        BooksbyGenrePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(BooksbyGenrePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/book-wall-1151405_1920.jpg"))); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1300, 650));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 1300, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        HomePage home = new HomePage();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void genrePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_genrePanelMouseClicked
        BooksbyGenrePanel.setVisible(true);
        GenderPanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);

    }//GEN-LAST:event_genrePanelMouseClicked

    private void genrePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genrePanelActionPerformed
        BooksbyGenrePanel.setVisible(true);
        GenderPanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);

    }//GEN-LAST:event_genrePanelActionPerformed

    private void genderPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_genderPanelMouseClicked
        GenderPanel.setVisible(true);
        BooksbyGenrePanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);

    }//GEN-LAST:event_genderPanelMouseClicked

    private void genderPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genderPanelActionPerformed
        GenderPanel.setVisible(true);
        BooksbyGenrePanel.setVisible(false);
        TopBorrowedPanel.setVisible(false);
    }//GEN-LAST:event_genderPanelActionPerformed

    private void topBorrowedPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topBorrowedPanelMouseClicked
        GenderPanel.setVisible(false);
        BooksbyGenrePanel.setVisible(false);
        TopBorrowedPanel.setVisible(true);
    }//GEN-LAST:event_topBorrowedPanelMouseClicked

    private void topBorrowedPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topBorrowedPanelActionPerformed
        GenderPanel.setVisible(false);
        BooksbyGenrePanel.setVisible(false);
        TopBorrowedPanel.setVisible(true);

    }//GEN-LAST:event_topBorrowedPanelActionPerformed

    private void btnRefreshTopBorrowedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTopBorrowedActionPerformed
        TopBorrowedPanel.setVisible(true);
        BooksbyGenrePanel.setVisible(false);
        GenderPanel.setVisible(false);
        updateTopBorrowedStatistics();
    }//GEN-LAST:event_btnRefreshTopBorrowedActionPerformed

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
            java.util.logging.Logger.getLogger(Statistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Statistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Statistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Statistics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Statistics().setVisible(true);
            }
        });
    }
    private ChartPanel genderChartPanel;
    private JTable tblGenderDetails;
    private DefaultTableModel genderTableModel;

    private JLabel lblTitle_Genre;
    private boolean isVolumeMode = true;
    private ChartPanel chartPanel;
    private JTable tblCategoryDetails;
    private DefaultTableModel tableModel;

    private DefaultTableModel topBooksTableModel;
    private DefaultTableModel topCategoriesTableModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BooksbyGenrePanel;
    private javax.swing.JPanel GenderPanel;
    private javax.swing.JPanel TopBorrowedPanel;
    private rojerusan.RSButtonHover btnBack;
    private rojerusan.RSButtonHover btnRefreshTopBorrowed;
    private javax.swing.JComboBox<Integer> cmbMonth;
    private javax.swing.JComboBox<String> cmbPeriodType;
    private javax.swing.JComboBox<Integer> cmbYear;
    private javax.swing.JPanel filterPanel;
    private rojeru_san.complementos.RSButtonHover genderPanel;
    private rojeru_san.complementos.RSButtonHover genrePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblNoB;
    private javax.swing.JLabel lblNoBR;
    private javax.swing.JLabel lblNoISBN;
    private javax.swing.JLabel lblNoOver;
    private javax.swing.JLabel lblNoR;
    private javax.swing.JLabel lblYear;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JScrollPane scrollPaneTopBooks;
    private javax.swing.JScrollPane scrollPaneTopCategories;
    private javax.swing.JTable tblTopBooks;
    private javax.swing.JTable tblTopCategories;
    private javax.swing.JPanel topBooksPanel;
    private rojeru_san.complementos.RSButtonHover topBorrowedPanel;
    private javax.swing.JPanel topCategoriesPanel;
    // End of variables declaration//GEN-END:variables
}
