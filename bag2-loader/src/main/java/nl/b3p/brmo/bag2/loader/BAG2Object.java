/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import java.util.Map;

public class BAG2Object {
    public String name;
    public Map<String,Object> attributes;

    public BAG2Object(String name, Map<String,Object> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String toString() {
        return name = "[" + attributes + "]";
    }
}
