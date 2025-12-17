package employee.management.system;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author Justine
 */
public class TimeInOutFrame1 extends javax.swing.JFrame implements Runnable, ThreadFactory {

    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));

    DashboardFrame dashboard;

    Webcam webcam;
    WebcamPanel webcamPanel;
    Executor executor = Executors.newSingleThreadExecutor(this);

    Timer timer;

    /**
     * Creates new form TimeInOutFrame
     */
    public TimeInOutFrame1(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        initComponents();

        timer = new Timer(500, e -> {
            dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        });
        timer.start();

        javax.swing.SwingUtilities.invokeLater(() -> employeeIdField.requestFocusInWindow());

        startCamera();
    }

    private void startCamera() {
        try {
            Webcam.setDriver(new com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver());

            if (Webcam.getWebcams().isEmpty()) {
                jLabel1.setText("No webcam detected!");
                JOptionPane.showMessageDialog(this, "No webcam detected!");
                return;
            }

            // Get the default webcam
            webcam = Webcam.getDefault();

            if (webcam == null) {
                jLabel1.setText("No webcam detected!");
                System.out.println("No webcam detected!");
                return;
            }

            // List all supported resolutions
            Dimension[] supported = webcam.getViewSizes();
            System.out.println("Webcam Supported resolutions:");
            for (Dimension d : supported) {
                System.out.println("- " + d.width + "x" + d.height);
            }

            // Pick the highest resolution
            Dimension highest = supported[0];
            for (Dimension d : supported) {
                if (d.width * d.height > highest.width * highest.height) {
                    highest = d;
                }
            }

            // Set to the highest resolution
            webcam.setViewSize(highest);

            System.out.println("\nUsing highest resolution: "
                    + highest.width + "x" + highest.height);

            //webcam = Webcam.getDefault();
            //webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setMirrored(true);

            camPanel.removeAll();
            camPanel.add(webcamPanel, java.awt.BorderLayout.CENTER);
            camPanel.revalidate();

            executor.execute(this);

        } catch (WebcamException | HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Error accessing camera: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage image = null;
            if (webcam.isOpen()) {
                if ((image = webcam.getImage()) == null) {
                    continue;
                }
            } else {
                break;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                Result result = new MultiFormatReader().decode(bitmap);
                if (result != null) {
                    employeeIdField.setText(result.toString());
                    timeInOut();

                    camPanel.removeAll();
                    camPanel.add(jLabel2, java.awt.BorderLayout.NORTH);
                    camPanel.add(jLabel1, java.awt.BorderLayout.CENTER);
                    camPanel.revalidate();
                    camPanel.repaint();

                    this.revalidate();
                    this.repaint();

                    try {
                        Thread.sleep(3500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    camPanel.removeAll();
                    camPanel.add(webcamPanel, java.awt.BorderLayout.CENTER);
                    camPanel.revalidate();
                    camPanel.repaint();

                    this.revalidate();
                    this.repaint();
                }
            } catch (NotFoundException e) {
                // QR not found in frame
            }
        } while (true);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "QRScannerThread");
        t.setDaemon(true);
        return t;
    }

    public void timeInOut() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String employeeId = employeeIdField.getText();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            String status = "";

            // Check if the employee_id is recorded
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM employees_table WHERE employee_id=?");
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String photoPath = rs.getString("photo_path");
                String name = rs.getString("full_name");
                nameLabel.setText(name);
                photoLabel.setIcon(new ImageIcon(new ImageIcon(photoPath).getImage().getScaledInstance(149, 149, Image.SCALE_SMOOTH)));
                photoLabel.setText("");

                // Check if already timed in today
                String checkQuery = "SELECT * FROM attendance_table WHERE employee_id=? AND date=?";
                pstmt = connection.prepareStatement(checkQuery);
                pstmt.setString(1, employeeId);
                pstmt.setDate(2, java.sql.Date.valueOf(today));
                rs = pstmt.executeQuery();

                if (!rs.next()) {
                    if (now.isBefore(LocalTime.NOON)) {
                        if (now.isAfter(LocalTime.of(8, 15))) {
                            status = "Late";
                        }

                        String insertQuery = "INSERT INTO attendance_table (employee_id, date, am_time_in, am_status, status) VALUES (?, ?, ?, ?, ?)";
                        pstmt = connection.prepareStatement(insertQuery);
                        pstmt.setString(1, employeeId);
                        pstmt.setDate(2, java.sql.Date.valueOf(today));
                        pstmt.setTime(3, java.sql.Time.valueOf(now));
                        pstmt.setString(4, status);
                        pstmt.setString(5, "Present");
                        pstmt.executeUpdate();

                        jLabel2.setText("AM Time In");
                        statusLabel.setText("STATUS: Time In, " + time + ", " + status);
                    } else {
                        if (now.isAfter(LocalTime.of(13, 15))) {
                            status = "Late";
                        }
                        String insertQuery = "INSERT INTO attendance_table (employee_id, date, pm_time_in, pm_status, status) VALUES (?, ?, ?, ?, ?)";
                        pstmt = connection.prepareStatement(insertQuery);
                        pstmt.setString(1, employeeId);
                        pstmt.setDate(2, java.sql.Date.valueOf(today));
                        pstmt.setTime(3, java.sql.Time.valueOf(now));
                        pstmt.setString(4, status);
                        pstmt.setString(5, "Present");
                        pstmt.executeUpdate();

                        jLabel2.setText("PM Time In");
                        statusLabel.setText("STATUS: Time In, " + time + ", " + status);
                    }
                } else {
                    Time amTimeIn = rs.getTime("am_time_in");
                    Time amTimeOut = rs.getTime("am_time_out");
                    if (amTimeOut == null && amTimeIn != null && now.isBefore(LocalTime.of(13, 00))) {
                        String updateQuery = "UPDATE attendance_table SET am_time_out=? WHERE employee_id=? AND date=?";
                        pstmt = connection.prepareStatement(updateQuery);
                        pstmt.setTime(1, java.sql.Time.valueOf(now));
                        pstmt.setString(2, employeeId);
                        pstmt.setDate(3, java.sql.Date.valueOf(today));
                        pstmt.executeUpdate();

                        jLabel2.setText("AM Time Out");
                        statusLabel.setText("STATUS: Time Out, " + time + ", " + status);

                        checkQuery = "SELECT * FROM attendance_table WHERE employee_id=? AND date=?";
                        pstmt = connection.prepareStatement(checkQuery);
                        pstmt.setString(1, employeeId);
                        pstmt.setDate(2, java.sql.Date.valueOf(today));
                        rs = pstmt.executeQuery();
                        if (rs.next()) {
                            amTimeIn = rs.getTime("am_time_in");
                            amTimeOut = rs.getTime("am_time_out");

                            // Compute am hours worked
                            LocalTime amtIn = amTimeIn.toLocalTime();
                            LocalTime amtOut = amTimeOut.toLocalTime();
                            double amHoursWorked = java.time.Duration.between(amtIn, amtOut).toMinutes() / 60.0;

                            // Compute total hours worked
                            double totalHoursWork = amHoursWorked;

                            updateQuery = "UPDATE attendance_table SET total_hours=? WHERE employee_id=? AND date=?";
                            pstmt = connection.prepareStatement(updateQuery);
                            pstmt.setDouble(1, totalHoursWork);
                            pstmt.setString(2, employeeId);
                            pstmt.setDate(3, java.sql.Date.valueOf(today));
                            pstmt.executeUpdate();
                        }
                    } else {
                        statusLabel.setText("STATUS: AM, Already Timed Out!");
                    }
                    if (now.isAfter(LocalTime.of(13, 00))) {
                        Time pmTimeIn = rs.getTime("pm_time_in");
                        if (pmTimeIn == null) {
                            if (now.isAfter(LocalTime.of(13, 15))) {
                                status = "Late";
                            }

                            String insertQuery = "UPDATE attendance_table SET pm_time_in=?, pm_status=? WHERE employee_id=? AND date=?";
                            pstmt = connection.prepareStatement(insertQuery);
                            pstmt.setTime(1, java.sql.Time.valueOf(now));
                            pstmt.setString(2, status);
                            pstmt.setString(3, employeeId);
                            pstmt.setDate(4, java.sql.Date.valueOf(today));
                            pstmt.executeUpdate();

                            jLabel2.setText("PM Time In");
                            statusLabel.setText("STATUS: Time In, " + time + ", " + status);
                        } else {
                            Time pmTimeOut = rs.getTime("pm_time_out");
                            if (pmTimeOut == null) {
                                String updateQuery = "UPDATE attendance_table SET pm_time_out=? WHERE employee_id=? AND date=?";
                                pstmt = connection.prepareStatement(updateQuery);
                                pstmt.setTime(1, java.sql.Time.valueOf(now));
                                pstmt.setString(2, employeeId);
                                pstmt.setDate(3, java.sql.Date.valueOf(today));
                                pstmt.executeUpdate();

                                jLabel2.setText("PM Time Out");
                                statusLabel.setText("STATUS: Time Out, " + time + ", " + status);

                                checkQuery = "SELECT * FROM attendance_table WHERE employee_id=? AND date=?";
                                pstmt = connection.prepareStatement(checkQuery);
                                pstmt.setString(1, employeeId);
                                pstmt.setDate(2, java.sql.Date.valueOf(today));
                                rs = pstmt.executeQuery();

                                if (rs.next()) {
                                    amTimeIn = rs.getTime("am_time_in");
                                    amTimeOut = rs.getTime("am_time_out");
                                    pmTimeIn = rs.getTime("pm_time_in");
                                    pmTimeOut = rs.getTime("pm_time_out");

                                    double amHoursWorked = 0;
                                    if (amTimeIn != null && amTimeOut != null) {
                                        // Compute am hours worked
                                        LocalTime amtIn = amTimeIn.toLocalTime();
                                        LocalTime amtOut = amTimeOut.toLocalTime();
                                        amHoursWorked = java.time.Duration.between(amtIn, amtOut).toMinutes() / 60.0;
                                    }

                                    double pmHoursWorked = 0;
                                    if (amTimeIn != null && amTimeOut != null) {
                                        // Compute pm hours worked
                                        LocalTime pmtIn = pmTimeIn.toLocalTime();
                                        LocalTime pmtOut = pmTimeOut.toLocalTime();
                                        pmHoursWorked = java.time.Duration.between(pmtIn, pmtOut).toMinutes() / 60.0;
                                    }
                                    
                                    // Compute total hours worked
                                    double totalHoursWork = amHoursWorked + pmHoursWorked;

                                    updateQuery = "UPDATE attendance_table SET total_hours=? WHERE employee_id=? AND date=?";
                                    pstmt = connection.prepareStatement(updateQuery);
                                    pstmt.setDouble(1, totalHoursWork);
                                    pstmt.setString(2, employeeId);
                                    pstmt.setDate(3, java.sql.Date.valueOf(today));
                                    pstmt.executeUpdate();
                                }
                            } else {
                                statusLabel.setText("STATUS: PM, Already Timed Out!");
                            }
                        }
                    }
                }
            } else {
                jLabel2.setText("Not Found!");
                statusLabel.setText("STATUS: No Record Found!");
            }

            javax.swing.Timer timer = new javax.swing.Timer(3500, e -> {
                employeeIdField.setText("");
                nameLabel.setText("NAME");
                photoLabel.setIcon(null);
                photoLabel.setText("PHOTO");
                statusLabel.setText("STATUS:");
            });
            timer.setRepeats(false);
            timer.start();

            dashboard.loadComboBox();
            dashboard.fetchAttendanceList();

        } catch (NumberFormatException | SQLException ex) {
            Logger.getLogger(TimeInOutFrame1.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        timeInOutHeaderLabel = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        bagPanel = new javax.swing.JPanel();
        dateTimePanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        westPanel = new javax.swing.JPanel();
        photoLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        employeeIdPanel = new javax.swing.JPanel();
        employeeIdLabel = new javax.swing.JLabel();
        employeeIdField = new javax.swing.JTextField();
        backButton = new javax.swing.JButton();
        timeInOutButton = new javax.swing.JButton();
        webCamPanel = new javax.swing.JPanel();
        camPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Time In/Out");
        setMinimumSize(new java.awt.Dimension(900, 600));
        setPreferredSize(new java.awt.Dimension(1000, 700));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        timeInOutHeaderLabel.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        timeInOutHeaderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeInOutHeaderLabel.setText("TIME IN/OUT");
        timeInOutHeaderLabel.setMaximumSize(new java.awt.Dimension(100, 100));
        timeInOutHeaderLabel.setMinimumSize(new java.awt.Dimension(100, 100));
        timeInOutHeaderLabel.setPreferredSize(new java.awt.Dimension(100, 100));
        getContentPane().add(timeInOutHeaderLabel, java.awt.BorderLayout.PAGE_START);

        centerPanel.setMinimumSize(new java.awt.Dimension(900, 600));
        centerPanel.setOpaque(false);
        centerPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        centerPanel.setLayout(new java.awt.GridBagLayout());

        bagPanel.setMinimumSize(new java.awt.Dimension(900, 500));
        bagPanel.setOpaque(false);
        bagPanel.setPreferredSize(new java.awt.Dimension(1200, 700));
        bagPanel.setLayout(new java.awt.GridBagLayout());

        dateTimePanel.setMinimumSize(new java.awt.Dimension(800, 80));
        dateTimePanel.setOpaque(false);
        dateTimePanel.setPreferredSize(new java.awt.Dimension(1000, 100));
        dateTimePanel.setLayout(new java.awt.GridBagLayout());

        dateLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        dateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateLabel.setText("DATE");
        dateTimePanel.add(dateLabel, new java.awt.GridBagConstraints());

        timeLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeLabel.setText("TIME");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        dateTimePanel.add(timeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        bagPanel.add(dateTimePanel, gridBagConstraints);

        westPanel.setMinimumSize(new java.awt.Dimension(400, 400));
        westPanel.setOpaque(false);
        westPanel.setPreferredSize(new java.awt.Dimension(600, 500));
        westPanel.setLayout(new java.awt.GridBagLayout());

        photoLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        photoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        photoLabel.setText("PHOTO");
        photoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        photoLabel.setMaximumSize(new java.awt.Dimension(150, 150));
        photoLabel.setMinimumSize(new java.awt.Dimension(150, 150));
        photoLabel.setPreferredSize(new java.awt.Dimension(150, 150));
        westPanel.add(photoLabel, new java.awt.GridBagConstraints());

        nameLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nameLabel.setText("NAME");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(nameLabel, gridBagConstraints);

        statusLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        statusLabel.setText("STATUS:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(statusLabel, gridBagConstraints);

        employeeIdPanel.setOpaque(false);
        employeeIdPanel.setLayout(new java.awt.GridBagLayout());

        employeeIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        employeeIdLabel.setText("Employee ID:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        employeeIdPanel.add(employeeIdLabel, gridBagConstraints);

        employeeIdField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        employeeIdField.setMinimumSize(new java.awt.Dimension(200, 35));
        employeeIdField.setPreferredSize(new java.awt.Dimension(250, 35));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        employeeIdPanel.add(employeeIdField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(employeeIdPanel, gridBagConstraints);

        backButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        backButton.setText("BACK");
        backButton.setPreferredSize(new java.awt.Dimension(100, 40));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(backButton, gridBagConstraints);

        timeInOutButton.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        timeInOutButton.setText("TIME IN/OUT");
        timeInOutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        timeInOutButton.setPreferredSize(new java.awt.Dimension(150, 40));
        timeInOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeInOutButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        westPanel.add(timeInOutButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        bagPanel.add(westPanel, gridBagConstraints);

        webCamPanel.setMinimumSize(new java.awt.Dimension(400, 400));
        webCamPanel.setOpaque(false);
        webCamPanel.setPreferredSize(new java.awt.Dimension(600, 500));
        webCamPanel.setLayout(new java.awt.GridBagLayout());

        camPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        camPanel.setMaximumSize(new java.awt.Dimension(600, 460));
        camPanel.setMinimumSize(new java.awt.Dimension(400, 300));
        camPanel.setOpaque(false);
        camPanel.setPreferredSize(new java.awt.Dimension(600, 460));
        camPanel.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Time In/Out");
        camPanel.add(jLabel2, java.awt.BorderLayout.PAGE_START);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Please wait...");
        camPanel.add(jLabel1, java.awt.BorderLayout.CENTER);

        webCamPanel.add(camPanel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        bagPanel.add(webCamPanel, gridBagConstraints);

        centerPanel.add(bagPanel, new java.awt.GridBagConstraints());

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to go Back?",
                "Confimation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            timer.stop();
            webcam.close();

            this.dispose();

        }
    }//GEN-LAST:event_backButtonActionPerformed

    private void timeInOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeInOutButtonActionPerformed
        timeInOut();
    }//GEN-LAST:event_timeInOutButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:

        timer.stop();
        webcam.close();

    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel bagPanel;
    private javax.swing.JPanel camPanel;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JPanel dateTimePanel;
    private javax.swing.JTextField employeeIdField;
    private javax.swing.JLabel employeeIdLabel;
    private javax.swing.JPanel employeeIdPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton timeInOutButton;
    private javax.swing.JLabel timeInOutHeaderLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel webCamPanel;
    private javax.swing.JPanel westPanel;
    // End of variables declaration//GEN-END:variables
}
