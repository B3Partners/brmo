/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.soap.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 'TRACE' level logging van de soap berichten.
 *
 * @author mprins
 */
public class LogMessageHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog(LogMessageHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if (LOG.isTraceEnabled()) {
            try {
                Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                String prefix = "<<< ";
                if (outboundProperty) {
                    prefix = ">>> ";
                }

                Iterator<MimeHeader> i = context.getMessage().getMimeHeaders().getAllHeaders();
                while (i.hasNext()) {
                    MimeHeader h = i.next();
                    LOG.trace(prefix + " " + h.getName() + ": " + h.getValue());
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                context.getMessage().writeTo(bos);
                LOG.trace(prefix + " " + bos.toString("UTF-8"));
            } catch (SOAPException | IOException ex) {
                LOG.trace(ex);
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }
}
