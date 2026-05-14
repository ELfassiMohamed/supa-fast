package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PatientPublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PatientPublisherService patientPublisherService;

    @Test
    void publishPatient_Success() {
        PatientDTO dto = new PatientDTO();
        dto.setEmail("test@example.com");

        patientPublisherService.publishPatient(dto);

        verify(rabbitTemplate).convertAndSend(eq("patient-exchange"), eq("patient.sync.request"), eq(dto));
    }
}
