package nl.b3p.brmo.soap.brk;

import javax.xml.ws.BindingProvider;

/**
 *
 * @author Chris
 */
public class ClientApp {
    
    public static void main (String[] args) {
             try { 
                GetBrkInfoImpl port = new GetBrkInfoImplService().getGetBrkInfoImplPort();
                log((BindingProvider)port);
                BrkInfoRequest req = new BrkInfoRequest();
                req.setGevoeligeInfoOphalen(true);
                KadOnrndZkInfoRequest koz = new KadOnrndZkInfoRequest();
                koz.setSectie("L");
                req.setKadOnrndZk(koz);
                BrkInfoResponse result = port.getBrkInfo(req);
                System.out.printf ("Result = %s\n",result.getKadOnrndZk().getIdentificatie());
            } catch (BrkInfoException_Exception ex) {
                System.out.printf ("<p>Exception: %s\n", ex.getFaultInfo ().getDetail ());
            }

    }

    private static void log(BindingProvider port) {
        if (Boolean.getBoolean("wsmonitor")) {
            String address = (String)port.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            address = address.replaceFirst("8080", "4040");
            port.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
        }
    }
}
