package employee.management.system;

import java.awt.Image;
import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ViewEmployeeFrame extends javax.swing.JFrame {

    private int employeeId; // id of the selected employee

    // ===== Constructor =====
    public ViewEmployeeFrame(int id) {
        initComponents();
        this.employeeId = id;
        loadEmployeeData();
    }

// ===== Load Employee Data =====
    private void loadEmployeeData() {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/employee_management_database",
                    "root", ""
            );
            String sql = "SELECT * FROM employees_table WHERE employee_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, employeeId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtId.setText(rs.getString("employee_id"));
                txtName.setText(rs.getString("full_name"));

                // ===== Handle Birth Date =====
                String birthStr = rs.getString("birth_date");
                if (birthStr != null && !birthStr.isEmpty()) {
                    try {
                        java.util.Date birthdateObj = new SimpleDateFormat("yyyy-MM-dd").parse(birthStr);
                        birthdate.setText(new SimpleDateFormat("dd MMM yyyy").format(birthdateObj));
                    } catch (Exception ex) {
                        birthdate.setText(birthStr); // fallback kung di ma-parse
                    }
                } else {
                    birthDate.setText("N/A");
                }

                txtGender.setText(rs.getString("gender"));
                txtAddress.setText(rs.getString("address"));
                txtContact.setText(rs.getString("contact_number"));

                // ===== Handle Hired Date =====
                String hiredStr = rs.getString("hired_date");
                if (hiredStr != null && !hiredStr.isEmpty()) {
                    try {
                        java.util.Date hiredDateObj = new SimpleDateFormat("yyyy-MM-dd").parse(hiredStr);
                        hiredDate.setText(new SimpleDateFormat("dd MMM yyyy").format(hiredDateObj));
                    } catch (Exception ex) {
                        hiredDate.setText(hiredStr); // fallback
                    }
                } else {
                    hiredDate.setText("N/A");
                }

                txtSalary.setText(String.valueOf(rs.getInt("salary")));
                txtEmail.setText(rs.getString("email_address"));
                txtPosition.setText(rs.getString("position"));
                txtDepartment.setText(rs.getString("department"));

                // ===== Load photo if exists =====
                String photoPath = rs.getString("photo_path");
                if (photoPath != null && !photoPath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(photoPath);
                    imageHolder.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                            imageHolder.getWidth(),
                            imageHolder.getHeight(),
                            java.awt.Image.SCALE_SMOOTH
                    )));
                } else {
                    imageHolder.setIcon(null);
                }
                
                qrPhotoLabel.setIcon(new ImageIcon(new ImageIcon("qrcodes/" + employeeId + ".png").getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH)));
                qrPhotoLabel.setText("");
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addEmployeePanel = new javax.swing.JPanel();
        name = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        birthDate = new javax.swing.JLabel();
        birthdate = new javax.swing.JTextField();
        gender = new javax.swing.JLabel();
        txtGender = new javax.swing.JTextField();
        address = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        contactNo = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        hiredate = new javax.swing.JLabel();
        hiredDate = new javax.swing.JTextField();
        photo = new javax.swing.JLabel();
        imageHolder = new javax.swing.JLabel();
        salaryLabel = new javax.swing.JLabel();
        txtSalary = new javax.swing.JTextField();
        email = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        position = new javax.swing.JLabel();
        txtPosition = new javax.swing.JTextField();
        department = new javax.swing.JLabel();
        txtDepartment = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        qrPhotoLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        addEmployeePanel.setBackground(new java.awt.Color(221, 221, 221));
        addEmployeePanel.setForeground(new java.awt.Color(30, 30, 30));

        name.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        name.setForeground(new java.awt.Color(51, 51, 51));
        name.setText("NAME");

        txtName.setEditable(false);
        txtName.setBackground(new java.awt.Color(255, 255, 255));
        txtName.setForeground(new java.awt.Color(0, 0, 0));
        txtName.setCaretColor(new java.awt.Color(0, 0, 0));
        txtName.setMinimumSize(new java.awt.Dimension(200, 30));
        txtName.setPreferredSize(new java.awt.Dimension(250, 30));

        birthDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        birthDate.setForeground(new java.awt.Color(51, 51, 51));
        birthDate.setText("BIRTH DATE");

        birthdate.setEditable(false);
        birthdate.setBackground(new java.awt.Color(255, 255, 255));
        birthdate.setForeground(new java.awt.Color(0, 0, 0));
        birthdate.setMinimumSize(new java.awt.Dimension(200, 30));
        birthdate.setPreferredSize(new java.awt.Dimension(250, 30));

        gender.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        gender.setForeground(new java.awt.Color(51, 51, 51));
        gender.setText("GENDER");

        txtGender.setEditable(false);
        txtGender.setBackground(new java.awt.Color(255, 255, 255));
        txtGender.setForeground(new java.awt.Color(0, 0, 0));
        txtGender.setMinimumSize(new java.awt.Dimension(200, 30));
        txtGender.setPreferredSize(new java.awt.Dimension(250, 30));

        address.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        address.setForeground(new java.awt.Color(51, 51, 51));
        address.setText("ADDRESS");

        txtAddress.setEditable(false);
        txtAddress.setBackground(new java.awt.Color(255, 255, 255));
        txtAddress.setForeground(new java.awt.Color(0, 0, 0));
        txtAddress.setMinimumSize(new java.awt.Dimension(200, 30));
        txtAddress.setPreferredSize(new java.awt.Dimension(250, 30));

        contactNo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        contactNo.setForeground(new java.awt.Color(51, 51, 51));
        contactNo.setText("CONTACT NO");

        txtContact.setEditable(false);
        txtContact.setBackground(new java.awt.Color(255, 255, 255));
        txtContact.setForeground(new java.awt.Color(0, 0, 0));
        txtContact.setMinimumSize(new java.awt.Dimension(200, 30));
        txtContact.setPreferredSize(new java.awt.Dimension(250, 30));

        hiredate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        hiredate.setForeground(new java.awt.Color(51, 51, 51));
        hiredate.setText("HIRED DATE");

        hiredDate.setEditable(false);
        hiredDate.setBackground(new java.awt.Color(255, 255, 255));
        hiredDate.setForeground(new java.awt.Color(0, 0, 0));
        hiredDate.setMinimumSize(new java.awt.Dimension(200, 30));
        hiredDate.setPreferredSize(new java.awt.Dimension(250, 30));

        photo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        photo.setForeground(new java.awt.Color(51, 51, 51));
        photo.setText("PHOTO");

        imageHolder.setForeground(new java.awt.Color(51, 51, 51));
        imageHolder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageHolder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        imageHolder.setPreferredSize(new java.awt.Dimension(150, 150));

        salaryLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        salaryLabel.setForeground(new java.awt.Color(51, 51, 51));
        salaryLabel.setText("SALARY");

        txtSalary.setEditable(false);
        txtSalary.setBackground(new java.awt.Color(255, 255, 255));
        txtSalary.setForeground(new java.awt.Color(0, 0, 0));
        txtSalary.setMinimumSize(new java.awt.Dimension(200, 30));
        txtSalary.setPreferredSize(new java.awt.Dimension(250, 30));

        email.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        email.setForeground(new java.awt.Color(51, 51, 51));
        email.setText("EMAIL");

        txtEmail.setEditable(false);
        txtEmail.setBackground(new java.awt.Color(255, 255, 255));
        txtEmail.setForeground(new java.awt.Color(0, 0, 0));
        txtEmail.setMinimumSize(new java.awt.Dimension(200, 30));
        txtEmail.setPreferredSize(new java.awt.Dimension(250, 30));

        position.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        position.setForeground(new java.awt.Color(51, 51, 51));
        position.setText("POSITION");

        txtPosition.setEditable(false);
        txtPosition.setBackground(new java.awt.Color(255, 255, 255));
        txtPosition.setForeground(new java.awt.Color(0, 0, 0));
        txtPosition.setMinimumSize(new java.awt.Dimension(200, 30));
        txtPosition.setPreferredSize(new java.awt.Dimension(250, 30));

        department.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        department.setForeground(new java.awt.Color(51, 51, 51));
        department.setText("DEPARTMENT");

        txtDepartment.setEditable(false);
        txtDepartment.setBackground(new java.awt.Color(255, 255, 255));
        txtDepartment.setForeground(new java.awt.Color(0, 0, 0));
        txtDepartment.setMinimumSize(new java.awt.Dimension(200, 30));
        txtDepartment.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("PERSONAL INFORMATION");

        btnClose.setBackground(new java.awt.Color(102, 102, 102));
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setText("CLOSE");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        idLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        idLabel.setForeground(new java.awt.Color(51, 51, 51));
        idLabel.setText("EMPLOYEE ID");

        txtId.setEditable(false);
        txtId.setBackground(new java.awt.Color(255, 255, 255));
        txtId.setForeground(new java.awt.Color(0, 0, 0));
        txtId.setCaretColor(new java.awt.Color(0, 0, 0));
        txtId.setMinimumSize(new java.awt.Dimension(200, 30));
        txtId.setPreferredSize(new java.awt.Dimension(250, 30));

        qrPhotoLabel.setText("jLabel1");
        qrPhotoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 51, 51));
        jLabel3.setText("QR");

        javax.swing.GroupLayout addEmployeePanelLayout = new javax.swing.GroupLayout(addEmployeePanel);
        addEmployeePanel.setLayout(addEmployeePanelLayout);
        addEmployeePanelLayout.setHorizontalGroup(
            addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                .addContainerGap(43, Short.MAX_VALUE)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(name, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(birthDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gender, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(address, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hiredate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(idLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(7, 7, 7)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                                .addComponent(hiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(313, 313, 313)
                                .addComponent(btnClose))
                            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(birthdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtGender, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(35, 35, 35)
                                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(addEmployeePanelLayout.createSequentialGroup()
                                            .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(department, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(position, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addEmployeePanelLayout.createSequentialGroup()
                                            .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(email, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(salaryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                                        .addComponent(contactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(49, Short.MAX_VALUE))
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(qrPhotoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74))))
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        addEmployeePanelLayout.setVerticalGroup(
            addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addEmployeePanelLayout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(20, 20, 20)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(addEmployeePanelLayout.createSequentialGroup()
                            .addGap(67, 67, 67)
                            .addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(imageHolder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(qrPhotoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addGap(21, 21, 21)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idLabel)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactNo)
                    .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(name)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(birthdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(birthDate))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gender)
                            .addComponent(txtGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(address)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(addEmployeePanelLayout.createSequentialGroup()
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(salaryLabel)
                            .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(email)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(position)
                            .addComponent(txtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(department)
                            .addComponent(txtDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(25, 25, 25)
                .addGroup(addEmployeePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hiredate)
                    .addComponent(hiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose))
                .addContainerGap(61, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 831, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(addEmployeePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 629, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(addEmployeePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:

        this.dispose(); // totally close the window

    }//GEN-LAST:event_btnCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addEmployeePanel;
    private javax.swing.JLabel address;
    private javax.swing.JLabel birthDate;
    private javax.swing.JTextField birthdate;
    private javax.swing.JButton btnClose;
    private javax.swing.JLabel contactNo;
    private javax.swing.JLabel department;
    private javax.swing.JLabel email;
    private javax.swing.JLabel gender;
    private javax.swing.JTextField hiredDate;
    private javax.swing.JLabel hiredate;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel imageHolder;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel name;
    private javax.swing.JLabel photo;
    private javax.swing.JLabel position;
    private javax.swing.JLabel qrPhotoLabel;
    private javax.swing.JLabel salaryLabel;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtDepartment;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtGender;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPosition;
    private javax.swing.JTextField txtSalary;
    // End of variables declaration//GEN-END:variables
}
