package newpackage;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AttendanceDataGenerator {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            List<Integer> employeeIds = fetchEmployeeIds(connection);

            if (employeeIds.isEmpty()) {
                System.out.println("No employees found. Attendance not generated.");
                return;
            }

            generateSampleAttendance(connection, employeeIds);
            System.out.println("Sample attendance data generated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Get employee IDs dynamically
    private static List<Integer> fetchEmployeeIds(Connection connection) throws SQLException {
        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT employee_id FROM employees_table";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ids.add(rs.getInt("employee_id"));
            }
        }
        return ids;
    }

    private static void generateSampleAttendance(Connection connection, List<Integer> employeeIds) throws SQLException {

        Random random = new Random();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalDate startDate = LocalDate.of(2026, 1, 1);

        String sql = """
            INSERT INTO attendance_table
            (employee_id, date, am_time_in, am_time_out, pm_time_in, pm_time_out,
             total_hours, am_status, pm_status, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (int day = 0; day < 30; day++) {
                LocalDate currentDate = startDate.plusDays(day);

                for (int empId : employeeIds) {

                    boolean attended = random.nextDouble() > 0.1; // 90% present

                    if (!attended) {
                        pstmt.setInt(1, empId);
                        pstmt.setString(2, currentDate.format(dateFormatter));
                        pstmt.setNull(3, Types.TIME);
                        pstmt.setNull(4, Types.TIME);
                        pstmt.setNull(5, Types.TIME);
                        pstmt.setNull(6, Types.TIME);
                        pstmt.setDouble(7, 0);
                        pstmt.setString(8, "Absent");
                        pstmt.setString(9, "Absent");
                        pstmt.setString(10, "Absent");
                        pstmt.addBatch();
                        continue;
                    }

                    LocalTime amIn  = LocalTime.of(8, 0).plusMinutes(random.nextInt(31));
                    LocalTime amOut = LocalTime.of(12, 0).minusMinutes(random.nextInt(31));
                    LocalTime pmIn  = LocalTime.of(13, 0).plusMinutes(random.nextInt(31));
                    LocalTime pmOut = LocalTime.of(17, 0).plusMinutes(random.nextInt(61));

                    double totalHours = Duration.between(amIn, pmOut).toMinutes() / 60.0;

                    pstmt.setInt(1, empId);
                    pstmt.setString(2, currentDate.format(dateFormatter));
                    pstmt.setString(3, amIn.format(timeFormatter));
                    pstmt.setString(4, amOut.format(timeFormatter));
                    pstmt.setString(5, pmIn.format(timeFormatter));
                    pstmt.setString(6, pmOut.format(timeFormatter));
                    pstmt.setDouble(7, totalHours);
                    pstmt.setString(8, "Present");
                    pstmt.setString(9, "Present");
                    pstmt.setString(10, totalHours >= 8 ? "Present" : "Undertime");

                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }
}
