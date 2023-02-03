/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import javax.xml.stream.Location;
import nl.b3p.brmo.bag2.schema.BAG2Object;

public class BAG2WijzigingMutatie extends BAG2Mutatie {
  private BAG2Object was;
  private BAG2Object wordt;

  BAG2WijzigingMutatie(Location location, BAG2Object was, BAG2Object wordt) {
    super(location);
    this.was = was;
    this.wordt = wordt;
  }

  public BAG2Object getWas() {
    return was;
  }

  public BAG2Object getWordt() {
    return wordt;
  }

  @Override
  public String toString() {
    return "BAG2WijzigingMutatie{"
        + "location="
        + getLocation()
        + ", was="
        + was
        + ", wordt="
        + wordt
        + '}';
  }
}
