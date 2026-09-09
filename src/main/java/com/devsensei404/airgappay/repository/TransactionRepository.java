package com.devsensei404.airgappay.repository;

import com.devsensei404.airgappay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByPacketHash(String packetHash);
}