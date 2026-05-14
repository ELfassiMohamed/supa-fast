package com.patient_service.services;

import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.models.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private PatientService patientService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private Patient patient;
    private PatientRequestResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setEmail("patient@example.com");
        patient.setFirstName("John");

        responseDTO = new PatientRequestResponseDTO();
        responseDTO.setRequestId("req123");
        responseDTO.setSubject("Test Request");
        responseDTO.setStatus("COMPLETED");
        responseDTO.setMessage("Response message");
    }

    @Test
    void notifyPatient_Success() {
        when(patientService.findById("patient123")).thenReturn(patient);
        when(emailService.isEmailConfigured()).thenReturn(true);

        notificationService.notifyPatient(responseDTO, "patient123");

        verify(emailService).sendEmail(eq("patient@example.com"), anyString(), anyString());
    }

    @Test
    void notifyPatient_EmailNotConfigured() {
        when(patientService.findById("patient123")).thenReturn(patient);
        when(emailService.isEmailConfigured()).thenReturn(false);

        notificationService.notifyPatient(responseDTO, "patient123");

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void notifyPatient_NullInputs() {
        notificationService.notifyPatient(null, null);

        verify(patientService, never()).findById(anyString());
    }

    @Test
    void notifyPatient_ExceptionHandling() {
        when(patientService.findById("patient123")).thenThrow(new RuntimeException("DB error"));

        // Should not throw exception
        notificationService.notifyPatient(responseDTO, "patient123");
        
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
