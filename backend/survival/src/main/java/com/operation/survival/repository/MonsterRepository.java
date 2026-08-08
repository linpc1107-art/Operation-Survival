package com.operation.survival.repository;

import com.operation.survival.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterRepository
        extends JpaRepository<Monster, Long> {

    List<Monster> findByStage(Integer stage);
}