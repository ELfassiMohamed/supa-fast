package com.patient_service.services;

import com.patient_service.dto.MedicalHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MedicalRecordClientService medicalRecordClientService;

    private String serviceUrl = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(medicalRecordClientService, "medicalRecordServiceUrl", serviceUrl);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPatientMedicalRecords_Success() {
        List<Map<String, Object>> mockResponse = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("recordId", "rec123");
        record.put("patientId", "patient123");
        record.put("recordType", "CONSULTATION");
        mockResponse.add(record);

        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<MedicalHistoryResponse> result = medicalRecordClientService.getPatientMedicalRecords("patient123", "jwtToken");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("rec123", result.get(0).getRecordId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPatientMedicalRecords_Empty() {
        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<MedicalHistoryResponse> result = medicalRecordClientService.getPatientMedicalRecords("patient123", "jwtToken");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPatientMedicalRecords_Exception() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("API error"));

        List<MedicalHistoryResponse> result = medicalRecordClientService.getPatientMedicalRecords("patient123", "jwtToken");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
