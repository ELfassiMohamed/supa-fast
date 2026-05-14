package com.patient_service.services;

import com.patient_service.models.Patient;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey = "mySecretKey123456789012345678901234567890";
    private long expiration = 3600000; // 1h

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
    }

    @Test
    void generateAndExtractToken_Success() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com");
        patient.setId("patient123");

        String token = jwtService.generateToken(patient);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertEquals("ROLE_PATIENT", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_Success() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com");

        String token = jwtService.generateToken(patient);

        assertTrue(jwtService.isTokenValid(token, patient));
    }

    @Test
    void isTokenValid_InvalidUsername() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com");

        String token = jwtService.generateToken(patient);

        Patient otherPatient = new Patient();
        otherPatient.setEmail("other@example.com");

        assertFalse(jwtService.isTokenValid(token, otherPatient));
    }

    @Test
    void extractClaim_Success() {
        Patient patient = new Patient();
        patient.setEmail("test@example.com");

        String token = jwtService.generateToken(patient);

        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }
}
