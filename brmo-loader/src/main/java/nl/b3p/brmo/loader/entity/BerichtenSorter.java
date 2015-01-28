package nl.b3p.brmo.loader.entity;

import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * @author Chris
 */
public class BerichtenSorter implements Comparator<Bericht> {
    public static String SQL_ORDER_BY = "soort, object_ref, datum, volgordenummer";

    public int compare(Bericht one, Bericht another) {

        return new CompareToBuilder()
                .append(one.getSoort(), another.getSoort())
                .append(one.getObjectRef(), another.getObjectRef())
                .append(one.getDatum(), another.getDatum())
                .append(one.getVolgordeNummer(), another.getVolgordeNummer())
                .toComparison();
    }
}
