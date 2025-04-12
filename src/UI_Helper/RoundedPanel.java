/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI_Helper;

/**
 *
 * @author HS
 */
// RoundedPanel.java
import java.awt.*;
import javax.swing.*;

public class RoundedPanel extends JPanel {

    private int cornerRadius = 20;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}
