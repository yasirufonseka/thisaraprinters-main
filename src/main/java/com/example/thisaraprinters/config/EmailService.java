package com.example.thisaraprinters.config;

import com.example.thisaraprinters.model.Supplier;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    @Autowired
    private JavaMailSenderImpl mailSender;

    public void sendEmailForPriceRequest (List<Supplier> suppliers, String category, String specification, String message, Integer priceRequestId)  {


        for (Supplier supplier : suppliers) {
            try {
                int userId = supplier.getId();
                String toEmail = supplier.getEmail();

                String subject = "Request price for " + category + " " + specification;
                String formUrl = "http://localhost:8080/supplier/pricerequest/form?priceRequestId=" + priceRequestId + "&supplierId=" + userId;

                // Create a design for email body from html
                String htmlBody = "<html>" +
                        "<body style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                        "   <div style='max-width: 500px; border: 1px solid #ddd; padding: 20px; border-radius: 8px;'>" +
                        "       <h2>Price Request for </h2>" + category +
                        "       <p>Fill out your Prices</p><br>" +
                        "       <a href='" + formUrl + "' style='background-color: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>" +
                        "          Got to Form page " +
                        "       </a>" +

                        "   </div>" +
                        "<div style='max-width: 500px; border: 1px solid #ddd; padding: 20px; border-radius: 8px;'>" +
                       "<h3>"+ message+"</h3>"+
                        " </div>"+
                        "</body>" +
                        "</html>";

                // part that send the email
                MimeMessage messages = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(messages, true, "UTF-8"); // UTF-8 දැම්මේ සිංහල අකුරු ප්‍රශ්නයක් නැතුව යන්න

                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(messages);
                System.out.println("Email sent successfully to: " + toEmail);

            } catch (Exception e) {
                //print the error
                System.err.println("Error sending email to supplier ID: " + supplier.getId() + " - " + e.getMessage());
            }
        }
    }
}

