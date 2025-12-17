package newpackage;

import employee.management.system.DatabaseConnection;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class AttendanceDataGenerator {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement stmt = connection.createStatement();
            generateSampleAttendance(stmt);
            System.out.println("Sample attendance data generated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateSampleAttendance(Statement stmt) throws SQLException {
        Random random = new Random();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Generate attendance for 30 days (dec 1 - dec 30, 2025)
        LocalDate startDate = LocalDate.of(2025, 12, 1);

        // Employee IDs from 1000 to 1009 (the 10 sample employees)
        int[] employeeIds = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009};

        StringBuilder insertSQL = new StringBuilder();
        insertSQL.append("INSERT IGNORE INTO attendance_table (employee_id, date, am_time_in, am_time_out, pm_time_in, pm_time_out, total_hours, am_status, pm_status, status) VALUES ");

        for (int day = 0; day < 30; day++) {
            LocalDate currentDate = startDate.plusDays(day);

            for (int empId : employeeIds) {
                boolean attended = random.nextDouble() > 0.1; // 90% attendance rate

                if (attended) {
                    // AM Time In: 7:30-9:00 AM
                    LocalTime amIn = LocalTime.of(8, 0).minusMinutes(random.nextInt(60)).plusMinutes(random.nextInt(30));

                    // AM Time Out: 11:30 AM - 12:30 PM
                    LocalTime amOut = LocalTime.of(12, 0).minusMinutes(random.nextInt(30)).plusMinutes(random.nextInt(30));

                    // PM Time In: 1:00-2:00 PM
                    LocalTime pmIn = LocalTime.of(13, 30).minusMinutes(random.nextInt(30)).plusMinutes(random.nextInt(30));

                    // PM Time Out: 5:00-6:00 PM
                    LocalTime pmOut = LocalTime.of(17, 30).plusMinutes(random.nextInt(60));

                    double totalHours = Duration.between(amIn, pmOut).toMinutes() / 60.0;

                    String amStatus = amIn.isBefore(LocalTime.of(8, 0)) ? "" : "";
                    String pmStatus = pmOut.isAfter(LocalTime.of(17, 30)) ? "" : "";
                    String status = totalHours >= 8.0 ? "Present" : "";

                    insertSQL.append(String.format(
                            "(%d, '%s', '%s', '%s', '%s', '%s', %.2f, '%s', '%s', '%s'), ",
                            empId,
                            currentDate.format(dateFormatter),
                            amIn.format(timeFormatter),
                            amOut.format(timeFormatter),
                            pmIn.format(timeFormatter),
                            pmOut.format(timeFormatter),
                            totalHours,
                            amStatus,
                            pmStatus,
                            status
                    ));
                } else {
                    // Absent record
                    insertSQL.append(String.format(
                            "(%d, '%s', NULL, NULL, NULL, NULL, 0.00, 'Absent', 'Absent', 'Absent'), ",
                            empId,
                            currentDate.format(dateFormatter)
                    ));
                }
            }
        }

        // Remove last comma and execute
        if (insertSQL.length() > 0) {
            insertSQL.setLength(insertSQL.length() - 2); // Remove trailing ", "
            stmt.executeUpdate(insertSQL.toString());
        }
    }
}
