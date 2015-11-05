package nl.b3p.brmo.soap.brk;

import java.util.Map;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author Chris
 */
public class ClientApp {
    
    public static void main (String[] args) {
        
        String endpoint = "";
        String username = "";
        String password = "";        
        
             try { 
                GetBrkInfoImpl port = new GetBrkInfoImplService().getGetBrkInfoImplPort();
 
                 Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();

//                 String oldEndpoint = (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
//                 requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
//                 requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
//                 requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);

                 BrkInfoRequest req = new BrkInfoRequest();
                req.setGevoeligeInfoOphalen(true);
                KadOnrndZkInfoRequest koz = new KadOnrndZkInfoRequest();
                koz.setSectie("L");
                req.setKadOnrndZk(koz);
                BrkInfoResponse result = port.getBrkInfo(req);
                System.out.printf ("Result = %s\n",result.getKadOnrndZk().get(0).getIdentificatie());
            } catch (BrkInfoException_Exception ex) {
                System.out.printf ("<p>Exception: %s\n", ex.getFaultInfo ().getDetail ());
            }

    }
}
