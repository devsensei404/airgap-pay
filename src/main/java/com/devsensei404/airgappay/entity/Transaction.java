package com.devsensei404.airgappay.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name="transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transId;

    @Column(nullable = false)
    private String senderVpa;

    @Column(nullable = false)
    private String receiverVpa;

    @Column(nullable = false, unique = true, length = 64)
    private String packetHash; // SHA-256 hex of the encrypted packet

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant signedAt; // When the sender originally signed it (offline)

    @Column(nullable = false)
    private Instant settledAt; // When the backend actually processed it

    @Column(nullable = false)
    private String bridgeNodeId; // Which mesh node finally delivered it

    @Column(nullable = false)
    private int hopCount; // How many devices it passed through

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public Transaction(String senderVpa, String receiverVpa, String packetHash, BigDecimal amount,
                       Instant signedAt, Instant settledAt, String bridgeNodeId, int hopCount, Status status) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.packetHash = packetHash;
        this.amount = amount;
        this.signedAt = signedAt;
        this.settledAt = settledAt;
        this.bridgeNodeId = bridgeNodeId;
        this.hopCount = hopCount;
        this.status = status;
    }
}
