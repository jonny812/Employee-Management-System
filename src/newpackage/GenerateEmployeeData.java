package newpackage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateEmployeeData {

    private static final String[] GENDERS = {"Male", "Female", "Other"};

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            // -------------------- EMPLOYEES TABLE --------------------
            String createEmployeesTable
                    = "CREATE TABLE IF NOT EXISTS employees_table ("
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

            connection.createStatement().execute(createEmployeesTable);
            connection.createStatement().executeUpdate(
                    "ALTER TABLE employees_table AUTO_INCREMENT = 1000");

            // -------------------- GET VALID DEPT + POSITION + SALARY --------------------
            String fetchDeptPosSalary
                    = "SELECT dp.department_name, dp.position_name, ps.salary_rate "
                    + "FROM department_positions dp "
                    + "JOIN position_salary_rate ps "
                    + "ON dp.position_name = ps.position_name";

            List<Object[]> validRoles = new ArrayList<>();

            try (PreparedStatement ps = connection.prepareStatement(fetchDeptPosSalary); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    validRoles.add(new Object[]{
                        rs.getString("department_name"),
                        rs.getString("position_name"),
                        rs.getBigDecimal("salary_rate")
                    });
                }
            }

            if (validRoles.isEmpty()) {
                System.out.println("No department-position data found.");
                return;
            }

            // -------------------- INSERT SAMPLE EMPLOYEES --------------------
            String insertEmployee
                    = "INSERT INTO employees_table "
                    + "(photo_path, full_name, birth_date, gender, address, contact_number, "
                    + "email_address, position, department, salary, hired_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(insertEmployee)) {

                Random random = new Random();

                for (int i = 1; i <= 10; i++) {

                    Object[] role = validRoles.get(random.nextInt(validRoles.size()));

                    String department = role[0].toString();
                    String position = role[1].toString();
                    BigDecimal salary = (BigDecimal) role[2];

                    pstmt.setString(1, "photos/emp" + i + ".jpg");
                    pstmt.setString(2, generateRandomName());
                    pstmt.setDate(3, generateRandomDate(1960, 2000));
                    pstmt.setString(4, GENDERS[random.nextInt(GENDERS.length)]);
                    pstmt.setString(5, "Sample Address " + i);
                    pstmt.setString(6, "+6390000000" + i);
                    pstmt.setString(7, "employee" + i + "@company.com");
                    pstmt.setString(8, position);
                    pstmt.setString(9, department);
                    pstmt.setBigDecimal(10, salary);
                    pstmt.setDate(11, generateRandomDate(2010, 2025));

                    pstmt.executeUpdate();
                }
            }

            System.out.println("Sample employees inserted successfully (DB-linked).");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------- HELPERS --------------------
    private static String generateRandomName() {
        String[] first = {"John", "Jane", "Chris", "Anna", "Adam", "Sophia", "Michael", "Emma"};
        String[] last = {"Smith", "Johnson", "Brown", "Taylor", "Anderson", "Martin"};
        Random r = new Random();
        return first[r.nextInt(first.length)] + " " + last[r.nextInt(last.length)];
    }

    private static java.sql.Date generateRandomDate(int startYear, int endYear) {
        long start = java.sql.Date.valueOf(startYear + "-01-01").getTime();
        long end = java.sql.Date.valueOf(endYear + "-12-31").getTime();
        long random = ThreadLocalRandom.current().nextLong(start, end);
        return new java.sql.Date(random);
    }
}
