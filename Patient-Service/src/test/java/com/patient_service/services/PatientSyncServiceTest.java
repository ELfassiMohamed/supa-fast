package com.patient_service.services;

import com.patient_service.dto.PatientSyncRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientSyncServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PatientSyncService patientSyncService;

    @Test
    void handleSyncRequest_All() {
        PatientSyncRequest request = new PatientSyncRequest();
        request.setStatus("ALL");
        request.setProviderId("provider123");

        Patient patient = new Patient();
        patient.setEmail("test@example.com");

        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        patientSyncService.handleSyncRequest(request);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(List.class));
    }

    @Test
    void handleSyncRequest_SpecificStatus() {
        PatientSyncRequest request = new PatientSyncRequest();
        request.setStatus("ACTIVE");

        Patient patient = new Patient();
        patient.setAccountStatus(AccountStatus.ACTIVE);

        when(patientRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(Collections.singletonList(patient));

        patientSyncService.handleSyncRequest(request);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(List.class));
    }

    @Test
    void handleSyncRequest_InvalidStatus() {
        PatientSyncRequest request = new PatientSyncRequest();
        request.setStatus("INVALID");

        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        patientSyncService.handleSyncRequest(request);

        verify(patientRepository).findAll();
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(List.class));
    }
}
