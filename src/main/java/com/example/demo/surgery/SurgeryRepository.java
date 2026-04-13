package com.example.demo.surgery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurgeryRepository extends JpaRepository<Surgery, Integer> {
    List<Surgery> findAllByOrderByStartTimeAsc();
}