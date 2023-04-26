package com.example.restapidemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientRecordController.class)
class PatientRecordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PatientRecordRepository patientRecordRepository;

    PatientRecord RECORD_1 = new PatientRecord(1L, "Rayven Yor", 23, "Cebu Philippines");
    PatientRecord RECORD_2 = new PatientRecord(2L, "David Landup", 27, "New York USA");
    PatientRecord RECORD_3 = new PatientRecord(3L, "Jane Doe", 31, "New York USA");

    @Test
    void getAllRecords_success() throws Exception {
        List<PatientRecord> records = new ArrayList<>(Arrays.asList(RECORD_1, RECORD_2, RECORD_3));
        Mockito.when(patientRecordRepository.findAll()).thenReturn(records);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/patient")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].name", is("Jane Doe")))
                .andDo(print());
    }

    @Test
    void getPatientById_success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId()))
//                .thenReturn(java.util.Optional.of(RECORD_1));
                .thenReturn(Optional.of(RECORD_1));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/patient/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Rayven Yor")))
                .andDo(print());
    }

    @Test
    void createRecord_success() throws Exception {
        PatientRecord record = PatientRecord.builder()
                .name("John Doe")
                .age(47)
                .address("New York USA")
                .build();

        Mockito.when(patientRecordRepository.save(record)).thenReturn((record));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(record));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andDo(print());
    }

    @Test
    void updatePatientRecord_success() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .patientId(1L)
                .name("Rayven Zambo")
                .age(23)
                .address("Cebu Philippines")
                .build();

        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId()))
                .thenReturn(Optional.of(RECORD_1));
        Mockito.when(patientRecordRepository.save(updatedRecord))
                .thenReturn(updatedRecord);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Rayven Zambo")))
                .andDo(print());
    }

    @Test
    void updatePatientRecord_nullId() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .name("sherlock Holmes")
                .age(40)
                .address("221B Baker Street")
                .build();

        MockHttpServletRequestBuilder mockResult = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockResult)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result -> assertEquals("PatientRecord or ID must not be null!", result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    void updatePatientRecord_recordNotFound() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
                .patientId(5L)
                .name("Sherlock Holmes")
                .age(40)
                .address("22B Baker Street")
                .build();

        Mockito.when(patientRecordRepository.findById(updatedRecord.getPatientId()))
                .thenReturn(Optional.empty());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()))
                .andDo(print());
    }

    @Test
    void deletePatientById_Success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_2.getPatientId()))
                .thenReturn(Optional.of(RECORD_2));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .delete("/patient/2");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void deletePatientById_notFound() throws Exception {
        Mockito.when(patientRecordRepository.findById(5L))
                .thenReturn(Optional.empty());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .delete("/patient/5");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()))
                .andDo(print());
    }
}