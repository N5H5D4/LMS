/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jframe;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author hs
 */
public class DBConnection {
   
    static Connection con = null;
    
    public static Connection getConnection(){
        try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3306/LMS?useSSL=false&allowPublicKeyRetrieval=true";
                String user = "root";
                String password = "1234";
                con = DriverManager.getConnection(url, user, password);
                System.out.println("Connected to database!");
        } catch (Exception e) {
                e.printStackTrace();
}

        return con;
    }
}
