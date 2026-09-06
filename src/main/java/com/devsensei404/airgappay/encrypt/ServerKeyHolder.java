package com.devsensei404.airgappay.encrypt;

import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;

@Service
public class ServerKeyHolder {

    private static final int RSA_KEY_BITS = 2048;

    private KeyPair keyPair;

    public ServerKeyHolder() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(RSA_KEY_BITS);
        this.keyPair = keyPairGenerator.generateKeyPair();
    }


    public PublicKey getPublicKey(){
        return keyPair.getPublic();
    }
    public PrivateKey getPrivateKey(){
        return keyPair.getPrivate();
    }
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}