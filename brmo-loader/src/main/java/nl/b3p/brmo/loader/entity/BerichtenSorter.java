package nl.b3p.brmo.loader.entity;

import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * @author Chris
 */
public class BerichtenSorter implements Comparator<Bericht> {
    public static String SQL_ORDER_BY = "datum, volgordenummer, object_ref";

    public int compare(Bericht one, Bericht another) {

        return new CompareToBuilder()
                .append(one.getDatum(), another.getDatum())
                .append(one.getVolgordeNummer(), another.getVolgordeNummer())
                .append(one.getObjectRef(), another.getObjectRef())
                .toComparison();
    }
}
