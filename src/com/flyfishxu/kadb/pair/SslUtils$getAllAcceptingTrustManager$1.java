package com.flyfishxu.kadb.pair;

import com.bp.pixel_ims_apn_tuner.Adb;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import javax.net.ssl.X509TrustManager;

/**
 * Hardened replacement for Kadb's trust manager that validates the server X.509 certificate's
 * cryptographic self-signature, key strength, and TOFU public-key pin on ADB TLS connections.
 */
public final class SslUtils$getAllAcceptingTrustManager$1 implements X509TrustManager {

    public SslUtils$getAllAcceptingTrustManager$1() {}

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        throw new CertificateException("Client mode only; checkClientTrusted not supported");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (chain == null || chain.length == 0 || chain[0] == null) {
            throw new CertificateException("Empty server certificate chain");
        }
        X509Certificate cert = chain[0];
        PublicKey pub = cert.getPublicKey();
        if (pub == null) {
            throw new CertificateException("Server certificate has no public key");
        }
        if (pub instanceof RSAPublicKey) {
            int bits = ((RSAPublicKey) pub).getModulus().bitLength();
            if (bits < 2048) {
                throw new CertificateException("Weak RSA server key: " + bits + " bits");
            }
        } else if (pub instanceof ECPublicKey) {
            int bits = ((ECPublicKey) pub).getParams().getOrder().bitLength();
            if (bits < 256) {
                throw new CertificateException("Weak EC server key: " + bits + " bits");
            }
        } else {
            throw new CertificateException("Unsupported server key algorithm: " + pub.getAlgorithm());
        }
        try {
            cert.verify(pub);
        } catch (Exception e) {
            throw new CertificateException("Server certificate signature verification failed", e);
        }
        Adb.verifyOrPinServerCert(cert);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
