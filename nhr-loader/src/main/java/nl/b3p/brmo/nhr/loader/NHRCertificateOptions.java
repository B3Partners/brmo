/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.loader;

import picocli.CommandLine;

public class NHRCertificateOptions {
    @CommandLine.Option(names="--truststore")
    private String truststore;

    @CommandLine.Option(names="--truststore-password")
    private String truststorePassword;

    @CommandLine.Option(names="--keystore")
    private String keystore;

    @CommandLine.Option(names={"--keystore-password"}, interactive = true)
    private String keystorePassword;

    @CommandLine.Option(names={"--keystore-alias"})
    private String keystoreAlias;

    public String getTruststore() {
        return truststore;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystoreAlias() {
        return keystoreAlias;
    }

    public void setKeystoreAlias(String keystoreAlias) {
        this.keystoreAlias = keystoreAlias;
    }
}
