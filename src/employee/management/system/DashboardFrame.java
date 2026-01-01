package employee.management.system;

import newpackage.DatabaseConnection;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class DashboardFrame extends javax.swing.JFrame {

    private javax.swing.JLabel lblPhotoPreview;
    private javax.swing.JLabel lblPhotoPath;

    Preferences prefs = Preferences.userRoot().node("EmployeeManagementSystem.Preferences");

    String imagePath = null;
    File selectedFile;
    int selectedEmployeeId; // to store the selected employee for editing

    int userId, selectedUserId;
    String userPassword;

    CardLayout card;

    // Get today's date
    LocalDate today = LocalDate.now();

    String payPeriod;

    // Get the first and last day of the current month
    YearMonth yearMonth = YearMonth.from(today);
    LocalDate startOfMonth = yearMonth.atDay(1);
    LocalDate endOfMonth = yearMonth.atEndOfMonth();

    String payslip;

    TimeInOutFrame1 timeInOut;

    DecimalFormat df = new DecimalFormat("#,##0.00");

    private Map<String, String[]> departmentPositions = new HashMap<>();

    public DashboardFrame() {
        initComponents();

        df.setRoundingMode(RoundingMode.HALF_UP);

        Connect();
        fetchUser();
        fetchTheUser();
        fetch();
        fetchPayroll();
        fetchPositionRateData();
        loadComboBox();
        loadEmployeeListComboBox();
        loadPayrollComboBox();
        loadPositionRateComboBox();

        payPeriod = today.format(DateTimeFormatter.ofPattern("yyyy MMM, " + startOfMonth.getDayOfMonth() + " - " + endOfMonth.getDayOfMonth()));

        card = (CardLayout) (jPanel3.getLayout());

        card.show(jPanel3, "card1");

        addButtonHoverEffects();
        // -------------------- DASHBOARD COUNTS --------------------
        jLabel8.setText(today.toString());
        jLabel9.setText(payPeriod);
        updateDashboardCounts(); // Initial update of Active / Inactive / Total

        jTextField3 = (JTextField) jComboBox2.getEditor().getEditorComponent();
        jTextField3.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            private void searchAction() {
                String text = jTextField3.getText().trim();

                try (Connection connection = DatabaseConnection.getConnection()) {
                    String sql = "SELECT * FROM payroll_table WHERE (employee_id LIKE ? OR employee_name LIKE ? OR position LIKE ?) ORDER BY employee_id DESC";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, "%" + text + "%");
                    pstmt.setString(2, "%" + text + "%");
                    pstmt.setString(3, "%" + text + "%");
                    ResultSet rs = pstmt.executeQuery();

                    DefaultTableModel dtm = (DefaultTableModel) payrollTable.getModel();
                    dtm.setRowCount(0);
                    while (rs.next()) {
                        Vector v2 = new Vector();
                        v2.add(rs.getString("employee_id"));
                        v2.add(rs.getString("employee_name"));
                        v2.add(rs.getString("position"));
                        v2.add(rs.getString("net_pay"));
                        v2.add(df.format(rs.getDouble("gross_pay")));
                        dtm.addRow(v2);
                    }
                    pstmt = connection.prepareStatement("SELECT SUM(gross_pay) AS total_gross_pay FROM payroll_table");
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String[] obj = {"Total", null, null, df.format(rs.getDouble("total_gross_pay"))};
                        dtm.addRow(obj);
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Searching Failed!\n" + ex.getLocalizedMessage());
                }
            }
        });

        jTextField4 = (JTextField) jComboBox.getEditor().getEditorComponent();
        jTextField4.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            private void searchAction() {
                String text = jTextField4.getText().trim();

                try (Connection connection = DatabaseConnection.getConnection()) {
                    String sql = "SELECT * FROM employees_table WHERE (employee_id LIKE ? OR full_name LIKE ? OR position LIKE ? OR department LIKE ?) ORDER BY employee_id DESC";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, "%" + text + "%");
                    pstmt.setString(2, "%" + text + "%");
                    pstmt.setString(3, "%" + text + "%");
                    pstmt.setString(4, "%" + text + "%");
                    ResultSet rs = pstmt.executeQuery();

                    DefaultTableModel dtm = (DefaultTableModel) employeesTable.getModel();
                    dtm.setRowCount(0);
                    while (rs.next()) {
                        Vector v2 = new Vector();
                        v2.add(rs.getString("employee_id"));
                        v2.add(rs.getString("full_name"));
                        v2.add(rs.getString("position"));
                        v2.add(rs.getString("department"));
                        dtm.addRow(v2);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Searching Failed!\n" + ex.getLocalizedMessage());
                }
            }
        });

        jTextField1 = (JTextField) jComboBox3.getEditor().getEditorComponent();
        jTextField1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            private void searchAction() {

                String text = jTextField1.getText().trim();

                DefaultTableModel model = (DefaultTableModel) positionRateTable.getModel();
                model.setRowCount(0);

                String sql
                        = "SELECT dp.id, dp.department_name, dp.position_name, ps.salary_rate "
                        + "FROM department_positions dp "
                        + "JOIN position_salary_rate ps "
                        + "ON dp.position_name = ps.position_name "
                        + "WHERE dp.department_name LIKE ? "
                        + "OR dp.position_name LIKE ? "
                        + "OR CAST(ps.salary_rate AS CHAR) LIKE ? "
                        + "ORDER BY dp.department_name, dp.position_name";

                try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, "%" + text + "%");
                    ps.setString(2, "%" + text + "%");
                    ps.setString(3, "%" + text + "%");

                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("department_name"),
                            rs.getString("position_name"),
                            rs.getBigDecimal("salary_rate")
                        });
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Search failed!\n" + ex.getMessage());
                }
            }

        });

        jTextField2 = (JTextField) jComboBox1.getEditor().getEditorComponent();
        jTextField2.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchAction();
            }

            private void searchAction() {
                String text = jTextField2.getText().trim();

                try (Connection connection = DatabaseConnection.getConnection()) {
                    String sql = "SELECT * FROM employees_table "
                            + "LEFT JOIN attendance_table ON employees_table.employee_id = attendance_table.employee_id "
                            + "WHERE employees_table.employee_id LIKE ? "
                            + "OR employees_table.full_name LIKE ? "
                            + "OR attendance_table.date LIKE ? "
                            + "ORDER BY attendance_table.date DESC";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, "%" + text + "%");
                    pstmt.setString(2, "%" + text + "%");
                    pstmt.setString(3, "%" + text + "%");
                    ResultSet rs = pstmt.executeQuery();

                    DefaultTableModel dtm = (DefaultTableModel) attendanceTable.getModel();
                    dtm.setRowCount(0);
                    while (rs.next()) {
                        Vector v2 = new Vector();
                        v2.add(rs.getString("employee_id"));
                        v2.add(rs.getString("full_name"));
                        v2.add(rs.getString("date"));
                        v2.add(rs.getString("am_time_in"));
                        v2.add(rs.getString("am_time_out"));
                        v2.add(rs.getString("pm_time_in"));
                        v2.add(rs.getString("pm_time_out"));
                        dtm.addRow(v2);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Searching Failed!\n" + ex.getLocalizedMessage());
                }
            }
        });

    }

    public void updateDashboardCounts() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Total employees
            PreparedStatement totalStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM employees_table"
            );
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                countTotal.setText(String.valueOf(totalRs.getInt(1)));
            }

            // Active employees (assuming in attendance_table, 'date' = today)
            PreparedStatement activeStmt = connection.prepareStatement(
                    "SELECT COUNT(DISTINCT e.employee_id) FROM employees_table e "
                    + "JOIN attendance_table a ON e.employee_id = a.employee_id "
                    + "WHERE a.date = CURDATE() AND a.status = 'Present'"
            );
            ResultSet activeRs = activeStmt.executeQuery();
            if (activeRs.next()) {
                countActive.setText(String.valueOf(activeRs.getInt(1)));
            }

            // Inactive = total - active
            int total = Integer.parseInt(countTotal.getText());
            int active = Integer.parseInt(countActive.getText());
            countInactive.setText(String.valueOf(total - active));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void Connect() {
        try {
            Connection connection = DatabaseConnection.getRootConnection();
            Statement stmt = connection.createStatement();

            stmt.execute("CREATE DATABASE IF NOT EXISTS employee_management_database");

            connection = DatabaseConnection.getConnection();
            stmt = connection.createStatement();

            // -------------------- USERS TABLE --------------------
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users_table ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "admin BOOLEAN NOT NULL, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL)";
            stmt.execute(createUsersTable);

            // Default admin and user accounts
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

            // -------------------- ATTENDANCE TABLE --------------------
            String createAttendanceTable = "CREATE TABLE IF NOT EXISTS attendance_table ("
                    + "attendance_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "employee_id INT NOT NULL, "
                    + "date DATE NOT NULL, "
                    + "am_time_in TIME, "
                    + "am_time_out TIME, "
                    + "pm_time_in TIME, "
                    + "pm_time_out TIME, "
                    + "total_hours DECIMAL(5,2), "
                    + "am_status VARCHAR(20), "
                    + "pm_status VARCHAR(20), "
                    + "status VARCHAR(20), "
                    + "FOREIGN KEY (employee_id) REFERENCES employees_table(employee_id) "
                    + "ON DELETE CASCADE ON UPDATE CASCADE)";
            stmt.execute(createAttendanceTable);

            // -------------------- DEDUCTIONS TABLE --------------------
            String createDeductionsTable = "CREATE TABLE IF NOT EXISTS deduction_rates_table ("
                    + "deduction_rate_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "sss_rate DOUBLE UNIQUE NOT NULL, "
                    + "philhealth_rate DOUBLE UNIQUE NOT NULL, "
                    + "pagibig_rate DOUBLE UNIQUE NOT NULL)";
            stmt.execute(createDeductionsTable);

            // Insert default rates
            String insertRates = "INSERT IGNORE INTO deduction_rates_table (sss_rate, philhealth_rate, pagibig_rate) VALUES "
                    + "(0.045, 0.025, 0.02)";
            stmt.execute(insertRates);

            // -------------------- PAYROLL TABLE --------------------
            String createPayrollTable = "CREATE TABLE IF NOT EXISTS payroll_table ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "employee_id INT NOT NULL, "
                    + "employee_name VARCHAR(100), "
                    + "position VARCHAR(100), "
                    + "monthly_salary DOUBLE, "
                    + "total_present INT, "
                    + "total_absent INT, "
                    + "total_hours_worked DOUBLE, "
                    + "standard_work_days INT, "
                    + "standard_hours INT, "
                    + "hourly_rate DOUBLE, "
                    + "gross_pay DOUBLE(100, 2), "
                    + "sss DOUBLE, "
                    + "philhealth DOUBLE, "
                    + "pagibig DOUBLE, "
                    + "taxable_income DOUBLE, "
                    + "tax DOUBLE, "
                    + "total_deduction DOUBLE, "
                    + "net_pay DOUBLE, "
                    + "pay_period VARCHAR(50), "
                    + "prepared_by VARCHAR(100))";
            stmt.execute(createPayrollTable);

            String createDepartmentTable = "CREATE TABLE IF NOT EXISTS department_positions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "department_name VARCHAR(100) NOT NULL, "
                    + "position_name VARCHAR(100) NOT NULL, "
                    + "UNIQUE (department_name, position_name)"
                    + ")";
            stmt.execute(createDepartmentTable);

            String insertDepartment = "INSERT IGNORE INTO department_positions (department_name, position_name) VALUES "
                    + "('Executive / Leadership','Chief Operating Officer (COO)'),"
                    + "('Executive / Leadership','Chief Financial Officer (CFO)'),"
                    + "('Executive / Leadership','Chief Technology Officer (CTO)'),"
                    + "('Administration & Operations','Office Manager'),"
                    + "('Administration & Operations','Operations Coordinator'),"
                    + "('Administration & Operations','Administrative Assistant'),"
                    + "('Administration & Operations','Project Manager'),"
                    + "('Administration & Operations','Facilities Supervisor'),"
                    + "('Finance & Accounting','Accountant'),"
                    + "('Finance & Accounting','Financial Analyst'),"
                    + "('Finance & Accounting','Payroll Specialist'),"
                    + "('Finance & Accounting','Internal Auditor'),"
                    + "('Finance & Accounting','Accounts Payable Clerk'),"
                    + "('Human Resources','HR Manager'),"
                    + "('Human Resources','Recruiter / Talent Acquisition Specialist'),"
                    + "('Human Resources','Training & Development Coordinator'),"
                    + "('Human Resources','Compensation & Benefits Analyst'),"
                    + "('Human Resources','Employee Relations Specialist'),"
                    + "('Marketing & Sales','Vice President of Marketing'),"
                    + "('Marketing & Sales','Marketing Manager'),"
                    + "('Marketing & Sales','Social Media Specialist'),"
                    + "('Marketing & Sales','Sales Representative'),"
                    + "('Marketing & Sales','Business Development Manager'),"
                    + "('Marketing & Sales','Customer Success Manager'),"
                    + "('Information Technology (IT)','Software Engineer'),"
                    + "('Information Technology (IT)','Data Analyst'),"
                    + "('Information Technology (IT)','IT Support Specialist'),"
                    + "('Information Technology (IT)','Cybersecurity Analyst'),"
                    + "('Information Technology (IT)','Systems Administrator'),"
                    + "('Product Development & Design','Product Manager'),"
                    + "('Product Development & Design','UX/UI Designer'),"
                    + "('Product Development & Design','Graphic Designer'),"
                    + "('Product Development & Design','Quality Assurance Tester'),"
                    + "('Research & Development (R&D)','Research & Development Specialist'),"
                    + "('Customer Service','Call Center Agent'),"
                    + "('Customer Service','Technical Support Representative'),"
                    + "('Customer Service','Client Relations Coordinator'),"
                    + "('Customer Service','Customer Experience Specialist')";
            stmt.execute(insertDepartment);

            String createPositionSalaryRateTable = "CREATE TABLE IF NOT EXISTS position_salary_rate ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "position_name VARCHAR(100) NOT NULL UNIQUE, "
                    + "salary_rate DECIMAL(10,2) NOT NULL"
                    + ")";
            stmt.execute(createPositionSalaryRateTable);

            String insertPositionSalaryRate = "INSERT IGNORE INTO position_salary_rate (position_name, salary_rate) VALUES "
                    + "('Chief Operating Officer (COO)', 120000),"
                    + "('Chief Financial Officer (CFO)', 110000),"
                    + "('Chief Technology Officer (CTO)', 115000),"
                    + "('Vice President of Marketing', 90000),"
                    + "('Office Manager', 35000),"
                    + "('Operations Coordinator', 30000),"
                    + "('Administrative Assistant', 28000),"
                    + "('Project Manager', 60000),"
                    + "('Facilities Supervisor', 32000),"
                    + "('Accountant', 45000),"
                    + "('Financial Analyst', 50000),"
                    + "('Payroll Specialist', 40000),"
                    + "('Internal Auditor', 48000),"
                    + "('Accounts Payable Clerk', 30000),"
                    + "('HR Manager', 50000),"
                    + "('Recruiter / Talent Acquisition Specialist', 42000),"
                    + "('Training & Development Coordinator', 38000),"
                    + "('Compensation & Benefits Analyst', 46000),"
                    + "('Employee Relations Specialist', 40000),"
                    + "('Marketing Manager', 55000),"
                    + "('Social Media Specialist', 35000),"
                    + "('Sales Representative', 30000),"
                    + "('Business Development Manager', 60000),"
                    + "('Customer Success Manager', 48000),"
                    + "('Software Engineer', 65000),"
                    + "('Data Analyst', 60000),"
                    + "('IT Support Specialist', 35000),"
                    + "('Cybersecurity Analyst', 70000),"
                    + "('Systems Administrator', 58000),"
                    + "('Product Manager', 65000),"
                    + "('UX/UI Designer', 50000),"
                    + "('Graphic Designer', 40000),"
                    + "('Quality Assurance Tester', 42000),"
                    + "('Research & Development Specialist', 55000),"
                    + "('Call Center Agent', 28000),"
                    + "('Technical Support Representative', 32000),"
                    + "('Client Relations Coordinator', 36000),"
                    + "('Customer Experience Specialist', 38000)";
            stmt.execute(insertPositionSalaryRate);

            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Database Connecting Failed!\n" + ex.getLocalizedMessage());
            System.exit(0);
        }
    }

    public void fetchPositionRateData() {

        DefaultTableModel model = (DefaultTableModel) positionRateTable.getModel();
        model.setRowCount(0); // clear table

        String sql
                = "SELECT dp.id, dp.department_name, dp.position_name, ps.salary_rate "
                + "FROM department_positions dp "
                + "JOIN position_salary_rate ps "
                + "ON dp.position_name = ps.position_name "
                + "ORDER BY dp.department_name, dp.position_name";

        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), // department_position id
                    rs.getString("department_name"),
                    rs.getString("position_name"),
                    rs.getBigDecimal("salary_rate")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void fetchDeductions() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM deduction_rates_table"
            );
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                sssField.setText(rs.getString("sss_rate"));
                philhealthField.setText(rs.getString("philhealth_rate"));
                pagibigField.setText(rs.getString("pagibig_rate"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Fetching Payroll Failed!\n" + ex.getLocalizedMessage());
        }
    }

    public void fetchPayroll() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT employee_id, employee_name, position, gross_pay FROM payroll_table"
            );
            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel dtm = (DefaultTableModel) payrollTable.getModel();
            dtm.setRowCount(0);

            while (rs.next()) {
                Vector v = new Vector();
                v.add(rs.getInt("employee_id"));
                v.add(rs.getString("employee_name"));
                v.add(rs.getString("position"));
                v.add(df.format(rs.getDouble("gross_pay")));
                dtm.addRow(v);
            }
            pstmt = connection.prepareStatement("SELECT SUM(gross_pay) AS total_gross_pay FROM payroll_table");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String[] obj = {"Total", null, null, df.format(rs.getDouble("total_gross_pay"))};
                dtm.addRow(obj);
            }

            // Auto descending order based on "id"
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dtm);
            payrollTable.setRowSorter(sorter);
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            sorter.setSortKeys(sortKeys);
            sorter.sort();

        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Fetching Payroll Failed!\n" + ex.getLocalizedMessage());
        }
    }

    public void fetch() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT employee_id, full_name, position, department FROM employees_table"
            );
            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel dtm = (DefaultTableModel) employeesTable.getModel();
            dtm.setRowCount(0);
            while (rs.next()) {
                Vector v2 = new Vector();
                v2.add(rs.getString("employee_id"));
                v2.add(rs.getString("full_name"));
                v2.add(rs.getString("position"));
                v2.add(rs.getString("department"));
                dtm.addRow(v2);
            }

            // TableRowSorter para auto descending sa employee_id
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dtm);
            employeesTable.setRowSorter(sorter);
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
            sorter.setSortKeys(sortKeys);
            sorter.sort();

        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Fetching Failed!\n" + ex.getLocalizedMessage());
        }
    }

    public void fetchAttendanceList() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT employees_table.employee_id, employees_table.full_name, attendance_table.date, attendance_table.am_time_in, attendance_table.am_time_out, "
                    + "attendance_table.pm_time_in, attendance_table.pm_time_out "
                    + "FROM employees_table LEFT JOIN attendance_table ON employees_table.employee_id = attendance_table.employee_id ORDER BY attendance_id DESC");
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmt = rs.getMetaData();

            DefaultTableModel dtm = (DefaultTableModel) attendanceTable.getModel();
            dtm.setRowCount(0);
            while (rs.next()) {
                Vector v2 = new Vector();
                v2.add(rs.getString("employee_id"));
                v2.add(rs.getString("full_name"));
                v2.add(rs.getString("date"));
                v2.add(rs.getString("am_time_in"));
                v2.add(rs.getString("am_time_out"));
                v2.add(rs.getString("pm_time_in"));
                v2.add(rs.getString("pm_time_out"));
                dtm.addRow(v2);
            }

            // ✅ SORT TABLE BY FIRST COLUMN (Date) DESCENDING
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(attendanceTable.getModel());
            attendanceTable.setRowSorter(sorter);

            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(2, SortOrder.DESCENDING)); // column 3 = Date
            sorter.setSortKeys(sortKeys);
            sorter.sort();

        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Fetching Failed!\n" + ex.getLocalizedMessage());
        }
    }

    private void clearFields() {
        txtName.setText("");
        if (dateBirth != null) {
            dateBirth.setDate(null);
        }
        comboGender.setSelectedIndex(-1);
        txtAddress.setText("");
        txtContact.setText("");
        txtSalary.setText("");
        txtEmail.setText("");
        comboPosition.setSelectedIndex(-1);
        comboDepartment.setSelectedIndex(-1);
        if (dateHired != null) {
            dateHired.setDate(java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (lblPhotoPreview != null) {
            lblPhotoPreview.setIcon(null);
        }
        if (lblPhotoPath != null) {
            lblPhotoPath.setText("");
        }

        // ✅ Add this to reset the main imageHolder
        if (imageHolder != null) {
            imageHolder.setIcon(null);
            imageHolder.setText("PHOTO");
        }

        generateQr();

        imagePath = null;
    }

    public void fetchTheUser() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT user_id, username FROM users_table ORDER BY user_id DESC");
            ResultSet rs = pstmt.executeQuery();

            DefaultTableModel dtm = (DefaultTableModel) usersTable.getModel();
            dtm.setRowCount(0);
            while (rs.next()) {
                Vector v2 = new Vector();
                v2.add(rs.getString("user_id"));
                v2.add(rs.getString("username"));
                dtm.addRow(v2);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Fetching Failed!\n" + ex.getLocalizedMessage());
        }
    }

    public void fetchUser() {
        String savedUser = prefs.get("rememberedUser", null);
        if (savedUser != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users_table WHERE username = ?");
                pstmt.setString(1, savedUser);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    changePasswordUsernameLabel.setText("Username: " + rs.getString("username"));
                    userId = rs.getInt("user_id");
                    userPassword = rs.getString("password");
                }
            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Fetching User Failed!\n" + ex.getLocalizedMessage());
            }
        }
    }

    public void changePassword() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All Field Must be Field Out!");
            } else {
                if (userPassword.equals(currentPassword)) {
                    if (newPassword.equals(confirmPassword)) {
                        // Insert into database
                        String sql = "UPDATE users_table SET password = ? WHERE user_id = ?";
                        PreparedStatement pstmt = connection.prepareStatement(sql);

                        pstmt.setString(1, newPassword);
                        pstmt.setInt(2, userId);
                        pstmt.executeUpdate();

                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");

                        JOptionPane.showMessageDialog(this, "Change Password Successful!");
                    } else {
                        JOptionPane.showMessageDialog(this, "New and Confirm Password Must be Same!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Current Password is Incorrect!");
                }
            }
        } catch (java.sql.SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Changing Failed!\n" + ex.getMessage());
        }
    }

    public void addUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All Field Must be Field Out!");
        } else {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users_table (admin, username, password) VALUES (1, ?, ?) ");
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.executeUpdate();

                fetchTheUser();
                clearSelectionButtonActionPerformed(null);

                JOptionPane.showMessageDialog(this, "New User Added Successful!");

            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Adding Failed!\n" + ex.getLocalizedMessage());
            }
        }
    }

    public void updateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        int row = usersTable.getSelectedRow();
        selectedUserId = Integer.parseInt(usersTable.getValueAt(row, 0).toString());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All Field Must be Field Out!");
        } else {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE users_table SET username = ?, password = ? WHERE user_id = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setInt(3, selectedUserId);
                pstmt.executeUpdate();

                fetchTheUser();
                clearSelectionButtonActionPerformed(null);

                JOptionPane.showMessageDialog(this, "User Update Successful!");

            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Upadating Failed!\n" + ex.getLocalizedMessage());
            }
        }
    }

    public void removeUser() {
        int row = usersTable.getSelectedRow();
        selectedUserId = Integer.parseInt(usersTable.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to Remove this User?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("DELETE FROM users_table WHERE user_id = ?");
                pstmt.setInt(1, selectedUserId);
                pstmt.executeUpdate();

                fetchUser();
                fetchTheUser();
                fetch();
                clearSelectionButtonActionPerformed(null);

                JOptionPane.showMessageDialog(this, "User Remove Successful!");

            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Removing Failed!\n" + ex.getLocalizedMessage());
            }
        }
    }

    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFolder(f); // delete inside files/subfolders
                }
            }
        }
        folder.delete(); // delete the folder or file
    }

    public void generateQr() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT employee_id FROM employees_table ORDER BY employee_id DESC LIMIT 1");
            if (rs.next()) {
                File qrDir = new File("qrcodes");
                if (!qrDir.exists()) {
                    qrDir.mkdir();
                }
                int id = rs.getInt("employee_id") + 1;
                String text = Integer.toString(id);
                String filePath = "qrcodes/" + id + ".png";
                BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 500, 500);
                Path path = FileSystems.getDefault().getPath(filePath);
                MatrixToImageWriter.writeToPath(matrix, "PNG", path);
                System.out.println("QR Code created: " + filePath);

                qrHolder.setIcon(new ImageIcon(new ImageIcon("qrcodes/" + id + ".png").getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                qrHolder.setText("");

            }

        } catch (Exception ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Generating QR Code Failed!");
        }
    }

    private static double computeWithholdingTaxMonthly(double taxableIncome) {
        // Based on 2025 TRAIN withholding tax table (monthly income)
        // 0 – 20,833: 0%
        // 20,833.01 – 33,333: 15% of excess over 20,833
        // 33,333.01 – 66,666: 2,500 + 20% of excess over 33,333
        // 66,666.01 – 166,666: 10,833.33 + 25% of excess over 66,666
        // 166,666.01 – 666,666: 40,833.33 + 30% of excess over 166,666
        // above 666,666: 200,833.33 + 35% of excess over 666,666

        double tax = 0.0;
        if (taxableIncome <= 20833) {
            tax = 0;
        } else if (taxableIncome <= 33333) {
            tax = (taxableIncome - 20833) * 0.15;
        } else if (taxableIncome <= 66666) {
            tax = 2500 + (taxableIncome - 33333) * 0.20;
        } else if (taxableIncome <= 166666) {
            tax = 10833.33 + (taxableIncome - 66666) * 0.25;
        } else if (taxableIncome <= 666666) {
            tax = 40833.33 + (taxableIncome - 166666) * 0.30;
        } else {
            tax = 200833.33 + (taxableIncome - 666666) * 0.35;
        }
        return tax;
    }

    public void GeneratePaySlip(int id) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            String sql = "SELECT * FROM payroll_table WHERE employee_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    payslip = "<html>"
                            + "<body style='font-family: Arial, sans-serif; font-size: 9px; margin: 10px; padding: 10px;'>"
                            /* ===== TITLE ===== */
                            + "<div style='text-align: center; margin-bottom: 10px;'>"
                            + "<h2 style='margin:  0; font-size: 16px;'>EMSys</h2>"
                            + "<p style='margin: 2px 0; font-size:  9px;'>Project 1, Daet, Camarines Norte</p>"
                            + "<p style='margin: 2px 0; font-size: 11px; font-weight: bold;'>PAYSLIP</p>"
                            + "</div>"
                            + "<hr style='margin: 5px 0;'>"
                            /* ===== EMPLOYEE INFORMATION ===== */
                            + "<table width='100%' border='0' cellspacing='0' cellpadding='4' style='font-size: 9px; margin-bottom: 10px;'>"
                            + "<tr>"
                            + "<td width='25%'><b>Employee ID:</b><br>" + rs.getInt("employee_id") + "</td>"
                            + "<td width='30%'><b>Name:</b><br>" + rs.getString("employee_name") + "</td>"
                            + "<td width='20%'><b>Position:</b><br>" + rs.getString("position") + "</td>"
                            + "<td width='25%'><b>Pay Period:</b><br>" + rs.getString("pay_period") + "</td>"
                            + "</tr>"
                            + "<tr>"
                            + "<td colspan='4' style='border-top: 1px solid #000;'><b>Monthly Rate:</b> " + df.format(rs.getDouble("monthly_salary")) + "</td>"
                            + "</tr>"
                            + "</table>"
                            /* ===== WORK SUMMARY ===== */
                            + "<table width='100%' border='1' cellspacing='0' cellpadding='4' style='font-size: 9px; margin-bottom: 10px;'>"
                            + "<tr style='background-color: #f5f5f5;'>"
                            + "<th style='text-align: left;'>Present Days</th>"
                            + "<th style='text-align: left;'>Absent Days</th>"
                            + "<th style='text-align: left;'>Hours Worked</th>"
                            + "<th style='text-align: left;'>Standard Days</th>"
                            + "<th style='text-align: left;'>Standard Hours</th>"
                            + "<th style='text-align: left;'>Hourly Rate</th>"
                            + "</tr>"
                            + "<tr>"
                            + "<td>" + rs.getInt("total_present") + "</td>"
                            + "<td>" + rs.getInt("total_absent") + "</td>"
                            + "<td>" + df.format(rs.getDouble("total_hours_worked")) + "</td>"
                            + "<td>" + rs.getInt("standard_work_days") + "</td>"
                            + "<td>" + rs.getInt("standard_hours") + "</td>"
                            + "<td>" + df.format(rs.getDouble("hourly_rate")) + "</td>"
                            + "</tr>"
                            + "</table>"
                            /* ===== EARNINGS & DEDUCTIONS ===== */
                            + "<table width='100%' border='1' cellspacing='0' cellpadding='4' style='font-size: 9px; margin-bottom: 10px;'>"
                            + "<tr style='background-color:  #f5f5f5;'>"
                            + "<th width='33%' style='text-align: left;'>EARNINGS</th>"
                            + "<th width='33%' style='text-align: left;'>DEDUCTIONS</th>"
                            + "<th width='34%' style='text-align:  left;'>TAXES</th>"
                            + "</tr>"
                            + "<tr valign='top'>"
                            + "<td><b>Gross Pay:</b> " + df.format(rs.getDouble("gross_pay")) + "</td>"
                            + "<td>"
                            + "<b>SSS:</b> " + df.format(rs.getDouble("sss")) + "<br>"
                            + "<b>PhilHealth:</b> " + df.format(rs.getDouble("philhealth")) + "<br>"
                            + "<b>Pag-IBIG:</b> " + df.format(rs.getDouble("pagibig")) + ""
                            + "</td>"
                            + "<td>"
                            + "<b>Taxable Income:</b> " + df.format(rs.getDouble("taxable_income")) + "<br>"
                            + "<b>Tax:</b> " + df.format(rs.getDouble("tax")) + "<br>"
                            + "<b>Total Deduction:</b> " + df.format(rs.getDouble("total_deduction")) + ""
                            + "</td>"
                            + "</tr>"
                            + "</table>"
                            /* ===== NET PAY ===== */
                            + "<table width='100%' border='2' cellspacing='0' cellpadding='6' style='font-size: 10px; margin-bottom: 10px; background-color: #f0f0f0;'>"
                            + "<tr>"
                            + "<td width='50%'><b>NET PAY</b></td>"
                            + "<td width='50%' style='text-align: right; font-weight: bold; font-size: 12px;'>" + df.format(rs.getDouble("net_pay")) + "</td>"
                            + "</tr>"
                            + "</table>"
                            /* ===== FOOTER ===== */
                            + "<div style='text-align: right; font-size: 8px; margin-top: 10px;'>"
                            + "<p><b>Prepared By: </b> " + rs.getString("prepared_by") + "</p>"
                            + "</div>"
                            + "</body></html>";
                } else {
                    payslip = "<html><body>No payslip found for this employee.</body></html>";
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error generating payslip: " + ex.getMessage());
        }
    }

    public void recordPayslip() {

        final int HOURS_PER_DAY = 8;
        final int STANDARD_WORK_DAYS = 26;
        final double PAGIBIG_CAP = 10000;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            // Delete existing payroll
            try (PreparedStatement deleteStmt
                    = connection.prepareStatement("DELETE FROM payroll_table WHERE pay_period = ?")) {
                deleteStmt.setString(1, payPeriod);
                deleteStmt.executeUpdate();
            }

            // Get deduction rates
            double sssRate = 0, philhealthRate = 0, pagibigRate = 0;
            try (PreparedStatement rateStmt
                    = connection.prepareStatement(
                            "SELECT sss_rate, philhealth_rate, pagibig_rate FROM deduction_rates_table"); ResultSet rs = rateStmt.executeQuery()) {

                if (rs.next()) {
                    sssRate = rs.getDouble("sss_rate");
                    philhealthRate = rs.getDouble("philhealth_rate");
                    pagibigRate = rs.getDouble("pagibig_rate");
                }
            }

            // Get employees
            try (PreparedStatement empStmt
                    = connection.prepareStatement(
                            "SELECT employee_id, full_name, position, salary FROM employees_table"); ResultSet empRs = empStmt.executeQuery()) {

                while (empRs.next()) {

                    int employeeId = empRs.getInt("employee_id");
                    String employeeName = empRs.getString("full_name");
                    String position = empRs.getString("position");
                    double monthlySalary = empRs.getDouble("salary");

                    int standardHours = STANDARD_WORK_DAYS * HOURS_PER_DAY;

                    // Present days
                    int totalPresent = 0;
                    try (PreparedStatement presentStmt
                            = connection.prepareStatement(
                                    "SELECT COUNT(*) FROM attendance_table "
                                    + "WHERE employee_id = ? AND date BETWEEN ? AND ? AND status = 'Present'")) {

                        presentStmt.setInt(1, employeeId);
                        presentStmt.setDate(2, java.sql.Date.valueOf(startOfMonth));
                        presentStmt.setDate(3, java.sql.Date.valueOf(endOfMonth));

                        try (ResultSet rs = presentStmt.executeQuery()) {
                            if (rs.next()) {
                                totalPresent = rs.getInt(1);
                            }
                        }
                    }

                    int absentDays = Math.max(0, STANDARD_WORK_DAYS - totalPresent);

                    // Total hours worked
                    double totalHoursWorked = 0;
                    try (PreparedStatement hoursStmt
                            = connection.prepareStatement(
                                    "SELECT SUM(total_hours) FROM attendance_table "
                                    + "WHERE employee_id = ? AND date BETWEEN ? AND ?")) {

                        hoursStmt.setInt(1, employeeId);
                        hoursStmt.setDate(2, java.sql.Date.valueOf(startOfMonth));
                        hoursStmt.setDate(3, java.sql.Date.valueOf(endOfMonth));

                        try (ResultSet rs = hoursStmt.executeQuery()) {
                            if (rs.next()) {
                                totalHoursWorked = rs.getDouble(1);
                            }
                        }
                    }

                    // Salary computation
                    double hourlyRate = standardHours == 0 ? 0 : monthlySalary / standardHours;
                    double grossPay = hourlyRate * totalHoursWorked;

                    double sss = (monthlySalary * sssRate) * (totalHoursWorked / standardHours);
                    double philhealth = (monthlySalary * philhealthRate) * (totalHoursWorked / standardHours);
                    double pagibig = Math.min(monthlySalary, PAGIBIG_CAP)
                            * pagibigRate * (totalHoursWorked / standardHours);

                    double taxableIncome = grossPay - (sss + philhealth + pagibig);
                    double tax = computeWithholdingTaxMonthly(taxableIncome)
                            * (totalHoursWorked / standardHours);

                    double totalDeductions = sss + philhealth + pagibig + tax;
                    double netPay = grossPay - totalDeductions;

                    // Format to 2 decimal places
                    hourlyRate = Double.parseDouble(df.format(hourlyRate).replace(",", ""));
                    grossPay = Double.parseDouble(df.format(grossPay).replace(",", ""));
                    sss = Double.parseDouble(df.format(sss).replace(",", ""));
                    philhealth = Double.parseDouble(df.format(philhealth).replace(",", ""));
                    pagibig = Double.parseDouble(df.format(pagibig).replace(",", ""));
                    taxableIncome = Double.parseDouble(df.format(taxableIncome).replace(",", ""));
                    tax = Double.parseDouble(df.format(tax).replace(",", ""));
                    totalDeductions = Double.parseDouble(df.format(totalDeductions).replace(",", ""));
                    netPay = Double.parseDouble(df.format(netPay).replace(",", ""));

                    // Insert payroll
                    try (PreparedStatement insertStmt
                            = connection.prepareStatement(
                                    "INSERT INTO payroll_table ("
                                    + "employee_id, employee_name, position, monthly_salary, "
                                    + "total_present, total_absent, total_hours_worked, "
                                    + "standard_work_days, standard_hours, hourly_rate, gross_pay, "
                                    + "sss, philhealth, pagibig, taxable_income, tax, "
                                    + "total_deduction, net_pay, pay_period, prepared_by) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                        insertStmt.setInt(1, employeeId);
                        insertStmt.setString(2, employeeName);
                        insertStmt.setString(3, position);
                        insertStmt.setDouble(4, monthlySalary);
                        insertStmt.setInt(5, totalPresent);
                        insertStmt.setInt(6, absentDays);
                        insertStmt.setDouble(7, totalHoursWorked);
                        insertStmt.setInt(8, STANDARD_WORK_DAYS);
                        insertStmt.setInt(9, standardHours);
                        insertStmt.setDouble(10, hourlyRate);
                        insertStmt.setDouble(11, grossPay);
                        insertStmt.setDouble(12, sss);
                        insertStmt.setDouble(13, philhealth);
                        insertStmt.setDouble(14, pagibig);
                        insertStmt.setDouble(15, taxableIncome);
                        insertStmt.setDouble(16, tax);
                        insertStmt.setDouble(17, totalDeductions);
                        insertStmt.setDouble(18, netPay);
                        insertStmt.setString(19, payPeriod);
                        insertStmt.setString(20, "Admin");

                        insertStmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            fetchPayroll();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving payroll: " + ex.getMessage());
        }
    }

    public void loadComboBox() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT date FROM attendance_table ORDER BY date DESC");
            ResultSet rs = pstmt.executeQuery();
            jComboBox1.removeAllItems();
            jComboBox1.addItem("");
            while (rs.next()) {
                jComboBox1.addItem(rs.getString("date"));
            }
            jComboBox1.setSelectedItem(today);
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "LoadComboBox Failed!\n" + ex.getMessage());
        }
    }

    public void loadPositionRateComboBox() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT department_name FROM department_positions ORDER BY department_name DESC");
            ResultSet rs = pstmt.executeQuery();
            jComboBox3.removeAllItems();
            jComboBox3.addItem("");
            while (rs.next()) {
                jComboBox3.addItem(rs.getString("department_name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "LoadComboBox Failed!\n" + ex.getMessage());
        }
    }

    public void loadEmployeeListComboBox() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT employee_id FROM employees_table ORDER BY employee_id DESC");
            ResultSet rs = pstmt.executeQuery();
            jComboBox.removeAllItems();
            while (rs.next()) {
                jComboBox.addItem(rs.getString("employee_id"));
            }
            jComboBox.setSelectedIndex(-1);
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "LoadComboBox Failed!\n" + ex.getMessage());
        }
    }

    public void loadPayrollComboBox() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT employee_id FROM payroll_table ORDER BY employee_id DESC");
            ResultSet rs = pstmt.executeQuery();
            jComboBox2.removeAllItems();
            while (rs.next()) {
                jComboBox2.addItem(rs.getString("employee_id"));
            }
            jComboBox2.setSelectedIndex(-1);
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "LoadComboBox Failed!\n" + ex.getMessage());
        }
    }

    private void loadEmployeeData(int id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM employees_table WHERE employee_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("full_name"));
                comboGender.setSelectedItem(rs.getString("gender"));
                txtAddress.setText(rs.getString("address"));
                txtContact.setText(rs.getString("contact_number"));
                txtSalary.setText(rs.getString("salary"));
                txtEmail.setText(rs.getString("email_address"));
                comboPosition.setSelectedItem(rs.getString("position"));
                comboDepartment.setSelectedItem(rs.getString("department"));

                // ✅ Flexible date parsing (accepts both "yyyy-MM-dd" and "d MMM yyyy")
                String birthStr = rs.getString("birth_date");
                String hiredStr = rs.getString("hired_date");

                java.util.Date birth = null;
                java.util.Date hired = null;

                try {
                    birth = new SimpleDateFormat("yyyy-MM-dd").parse(birthStr);
                } catch (Exception e1) {
                    try {
                        birth = new SimpleDateFormat("d MMM yyyy").parse(birthStr);
                    } catch (Exception e2) {
                        birth = null;
                    }
                }

                try {
                    hired = new SimpleDateFormat("yyyy-MM-dd").parse(hiredStr);
                } catch (Exception e1) {
                    try {
                        hired = new SimpleDateFormat("d MMM yyyy").parse(hiredStr);
                    } catch (Exception e2) {
                        hired = null;
                    }
                }

                if (birth != null) {
                    dateBirth.setDate(birth);
                }
                if (hired != null) {
                    dateHired.setDate(hired);
                }

                selectedFile = new File(rs.getString("photo_path"));
                imagePath = selectedFile.getAbsolutePath();

                if (imagePath != null && !imagePath.isEmpty()) {
                    ImageIcon imageIcon = new ImageIcon(
                            new ImageIcon(imagePath).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                    );
                    imageHolder.setIcon(imageIcon);
                    imageHolder.setText("");
                }

                qrHolder.setIcon(new ImageIcon(new ImageIcon("qrcodes/" + id + ".png").getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                qrHolder.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load employee data: " + e.getMessage());
        }
    }

    public void updateEmployee(int id) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            String name = txtName.getText().trim();
            java.util.Date birthUtilDate = dateBirth.getDate(); // get Date object
            String gender = comboGender.getSelectedItem().toString();
            String address = txtAddress.getText().trim();
            String contact = txtContact.getText().trim();
            String salary = txtSalary.getText().trim();
            String email = txtEmail.getText().trim();
            String position = comboPosition.getSelectedItem().toString();
            String department = comboDepartment.getSelectedItem().toString();
            java.util.Date hiredUtilDate = dateHired.getDate(); // get Date object
            String photoPath = imagePath;

            java.sql.Date birthSqlDate = null;
            java.sql.Date hiredSqlDate = null;

            if (birthUtilDate != null) {
                birthSqlDate = new java.sql.Date(birthUtilDate.getTime());
            }
            if (hiredUtilDate != null) {
                hiredSqlDate = new java.sql.Date(hiredUtilDate.getTime());
            }

            // Create "photos" folder if it doesn't exist
            File photoDir = new File("photos");
            if (!photoDir.exists()) {
                photoDir.mkdir();
            }

            String fileName = selectedFile.getName();

            // 2️⃣ — Copy selected photo to "photos" folder
            File destinationFile = new File(photoDir, fileName);
            Files.copy(selectedFile.toPath(), destinationFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String sql = "UPDATE employees_table SET photo_path=?, full_name=?, birth_date=?, gender=?, address=?, contact_number=?, salary=?, email_address=?, position=?, department=?, hired_date=? WHERE employee_id=?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, photoPath);
            pst.setString(2, name);
            pst.setDate(3, birthSqlDate);
            pst.setString(4, gender);
            pst.setString(5, address);
            pst.setString(6, contact);
            pst.setString(7, salary);
            pst.setString(8, email);
            pst.setString(9, position);
            pst.setString(10, department);
            pst.setDate(11, hiredSqlDate);
            pst.setInt(12, id);

            int updated = pst.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "✅ Employee updated successfully!");

                card.show(jPanel3, "card4");

                btnEdit.setEnabled(false);
                btnRemove.setEnabled(false);
                unselectButton.setEnabled(false);
                employeesTable.clearSelection();

                fetch();
                loadEmployeeListComboBox();

            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Failed to update employee!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error updating employee: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu1 = new javax.swing.JPopupMenu();
        update = new javax.swing.JMenuItem();
        remove = new javax.swing.JMenuItem();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        dashboardmain = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        panel1 = new javax.swing.JPanel();
        countActive = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        panel2 = new javax.swing.JPanel();
        countInactive = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        panel3 = new javax.swing.JPanel();
        countTotal = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        addEmployeePanel = new javax.swing.JPanel();
        name = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        birthDate = new javax.swing.JLabel();
        dateBirth = new com.toedter.calendar.JDateChooser();
        gender = new javax.swing.JLabel();
        comboGender = new javax.swing.JComboBox<>();
        address = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        contactNo = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        hiredate = new javax.swing.JLabel();
        dateHired = new com.toedter.calendar.JDateChooser();
        imageHolder = new javax.swing.JLabel();
        btnUploadPhoto = new javax.swing.JButton();
        salaryLabel = new javax.swing.JLabel();
        txtSalary = new javax.swing.JTextField();
        email = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        positionLabel = new javax.swing.JLabel();
        comboPosition = new javax.swing.JComboBox<>();
        departmentAddLabel = new javax.swing.JLabel();
        comboDepartment = new javax.swing.JComboBox<>();
        btnADD = new javax.swing.JButton();
        btnCLEAR = new javax.swing.JButton();
        btnCANCEL = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        qrHolder = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnSAVE1 = new javax.swing.JButton();
        attendanceList = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        btnExport = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        attendanceTable = new javax.swing.JTable();
        viewEmployeeList = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jComboBox = new javax.swing.JComboBox<>();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        unselectButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        employeesTable = new javax.swing.JTable();
        payroll = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox<>();
        btnView1 = new javax.swing.JButton();
        unselectButton1 = new javax.swing.JButton();
        btnExport1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        payrollTable = new javax.swing.JTable();
        settings = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        changePasswordPanel = new javax.swing.JPanel();
        changePasswordUsernameLabel = new javax.swing.JLabel();
        currentPasswordLabel = new javax.swing.JLabel();
        currentPasswordField = new javax.swing.JPasswordField();
        newPasswordLabel = new javax.swing.JLabel();
        newPasswordField = new javax.swing.JPasswordField();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmPasswordField = new javax.swing.JPasswordField();
        changeButton = new javax.swing.JButton();
        changePasswordShowPassword = new javax.swing.JCheckBox();
        manageUsersPanel = new javax.swing.JPanel();
        addUpdatePanel = new javax.swing.JPanel();
        addUpdateLabel = new javax.swing.JLabel();
        addUpdateUsernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        addUpdatePasswordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        manageUsersShowPassword = new javax.swing.JCheckBox();
        addButton = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        usersTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        clearSelectionButton = new javax.swing.JButton();
        manageDeductions = new javax.swing.JPanel();
        addUpdatePanel1 = new javax.swing.JPanel();
        manageDeductionLabel = new javax.swing.JLabel();
        addUpdateUsernameLabel1 = new javax.swing.JLabel();
        pagibigField = new javax.swing.JTextField();
        addUpdatePasswordLabel2 = new javax.swing.JLabel();
        philhealthField = new javax.swing.JTextField();
        addUpdateUsernameLabel2 = new javax.swing.JLabel();
        sssField = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        database = new javax.swing.JPanel();
        resetDatabaseButton = new javax.swing.JButton();
        managePositionRates = new javax.swing.JPanel();
        addUpdatePanel2 = new javax.swing.JPanel();
        addUpdateLabel1 = new javax.swing.JLabel();
        departmentLabel = new javax.swing.JLabel();
        departmentField = new javax.swing.JTextField();
        addUpdateUsernameLabel3 = new javax.swing.JLabel();
        positionField = new javax.swing.JTextField();
        addUpdatePasswordLabel1 = new javax.swing.JLabel();
        addButton1 = new javax.swing.JButton();
        rateField = new javax.swing.JTextField();
        tablePanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        positionRateTable = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        removeButton1 = new javax.swing.JButton();
        updateButton1 = new javax.swing.JButton();
        clearSelectionButton1 = new javax.swing.JButton();
        jComboBox3 = new javax.swing.JComboBox<>();

        jPopupMenu1.setBackground(new java.awt.Color(0, 153, 153));
        jPopupMenu1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPopupMenu1.setForeground(new java.awt.Color(102, 102, 102));
        jPopupMenu1.setBorderPainted(false);

        update.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        update.setText("Update");
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });
        jPopupMenu1.add(update);

        remove.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        remove.setText("Remove");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });
        jPopupMenu1.add(remove);

        jTextField1.setText("jTextField1");

        jTextField2.setText("jTextField1");

        jTextField3.setText("jTextField1");

        jTextField4.setText("jTextField4");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Employee Management System");
        setMinimumSize(new java.awt.Dimension(1000, 600));

        jPanel1.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(26, 0, 51));
        jPanel2.setForeground(new java.awt.Color(26, 0, 51));
        jPanel2.setAutoscrolls(true);
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 287));

        jLabel1.setBackground(new java.awt.Color(204, 255, 255));
        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("EMSys");

        jButton4.setBackground(new java.awt.Color(26, 0, 51));
        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton4.setForeground(new java.awt.Color(78, 141, 245));
        jButton4.setText("DASHBOARD");
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setFocusable(false);
        jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton4.setMaximumSize(new java.awt.Dimension(128, 33));
        jButton4.setMinimumSize(new java.awt.Dimension(128, 33));
        jButton4.setPreferredSize(new java.awt.Dimension(128, 33));
        jButton4.setSelected(true);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(26, 0, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton1.setForeground(new java.awt.Color(78, 141, 245));
        jButton1.setText("ADD EMPLOYEE");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton1.setSelected(true);
        jButton1.setVerifyInputWhenFocusTarget(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(26, 0, 51));
        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton3.setForeground(new java.awt.Color(78, 141, 245));
        jButton3.setText("ATTENDANCE LIST");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.setFocusable(false);
        jButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton3.setSelected(true);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(26, 0, 51));
        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton2.setForeground(new java.awt.Color(78, 141, 245));
        jButton2.setText("VIEW EMPLOYEE LIST");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.setFocusable(false);
        jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton2.setSelected(true);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(26, 0, 51));
        jButton5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton5.setForeground(new java.awt.Color(78, 141, 245));
        jButton5.setText("PAYROLL");
        jButton5.setBorderPainted(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setFocusPainted(false);
        jButton5.setFocusable(false);
        jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton5.setSelected(true);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(30, 0, 50));
        jButton6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton6.setForeground(new java.awt.Color(78, 141, 245));
        jButton6.setText("LOG OUT ");
        jButton6.setBorderPainted(false);
        jButton6.setContentAreaFilled(false);
        jButton6.setFocusPainted(false);
        jButton6.setFocusable(false);
        jButton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton6.setPreferredSize(new java.awt.Dimension(146, 33));
        jButton6.setSelected(true);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(26, 0, 51));
        jButton7.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton7.setForeground(new java.awt.Color(78, 141, 245));
        jButton7.setText("SETTINGS");
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);
        jButton7.setFocusable(false);
        jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton7.setPreferredSize(new java.awt.Dimension(146, 33));
        jButton7.setSelected(true);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(26, 0, 51));
        jButton8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton8.setForeground(new java.awt.Color(78, 141, 245));
        jButton8.setText("SCAN QR CODE");
        jButton8.setBorderPainted(false);
        jButton8.setContentAreaFilled(false);
        jButton8.setFocusable(false);
        jButton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton8.setMaximumSize(new java.awt.Dimension(128, 33));
        jButton8.setMinimumSize(new java.awt.Dimension(128, 33));
        jButton8.setPreferredSize(new java.awt.Dimension(128, 33));
        jButton8.setSelected(true);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(26, 0, 51));
        jButton10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton10.setForeground(new java.awt.Color(78, 141, 245));
        jButton10.setText("POSITION RATES");
        jButton10.setBorderPainted(false);
        jButton10.setContentAreaFilled(false);
        jButton10.setFocusPainted(false);
        jButton10.setFocusable(false);
        jButton10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton10.setSelected(true);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                                .addComponent(jButton7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel1)
                .addGap(35, 35, 35)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));
        jPanel3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.setLayout(new java.awt.CardLayout());

        dashboardmain.setBackground(new java.awt.Color(221, 221, 221));
        dashboardmain.setForeground(new java.awt.Color(51, 51, 51));
        dashboardmain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dashboardmainMouseEntered(evt);
            }
        });
        dashboardmain.setLayout(new java.awt.GridBagLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 51, 51));
        jLabel6.setText("DASHBOARD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 50, 10);
        dashboardmain.add(jLabel6, gridBagConstraints);

        panel1.setBackground(new java.awt.Color(0, 204, 102));
        panel1.setMinimumSize(new java.awt.Dimension(100, 60));
        panel1.setPreferredSize(new java.awt.Dimension(200, 150));
        panel1.setLayout(new java.awt.GridBagLayout());

        countActive.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        countActive.setForeground(new java.awt.Color(255, 255, 255));
        countActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        countActive.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel1.add(countActive, gridBagConstraints);

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("PRESENT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dashboardmain.add(panel1, gridBagConstraints);

        panel2.setBackground(new java.awt.Color(231, 76, 60));
        panel2.setMinimumSize(new java.awt.Dimension(100, 60));
        panel2.setPreferredSize(new java.awt.Dimension(200, 150));
        panel2.setLayout(new java.awt.GridBagLayout());

        countInactive.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        countInactive.setForeground(new java.awt.Color(255, 255, 255));
        countInactive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        countInactive.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel2.add(countInactive, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("ABSENT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel2.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dashboardmain.add(panel2, gridBagConstraints);

        panel3.setBackground(new java.awt.Color(52, 152, 219));
        panel3.setMinimumSize(new java.awt.Dimension(100, 60));
        panel3.setPreferredSize(new java.awt.Dimension(200, 150));
        panel3.setLayout(new java.awt.GridBagLayout());

        countTotal.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        countTotal.setForeground(new java.awt.Color(255, 255, 255));
        countTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        countTotal.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel3.add(countTotal, gridBagConstraints);

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("TOTAL OF EMPLOYEE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panel3.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dashboardmain.add(panel3, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 51, 51));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("DATE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        dashboardmain.add(jLabel8, gridBagConstraints);

        jPanel3.add(dashboardmain, "card1");

        addEmployeePanel.setBackground(new java.awt.Color(221, 221, 221));
        addEmployeePanel.setForeground(new java.awt.Color(30, 30, 30));
        addEmployeePanel.setAutoscrolls(true);

        name.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        name.setForeground(new java.awt.Color(51, 51, 51));
        name.setText("NAME");

        txtName.setBackground(new java.awt.Color(255, 255, 255));
        txtName.setForeground(new java.awt.Color(51, 51, 51));
        txtName.setCaretColor(new java.awt.Color(0, 0, 0));
        txtName.setPreferredSize(new java.awt.Dimension(250, 30));

        birthDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        birthDate.setForeground(new java.awt.Color(51, 51, 51));
        birthDate.setText("BIRTH DATE");

        dateBirth.setBackground(new java.awt.Color(0, 204, 204));
        dateBirth.setPreferredSize(new java.awt.Dimension(250, 30));

        gender.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        gender.setForeground(new java.awt.Color(51, 51, 51));
        gender.setText("GENDER");

        comboGender.setBackground(new java.awt.Color(0, 153, 153));
        comboGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female", "Other" }));
        comboGender.setSelectedIndex(-1);
        comboGender.setPreferredSize(new java.awt.Dimension(250, 30));
        comboGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboGenderActionPerformed(evt);
            }
        });

        address.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        address.setForeground(new java.awt.Color(51, 51, 51));
        address.setText("ADDRESS");

        txtAddress.setBackground(new java.awt.Color(255, 255, 255));
        txtAddress.setForeground(new java.awt.Color(51, 51, 51));
        txtAddress.setPreferredSize(new java.awt.Dimension(250, 30));

        contactNo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        contactNo.setForeground(new java.awt.Color(51, 51, 51));
        contactNo.setText("CONTACT NO");

        txtContact.setBackground(new java.awt.Color(255, 255, 255));
        txtContact.setForeground(new java.awt.Color(51, 51, 51));
        txtContact.setPreferredSize(new java.awt.Dimension(250, 30));

        hiredate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        hiredate.setForeground(new java.awt.Color(51, 51, 51));
        hiredate.setText("HIRED DATE");

        dateHired.setBackground(new java.awt.Color(0, 204, 204));
        dateHired.setForeground(new java.awt.Color(255, 255, 255));
        dateHired.setPreferredSize(new java.awt.Dimension(250, 30));

        imageHolder.setForeground(new java.awt.Color(51, 51, 51));
        imageHolder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageHolder.setText("PHOTO");
        imageHolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnUploadPhoto.setBackground(new java.awt.Color(0, 153, 153));
        btnUploadPhoto.setText("UPLOAD  PHOTO");
        btnUploadPhoto.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnUploadPhoto.setBorderPainted(false);
        btnUploadPhoto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUploadPhoto.setFocusable(false);
        btnUploadPhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadPhotoActionPerformed(evt);
            }
        });

        salaryLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        salaryLabel.setForeground(new java.awt.Color(51, 51, 51));
        salaryLabel.setText("SALARY (MONTHLY)");

        txtSalary.setBackground(new java.awt.Color(255, 255, 255));
        txtSalary.setPreferredSize(new java.awt.Dimension(250, 30));

        email.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        email.setForeground(new java.awt.Color(51, 51, 51));
        email.setText("EMAIL");

        txtEmail.setBackground(new java.awt.Color(255, 255, 255));
        txtEmail.setForeground(new java.awt.Color(51, 51, 51));
        txtEmail.setPreferredSize(new java.awt.Dimension(250, 30));

        positionLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        positionLabel.setForeground(new java.awt.Color(51, 51, 51));
        positionLabel.setText("POSITION");

        comboPosition.setBackground(new java.awt.Color(0, 153, 153));
        comboPosition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chief Operating Officer (COO)", "Chief Financial Officer (CFO)", "Chief Technology Officer (CTO)", "Vice President of Marketing", "Office Manager", "Operations Coordinator", "Administrative Assistant", "Project Manager", "Facilities Supervisor", "Accountant", "Financial Analyst", "Payroll Specialist", "Internal Auditor", "Accounts Payable Clerk", "HR Manager", "Recruiter / Talent Acquisition Specialist", "Training & Development Coordinator", "Compensation & Benefits Analyst", "Employee Relations Specialist", "Marketing Manager", "Social Media Specialist", "Sales Representative", "Business Development Manager", "Customer Success Manager", "Software Engineer", "Data Analyst", "IT Support Specialist", "Cybersecurity Analyst", "Systems Administrator", "Product Manager", "UX/UI Designer", "Graphic Designer", "Quality Assurance Tester", "Research & Development Specialist", "Call Center Agent", "Technical Support Representative", "Client Relations Coordinator", "Customer Experience Specialist" }));
        comboPosition.setSelectedIndex(-1);
        comboPosition.setPreferredSize(new java.awt.Dimension(250, 30));
        comboPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboPositionActionPerformed(evt);
            }
        });

        departmentAddLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        departmentAddLabel.setForeground(new java.awt.Color(51, 51, 51));
        departmentAddLabel.setText("DEPARTMENT");

        comboDepartment.setBackground(new java.awt.Color(0, 153, 153));
        comboDepartment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Executive / Leadership", "Administration & Operations", "Finance & Accounting", "Human Resources", "Marketing & Sales", "Information Technology (IT)", "Product Development & Design", "Research & Development (R&D)", "Customer Service" }));
        comboDepartment.setSelectedIndex(-1);
        comboDepartment.setPreferredSize(new java.awt.Dimension(250, 30));
        comboDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboDepartmentActionPerformed(evt);
            }
        });

        btnADD.setBackground(new java.awt.Color(0, 204, 102));
        btnADD.setForeground(new java.awt.Color(204, 255, 255));
        btnADD.setText("ADD");
        btnADD.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnADD.setFocusable(false);
        btnADD.setMinimumSize(new java.awt.Dimension(65, 30));
        btnADD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnADDActionPerformed(evt);
            }
        });

        btnCLEAR.setBackground(new java.awt.Color(13, 148, 136));
        btnCLEAR.setForeground(new java.awt.Color(204, 255, 255));
        btnCLEAR.setText("CLEAR");
        btnCLEAR.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCLEAR.setFocusable(false);
        btnCLEAR.setMinimumSize(new java.awt.Dimension(70, 30));
        btnCLEAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCLEARActionPerformed(evt);
            }
        });

        btnCANCEL.setBackground(new java.awt.Color(239, 68, 68));
        btnCANCEL.setForeground(new java.awt.Color(204, 255, 255));
        btnCANCEL.setText("CANCEL");
        btnCANCEL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCANCEL.setEnabled(false);
        btnCANCEL.setFocusable(false);
        btnCANCEL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCANCELActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("PERSONAL INFORMATION");

        qrHolder.setForeground(new java.awt.Color(51, 51, 51));
        qrHolder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        qrHolder.setText("QR");
        qrHolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("QR Code");

        btnSAVE1.setBackground(new java.awt.Color(0, 204, 102));
        btnSAVE1.setForeground(new java.awt.Color(204, 255, 255));
        btnSAVE1.setText("SAVE");
        btnSAVE1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSAVE1.setEnabled(false);
        btnSAVE1.setFocusable(false);
        btnSAVE1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSAVE1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addEmployeePanelLayout = new javax.swing.GroupLayout(addEmployeePanel);
        addEmployeePanel.setLayout(addEmployeePanelLayout);
        addEmployeePanelLayout.setHorizontalGroup(
            addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                .addContainerGap(166, Short.MAX_VALUE)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(birthDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dateBirth, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboGender, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(60, 60, 60)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hiredate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateHired, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(positionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(salaryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(departmentAddLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addComponent(btnADD, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btnSAVE1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCLEAR, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btnCANCEL))
                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(addEmployeePanelLayout.createSequentialGroup()
                            .addComponent(imageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(60, 60, 60)
                            .addComponent(qrHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(addEmployeePanelLayout.createSequentialGroup()
                            .addComponent(btnUploadPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(60, 60, 60)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(171, Short.MAX_VALUE))
        );
        addEmployeePanelLayout.setVerticalGroup(
            addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                .addContainerGap(54, Short.MAX_VALUE)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(imageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(qrHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(58, 58, 58)
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUploadPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(birthDate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(departmentAddLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateBirth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(positionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(salaryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(contactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hiredate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dateHired, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnADD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCANCEL)
                    .addComponent(btnSAVE1)
                    .addComponent(btnCLEAR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        jPanel3.add(addEmployeePanel, "card2");

        attendanceList.setBackground(new java.awt.Color(221, 221, 221));
        attendanceList.setLayout(new java.awt.BorderLayout());

        jPanel5.setBackground(new java.awt.Color(26, 0, 51));
        jPanel5.setPreferredSize(new java.awt.Dimension(1232, 60));

        jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox1.setEditable(true);
        jComboBox1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox1.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Search" }));
        jComboBox1.setPreferredSize(new java.awt.Dimension(300, 30));

        btnExport.setBackground(new java.awt.Color(0, 204, 102));
        btnExport.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setText("SAVE");
        btnExport.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 596, Short.MAX_VALUE)
                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExport))
                .addGap(16, 16, 16))
        );

        attendanceList.add(jPanel5, java.awt.BorderLayout.NORTH);

        attendanceTable.setBackground(new java.awt.Color(255, 255, 255));
        attendanceTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        attendanceTable.setForeground(new java.awt.Color(0, 0, 0));
        attendanceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Employee ID", "Full Name", "Date", "AM Time In", "AM Time Out", "PM Time In", "PM Time Out"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        attendanceTable.setFocusable(false);
        attendanceTable.setGridColor(new java.awt.Color(0, 0, 0));
        attendanceTable.setRowHeight(30);
        attendanceTable.setRowMargin(10);
        attendanceTable.setShowGrid(true);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(attendanceTable);

        attendanceList.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.add(attendanceList, "card3");

        viewEmployeeList.setBackground(new java.awt.Color(221, 221, 221));
        viewEmployeeList.setLayout(new java.awt.BorderLayout());

        jPanel4.setBackground(new java.awt.Color(26, 0, 51));
        jPanel4.setPreferredSize(new java.awt.Dimension(1232, 60));

        jComboBox.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox.setEditable(true);
        jComboBox.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Search" }));
        jComboBox.setPreferredSize(new java.awt.Dimension(300, 30));

        btnEdit.setBackground(new java.awt.Color(0, 204, 102));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEdit.setForeground(new java.awt.Color(255, 255, 255));
        btnEdit.setText("EDIT");
        btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEdit.setEnabled(false);
        btnEdit.setFocusable(false);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnRemove.setBackground(new java.awt.Color(231, 76, 60));
        btnRemove.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRemove.setForeground(new java.awt.Color(255, 255, 255));
        btnRemove.setText("REMOVE");
        btnRemove.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRemove.setEnabled(false);
        btnRemove.setFocusable(false);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        unselectButton.setBackground(new java.awt.Color(255, 255, 255));
        unselectButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        unselectButton.setForeground(new java.awt.Color(51, 51, 51));
        unselectButton.setText("UNSELECT");
        unselectButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        unselectButton.setEnabled(false);
        unselectButton.setFocusable(false);
        unselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 411, Short.MAX_VALUE)
                .addComponent(unselectButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemove)
                    .addComponent(btnEdit)
                    .addComponent(unselectButton))
                .addGap(15, 15, 15))
        );

        viewEmployeeList.add(jPanel4, java.awt.BorderLayout.NORTH);

        employeesTable.setBackground(new java.awt.Color(255, 255, 255));
        employeesTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        employeesTable.setForeground(new java.awt.Color(0, 0, 0));
        employeesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Employee ID", "Name", "Position", "Department"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        employeesTable.setFocusable(false);
        employeesTable.setGridColor(new java.awt.Color(0, 0, 0));
        employeesTable.setRowHeight(30);
        employeesTable.setRowMargin(10);
        employeesTable.setShowGrid(true);
        employeesTable.getTableHeader().setReorderingAllowed(false);
        employeesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                employeesTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(employeesTable);

        viewEmployeeList.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.add(viewEmployeeList, "card4");

        payroll.setBackground(new java.awt.Color(221, 221, 221));
        payroll.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(26, 0, 51));
        jPanel6.setPreferredSize(new java.awt.Dimension(1232, 60));

        jComboBox2.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox2.setEditable(true);
        jComboBox2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox2.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Search" }));
        jComboBox2.setPreferredSize(new java.awt.Dimension(300, 30));

        btnView1.setBackground(new java.awt.Color(255, 255, 255));
        btnView1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnView1.setForeground(new java.awt.Color(51, 51, 51));
        btnView1.setText("VIEW");
        btnView1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnView1.setEnabled(false);
        btnView1.setFocusable(false);
        btnView1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnView1ActionPerformed(evt);
            }
        });

        unselectButton1.setBackground(new java.awt.Color(255, 255, 255));
        unselectButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        unselectButton1.setForeground(new java.awt.Color(51, 51, 51));
        unselectButton1.setText("UNSELECT");
        unselectButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        unselectButton1.setEnabled(false);
        unselectButton1.setFocusable(false);
        unselectButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectButton1ActionPerformed(evt);
            }
        });

        btnExport1.setBackground(new java.awt.Color(0, 204, 102));
        btnExport1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExport1.setForeground(new java.awt.Color(255, 255, 255));
        btnExport1.setText("SAVE");
        btnExport1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExport1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExport1ActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Date");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 333, Short.MAX_VALUE)
                .addComponent(unselectButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnView1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnExport1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnView1)
                    .addComponent(unselectButton1)
                    .addComponent(btnExport1)
                    .addComponent(jLabel9))
                .addGap(15, 15, 15))
        );

        payroll.add(jPanel6, java.awt.BorderLayout.NORTH);

        payrollTable.setBackground(new java.awt.Color(255, 255, 255));
        payrollTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        payrollTable.setForeground(new java.awt.Color(0, 0, 0));
        payrollTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Employee ID", "Name", "Position", "Gross Pay"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        payrollTable.setFocusable(false);
        payrollTable.setGridColor(new java.awt.Color(0, 0, 0));
        payrollTable.setRowHeight(30);
        payrollTable.setRowMargin(10);
        payrollTable.setShowGrid(true);
        payrollTable.getTableHeader().setReorderingAllowed(false);
        payrollTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                payrollTableMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(payrollTable);

        payroll.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel3.add(payroll, "card5");

        settings.setBackground(new java.awt.Color(221, 221, 221));
        settings.setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.setBackground(new java.awt.Color(26, 0, 51));
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(700, 500));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(900, 600));

        changePasswordPanel.setBackground(new java.awt.Color(26, 0, 51));
        changePasswordPanel.setLayout(new java.awt.GridBagLayout());

        changePasswordUsernameLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        changePasswordUsernameLabel.setForeground(new java.awt.Color(255, 255, 255));
        changePasswordUsernameLabel.setText("Username:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        changePasswordPanel.add(changePasswordUsernameLabel, gridBagConstraints);

        currentPasswordLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        currentPasswordLabel.setForeground(new java.awt.Color(255, 255, 255));
        currentPasswordLabel.setText("Current Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        changePasswordPanel.add(currentPasswordLabel, gridBagConstraints);

        currentPasswordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        currentPasswordField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        changePasswordPanel.add(currentPasswordField, gridBagConstraints);

        newPasswordLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        newPasswordLabel.setForeground(new java.awt.Color(255, 255, 255));
        newPasswordLabel.setText("New Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        changePasswordPanel.add(newPasswordLabel, gridBagConstraints);

        newPasswordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        newPasswordField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        changePasswordPanel.add(newPasswordField, gridBagConstraints);

        confirmPasswordLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        confirmPasswordLabel.setForeground(new java.awt.Color(255, 255, 255));
        confirmPasswordLabel.setText("Confirm Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
        changePasswordPanel.add(confirmPasswordLabel, gridBagConstraints);

        confirmPasswordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        confirmPasswordField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        changePasswordPanel.add(confirmPasswordField, gridBagConstraints);

        changeButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        changeButton.setText("CHANGE");
        changeButton.setFocusable(false);
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        changePasswordPanel.add(changeButton, gridBagConstraints);

        changePasswordShowPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        changePasswordShowPassword.setForeground(new java.awt.Color(255, 255, 255));
        changePasswordShowPassword.setText("Show Passwords");
        changePasswordShowPassword.setFocusable(false);
        changePasswordShowPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordShowPasswordActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        changePasswordPanel.add(changePasswordShowPassword, gridBagConstraints);

        jTabbedPane1.addTab("Change Password", changePasswordPanel);

        manageUsersPanel.setBackground(new java.awt.Color(26, 0, 51));
        manageUsersPanel.setLayout(new java.awt.GridLayout(1, 0));

        addUpdatePanel.setOpaque(false);
        addUpdatePanel.setLayout(new java.awt.GridBagLayout());

        addUpdateLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        addUpdateLabel.setForeground(new java.awt.Color(255, 255, 255));
        addUpdateLabel.setText("Add/Update User");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel.add(addUpdateLabel, gridBagConstraints);

        addUpdateUsernameLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdateUsernameLabel.setForeground(new java.awt.Color(255, 255, 255));
        addUpdateUsernameLabel.setText("Username:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        addUpdatePanel.add(addUpdateUsernameLabel, gridBagConstraints);

        usernameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        usernameField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel.add(usernameField, gridBagConstraints);

        addUpdatePasswordLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdatePasswordLabel.setForeground(new java.awt.Color(255, 255, 255));
        addUpdatePasswordLabel.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
        addUpdatePanel.add(addUpdatePasswordLabel, gridBagConstraints);

        passwordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        passwordField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        addUpdatePanel.add(passwordField, gridBagConstraints);

        manageUsersShowPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        manageUsersShowPassword.setForeground(new java.awt.Color(255, 255, 255));
        manageUsersShowPassword.setText("Show Password");
        manageUsersShowPassword.setFocusable(false);
        manageUsersShowPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageUsersShowPasswordActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        addUpdatePanel.add(manageUsersShowPassword, gridBagConstraints);

        addButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addButton.setText("ADD");
        addButton.setFocusable(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel.add(addButton, gridBagConstraints);

        manageUsersPanel.add(addUpdatePanel);

        tablePanel.setOpaque(false);
        tablePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setBorder(null);

        usersTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "User Id", "Username"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        usersTable.setFocusable(false);
        usersTable.setShowGrid(true);
        usersTable.setSurrendersFocusOnKeystroke(true);
        usersTable.getTableHeader().setReorderingAllowed(false);
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usersTableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(usersTable);

        tablePanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jPanel8.setOpaque(false);
        jPanel8.setPreferredSize(new java.awt.Dimension(400, 50));
        jPanel8.setLayout(new java.awt.GridBagLayout());

        removeButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        removeButton.setText("REMOVE");
        removeButton.setEnabled(false);
        removeButton.setFocusable(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        jPanel8.add(removeButton, new java.awt.GridBagConstraints());

        updateButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        updateButton.setText("UPDATE");
        updateButton.setEnabled(false);
        updateButton.setFocusable(false);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        jPanel8.add(updateButton, new java.awt.GridBagConstraints());

        clearSelectionButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        clearSelectionButton.setText("CLEAR SELECTION");
        clearSelectionButton.setEnabled(false);
        clearSelectionButton.setFocusable(false);
        clearSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectionButtonActionPerformed(evt);
            }
        });
        jPanel8.add(clearSelectionButton, new java.awt.GridBagConstraints());

        tablePanel.add(jPanel8, java.awt.BorderLayout.SOUTH);

        manageUsersPanel.add(tablePanel);

        jTabbedPane1.addTab("Manage Users", manageUsersPanel);

        manageDeductions.setBackground(new java.awt.Color(26, 0, 51));
        manageDeductions.setLayout(new java.awt.GridLayout(1, 0));

        addUpdatePanel1.setOpaque(false);
        addUpdatePanel1.setLayout(new java.awt.GridBagLayout());

        manageDeductionLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        manageDeductionLabel.setForeground(new java.awt.Color(255, 255, 255));
        manageDeductionLabel.setText("Manage Deductions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel1.add(manageDeductionLabel, gridBagConstraints);

        addUpdateUsernameLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdateUsernameLabel1.setForeground(new java.awt.Color(255, 255, 255));
        addUpdateUsernameLabel1.setText("PagIbig:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        addUpdatePanel1.add(addUpdateUsernameLabel1, gridBagConstraints);

        pagibigField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        pagibigField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel1.add(pagibigField, gridBagConstraints);

        addUpdatePasswordLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdatePasswordLabel2.setForeground(new java.awt.Color(255, 255, 255));
        addUpdatePasswordLabel2.setText("PhilHealth:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
        addUpdatePanel1.add(addUpdatePasswordLabel2, gridBagConstraints);

        philhealthField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        philhealthField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel1.add(philhealthField, gridBagConstraints);

        addUpdateUsernameLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdateUsernameLabel2.setForeground(new java.awt.Color(255, 255, 255));
        addUpdateUsernameLabel2.setText("SSS:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        addUpdatePanel1.add(addUpdateUsernameLabel2, gridBagConstraints);

        sssField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        sssField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel1.add(sssField, gridBagConstraints);

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton9.setText("SAVE");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel1.add(jButton9, gridBagConstraints);

        manageDeductions.add(addUpdatePanel1);

        jTabbedPane1.addTab("Manage Deductions", manageDeductions);

        database.setBackground(new java.awt.Color(26, 0, 51));
        database.setLayout(new java.awt.GridBagLayout());

        resetDatabaseButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        resetDatabaseButton.setText("RESET DATABASE");
        resetDatabaseButton.setFocusable(false);
        resetDatabaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetDatabaseButtonActionPerformed(evt);
            }
        });
        database.add(resetDatabaseButton, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab("Database", database);

        settings.add(jTabbedPane1, new java.awt.GridBagConstraints());

        jPanel3.add(settings, "card6");

        managePositionRates.setBackground(new java.awt.Color(221, 221, 221));
        managePositionRates.setLayout(new java.awt.GridLayout(1, 0));

        addUpdatePanel2.setOpaque(false);
        addUpdatePanel2.setLayout(new java.awt.GridBagLayout());

        addUpdateLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        addUpdateLabel1.setForeground(new java.awt.Color(51, 51, 51));
        addUpdateLabel1.setText("Add/Update RATES");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel2.add(addUpdateLabel1, gridBagConstraints);

        departmentLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        departmentLabel.setForeground(new java.awt.Color(51, 51, 51));
        departmentLabel.setText("Department:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        addUpdatePanel2.add(departmentLabel, gridBagConstraints);

        departmentField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        departmentField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel2.add(departmentField, gridBagConstraints);

        addUpdateUsernameLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdateUsernameLabel3.setForeground(new java.awt.Color(51, 51, 51));
        addUpdateUsernameLabel3.setText("Position:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        addUpdatePanel2.add(addUpdateUsernameLabel3, gridBagConstraints);

        positionField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        positionField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel2.add(positionField, gridBagConstraints);

        addUpdatePasswordLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        addUpdatePasswordLabel1.setForeground(new java.awt.Color(51, 51, 51));
        addUpdatePasswordLabel1.setText("Rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
        addUpdatePanel2.add(addUpdatePasswordLabel1, gridBagConstraints);

        addButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addButton1.setText("ADD");
        addButton1.setFocusable(false);
        addButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel2.add(addButton1, gridBagConstraints);

        rateField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rateField.setPreferredSize(new java.awt.Dimension(200, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        addUpdatePanel2.add(rateField, gridBagConstraints);

        managePositionRates.add(addUpdatePanel2);

        tablePanel1.setOpaque(false);
        tablePanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setBorder(null);

        positionRateTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        positionRateTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Id", "Department", "Position", "Rate"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        positionRateTable.setFocusable(false);
        positionRateTable.setRowHeight(25);
        positionRateTable.setRowMargin(3);
        positionRateTable.setShowGrid(true);
        positionRateTable.setSurrendersFocusOnKeystroke(true);
        positionRateTable.getTableHeader().setReorderingAllowed(false);
        positionRateTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                positionRateTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(positionRateTable);

        tablePanel1.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(400, 50));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        removeButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        removeButton1.setText("REMOVE");
        removeButton1.setEnabled(false);
        removeButton1.setFocusable(false);
        removeButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButton1ActionPerformed(evt);
            }
        });
        jPanel9.add(removeButton1, new java.awt.GridBagConstraints());

        updateButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        updateButton1.setText("UPDATE");
        updateButton1.setEnabled(false);
        updateButton1.setFocusable(false);
        updateButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButton1ActionPerformed(evt);
            }
        });
        jPanel9.add(updateButton1, new java.awt.GridBagConstraints());

        clearSelectionButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        clearSelectionButton1.setText("CLEAR SELECTION");
        clearSelectionButton1.setEnabled(false);
        clearSelectionButton1.setFocusable(false);
        clearSelectionButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectionButton1ActionPerformed(evt);
            }
        });
        jPanel9.add(clearSelectionButton1, new java.awt.GridBagConstraints());

        tablePanel1.add(jPanel9, java.awt.BorderLayout.SOUTH);

        jComboBox3.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox3.setEditable(true);
        jComboBox3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox3.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Search" }));
        jComboBox3.setPreferredSize(new java.awt.Dimension(300, 30));
        tablePanel1.add(jComboBox3, java.awt.BorderLayout.NORTH);

        managePositionRates.add(tablePanel1);

        jPanel3.add(managePositionRates, "card8");

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        btnEdit.setEnabled(false);
        btnRemove.setEnabled(false);
        unselectButton.setEnabled(false);

        card.show(jPanel3, "card4");
        fetch();
        loadEmployeeListComboBox();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card3");
        fetchAttendanceList();
        loadComboBox();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnUploadPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadPhotoActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Employee Photo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            imagePath = selectedFile.getAbsolutePath(); // i-save yung path sa global variable mo

            // Ipakita sa label (optional kung may label ka para makita path)
            // Halimbawa: jLabel13.setText(imagePath); // kung gusto mong makita sa design mo
            // Display image preview sa jLabel13 (kung yan ang image display mo)
            ImageIcon imageIcon = new ImageIcon(
                    new ImageIcon(imagePath).getImage()
                            .getScaledInstance(imageHolder.getWidth(), imageHolder.getHeight(), Image.SCALE_SMOOTH)
            );
            imageHolder.setIcon(imageIcon);
            imageHolder.setText("");
        }
    }//GEN-LAST:event_btnUploadPhotoActionPerformed

    private void btnADDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnADDActionPerformed
        try (Connection connection = DatabaseConnection.getConnection()) {
            String name = txtName.getText().trim();
            java.util.Date birthUtilDate = dateBirth.getDate(); // get Date object
            String gender = comboGender.getSelectedItem().toString();
            String address = txtAddress.getText().trim();
            String contact = txtContact.getText().trim();
            String salary = txtSalary.getText().trim();
            String email = txtEmail.getText().trim();
            String position = comboPosition.getSelectedItem().toString();
            String department = comboDepartment.getSelectedItem().toString();
            java.util.Date hiredUtilDate = dateHired.getDate(); // get Date object
            String photoPath = imagePath;

            // Validate
            if (name.isEmpty() || birthUtilDate == null || address.isEmpty() || contact.isEmpty()
                    || email.isEmpty() || position.isEmpty() || department.isEmpty() || hiredUtilDate == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create "photos" folder if it doesn't exist
            File photoDir = new File("photos");
            if (!photoDir.exists()) {
                photoDir.mkdir();
            }

            String fileName = selectedFile.getName();

            // 2️⃣ — Copy selected photo to "photos" folder
            File destinationFile = new File(photoDir, fileName);
            Files.copy(selectedFile.toPath(), destinationFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 3️⃣ — Prepare to insert into database
            photoPath = destinationFile.getPath();

            // Convert java.util.Date to java.sql.Date
            java.sql.Date birthDate = new java.sql.Date(birthUtilDate.getTime());
            java.sql.Date hiredDate = new java.sql.Date(hiredUtilDate.getTime());

            // SQL Insert
            String sql = "INSERT INTO employees_table (photo_path, full_name, birth_date, gender, address, contact_number, salary, email_address, position, department, hired_date) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, photoPath);
            pst.setString(2, name);
            pst.setDate(3, birthDate);
            pst.setString(4, gender);
            pst.setString(5, address);
            pst.setString(6, contact);
            pst.setString(7, salary);
            pst.setString(8, email);
            pst.setString(9, position);
            pst.setString(10, department);
            pst.setDate(11, hiredDate);

            int inserted = pst.executeUpdate();

            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Employee added successfully!");

                // ✅ Reset lahat ng fields, kasama na picture
                clearFields();

                fetch(); // refresh table data
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add employee!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnADDActionPerformed

    private void btnCLEARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCLEARActionPerformed
        clearFields();
    }//GEN-LAST:event_btnCLEARActionPerformed

    private void btnCANCELActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCANCELActionPerformed
        if (JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel? All unsaved data will be cleared.",
                "Cancel",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION) {
            clearFields();

            btnEdit.setEnabled(false);
            btnRemove.setEnabled(false);
            unselectButton.setEnabled(false);
            employeesTable.clearSelection();

            card.show(jPanel3, "card4");
        }

    }//GEN-LAST:event_btnCANCELActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        int row = employeesTable.getSelectedRow();
        String id = employeesTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to Remove this?.",
                "Confimation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("DELETE FROM employees_table WHERE employee_id = ?");
                pstmt.setString(1, id);

                pstmt.execute();

                fetch();

                JOptionPane.showMessageDialog(this, "Product Removed!");
            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_removeActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        int row = employeesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit!");
            return;
        }

        int id = Integer.parseInt(employeesTable.getValueAt(row, 0).toString());
        selectedEmployeeId = id; // store globally

        loadEmployeeData(id);
        card.show(jPanel3, "card2");

        btnADD.setEnabled(false);
        btnSAVE1.setEnabled(true);
        btnCANCEL.setEnabled(true);

        jPanel3.revalidate();
        jPanel3.repaint();

    }//GEN-LAST:event_updateActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card2");
        clearFields();
        generateQr();

        btnADD.setEnabled(true);
        btnSAVE1.setEnabled(false);
        btnCANCEL.setEnabled(false);

        this.revalidate();
        this.repaint();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to Logout?",
                "Confimation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginPage(this).setVisible(true);
        }
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card1");
        jLabel8.setText(today.toString());
        updateDashboardCounts();
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card5");
        btnView1.setEnabled(false);
        unselectButton1.setEnabled(false);
        payrollTable.clearSelection();
        recordPayslip();
        fetchPayroll();
        loadPayrollComboBox();
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card6");
        fetchDeductions();
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed

        int row = employeesTable.getSelectedRow();
        String id = employeesTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to Remove this?.",
                "Confimation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = connection.prepareStatement("SELECT photo_path FROM employees_table WHERE employee_id = ?");
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    File photoFile = new File(rs.getString("photo_path"));
                    File qrFile = new File("qrcodes/" + id + ".png");
                    if (photoFile.delete() && qrFile.delete()) {
                        System.out.println(photoFile.getName() + " and " + qrFile.getName() + " File Deleted");
                    }

                    pstmt = connection.prepareStatement("DELETE FROM employees_table WHERE employee_id = ?");
                    pstmt.setString(1, id);
                    pstmt.executeUpdate();

                    fetch();

                    btnEdit.setEnabled(false);
                    btnRemove.setEnabled(false);
                    unselectButton.setEnabled(false);

                    JOptionPane.showMessageDialog(this, "Employee Removed!");

                }
            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = employeesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit!");
            return;
        }

        int id = Integer.parseInt(employeesTable.getValueAt(row, 0).toString());
        selectedEmployeeId = id; // store globally

        loadEmployeeData(id);
        card.show(jPanel3, "card2");

        btnADD.setEnabled(false);
        btnSAVE1.setEnabled(true);
        btnCANCEL.setEnabled(true);

        jPanel3.revalidate();
        jPanel3.repaint();
    }//GEN-LAST:event_btnEditActionPerformed

    private void comboGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboGenderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboGenderActionPerformed

    private void comboPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboPositionActionPerformed
        // TODO add your handling code here:
        if (comboPosition.getSelectedItem() != null) {
            String position = comboPosition.getSelectedItem().toString();

            String sql = "SELECT salary_rate FROM position_salary_rate WHERE position_name = ?";

            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setString(1, position);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    txtSalary.setText(rs.getBigDecimal("salary_rate").toString());
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_comboPositionActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        // ===== SAVE AS (Any File Type) =====
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save As (Any File Type)");
        fileChooser.setAcceptAllFileFilterUsed(true);

        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(file)) {

                // Write column headers
                for (int i = 0; i < attendanceTable.getColumnCount(); i++) {
                    writer.write(attendanceTable.getColumnName(i));
                    if (i < attendanceTable.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");

                // Write all rows
                for (int row = 0; row < attendanceTable.getRowCount(); row++) {
                    for (int col = 0; col < attendanceTable.getColumnCount(); col++) {
                        Object value = attendanceTable.getValueAt(row, col);
                        writer.write(value == null ? "" : value.toString());
                        if (col < attendanceTable.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(null,
                        "Attendance exported successfully!\nSaved as: " + file.getName());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error exporting file!");
            }
        }

        // ===== PRINT TABLE =====
        try {
            boolean complete = attendanceTable.print(
                    javax.swing.JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("Attendance List"), // header
                    new MessageFormat("Page {0}") // footer
            );

            if (complete) {
                JOptionPane.showMessageDialog(null, "Printing Complete!");
            } else {
                JOptionPane.showMessageDialog(null, "Printing Cancelled");
            }

        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(null, "Printing Failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnExportActionPerformed

    private void employeesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_employeesTableMouseClicked
        if (evt.getClickCount() == 2) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
        btnEdit.setEnabled(true);
        btnRemove.setEnabled(true);
        unselectButton.setEnabled(true);
    }//GEN-LAST:event_employeesTableMouseClicked

    private void dashboardmainMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardmainMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardmainMouseEntered

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        changePassword();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void changePasswordShowPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordShowPasswordActionPerformed
        if (changePasswordShowPassword.isSelected()) {
            currentPasswordField.setEchoChar((char) 0);
            newPasswordField.setEchoChar((char) 0);
            confirmPasswordField.setEchoChar((char) 0);
        } else {
            currentPasswordField.setEchoChar('•');
            newPasswordField.setEchoChar('•');
            confirmPasswordField.setEchoChar('•');
        }
    }//GEN-LAST:event_changePasswordShowPasswordActionPerformed

    private void manageUsersShowPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageUsersShowPasswordActionPerformed
        if (manageUsersShowPassword.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }//GEN-LAST:event_manageUsersShowPasswordActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addUser();
    }//GEN-LAST:event_addButtonActionPerformed

    private void usersTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usersTableMouseClicked
        addButton.setEnabled(false);
        removeButton.setEnabled(true);
        updateButton.setEnabled(true);
        clearSelectionButton.setEnabled(true);
        int row = usersTable.getSelectedRow();
        selectedUserId = Integer.parseInt(usersTable.getValueAt(row, 0).toString());
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users_table WHERE user_id = ?");
            pstmt.setInt(1, selectedUserId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                passwordField.setText(rs.getString("password"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Fetching User Failed!\n" + ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_usersTableMouseClicked

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeUser();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateUser();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void clearSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectionButtonActionPerformed
        addButton.setEnabled(true);
        removeButton.setEnabled(false);
        updateButton.setEnabled(false);
        clearSelectionButton.setEnabled(false);
        usersTable.clearSelection();
        usernameField.setText("");
        passwordField.setText("");
    }//GEN-LAST:event_clearSelectionButtonActionPerformed

    private void resetDatabaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetDatabaseButtonActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to RESET The DATABASE? This Cannot be Undone", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt;

//                File photoFile = new File("photos/");
//                deleteFolder(photoFile);
//                File qrFile = new File("qrcodes/");
//                deleteFolder(qrFile);
//
//                System.out.println(photoFile.getName() + " and " + qrFile.getName() + " File Deleted");
                pstmt = connection.prepareStatement("DROP DATABASE employee_management_database");
                pstmt.executeUpdate();

                pstmt = connection.prepareStatement("CREATE DATABASE employee_management_database");
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "RESET Successful!");

                dispose();
                java.awt.EventQueue.invokeLater(() -> {
                    new LoginPage(this).setVisible(true);
                });
            } catch (SQLException ex) {
                Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Reset Failed!\n" + ex.getLocalizedMessage());
            }
        }
    }//GEN-LAST:event_resetDatabaseButtonActionPerformed

    private void unselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unselectButtonActionPerformed
        // TODO add your handling code here:
        btnEdit.setEnabled(false);
        btnRemove.setEnabled(false);
        unselectButton.setEnabled(false);
        employeesTable.clearSelection();
    }//GEN-LAST:event_unselectButtonActionPerformed

    private void btnView1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnView1ActionPerformed

        // TODO add your handling code here:
        int row = payrollTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to view!");
            return;
        }

        int id = Integer.parseInt(payrollTable.getValueAt(row, 0).toString());
        selectedEmployeeId = id; // store globally

        GeneratePaySlip(selectedEmployeeId);

        // CREATE TEXT AREA
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(payslip);
        editorPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        Object[] options = {"Print", "Close"};

        int choice = JOptionPane.showOptionDialog(
                this,
                scrollPane,
                "PAYSLIP",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            new Thread(() -> {
                try {
                    editorPane.print();
                } catch (PrinterException ex) {
                    javax.swing.SwingUtilities.invokeLater(()
                            -> JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage())
                    );
                }
            }).start();
        }


    }//GEN-LAST:event_btnView1ActionPerformed

    private void unselectButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unselectButton1ActionPerformed
        // TODO add your handling code here:
        btnView1.setEnabled(false);
        unselectButton1.setEnabled(false);
        payrollTable.clearSelection();
    }//GEN-LAST:event_unselectButton1ActionPerformed

    private void payrollTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_payrollTableMouseClicked
        // TODO add your handling code here:
        int row = payrollTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit!");
            return;
        }

        int id = Integer.parseInt(payrollTable.getValueAt(row, 0).toString());
        selectedEmployeeId = id; // store globally

        btnView1.setEnabled(true);
        unselectButton1.setEnabled(true);
    }//GEN-LAST:event_payrollTableMouseClicked

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        if (timeInOut == null || !timeInOut.isDisplayable()) {
            timeInOut = new TimeInOutFrame1(this);
        }

        timeInOut.setVisible(true);
        timeInOut.toFront();

        this.dispose();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void btnSAVE1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSAVE1ActionPerformed
        // TODO add your handling code here:
        updateEmployee(selectedEmployeeId);
    }//GEN-LAST:event_btnSAVE1ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        String sss = sssField.getText();
        String philhealth = philhealthField.getText();
        String pagibig = pagibigField.getText();

        if (sss.isEmpty() || philhealth.isEmpty() || pagibig.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All Field Must be Field Out!");
        } else {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to save this?", "Confirmation",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                try (Connection connection = DatabaseConnection.getConnection()) {
                    PreparedStatement pstmt = connection.prepareStatement("UPDATE deduction_rates_table SET sss_rate = ?, philhealth_rate = ?, pagibig_rate = ?  WHERE deduction_rate_id = ?");
                    pstmt.setString(1, sss);
                    pstmt.setString(2, philhealth);
                    pstmt.setString(3, pagibig);
                    pstmt.setInt(4, 1);
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Update Successful!");

                    fetchDeductions();

                } catch (SQLException ex) {
                    Logger.getLogger(DashboardFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, "Upadating Failed!\n" + ex.getLocalizedMessage());
                }
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void btnExport1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExport1ActionPerformed
        // TODO add your handling code here:
        new Thread(() -> {
            try {
                payrollTable.print();
            } catch (PrinterException ex) {
                javax.swing.SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage())
                );
            }
        }).start();

    }//GEN-LAST:event_btnExport1ActionPerformed

    private void comboDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboDepartmentActionPerformed
        // TODO add your handling code here:
        if (comboDepartment.getSelectedItem() != null) {

            comboPosition.removeAllItems();

            String sql = "SELECT position_name FROM department_positions "
                    + "WHERE department_name = ? ORDER BY position_name";

            try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, comboDepartment.getSelectedItem().toString());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    comboPosition.addItem(rs.getString("position_name"));
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_comboDepartmentActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        card.show(jPanel3, "card8");
        fetchPositionRateData();
        loadPositionRateComboBox();

        departmentField.setText("");
        positionField.setText("");
        rateField.setText("");
        positionRateTable.clearSelection();

        addButton1.setEnabled(true);
        updateButton1.setEnabled(false);
        removeButton1.setEnabled(false);
        clearSelectionButton1.setEnabled(false);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void addButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButton1ActionPerformed
        // TODO add your handling code here:
        String depeartment = departmentField.getText().trim();
        String position = positionField.getText().trim();
        String salaryText = rateField.getText().trim();

        if (position.isEmpty() || salaryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both position and salary.");
            return;
        }

        try {
            BigDecimal salary = new BigDecimal(salaryText);

            String sqlDeparment = "INSERT INTO department_positions (department_name, position_name) VALUES (?, ?)";

            try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlDeparment)) {

                ps.setString(1, depeartment);
                ps.setString(2, position);
                ps.executeUpdate();

            }

            String sqlPosition = "INSERT INTO position_salary_rate (position_name, salary_rate) VALUES (?, ?)";

            try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlPosition)) {

                ps.setString(1, position);
                ps.setBigDecimal(2, salary);
                ps.executeUpdate();

            }

            fetchPositionRateData();
            JOptionPane.showMessageDialog(this, "Add successful.");

            departmentField.setText("");
            positionField.setText("");
            rateField.setText("");
            positionRateTable.clearSelection();

            addButton1.setEnabled(true);
            updateButton1.setEnabled(false);
            removeButton1.setEnabled(false);
            clearSelectionButton1.setEnabled(false);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary must be a valid number.");
        }
    }//GEN-LAST:event_addButton1ActionPerformed

    private void positionRateTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_positionRateTableMouseClicked
        // TODO add your handling code here:
        int selectedRow = positionRateTable.getSelectedRow();
        if (selectedRow != -1) {
            departmentField.setText(positionRateTable.getValueAt(selectedRow, 1).toString());
            positionField.setText(positionRateTable.getValueAt(selectedRow, 2).toString());
            rateField.setText(positionRateTable.getValueAt(selectedRow, 3).toString());
        }

        addButton1.setEnabled(false);
        updateButton1.setEnabled(true);
        removeButton1.setEnabled(true);
        clearSelectionButton1.setEnabled(true);

    }//GEN-LAST:event_positionRateTableMouseClicked

    private void removeButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButton1ActionPerformed
        // TODO add your handling code here:    
        int selectedRow = positionRateTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a position to delete.");
            return;
        }

        int id = Integer.parseInt(positionRateTable.getValueAt(selectedRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this position?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlDeparment = "DELETE FROM department_positions WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlDeparment)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

        String sqlPosition = "DELETE FROM position_salary_rate WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlPosition)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Position deleted successfully.");
            fetchPositionRateData();

            departmentField.setText("");
            positionField.setText("");
            rateField.setText("");
            positionRateTable.clearSelection();

            addButton1.setEnabled(true);
            updateButton1.setEnabled(false);
            removeButton1.setEnabled(false);
            clearSelectionButton1.setEnabled(false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_removeButton1ActionPerformed

    private void updateButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButton1ActionPerformed
        // TODO add your handling code here:
        int selectedRow = positionRateTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a position to update.");
            return;
        }

        int id = Integer.parseInt(positionRateTable.getValueAt(selectedRow, 0).toString());

        String department = departmentField.getText().trim();
        String position = positionField.getText().trim();
        String salaryText = rateField.getText().trim();

        if (position.isEmpty() || salaryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both position and salary.");
            return;
        }

        try {
            BigDecimal salary = new BigDecimal(salaryText);

            String sqlDepartment = "UPDATE department_positions SET department_name = ?, position_name = ? WHERE id = ?";

            try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlDepartment)) {

                ps.setString(1, department);
                ps.setString(2, position);
                ps.setInt(3, id);
                ps.executeUpdate();

            }

            String sqlPosition = "UPDATE position_salary_rate SET position_name = ?, salary_rate = ? WHERE id = ?";

            try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sqlPosition)) {

                ps.setString(1, position);
                ps.setBigDecimal(2, salary);
                ps.setInt(3, id);
                ps.executeUpdate();

            }

            fetchPositionRateData();
            JOptionPane.showMessageDialog(this, "Update successful.");

            departmentField.setText("");
            positionField.setText("");
            rateField.setText("");
            positionRateTable.clearSelection();

            addButton1.setEnabled(true);
            updateButton1.setEnabled(false);
            removeButton1.setEnabled(false);
            clearSelectionButton1.setEnabled(false);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary must be a valid number.");
        }
    }//GEN-LAST:event_updateButton1ActionPerformed

    private void clearSelectionButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectionButton1ActionPerformed
        // TODO add your handling code here:

        departmentField.setText("");
        positionField.setText("");
        rateField.setText("");
        positionRateTable.clearSelection();

        addButton1.setEnabled(true);
        updateButton1.setEnabled(false);
        removeButton1.setEnabled(false);
        clearSelectionButton1.setEnabled(false);

    }//GEN-LAST:event_clearSelectionButton1ActionPerformed
    private JButton activeButton = null; // keeps track of the current active button

    private void addButtonHoverEffects() {
        simpleHover(jButton1);
        simpleHover(jButton2);
        simpleHover(jButton3);
        simpleHover(jButton6);
        simpleHover(jButton4);
        simpleHover(jButton5);
        simpleHover(jButton7);
        simpleHover(jButton8);
        simpleHover(jButton10);

        setActiveButton(jButton4);
        activeButton = jButton4;
    }

    private void simpleHover(JButton button) {
        Color defaultColor = new Color(26, 0, 51);
        Color hoverColor = new Color(45, 45, 85);
        Color activeColor = new Color(78, 141, 245); // color for active button

        button.setOpaque(true);
        button.setBackground(defaultColor);
        button.setForeground(new Color(78, 141, 245));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(hoverColor);
                    button.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(defaultColor);
                    button.setForeground(new Color(78, 141, 245));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveButton(button);
            }
        });
    }

// call this when you want to set a button as active
    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            // reset previous active button
            activeButton.setBackground(new Color(26, 0, 51));
            activeButton.setForeground(new Color(78, 141, 245));
        }

        activeButton = button;
        activeButton.setBackground(new Color(78, 141, 245)); // active background
        activeButton.setForeground(Color.WHITE);              // active text color
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addButton1;
    private javax.swing.JPanel addEmployeePanel;
    private javax.swing.JLabel addUpdateLabel;
    private javax.swing.JLabel addUpdateLabel1;
    private javax.swing.JPanel addUpdatePanel;
    private javax.swing.JPanel addUpdatePanel1;
    private javax.swing.JPanel addUpdatePanel2;
    private javax.swing.JLabel addUpdatePasswordLabel;
    private javax.swing.JLabel addUpdatePasswordLabel1;
    private javax.swing.JLabel addUpdatePasswordLabel2;
    private javax.swing.JLabel addUpdateUsernameLabel;
    private javax.swing.JLabel addUpdateUsernameLabel1;
    private javax.swing.JLabel addUpdateUsernameLabel2;
    private javax.swing.JLabel addUpdateUsernameLabel3;
    private javax.swing.JLabel address;
    private javax.swing.JPanel attendanceList;
    private javax.swing.JTable attendanceTable;
    private javax.swing.JLabel birthDate;
    private javax.swing.JButton btnADD;
    private javax.swing.JButton btnCANCEL;
    private javax.swing.JButton btnCLEAR;
    public javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnExport1;
    public javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSAVE1;
    private javax.swing.JButton btnUploadPhoto;
    private javax.swing.JButton btnView1;
    private javax.swing.JButton changeButton;
    private javax.swing.JPanel changePasswordPanel;
    private javax.swing.JCheckBox changePasswordShowPassword;
    private javax.swing.JLabel changePasswordUsernameLabel;
    private javax.swing.JButton clearSelectionButton;
    private javax.swing.JButton clearSelectionButton1;
    private javax.swing.JComboBox<String> comboDepartment;
    private javax.swing.JComboBox<String> comboGender;
    private javax.swing.JComboBox<String> comboPosition;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JLabel contactNo;
    private javax.swing.JLabel countActive;
    private javax.swing.JLabel countInactive;
    private javax.swing.JLabel countTotal;
    private javax.swing.JPasswordField currentPasswordField;
    private javax.swing.JLabel currentPasswordLabel;
    private javax.swing.JPanel dashboardmain;
    private javax.swing.JPanel database;
    private com.toedter.calendar.JDateChooser dateBirth;
    private com.toedter.calendar.JDateChooser dateHired;
    private javax.swing.JLabel departmentAddLabel;
    private javax.swing.JTextField departmentField;
    private javax.swing.JLabel departmentLabel;
    private javax.swing.JLabel email;
    public javax.swing.JTable employeesTable;
    private javax.swing.JLabel gender;
    private javax.swing.JLabel hiredate;
    private javax.swing.JLabel imageHolder;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JLabel manageDeductionLabel;
    private javax.swing.JPanel manageDeductions;
    private javax.swing.JPanel managePositionRates;
    private javax.swing.JPanel manageUsersPanel;
    private javax.swing.JCheckBox manageUsersShowPassword;
    private javax.swing.JLabel name;
    private javax.swing.JPasswordField newPasswordField;
    private javax.swing.JLabel newPasswordLabel;
    private javax.swing.JTextField pagibigField;
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel panel2;
    private javax.swing.JPanel panel3;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JPanel payroll;
    private javax.swing.JTable payrollTable;
    private javax.swing.JTextField philhealthField;
    private javax.swing.JTextField positionField;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JTable positionRateTable;
    private javax.swing.JLabel qrHolder;
    private javax.swing.JTextField rateField;
    private javax.swing.JMenuItem remove;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeButton1;
    private javax.swing.JButton resetDatabaseButton;
    private javax.swing.JLabel salaryLabel;
    private javax.swing.JPanel settings;
    private javax.swing.JTextField sssField;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JPanel tablePanel1;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSalary;
    public javax.swing.JButton unselectButton;
    private javax.swing.JButton unselectButton1;
    private javax.swing.JMenuItem update;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton updateButton1;
    private javax.swing.JTextField usernameField;
    private javax.swing.JTable usersTable;
    private javax.swing.JPanel viewEmployeeList;
    // End of variables declaration//GEN-END:variables
}
