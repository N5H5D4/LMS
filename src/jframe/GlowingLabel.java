/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jframe;

import java.awt.*;
import javax.swing.*;
/**
 *
 * @author HS
 */
public class GlowingLabel extends JLabel {
    public GlowingLabel(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 36));  // Tùy chọn font và cỡ chữ
        setForeground(Color.BLACK);  // Màu chữ chính
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        String text = getText();
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2 - 5;

        // Bật chế độ khử răng cưa để viền mượt hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Màu viền sáng
        g2d.setColor(Color.CYAN);
        int glowSize = 4;

        // Tạo hiệu ứng phát sáng
        for (int i = -glowSize; i <= glowSize; i++) {
            for (int j = -glowSize; j <= glowSize; j++) {
                if (Math.abs(i) + Math.abs(j) <= glowSize) {
                    g2d.drawString(text, x + i, y + j);
                }
            }
        }

        // Vẽ chữ chính
        g2d.setColor(getForeground());
        g2d.drawString(text, x, y);

        g2d.dispose();
    }
}
