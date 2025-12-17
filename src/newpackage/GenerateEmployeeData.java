package newpackage;

import employee.management.system.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateEmployeeData {

    private static final String[] POSITIONS = {
        "Chief Operating Officer (COO)",
        "Chief Financial Officer (CFO)",
        "Chief Technology Officer (CTO)",
        "Vice President of Marketing",
        "Office Manager",
        "Operations Coordinator",
        "Administrative Assistant",
        "Project Manager",
        "Facilities Supervisor",
        "Accountant",
        "Financial Analyst",
        "Payroll Specialist",
        "Internal Auditor",
        "Accounts Payable Clerk",
        "HR Manager",
        "Recruiter / Talent Acquisition Specialist",
        "Training & Development Coordinator",
        "Compensation & Benefits Analyst",
        "Employee Relations Specialist",
        "Marketing Manager",
        "Social Media Specialist",
        "Sales Representative",
        "Business Development Manager",
        "Customer Success Manager",
        "Software Engineer",
        "Data Analyst",
        "IT Support Specialist",
        "Cybersecurity Analyst",
        "Systems Administrator",
        "Product Manager",
        "UX/UI Designer",
        "Graphic Designer",
        "Quality Assurance Tester",
        "Research & Development Specialist",
        "Call Center Agent",
        "Technical Support Representative",
        "Client Relations Coordinator",
        "Customer Experience Specialist"
    };

    private static final String[] DEPARTMENTS = {
        "Executive / Leadership",
        "Administration & Operations",
        "Finance & Accounting",
        "Human Resources",
        "Marketing & Sales",
        "Information Technology (IT)",
        "Product Development & Design",
        "Customer Service",
        "Research & Development (R&D)",
        "Legal & Compliance",
        "Procurement & Supply Chain",
        "Manufacturing / Production",
        "Quality Assurance",
        "Public Relations & Communications"
    };

    private static final String[] GENDERS = {"Male", "Female"};

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection(); Statement stmt = connection.createStatement()) {

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

            // Insert sample data
            String insertEmployee = "INSERT INTO employees_table (photo_path, full_name, birth_date, gender, address, contact_number, email_address, position, department, salary, hired_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(insertEmployee)) {
                for (int i = 1; i <= 10; i++) {
                    pstmt.setString(1, "photos/emp" + i + ".jpg"); // Sample photo path
                    pstmt.setString(2, generateRandomName()); // Random full name
                    pstmt.setDate(3, generateRandomDate(1960, 2000)); // Random birth date
                    pstmt.setString(4, GENDERS[new Random().nextInt(GENDERS.length)]); // Random gender
                    pstmt.setString(5, "Some Address " + i + ", City"); // Sample address
                    pstmt.setString(6, "+123456789" + i); // Sample contact number
                    pstmt.setString(7, "employee" + i + "@company.com"); // Sample email
                    pstmt.setString(8, POSITIONS[new Random().nextInt(POSITIONS.length)]); // Random position
                    pstmt.setString(9, DEPARTMENTS[new Random().nextInt(DEPARTMENTS.length)]); // Random department
                    pstmt.setDouble(10, 30000 + new Random().nextInt(70001)); // Random salary range between 30,000 and 100,000
                    pstmt.setDate(11, generateRandomDate(2005, 2025)); // Random hired date
                    pstmt.executeUpdate();
                }

                System.out.println("Sample employees data inserted successfully!");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomName() {
        String[] firstNames = {"John", "Jane", "Chris", "Anna", "Adam", "Sophia", "Michael", "Emma", "Ethan", "Olivia"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin"};
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    private static java.sql.Date generateRandomDate(int startYear, int endYear) {
        long startMillis = java.sql.Date.valueOf(startYear + "-01-01").getTime();
        long endMillis = java.sql.Date.valueOf(endYear + "-12-31").getTime();
        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
        return new java.sql.Date(randomMillis);
    }
}
