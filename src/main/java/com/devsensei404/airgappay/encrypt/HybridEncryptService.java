package com.devsensei404.airgappay.encrypt;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class HybridEncryptService {

    public byte[] encrypt(byte[] plaintext, SecretKey aesKey,
                          byte[] iv, PublicKey publicKey) throws Exception {

        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(
                Cipher.ENCRYPT_MODE,
                aesKey,
                new GCMParameterSpec(128, iv)
        );

        byte[] aesCiphertext = aesCipher.doFinal(plaintext);

        Cipher rsaCipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        );

        rsaCipher.init(
                Cipher.ENCRYPT_MODE,
                publicKey
        );

        byte[] encryptedAesKey = rsaCipher.doFinal(
                aesKey.getEncoded()
        );

        int totalLength =
                encryptedAesKey.length
                        + iv.length
                        + aesCiphertext.length;

        ByteBuffer buf = ByteBuffer.allocate(totalLength);

        buf.put(encryptedAesKey);
        buf.put(iv);
        buf.put(aesCiphertext);

        byte[] wireFormat = buf.array();

        return wireFormat;
    }


    public String decrypt(byte[] wireFormat,
                          PrivateKey serverPrivateKey) throws Exception {

        // Step B — unpack

        byte[] encryptedAesKey = new byte[256];

        byte[] iv = new byte[12];

        byte[] aesCiphertext = new byte[
                wireFormat.length
                        - encryptedAesKey.length
                        - iv.length
                ];

        ByteBuffer buf = ByteBuffer.wrap(wireFormat);

        buf.get(encryptedAesKey);
        buf.get(iv);
        buf.get(aesCiphertext);


        // RSA-decrypt the AES key

        Cipher rsaCipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        );

        rsaCipher.init(
                Cipher.DECRYPT_MODE,
                serverPrivateKey
        );

        byte[] aesKeyBytes =
                rsaCipher.doFinal(encryptedAesKey);

        SecretKey aesKey =
                new SecretKeySpec(aesKeyBytes, "AES");


        // AES-GCM decrypt

        Cipher aesCipher =
                Cipher.getInstance("AES/GCM/NoPadding");

        aesCipher.init(
                Cipher.DECRYPT_MODE,
                aesKey,
                new GCMParameterSpec(128, iv)
        );

        byte[] plaintext =
                aesCipher.doFinal(aesCiphertext);

        return new String(plaintext);
    }
}
