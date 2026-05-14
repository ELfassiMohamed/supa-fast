package com.patient_service.listeners;

import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.services.NotificationService;
import com.patient_service.services.RequestResponseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestResponseListenerTest {

    @Mock
    private RequestResponseService requestResponseService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RequestResponseListener requestResponseListener;

    @Test
    void handleRequestResponse_Success() {
        Map<String, Object> message = new HashMap<>();
        message.put("requestId", "req123");
        message.put("patientId", "patient123");
        message.put("status", "COMPLETED");
        message.put("message", "Processed");

        requestResponseListener.handleRequestResponse(message);

        verify(requestResponseService).saveResponse(any(PatientRequestResponseDTO.class));
        verify(notificationService).notifyPatient(any(PatientRequestResponseDTO.class), eq("patient123"));
    }

    @Test
    void handleRequestResponse_InvalidMessage() {
        Map<String, Object> message = new HashMap<>();
        // missing requestId

        requestResponseListener.handleRequestResponse(message);

        verify(requestResponseService, never()).saveResponse(any());
        verify(notificationService, never()).notifyPatient(any(), anyString());
    }

    @Test
    void handleRequestResponse_MissingPatientId() {
        Map<String, Object> message = new HashMap<>();
        message.put("requestId", "req123");
        // missing patientId

        requestResponseListener.handleRequestResponse(message);

        verify(requestResponseService).saveResponse(any(PatientRequestResponseDTO.class));
        verify(notificationService, never()).notifyPatient(any(), anyString());
    }
}
