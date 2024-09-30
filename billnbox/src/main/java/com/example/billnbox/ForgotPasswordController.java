package com.example.billnbox;

import java.security.SecureRandom;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Transport;
import java.io.IOException;
import java.sql.*;

public class ForgotPasswordController {

    @FXML
    private TextField Username;

    @FXML
    private Button submitButton;

    private static String UserName;

    public class PasswordGenerator {
        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final int PASSWORD_LENGTH = 8;

        public static String generatePassword() {
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                password.append(CHARACTERS.charAt(index));
            }
            return password.toString();
        }
    }

    public static void updatePassword(String userName) throws SQLException {
        String table = null;
        String emailID = null;

        // Queries to fetch EmailID and update password in both tables
        String selectEmailQueryOwner = "SELECT EmailID FROM Owner WHERE UserName = ?";
        String selectEmailQueryEmployee = "SELECT EmailID FROM Employee WHERE UserName = ?";
        String updatePasswordQueryOwner = "UPDATE Owner SET password = ? WHERE UserName = ?";
        String updatePasswordQueryEmployee = "UPDATE Employee SET password = ? WHERE UserName = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            // Step 1: Try fetching EmailID from Owner table
            try (PreparedStatement selectStatementOwner = connection.prepareStatement(selectEmailQueryOwner)) {
                selectStatementOwner.setString(1, userName);
                ResultSet resultSet = selectStatementOwner.executeQuery();

                if (resultSet.next()) {
                    emailID = resultSet.getString("EmailID");
                    table = "Owner";
                }
            }

            // Step 2: If not found in Owner, try Employee table
            if (emailID == null) {
                try (PreparedStatement selectStatementEmployee = connection.prepareStatement(selectEmailQueryEmployee)) {
                    selectStatementEmployee.setString(1, userName);
                    ResultSet resultSet = selectStatementEmployee.executeQuery();

                    if (resultSet.next()) {
                        emailID = resultSet.getString("EmailID");
                        table = "Employee";
                    }
                }
            }

            // Step 3: If user not found in either table
            if (emailID == null) {
                System.out.println("User not found in both Owner and Employee tables!");
                return;
            }

            // Step 4: Generate new password and update it in the corresponding table
            String newPassword = PasswordGenerator.generatePassword();
            String updateQuery = table.equals("Owner") ? updatePasswordQueryOwner : updatePasswordQueryEmployee;

            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newPassword);
                updateStatement.setString(2, userName);
                updateStatement.executeUpdate();
            }

            // Step 5: Send the generated password to the fetched EmailID
            EmailSender.sendEmail(emailID, newPassword);
            System.out.println("Password updated and sent to email: " + emailID);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class EmailSender {

        public static void sendEmail(String recipientEmail, String newPassword) {
            String senderEmail = ""; // your email address
            String senderPassword = ""; // your email password

            // SMTP server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");

            // Create a session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            try {
                // Create a message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Password Reset");

                // Build the email body with UserName, newPassword, and company name
                String emailBody = "Dear " + UserName + ",\n\n" +
                        "We have received a request to reset your password. Your new password is: " + newPassword + "\n\n" +
                        "Please use this password to log in and update your password as soon as possible. " +
                        "For your security, we recommend that you change this temporary password to a new one immediately after logging in.\n\n" +
                        "If you did not request this password reset, please ignore this message.\n\n" +
                        "Best regards,\n" +
                        "The Bill-N-Box Support Team";

                // Set the email body content
                message.setText(emailBody);

                // Send the message
                Transport.send(message);
                System.out.println("Email sent successfully!");

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        navigateToPage(event, "1-login-page.fxml");
    }

    @FXML
    private void forgotPasswordSubmit(ActionEvent event) throws SQLException {
        UserName = Username.getText();

        // Navigate to the next page before calling updatePassword
        navigateToPage(event, "7-forgot-password-2.fxml");

        // Call updatePassword after navigation
        updatePassword(UserName);
    }

    // Simple navigation without data passing
    private void navigateToPage(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
