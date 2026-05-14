package com.patient_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private String fromEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
    }

    @Test
    void sendEmail_Success() {
        emailService.sendEmail("to@example.com", "Subject", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_Failure() {
        doThrow(new org.springframework.mail.MailSendException("Error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.sendEmail("to@example.com", "Subject", "Body"));
    }

    @Test
    void isEmailConfigured_Success() {
        assertTrue(emailService.isEmailConfigured());
    }
}
