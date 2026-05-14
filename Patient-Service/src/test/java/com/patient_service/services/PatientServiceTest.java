package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import com.patient_service.dto.ProfileCompletionRequest;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PatientPublisherService patientPublisherService;

    @Mock
    private PatientMapper mapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private String email = "test@example.com";
    private String password = "password123";

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient123");
        patient.setEmail(email);
        patient.setPassword("encodedPassword");
        patient.setAccountStatus(AccountStatus.PENDING);
    }

    @Test
    void loadUserByUsername_Success() {
        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(patient));

        UserDetails userDetails = patientService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> patientService.loadUserByUsername(email));
    }

    @Test
    void registerPatient_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName("John");
        request.setLastName("Doe");

        when(patientRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(mapper.toDto(any(Patient.class))).thenReturn(new PatientDTO());

        Patient registered = patientService.registerPatient(email, password, request);

        assertNotNull(registered);
        verify(patientRepository).save(any(Patient.class));
        verify(patientPublisherService).publishPatient(any(PatientDTO.class));
    }

    @Test
    void registerPatient_AlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        when(patientRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> patientService.registerPatient(email, password, request));
    }

    @Test
    void completePatientProfile_Success() {
        ProfileCompletionRequest request = new ProfileCompletionRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("0600000000");

        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient updated = patientService.completePatientProfile("patient123", request);

        assertNotNull(updated);
        assertEquals("John", patient.getFirstName());
        verify(patientRepository).save(patient);
    }

    @Test
    void getProfileStatus_Pending() {
        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));

        ProfileStatusResponse status = patientService.getProfileStatus("patient123");

        assertNotNull(status);
        assertEquals(AccountStatus.PENDING, status.getAccountStatus());
        assertFalse(status.isBasicProfileComplete());
    }

    @Test
    void activatePatient_Success() {
        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient activated = patientService.activatePatient("patient123");

        assertEquals(AccountStatus.ACTIVE, activated.getAccountStatus());
    }

    @Test
    void deactivatePatient_Success() {
        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient deactivated = patientService.deactivatePatient("patient123");

        assertEquals(AccountStatus.INACTIVE, deactivated.getAccountStatus());
    }

    @Test
    void findByEmail_Success() {
        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(patient));

        Patient found = patientService.findByEmail(email);

        assertNotNull(found);
        assertEquals(email, found.getEmail());
    }

    @Test
    void findById_Success() {
        when(patientRepository.findById("patient123")).thenReturn(Optional.of(patient));

        Patient found = patientService.findById("patient123");

        assertNotNull(found);
        assertEquals("patient123", found.getId());
    }

    @Test
    void getAllPatients_Success() {
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        List<Patient> patients = patientService.getAllPatients();

        assertFalse(patients.isEmpty());
        assertEquals(1, patients.size());
    }
}
