package com.devsensei404.airgappay.encrypt;

import com.devsensei404.airgappay.dto.PaymentInstructions;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;

@Service
public class HybridEncryptService {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final int KEY_LENGTH_PREFIX_BYTES = 4;
    private static final int AES_KEY_BITS = 256;
    private static final OAEPParameterSpec OAEP_PARAMS = new OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT);
    private final SecureRandom secureRandom = new SecureRandom();

    private final ObjectMapper objectMapper;

    public HybridEncryptService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] encrypt(PaymentInstructions instruction, PublicKey publicKey) throws Exception {

        byte[] plaintext = objectMapper.writeValueAsBytes(instruction);
        byte[] iv = new byte[GCM_IV_BYTES];
        secureRandom.nextBytes(iv);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_BITS);
        SecretKey aesKey = keyGenerator.generateKey();

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(
                Cipher.ENCRYPT_MODE,
                aesKey,
                new GCMParameterSpec(GCM_TAG_BITS, iv)
        );

        byte[] aesCiphertext = aesCipher.doFinal(plaintext);

        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);

        rsaCipher.init(
                Cipher.ENCRYPT_MODE,
                publicKey, OAEP_PARAMS
        );

        byte[] encryptedAesKey = rsaCipher.doFinal(
                aesKey.getEncoded()
        );

        int totalLength =
                encryptedAesKey.length
                        + iv.length
                        + aesCiphertext.length;

        // KEY_LENGTH_PREFIX_BYTES for the length prefix that records how long the
        // RSA-encrypted AES key is, so decrypt() doesn't have to hardcode a key-size assumption.
        ByteBuffer buf = ByteBuffer.allocate(totalLength + KEY_LENGTH_PREFIX_BYTES);

        buf.putInt(encryptedAesKey.length);
        buf.put(encryptedAesKey);
        buf.put(iv);
        buf.put(aesCiphertext);

        return buf.array();
    }


    public PaymentInstructions decrypt(byte[] wireFormat,
                          PrivateKey serverPrivateKey) throws Exception {

        // Step B — unpack. Wrap first, THEN read from it.
        ByteBuffer buf = ByteBuffer.wrap(wireFormat);

        int keyLen = buf.getInt();

        byte[] encryptedAesKey = new byte[keyLen];
        byte[] iv = new byte[GCM_IV_BYTES];

        // Subtract KEY_LENGTH_PREFIX_BYTES already consumed by getInt() above, on top of
        // the key and IV lengths, or this under/overshoots the remaining buffer.
        byte[] aesCiphertext = new byte[
                wireFormat.length
                        - KEY_LENGTH_PREFIX_BYTES
                        - encryptedAesKey.length
                        - iv.length
                ];

        buf.get(encryptedAesKey);
        buf.get(iv);
        buf.get(aesCiphertext);


        // RSA-decrypt the AES key

        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);

        rsaCipher.init(
                Cipher.DECRYPT_MODE,
                serverPrivateKey,
                OAEP_PARAMS
        );

        byte[] aesKeyBytes =
                rsaCipher.doFinal(encryptedAesKey);

        SecretKey aesKey =
                new SecretKeySpec(aesKeyBytes, "AES");


        // AES-GCM decrypt

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);

        aesCipher.init(
                Cipher.DECRYPT_MODE,
                aesKey,
                new GCMParameterSpec(GCM_TAG_BITS, iv)
        );

        byte[] plaintext =
                aesCipher.doFinal(aesCiphertext);

        return objectMapper.readValue(plaintext, PaymentInstructions.class);
    }

    public String hashCiphertext(byte[] ciphertext) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(ciphertext);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

}
