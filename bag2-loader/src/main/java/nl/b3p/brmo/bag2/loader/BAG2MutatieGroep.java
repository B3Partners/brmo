/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2Object;

import java.util.List;

public class BAG2MutatieGroep {
    private List<BAG2Mutatie> mutaties;

    BAG2MutatieGroep(List<BAG2Mutatie> mutaties) {
        this.mutaties = mutaties;
    }

    public List<BAG2Mutatie> getMutaties() {
        return mutaties;
    }

    public boolean isSingleToevoeging() {
        return mutaties.size() == 1 && mutaties.get(0) instanceof BAG2ToevoegingMutatie;
    }

    public BAG2Object getSingleToevoeging() {
        if (!isSingleToevoeging()) {
            throw new IllegalStateException("Not a single toevoeging mutatieGroep");
        }
        return ((BAG2ToevoegingMutatie)mutaties.get(0)).getToevoeging();
    }
}
