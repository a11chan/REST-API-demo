package com.example.resteapidemo;

import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/patient")
public class PatientRecordController {

    private PatientRecordRepository patientRecordRepository;

    public PatientRecordController(PatientRecordRepository patientRecordRepository) {
        this.patientRecordRepository = patientRecordRepository;
    }

    @GetMapping
    public List<PatientRecord> getAllRecords() {
        return patientRecordRepository.findAll();
    }

    @GetMapping(value = "{patientId}")
    public PatientRecord getPatientById(@PathVariable(value = "patientId") Long patientId) {
        return patientRecordRepository.findById(patientId).get();
    }

    @PostMapping
    public PatientRecord createRecord(@RequestBody @Valid PatientRecord patientRecord) {
        return patientRecordRepository.save(patientRecord);
    }

    @PutMapping
    public PatientRecord updatePatientRecord(@RequestBody PatientRecord patientRecord) {
        if (patientRecord == null || patientRecord.getPatientId() == null) {
            throw new InvalidRequestException("PatientRecord or ID must not be null!");
        }

        Optional<PatientRecord> optionalRecord = patientRecordRepository.findById(patientRecord.getPatientId());
        if (optionalRecord.isEmpty()) {
            throw new EntityNotFoundException("Patient with ID " + patientRecord.getPatientId() + " does not exist.");
        }

        PatientRecord existingPatientRecord = optionalRecord.get();

        existingPatientRecord.setName(patientRecord.getName());
        existingPatientRecord.setAge(patientRecord.getAge());
        existingPatientRecord.setAddress(patientRecord.getAddress());

        return patientRecordRepository.save(existingPatientRecord);
    }

    @DeleteMapping(value = "{patientId}")
    public void deletePatientById(@PathVariable(value = "patientId") Long patientId) {
        if (patientRecordRepository.findById(patientId).isEmpty()) {
            throw new EntityNotFoundException("Patient with ID " + patientId + " does not exist.");
        }
        patientRecordRepository.deleteById(patientId);
    }
}
