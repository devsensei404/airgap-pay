package com.devsensei404.airgappay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInstructions {
    private String senderVPA;
    private String receiverVPA;
    private BigDecimal amt;
    private String pinHash;
    private UUID nonce;
    private Instant timestamp;
}
