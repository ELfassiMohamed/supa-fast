package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PatientMapperTest {

    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        patientMapper = new PatientMapper();
    }

    @Test
    void toDto_Success() {
        Patient patient = new Patient();
        patient.setId("patient123");
        patient.setEmail("test@example.com");
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setAccountStatus(AccountStatus.ACTIVE);
        patient.setPhone("0600000000");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Male");
        patient.setAddress("123 Street");
        patient.setCity("Casablanca");
        patient.setState("State");
        patient.setZipCode("20000");
        patient.setCountry("Morocco");

        PatientDTO dto = patientMapper.toDto(patient);

        assertNotNull(dto);
        assertEquals(patient.getId(), dto.getId());
        assertEquals(patient.getEmail(), dto.getEmail());
        assertEquals(patient.getFirstName(), dto.getFirstName());
        assertEquals(patient.getLastName(), dto.getLastName());
        assertEquals(patient.getAccountStatus(), dto.getAccountStatus());
        assertEquals(patient.getPhone(), dto.getPhone());
    }

    @Test
    void toDto_Null() {
        assertNull(patientMapper.toDto(null));
    }

    @Test
    void toEntity_Success() {
        PatientDTO dto = new PatientDTO();
        dto.setId("patient123");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setAccountStatus(AccountStatus.ACTIVE);

        Patient entity = patientMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getFirstName(), entity.getFirstName());
        assertEquals(dto.getLastName(), entity.getLastName());
    }

    @Test
    void toEntity_Null() {
        assertNull(patientMapper.toEntity(null));
    }
}
