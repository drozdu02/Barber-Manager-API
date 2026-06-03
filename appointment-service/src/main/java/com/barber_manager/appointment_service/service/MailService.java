package com.barber_manager.appointment_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendConfirmationEmail(
            String to,
            String customerName,
            String reservationCode,
            String reservationDate
    ) throws MessagingException {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("reservationCode", reservationCode);
        context.setVariable("reservationDate", reservationDate);
        context.setVariable("customerEmail", to);

        sendHtmlEmail(to, "Potwierdzenie rezerwacji — Barber Manager", "reservation-confirmation", context);
    }

    public void sendReminderEmail(
            String to,
            String customerName,
            String serviceName,
            String reservationCode,
            String reservationDate
    ) throws MessagingException {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("serviceName", serviceName);
        context.setVariable("reservationCode", reservationCode);
        context.setVariable("reservationDate", reservationDate);
        context.setVariable("customerEmail", to);

        sendHtmlEmail(to, "Przypomnienie o wizycie — Barber Manager", "reservation-reminder", context);
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Context context)
            throws MessagingException {
        String htmlBody = templateEngine.process(templateName, context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessage = new MimeMessageHelper(message, "UTF-8");

        mimeMessage.setTo(to);
        mimeMessage.setSubject(subject);
        mimeMessage.setText(htmlBody, true);
        mailSender.send(message);
    }
}
