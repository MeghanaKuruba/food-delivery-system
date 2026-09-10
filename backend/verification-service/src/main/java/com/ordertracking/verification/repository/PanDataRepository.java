package com.ordertracking.verification.repository;

import com.ordertracking.verification.entity.PanData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PanDataRepository extends JpaRepository<PanData, String> {

    Optional<PanData> findByPanNumberIgnoreCase(String panNumber);

    boolean existsByPanNumberIgnoreCase(String panNumber);
}