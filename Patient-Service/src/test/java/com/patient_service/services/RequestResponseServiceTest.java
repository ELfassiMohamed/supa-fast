package com.patient_service.services;

import com.patient_service.dto.PatientRequestResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RequestResponseServiceTest {

    private RequestResponseService requestResponseService;
    private PatientRequestResponseDTO response;

    @BeforeEach
    void setUp() {
        requestResponseService = new RequestResponseService();
        response = new PatientRequestResponseDTO();
        response.setRequestId("req123");
        response.setPatientId("patient123");
        response.setStatus("COMPLETED");
        response.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void saveAndGetResponse_Success() {
        requestResponseService.saveResponse(response);

        assertTrue(requestResponseService.hasResponse("req123"));
        assertEquals(response, requestResponseService.getResponse("req123"));
    }

    @Test
    void saveResponse_Null() {
        requestResponseService.saveResponse(null);
        assertEquals(0, requestResponseService.getAllResponses().size());
    }

    @Test
    void removeResponse_Success() {
        requestResponseService.saveResponse(response);
        requestResponseService.removeResponse("req123");

        assertFalse(requestResponseService.hasResponse("req123"));
    }

    @Test
    void getPatientNotifications_Success() {
        requestResponseService.saveResponse(response);
        
        PatientRequestResponseDTO response2 = new PatientRequestResponseDTO();
        response2.setRequestId("req456");
        response2.setPatientId("patient123");
        response2.setUpdatedAt(LocalDateTime.now().plusMinutes(1));
        requestResponseService.saveResponse(response2);

        List<PatientRequestResponseDTO> notifications = requestResponseService.getPatientNotifications("patient123");

        assertEquals(2, notifications.size());
        assertEquals("req456", notifications.get(0).getRequestId()); // More recent first
    }

    @Test
    void getAllResponses_Success() {
        requestResponseService.saveResponse(response);
        Map<String, PatientRequestResponseDTO> responses = requestResponseService.getAllResponses();
        assertEquals(1, responses.size());
    }
}
