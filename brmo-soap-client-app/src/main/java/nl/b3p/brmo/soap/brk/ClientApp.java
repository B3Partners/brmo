package nl.b3p.brmo.soap.brk;

import java.util.Map;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author Chris
 */
public class ClientApp {

    public static void main(String[] args) {

        String endpoint = "";
        String username = "admin";
        String password = "*******";

        try {
            GetBrkInfoImpl port = new GetBrkInfoImplService().getGetBrkInfoImplPort();

            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();

            String oldEndpoint = (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
//            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
            requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
            BrkInfoRequest req = new BrkInfoRequest();
            req.setGevoeligeInfoOphalen(true);
            req.setSubjectsToevoegen(true);
            req.setAdressenToevoegen(true);
            req.setZoekgebied("POINT(177753 576550)");
//            req.setZoekgebied("POINT(172753 571550)");
            req.setBufferLengte(10);

            KadOnrndZkInfoRequest koz = new KadOnrndZkInfoRequest();
//            koz.setSectie("L");
//            koz.setIdentificatie("47100002870000");
            req.setKadOnrndZk(koz);
//            PerceelAdresInfoRequest pa = new PerceelAdresInfoRequest();
//            pa.setPostcode("8378J");
//            req.setPerceelAdres(pa);

            BrkInfoResponse result = port.getBrkInfo(req);
            System.out.printf("Size = %s\n", result.getKadOnrndZk().size());

            KadOnrndZkInfoResponse kozr = result.getKadOnrndZk().get(0);
            if (kozr != null) {
                System.out.printf("Identificatie = %s\n", kozr.getIdentificatie());
                AdressenResponse ar = kozr.getAdressen();
                if (ar != null) {
                    AdresResponse bar = ar.getBAGAdres().get(0);
                    if (bar != null) {
                        System.out.printf("Straatnaam = %s\n", bar.getStraatNaam());
                    }
                }
                RechtenResponse rr = kozr.getRechten();
                if (rr != null) {
                    ZakelijkRechtResponse zrr = rr.getZakelijkRecht().get(0);
                    if (zrr != null) {
                        System.out.printf("Recht = %s\n", zrr.getAardVerkregenRecht());
                        NatuurlijkPersoonResponse npr = zrr.getNatuurlijkPersoon();
                        if (npr != null) {
                            System.out.printf("Naam = %s\n", npr.getGeslachtsnaam());
                        }
                        NietNatuurlijkPersoonResponse nnpr = zrr.getNietNatuurlijkPersoon();
                        if (nnpr != null) {
                            System.out.printf("Bedrijf = %s\n", nnpr.getNaam());
                        }
                    }
                }
            }
        } catch (BrkInfoException_Exception ex) {
            System.out.printf("Exception: %s\n", ex.getFaultInfo().getDetail());
        }

    }
}
