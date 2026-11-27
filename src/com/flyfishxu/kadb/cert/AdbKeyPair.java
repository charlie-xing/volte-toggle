package com.flyfishxu.kadb.cert;

import com.flyfishxu.kadb.core.AdbMessage;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Objects;

/**
 * Hardened replacement for Kadb's AdbKeyPair that disables the legacy plaintext
 * A_AUTH (ADB_AUTH_TOKEN) RSA signing oracle. Wireless Debugging exclusively uses
 * TLS 1.3 (STLS), where client authentication is bound to the TLS handshake transcript.
 */
public final class AdbKeyPair {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final Certificate certificate;

    public AdbKeyPair(PrivateKey privateKey, PublicKey publicKey, Certificate certificate) {
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
        this.certificate = Objects.requireNonNull(certificate, "certificate");
    }

    public final PrivateKey getPrivateKey() {
        return privateKey;
    }

    public final PublicKey getPublicKey() {
        return publicKey;
    }

    public final Certificate getCertificate() {
        return certificate;
    }

    public final byte[] signPayload$kadb_release(AdbMessage message) {
        throw new SecurityException(
                "Legacy plaintext ADB AUTH is disabled; Wireless Debugging TLS (STLS) is required");
    }
}
