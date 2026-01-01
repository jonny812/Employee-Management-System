package newpackage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Admin
 */
public class QrGenerator {

    Connection connection;

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/employee_management_database", "root", "");
            Statement stmt = connection.createStatement();

            // -------------------- USERS TABLE --------------------
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users_table ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "admin BOOLEAN NOT NULL, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL)";
            stmt.execute(createUsersTable);

            // Default admin account
            stmt.executeUpdate(
                    "INSERT IGNORE INTO users_table (admin, username, password) VALUES "
                    + "(1, 'admin', 'admin') "
            );

            // -------------------- EMPLOYEES TABLE --------------------
            String createEmployeesTable = "CREATE TABLE IF NOT EXISTS employees_table ("
                    + "employee_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "photo_path VARCHAR(255) NOT NULL, "
                    + "full_name VARCHAR(200) NOT NULL, "
                    + "birth_date DATE NOT NULL, "
                    + "gender ENUM('Male','Female','Other') NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "contact_number VARCHAR(20) NOT NULL, "
                    + "email_address VARCHAR(200) UNIQUE NOT NULL, "
                    + "position VARCHAR(100) NOT NULL, "
                    + "department VARCHAR(100) NOT NULL, "
                    + "salary DECIMAL(10,2) NOT NULL, "
                    + "hired_date DATE NOT NULL)";
            stmt.execute(createEmployeesTable);

            // Start employee_id at 1000 for cleaner IDs
            stmt.executeUpdate("ALTER TABLE employees_table AUTO_INCREMENT = 1000;");

            // -------------------- INSERT SAMPLE EMPLOYEES --------------------
            String insertEmployees = "INSERT IGNORE INTO employees_table "
                    + "(photo_path, full_name, birth_date, gender, address, contact_number, email_address, position, department, salary, hired_date) VALUES "
                    + "('photos/emp1.jpg', 'Juan Dela Cruz', '1995-04-12', 'Male', 'Manila City', '09171234567', 'juan.cruz@example.com', 'Software Engineer', 'IT Department', 35000.00, '2022-03-10'), "
                    + "('photos/emp2.jpg', 'Maria Santos', '1998-07-21', 'Female', 'Quezon City', '09281234567', 'maria.santos@example.com', 'HR Officer', 'Human Resources', 30000.00, '2021-11-05'), "
                    + "('photos/emp3.jpg', 'Mark Reyes', '1992-01-18', 'Male', 'Pasig City', '09181231234', 'mark.reyes@example.com', 'Accountant', 'Finance', 32000.00, '2020-06-15'), "
                    + "('photos/emp4.jpg', 'Angela Cruz', '1996-10-04', 'Female', 'Cebu City', '09351231231', 'angela.cruz@.com', 'Graphic Designer', 'Marketing', 28000.00, '2023-01-12'), "
                    + "('photos/emp5.jpg', 'John Bautista', '1993-03-09', 'Male', 'Davao City', '09491234567', 'john.bautista@example.com', 'IT Support', 'IT Department', 26000.00, '2021-05-20'), "
                    + "('photos/emp6.jpg', 'Catherine Lim', '1997-12-11', 'Female', 'Makati City', '09291231231', 'catherine.lim@example.com', 'Sales Associate', 'Sales', 25000.00, '2022-10-01'), "
                    + "('photos/emp7.jpg', 'Joseph Tan', '1990-02-27', 'Male', 'Taguig City', '09191231212', 'joseph.tan@example.com', 'Project Manager', 'Operations', 45000.00, '2019-04-08'), "
                    + "('photos/emp8.jpg', 'Elaine Garcia', '1999-05-30', 'Female', 'Las Piñas City', '09301231231', 'elaine.garcia@example.com', 'Receptionist', 'Front Desk', 20000.00, '2023-08-03'), "
                    + "('photos/emp9.jpg', 'Patrick Villanueva', '1994-09-23', 'Male', 'Caloocan City', '09181231234', 'patrick.villanueva@example.com', 'Network Technician', 'IT Department', 27000.00, '2021-09-10'), "
                    + "('photos/emp10.jpg', 'Liza Ramos', '1991-06-25', 'Female', 'Baguio City', '09271231231', 'liza.ramos@example.com', 'Administrative Assistant', 'Admin', 23000.00, '2020-02-17');";
            stmt.executeUpdate(insertEmployees);

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(QrGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT employee_id FROM employees_table ORDER BY employee_id DESC LIMIT 1");
            if (rs.next()) {
                File qrDir = new File("qrcodes");
                if (!qrDir.exists()) {
                    qrDir.mkdir();
                }
            }

            int id = rs.getInt("employee_id");

            for (int i = 1000; i <= id; i++) {
                String text = Integer.toString(i);
                String filePath = "qrcodes/" + text + ".png";
                BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 500, 500);
                Path path = FileSystems.getDefault().getPath(filePath);
                MatrixToImageWriter.writeToPath(matrix, "PNG", path);
                System.out.println("QR Code created: " + filePath);
            }

        } catch (WriterException | SQLException | IOException ex) {
            Logger.getLogger(QrGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
