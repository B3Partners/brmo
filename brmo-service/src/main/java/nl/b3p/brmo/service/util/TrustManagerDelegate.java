/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.service.util;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *
 * @author mprins
 */
public class TrustManagerDelegate implements X509TrustManager {

    private final X509TrustManager mainTrustManager;
    private final X509TrustManager fallbackTrustManager;

    public TrustManagerDelegate(X509TrustManager mainTrustManager, X509TrustManager fallbackTrustManager) {
        this.mainTrustManager = mainTrustManager;
        this.fallbackTrustManager = fallbackTrustManager;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType) throws CertificateException {
        try {
            mainTrustManager.checkClientTrusted(x509Certificates, authType);
        } catch (CertificateException ignored) {
            this.fallbackTrustManager.checkClientTrusted(x509Certificates, authType);
        }
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String authType) throws CertificateException {
        try {
            mainTrustManager.checkServerTrusted(x509Certificates, authType);
        } catch (CertificateException ignored) {
            this.fallbackTrustManager.checkServerTrusted(x509Certificates, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.fallbackTrustManager.getAcceptedIssuers();
    }
}
