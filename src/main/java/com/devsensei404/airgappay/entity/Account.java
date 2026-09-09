package com.devsensei404.airgappay.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="accounts")
@Data
@NoArgsConstructor

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vpa;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, precision =14 , scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    public Account(String vpa, String holderName, BigDecimal balance){
        this.vpa=vpa;
        this.holderName=holderName;
        this.balance=balance;
    }
}
