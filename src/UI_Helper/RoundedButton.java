/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI_Helper;

/**
 *
 * @author HS
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class RoundedButton extends JButton {

    private Color backgroundColor = new Color(0, 150, 136);
    private Color hoverColor = new Color(0, 172, 150);
    private Color textColor = Color.WHITE;
    private Color hoverTextColor = Color.WHITE;
    private int cornerRadius = 30;
    private boolean hovered = false;
    private String borderType = "none";
    private Color borderColor = Color.BLACK;
    private int borderThickness = 1;

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(true); 
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hovered = false;
                repaint();
            }
        });
    }
    public void setButtonSize(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setSize(new Dimension(width, height));
        revalidate();
        repaint();
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
        repaint();
    }

    public void setTextColor(Color color) {
        this.textColor = color;
        setForeground(color);
        repaint();
    }

    public void setHoverTextColor(Color color) {
        this.hoverTextColor = color;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setBorderType(String type) {
        if ("none".equalsIgnoreCase(type) || "matte".equalsIgnoreCase(type) || "line".equalsIgnoreCase(type)) {
            this.borderType = type.toLowerCase();
            repaint();
        }
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = Math.max(1, thickness);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(hovered ? hoverColor : backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        setForeground(hovered ? hoverTextColor : textColor);

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    public void paintBorder(Graphics g) {
        if ("none".equals(borderType)) {
            return; 
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);

        if ("matte".equals(borderType)) {
            // MatteBorder
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(borderThickness / 2, borderThickness / 2,
                    getWidth() - borderThickness, getHeight() - borderThickness,
                    cornerRadius, cornerRadius);
        } else if ("line".equals(borderType)) {
            // LineBorder
            g2.setStroke(new BasicStroke(borderThickness));
            g2.drawRoundRect(borderThickness / 2, borderThickness / 2,
                    getWidth() - borderThickness, getHeight() - borderThickness,
                    cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
