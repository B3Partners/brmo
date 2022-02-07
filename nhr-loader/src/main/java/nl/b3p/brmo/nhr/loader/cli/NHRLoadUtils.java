/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.loader.cli;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.nhr.loader.NHRCertificateOptions;
import nl.b3p.brmo.nhr.loader.NHRDatabaseOptions;
import nl.kvk.schemas.schemas.hrip.dataservice._2015._02.Dataservice;
import nl.kvk.schemas.schemas.hrip.dataservice._2015._02.DataserviceService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;


public class NHRLoadUtils {
    public static BrmoFramework getFramework(NHRDatabaseOptions databaseOptions) throws BrmoException {
        BasicDataSource dsStaging = new BasicDataSource();

        dsStaging.setUrl(databaseOptions.getConnectionString());
        dsStaging.setUsername(databaseOptions.getUser());
        dsStaging.setPassword(databaseOptions.getPassword());

        BrmoFramework fw = new BrmoFramework(dsStaging, null);
        fw.setOrderBerichten(true);
        return fw;
    }

    private static Properties getCryptoProperties(NHRCertificateOptions certificateOptions) {
        Properties props = new Properties();

        props.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        props.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", certificateOptions.getKeystore());
        props.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", certificateOptions.getKeystorePassword());
        props.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "PKCS12");
        props.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", certificateOptions.getKeystoreAlias());

        return props;
    }

    public static Dataservice getDataservice(String targetLocation, boolean preprod, NHRCertificateOptions certificateOptions) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DataserviceService dataServiceService = new DataserviceService();
        Dataservice dataService = dataServiceService.getDataserviceSoap11();
        Client client = ClientProxy.getClient(dataService);
        Endpoint endpoint = client.getEndpoint();
        HTTPConduit http = (HTTPConduit) client.getConduit();

        // Set up TLS certificates/keys
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");

        try (FileInputStream trustStoreFile = new FileInputStream(certificateOptions.getTruststore())) {
            trustStore.load(trustStoreFile, certificateOptions.getTruststorePassword().toCharArray());
        }
        trustManagerFactory.init(trustStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (FileInputStream keyStoreFile = new FileInputStream(certificateOptions.getKeystore())) {
            keyStore.load(keyStoreFile, certificateOptions.getKeystorePassword().toCharArray());
        }
        keyManagerFactory.init(keyStore, certificateOptions.getKeystorePassword().toCharArray());

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setKeyManagers(keyManagerFactory.getKeyManagers());
        tlsClientParameters.setSslContext(context);
        http.setTlsClientParameters(tlsClientParameters);

        Map<String, Object> props = new HashMap<String, Object>();

        props.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.SIGNATURE);
        props.put(WSHandlerConstants.INCLUDE_SIGNATURE_TOKEN, "true");
        props.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
        props.put(WSHandlerConstants.USE_SINGLE_CERTIFICATE, "false");

        props.put(WSHandlerConstants.SIG_PROP_REF_ID, "signatureProperties");
        props.put("signatureProperties", getCryptoProperties(certificateOptions));

        props.put(WSHandlerConstants.SIGNATURE_PARTS,
                  "{}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;"
                  + "{}{http://schemas.xmlsoap.org/soap/envelope/}Body;"
                  + "{}{http://www.w3.org/2005/08/addressing}To;"
                  + "{}{http://www.w3.org/2005/08/addressing}ReplyTo;"
                  + "{}{http://www.w3.org/2005/08/addressing}MessageID;"
                  + "{}{http://www.w3.org/2005/08/addressing}Action");

        props.put(WSHandlerConstants.USER, certificateOptions.getKeystoreAlias());
        props.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    ((WSPasswordCallback) callback).setPassword(certificateOptions.getKeystorePassword());
                    return;
                }
            }
        });

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(props);
        endpoint.getOutInterceptors().add(wssOut);

        // Set up necessary WS-Addressing fields.
        AddressingProperties maps = new AddressingProperties();
        EndpointReferenceType anonref = new EndpointReferenceType();
        AttributedURIType anonymous = new AttributedURIType();
        anonymous.setValue("http://www.w3.org/2005/08/addressing/anonymous");
        anonref.setAddress(anonymous);
        maps.setReplyTo(anonref);
        maps.setFaultTo(anonref);

        // <wsa:To> needs to point at a predefined value.
        EndpointReferenceType toval = new EndpointReferenceType();
        AttributedURIType target = new AttributedURIType();
        target.setValue(preprod ? "http://es.kvk.nl/kvk-DataservicePP/2015/02" : "http://es.kvk.nl/kvk-Dataservice/2015/02");
        toval.setAddress(target);
        maps.setTo(toval);

        BindingProvider bindingProvider = (BindingProvider) dataService;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, targetLocation);
        bindingProvider.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, maps);

        return dataService;
    }
}
