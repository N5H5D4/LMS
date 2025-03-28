/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jframe;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import static jframe.DBConnection.con;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 *
 * @author HS
 */
public class SignUpPage extends javax.swing.JFrame {

    /**
     * Creates new form SignUpPage
     */
    public SignUpPage() {
        initComponents();
    }

    public void insertSignupDetails() {
        String name = txt_UserName.getText();
        String password = txt_Pass.getText();
        String email = txt_Email.getText();
        String phone = txt_PhoneNumber.getText();

        try {
            Connection con = DBConnection.getConnection();
            String sql = "insert into librarians(name, password, email, phone) values(?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, name);
            pst.setString(2, password);
            pst.setString(3, email);
            pst.setString(4, phone);

            int updatedRowCount = pst.executeUpdate();

            if (updatedRowCount > 0) {
                JOptionPane.showMessageDialog(this, "Recorded Inserted Successfully");
                LoginPage page = new LoginPage();
                page.setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Recorded Insertion Failure");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean validateSignup() {
        String name = txt_UserName.getText();
        String password = txt_Pass.getText();
        String email = txt_Email.getText();
        String phone = txt_PhoneNumber.getText();

        if (name.equals("")) {
            JOptionPane.showMessageDialog(this, "please enter username");
            return false;
        }

        if (password.equals("")) {
            JOptionPane.showMessageDialog(this, "please enter password");
            return false;
        }

        if (email.equals("") || !email.matches("^.+@.+\\..+$")) {
            JOptionPane.showMessageDialog(this, "please enter valid email");
            return false;
        }

        if (phone.equals("")) {
            JOptionPane.showMessageDialog(this, "please enter contact number");
            return false;
        }

        return true;
    }

    public boolean validateLogin() {
        String name = txt_UserName.getText();
        String pass = txt_Pass.getText();

        if (name.equals("")) {
            JOptionPane.showMessageDialog(this, "please enter username");
            return false;
        }
        if (pass.equals("")) {
            JOptionPane.showMessageDialog(this, "please enter password");
            return false;
        }

        return true;
    }

    //verify creds
    public void login() {
        String name = txt_UserName.getText();
        String pass = txt_Pass.getText();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("select * from librarians where name = ? and password = ?");

            pst.setString(1, name);
            pst.setString(2, pass);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "login successful");
                HomePage home = new HomePage();
                home.setVisible(true);
                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "incorrect username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //check duplicate users
    public boolean checkDuplicateUser() {
        String name = txt_UserName.getText();
        String pass = txt_Pass.getText();
        String email = txt_Email.getText();
        boolean isExist = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DBConnection.getConnection();

            PreparedStatement pst = con.prepareStatement("select * from librarians where name = ? and password = ? and email = ?");
            pst.setString(1, name);
            pst.setString(2, pass);
            pst.setString(3, email);            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                isExist = true;
            } else {
                isExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
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
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txt_UserName = new app.bolivia.swing.JCTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txt_Email = new app.bolivia.swing.JCTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txt_PhoneNumber = new app.bolivia.swing.JCTextField();
        rSMaterialButtonCircle1 = new rojerusan.RSMaterialButtonCircle();
        rSMaterialButtonCircle2 = new rojerusan.RSMaterialButtonCircle();
        jLabel1 = new javax.swing.JLabel();
        txt_Pass = new rojerusan.RSPasswordTextPlaceHolder();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("SIGNUP / SIGNIN");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 40, -1, -1));

        jLabel8.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 260, -1, -1));

        txt_UserName.setBackground(new java.awt.Color(255, 255, 255));
        txt_UserName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txt_UserName.setForeground(new java.awt.Color(0, 0, 0));
        txt_UserName.setToolTipText("");
        txt_UserName.setCaretColor(new java.awt.Color(0, 0, 0));
        txt_UserName.setDisabledTextColor(new java.awt.Color(0, 51, 51));
        txt_UserName.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txt_UserName.setPlaceholder("ENTER YOUR NAME");
        txt_UserName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_UserNameFocusLost(evt);
            }
        });
        txt_UserName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_UserNameActionPerformed(evt);
            }
        });
        jPanel2.add(txt_UserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 180, 380, 40));

        jLabel9.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("CREATE NEW ACCOUNT OR LOGIN");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 110, -1, -1));

        jLabel10.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/user.png"))); // NOI18N
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 170, -1, -1));

        jLabel11.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("USER NAME");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 160, -1, 20));

        jLabel12.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("PASSWORD");
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 270, -1, -1));

        jLabel13.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/padlock.png"))); // NOI18N
        jPanel2.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 280, -1, -1));

        jLabel14.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setText("EMAIL");
        jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 390, -1, -1));

        jLabel15.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 0, 0));
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/email2.png"))); // NOI18N
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 400, -1, -1));

        txt_Email.setBackground(new java.awt.Color(255, 255, 255));
        txt_Email.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txt_Email.setForeground(new java.awt.Color(0, 0, 0));
        txt_Email.setToolTipText("");
        txt_Email.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txt_Email.setPlaceholder("ENTER YOUR NAME");
        txt_Email.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_EmailActionPerformed(evt);
            }
        });
        jPanel2.add(txt_Email, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 410, 380, 40));

        jLabel16.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
        jLabel16.setText("NUMBER PHONE");
        jPanel2.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 500, -1, -1));

        jLabel17.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/phone-call.png"))); // NOI18N
        jPanel2.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 510, -1, -1));

        txt_PhoneNumber.setBackground(new java.awt.Color(255, 255, 255));
        txt_PhoneNumber.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txt_PhoneNumber.setForeground(new java.awt.Color(0, 0, 0));
        txt_PhoneNumber.setToolTipText("");
        txt_PhoneNumber.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txt_PhoneNumber.setPlaceholder("ENTER YOUR NAME");
        txt_PhoneNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_PhoneNumberActionPerformed(evt);
            }
        });
        jPanel2.add(txt_PhoneNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 520, 380, 40));

        rSMaterialButtonCircle1.setText("SIGNIN");
        rSMaterialButtonCircle1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSMaterialButtonCircle1ActionPerformed(evt);
            }
        });
        jPanel2.add(rSMaterialButtonCircle1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 650, 380, 60));

        rSMaterialButtonCircle2.setBackground(new java.awt.Color(255, 51, 51));
        rSMaterialButtonCircle2.setText("SIGNUP");
        rSMaterialButtonCircle2.setBorderPainted(false);
        rSMaterialButtonCircle2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rSMaterialButtonCircle2ActionPerformed(evt);
            }
        });
        jPanel2.add(rSMaterialButtonCircle2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 580, 380, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PICTURE_icon/li4.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        txt_Pass.setBackground(new java.awt.Color(255, 255, 255));
        txt_Pass.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        txt_Pass.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txt_Pass.setPhColor(new java.awt.Color(0, 51, 51));
        txt_Pass.setPlaceholder("ENTER PASSWORD");
        jPanel2.add(txt_Pass, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 300, 380, 40));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 750, 750));

        jPanel1.setBackground(new java.awt.Color(51, 255, 0));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("WELLCOME TO LIBARY MANAGEMENT APPLICATION!");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 150, -1, -1));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/woman-librarian-concept-free-vector-removebg-preview.png"))); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 620, 590));

        jLabel6.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 36)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Hi! Librarians!");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 90, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 0, 750, 750));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txt_UserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_UserNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_UserNameActionPerformed

    private void txt_EmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_EmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_EmailActionPerformed

    private void txt_PhoneNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_PhoneNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_PhoneNumberActionPerformed

    private void rSMaterialButtonCircle1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rSMaterialButtonCircle1ActionPerformed
         if (validateLogin()) {
           login();
        }
    }//GEN-LAST:event_rSMaterialButtonCircle1ActionPerformed

    private void rSMaterialButtonCircle2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rSMaterialButtonCircle2ActionPerformed
        if (validateSignup() == true) {
            if (checkDuplicateUser() == false) {
                insertSignupDetails();
            } else {
                JOptionPane.showMessageDialog(this, "username already exist");
            }

        }

    }//GEN-LAST:event_rSMaterialButtonCircle2ActionPerformed

    private void txt_UserNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_UserNameFocusLost
        if (checkDuplicateUser() == true) {
            JOptionPane.showMessageDialog(this, "username already exist");
        }
    }//GEN-LAST:event_txt_UserNameFocusLost

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
            java.util.logging.Logger.getLogger(SignUpPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SignUpPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SignUpPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SignUpPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SignUpPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private rojerusan.RSMaterialButtonCircle rSMaterialButtonCircle1;
    private rojerusan.RSMaterialButtonCircle rSMaterialButtonCircle2;
    private app.bolivia.swing.JCTextField txt_Email;
    private rojerusan.RSPasswordTextPlaceHolder txt_Pass;
    private app.bolivia.swing.JCTextField txt_PhoneNumber;
    private app.bolivia.swing.JCTextField txt_UserName;
    // End of variables declaration//GEN-END:variables
}
