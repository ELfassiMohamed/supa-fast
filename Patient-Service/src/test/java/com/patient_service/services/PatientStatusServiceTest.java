package com.patient_service.services;

import com.patient_service.dto.PatientStatusUpdateMessage;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientStatusServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientStatusService patientStatusService;

    @Test
    void handleStatusUpdate_Success() {
        PatientStatusUpdateMessage message = new PatientStatusUpdateMessage();
        message.setPatientId("patient123");
        message.setNewStatus("ACTIVE");

        Patient patient = new Patient();
        patient.setId("patient123");
        patient.setAccountStatus(AccountStatus.PENDING);

        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));

        patientStatusService.handleStatusUpdate(message);

        verify(patientRepository).save(patient);
        assert(patient.getAccountStatus() == AccountStatus.ACTIVE);
    }

    @Test
    void handleStatusUpdate_PatientNotFound() {
        PatientStatusUpdateMessage message = new PatientStatusUpdateMessage();
        message.setPatientId("unknown");

        when(patientRepository.findById("unknown")).thenReturn(Optional.empty());

        patientStatusService.handleStatusUpdate(message);

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void handleStatusUpdate_InvalidStatus() {
        PatientStatusUpdateMessage message = new PatientStatusUpdateMessage();
        message.setPatientId("patient123");
        message.setNewStatus("INVALID");

        Patient patient = new Patient();
        patient.setId("patient123");

        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));

        patientStatusService.handleStatusUpdate(message);

        verify(patientRepository, never()).save(any(Patient.class));
    }
}
