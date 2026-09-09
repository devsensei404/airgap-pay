package com.devsensei404.airgappay.repository;

import com.devsensei404.airgappay.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByVpa(String vpa);
}