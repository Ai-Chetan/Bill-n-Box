package com.example.billnbox;

import java.security.SecureRandom;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
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

    @FXML
    private RadioButton radiobtn1, radiobtn2;

    private static String UserName;
    private static boolean isOwner = false;

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

    public static void updatePassword(String userName, boolean isOwner) throws SQLException {
        String table = isOwner ? "Owner" : "Employee"; // Select the table based on isOwner flag
        String emailID = null;

        // Query to fetch the EmailID from the respective table
        String selectEmailQuery = "SELECT EmailID FROM " + table + " WHERE UserName = ?";

        // Update the password query
        String updateQuery = "UPDATE " + table + " SET password = ? WHERE UserName = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            // Step 1: Fetch the EmailID
            try (PreparedStatement selectStatement = connection.prepareStatement(selectEmailQuery)) {
                selectStatement.setString(1, userName);
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    emailID = resultSet.getString("EmailID");  // Get the EmailID from the result
                } else {
                    System.out.println("User not found!");
                    return;
                }
            }

            // Step 2: Generate new password and update it in the database
            String newPassword = PasswordGenerator.generatePassword();
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newPassword);
                updateStatement.setString(2, userName);
                updateStatement.executeUpdate();
            }

            // Step 3: Send the generated password to the fetched EmailID
            if (emailID != null) {
                EmailSender.sendEmail(emailID, newPassword);  // Email the new password to the user
                System.out.println("Password updated and sent to email: " + emailID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public class EmailSender {

        public static void sendEmail(String recipientEmail, String newPassword) {
            String senderEmail = "billnboxibms@gmail.com"; // your email address
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
                message.setText("Your new password is: " + newPassword);

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
        if(!radiobtn1.isSelected()&&!radiobtn2.isSelected()) {
            submitButton.setDisable(true);
        } else if (radiobtn1.isSelected()) {
            isOwner = true;
        } else if (radiobtn2.isSelected()) {
            isOwner = false;
        }
        UserName = Username.getText();
        navigateToPage(event, "7-forgot-password-2.fxml");
        updatePassword(UserName, isOwner);
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