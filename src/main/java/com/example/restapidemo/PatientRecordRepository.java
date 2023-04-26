package com.example.resteapidemo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {
}
