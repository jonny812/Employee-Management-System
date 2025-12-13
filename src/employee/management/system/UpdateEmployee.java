package employee.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import javax.swing.JOptionPane;

public class UpdateEmployee extends javax.swing.JPanel {

    private int employeeId; // Store employee ID for updating later

    DashboardFrame dashboard;
    
    File selectedFile;
    
    String selectedImagePath;

    public UpdateEmployee(int employeeId, DashboardFrame dashboard) {
        initComponents();
        this.employeeId = employeeId;
        this.dashboard = dashboard;
        loadEmployeeData(employeeId);

    }

    private void loadEmployeeData(int id) {
        Connection conn;
        PreparedStatement pst;
        ResultSet rs;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee_management_database", "root", "");
            String sql = "SELECT * FROM employees_table WHERE employee_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            rs = pst.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("full_name"));
                genderBox.setSelectedItem(rs.getString("gender"));
                txtaddress.setText(rs.getString("address"));
                txtcontact.setText(rs.getString("contact_number"));
                txtSalary.setText(rs.getString("salary"));
                txtEmail.setText(rs.getString("email_address"));
                boxPosition.setSelectedItem(rs.getString("position"));
                boxDepartment.setSelectedItem(rs.getString("department"));

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
                    date.setDate(birth);
                }
                if (hired != null) {
                    hiredData.setDate(hired);
                }

                // ✅ LOAD PHOTO if exists
                String imgPath = rs.getString("photo_path");
                if (imgPath != null && !imgPath.isEmpty()) {
                    ImageIcon imageIcon = new ImageIcon(
                            new ImageIcon(imgPath).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH)
                    );
                    photo.setIcon(imageIcon);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load employee data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Title = new javax.swing.JLabel();
        name = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        birthDate = new javax.swing.JLabel();
        date = new com.toedter.calendar.JDateChooser();
        gender = new javax.swing.JLabel();
        genderBox = new javax.swing.JComboBox<>();
        address = new javax.swing.JLabel();
        txtaddress = new javax.swing.JTextField();
        contactNo = new javax.swing.JLabel();
        txtcontact = new javax.swing.JTextField();
        hiredDate = new javax.swing.JLabel();
        hiredData = new com.toedter.calendar.JDateChooser();
        photoLabel = new javax.swing.JLabel();
        btnUploadPhoto = new javax.swing.JButton();
        salary = new javax.swing.JLabel();
        txtSalary = new javax.swing.JTextField();
        email = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        position = new javax.swing.JLabel();
        boxPosition = new javax.swing.JComboBox<>();
        department = new javax.swing.JLabel();
        boxDepartment = new javax.swing.JComboBox<>();
        photo = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(221, 221, 221));
        jPanel1.setPreferredSize(new java.awt.Dimension(1198, 799));

        Title.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        Title.setForeground(new java.awt.Color(51, 51, 51));
        Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Title.setText("PERSONAL INFORMATION");

        name.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        name.setForeground(new java.awt.Color(51, 51, 51));
        name.setText("NAME");

        txtName.setBackground(new java.awt.Color(255, 255, 255));
        txtName.setPreferredSize(new java.awt.Dimension(250, 30));

        birthDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        birthDate.setForeground(new java.awt.Color(51, 51, 51));
        birthDate.setText("BIRTHDATE");

        date.setBackground(new java.awt.Color(255, 255, 255));
        date.setForeground(new java.awt.Color(255, 255, 255));
        date.setPreferredSize(new java.awt.Dimension(250, 30));

        gender.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        gender.setForeground(new java.awt.Color(51, 51, 51));
        gender.setText("GENDER");

        genderBox.setBackground(new java.awt.Color(0, 153, 153));
        genderBox.setEditable(true);
        genderBox.setForeground(new java.awt.Color(255, 255, 255));
        genderBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NONE", "Male", "Female" }));
        genderBox.setPreferredSize(new java.awt.Dimension(250, 30));
        genderBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genderBoxActionPerformed(evt);
            }
        });

        address.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        address.setForeground(new java.awt.Color(51, 51, 51));
        address.setText("ADDRESS");

        txtaddress.setBackground(new java.awt.Color(255, 255, 255));
        txtaddress.setForeground(new java.awt.Color(51, 51, 51));
        txtaddress.setPreferredSize(new java.awt.Dimension(250, 30));

        contactNo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        contactNo.setForeground(new java.awt.Color(51, 51, 51));
        contactNo.setText("CONTACT NO");

        txtcontact.setBackground(new java.awt.Color(255, 255, 255));
        txtcontact.setForeground(new java.awt.Color(51, 51, 51));
        txtcontact.setPreferredSize(new java.awt.Dimension(250, 30));

        hiredDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        hiredDate.setForeground(new java.awt.Color(51, 51, 51));
        hiredDate.setText("HIRED DATE");

        hiredData.setBackground(new java.awt.Color(255, 255, 255));
        hiredData.setForeground(new java.awt.Color(51, 51, 51));
        hiredData.setPreferredSize(new java.awt.Dimension(250, 30));

        photoLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        photoLabel.setForeground(new java.awt.Color(51, 51, 51));
        photoLabel.setText("PHOTO");

        btnUploadPhoto.setBackground(new java.awt.Color(0, 153, 153));
        btnUploadPhoto.setText("UPLOAD PHOTO");
        btnUploadPhoto.setBorderPainted(false);
        btnUploadPhoto.setMaximumSize(new java.awt.Dimension(95, 20));
        btnUploadPhoto.setMinimumSize(new java.awt.Dimension(95, 20));
        btnUploadPhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadPhotoActionPerformed(evt);
            }
        });

        salary.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        salary.setForeground(new java.awt.Color(51, 51, 51));
        salary.setText("SALARY");

        txtSalary.setBackground(new java.awt.Color(255, 255, 255));
        txtSalary.setPreferredSize(new java.awt.Dimension(250, 30));
        txtSalary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSalaryActionPerformed(evt);
            }
        });

        email.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        email.setForeground(new java.awt.Color(51, 51, 51));
        email.setText("EMAIL");

        txtEmail.setBackground(new java.awt.Color(255, 255, 255));
        txtEmail.setPreferredSize(new java.awt.Dimension(250, 30));

        position.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        position.setForeground(new java.awt.Color(51, 51, 51));
        position.setText("POSITION");

        boxPosition.setBackground(new java.awt.Color(0, 153, 153));
        boxPosition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chief Executive Officer (CEO)  ", "Chief Operating Officer (COO)  ", "Chief Financial Officer (CFO)  ", "Chief Technology Officer (CTO)  ", "Vice President of Marketing  ", "Office Manager  ", "Operations Coordinator  ", "Administrative Assistant  ", "Project Manager  ", "Facilities Supervisor  ", "Accountant  ", "Financial Analyst  ", "Payroll Specialist  ", "Internal Auditor  ", "Accounts Payable Clerk  ", "HR Manager  ", "Recruiter / Talent Acquisition Specialist  ", "Training & Development Coordinator  ", "Compensation & Benefits Analyst  ", "Employee Relations Specialist  ", "Marketing Manager  ", "Social Media Specialist  ", "Sales Representative  ", "Business Development Manager  ", "Customer Success Manager  ", "Software Engineer  ", "Data Analyst  ", "IT Support Specialist  ", "Cybersecurity Analyst  ", "Systems Administrator  ", "Product Manager  ", "UX/UI Designer  ", "Graphic Designer  ", "Quality Assurance Tester  ", "Research & Development Specialist  ", "Call Center Agent  ", "Technical Support Representative  ", "Client Relations Coordinator  ", "Customer Experience Specialist  " }));
        boxPosition.setPreferredSize(new java.awt.Dimension(250, 30));

        department.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        department.setForeground(new java.awt.Color(51, 51, 51));
        department.setText("DEPARTMENT");

        boxDepartment.setBackground(new java.awt.Color(0, 153, 153));
        boxDepartment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Executive / Leadership  ", "Administration & Operations  ", "Finance & Accounting  ", "Human Resources  ", "Marketing & Sales  ", "Information Technology (IT)  ", "Product Development & Design  ", "Customer Service  ", "Research & Development (R&D)  ", "Legal & Compliance  ", "Procurement & Supply Chain  ", "Manufacturing / Production  ", "Quality Assurance  ", "Public Relations & Communications  " }));
        boxDepartment.setPreferredSize(new java.awt.Dimension(250, 30));

        photo.setForeground(new java.awt.Color(51, 51, 51));
        photo.setMaximumSize(new java.awt.Dimension(150, 150));
        photo.setMinimumSize(new java.awt.Dimension(150, 150));
        photo.setPreferredSize(new java.awt.Dimension(150, 150));

        btnSave.setBackground(new java.awt.Color(13, 148, 136));
        btnSave.setText("UPDATE");
        btnSave.setPreferredSize(new java.awt.Dimension(100, 30));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(239, 68, 68));
        btnCancel.setText("CANCEL");
        btnCancel.setPreferredSize(new java.awt.Dimension(100, 30));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(birthDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtaddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(genderBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(contactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtcontact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(department, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(email, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(position, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(salary, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(photoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(boxPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boxDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUploadPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(hiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hiredData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(266, 266, 266)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(photoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(photo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUploadPhoto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(birthDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(genderBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtaddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtcontact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(hiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hiredData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(salary, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(position, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boxPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(department, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boxDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(73, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnUploadPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadPhotoActionPerformed
        // TODO add your handling code here:
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Select Employee Photo");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(this);

        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();

            // show preview
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(selectedImagePath).getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH)
            );
            photo.setIcon(icon);
        }
    }//GEN-LAST:event_btnUploadPhotoActionPerformed

    private void genderBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genderBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_genderBoxActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee_management_database", "root", "")) {

            String name = txtName.getText().trim();
            String gender = genderBox.getSelectedItem().toString();
            String address = txtaddress.getText().trim();
            String contact = txtcontact.getText().trim();
            String salary = txtSalary.getText().trim();
            String email = txtEmail.getText().trim();
            String position = boxPosition.getSelectedItem().toString();
            String department = boxDepartment.getSelectedItem().toString();
            String photoPath = selectedImagePath;

            java.util.Date birthUtilDate = date.getDate();
            java.util.Date hiredUtilDate = hiredData.getDate();

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
            PreparedStatement pst = conn.prepareStatement(sql);
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
            pst.setInt(12, employeeId);

            
            int updated = pst.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "✅ Employee updated successfully!");

                // ✅ Refresh the table inside DashboardFrame safely
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (window instanceof DashboardFrame) {
                    ((DashboardFrame) window).fetch();
                }

                // ✅ Then return to the ViewEmployeeList panel
                java.awt.Container parent = this.getParent();
                if (parent.getLayout() instanceof java.awt.CardLayout) {
                    java.awt.CardLayout card = (java.awt.CardLayout) parent.getLayout();
                    card.show(parent, "viewEmployeeList");
                }

                dashboard.btnUpdate.setEnabled(false);
                dashboard.btnRemove.setEnabled(false);
                dashboard.generatePayslipButton.setEnabled(false);
                dashboard.unselectButton.setEnabled(false);
                dashboard.employeesTable.clearSelection();

                dashboard.card.show(dashboard.jPanel3, "card4");
                dashboard = null;

            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Failed to update employee!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error updating employee: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        dashboard.card.show(dashboard.jPanel3, "card4");
        dashboard = null;

        // ✅ Refresh the table inside DashboardFrame safely
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardFrame) {
            ((DashboardFrame) window).fetch();
        }

        // ✅ Then return to the ViewEmployeeList panel
        java.awt.Container parent = this.getParent();
        if (parent.getLayout() instanceof java.awt.CardLayout) {
            java.awt.CardLayout card = (java.awt.CardLayout) parent.getLayout();
            card.show(parent, "viewEmployeeList");
        }

    }//GEN-LAST:event_btnCancelActionPerformed

    private void txtSalaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSalaryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSalaryActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Title;
    private javax.swing.JLabel address;
    private javax.swing.JLabel birthDate;
    private javax.swing.JComboBox<String> boxDepartment;
    private javax.swing.JComboBox<String> boxPosition;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUploadPhoto;
    private javax.swing.JLabel contactNo;
    private com.toedter.calendar.JDateChooser date;
    private javax.swing.JLabel department;
    private javax.swing.JLabel email;
    private javax.swing.JLabel gender;
    private javax.swing.JComboBox<String> genderBox;
    private com.toedter.calendar.JDateChooser hiredData;
    private javax.swing.JLabel hiredDate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel name;
    private javax.swing.JLabel photo;
    private javax.swing.JLabel photoLabel;
    private javax.swing.JLabel position;
    private javax.swing.JLabel salary;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSalary;
    private javax.swing.JTextField txtaddress;
    private javax.swing.JTextField txtcontact;
    // End of variables declaration//GEN-END:variables
}
