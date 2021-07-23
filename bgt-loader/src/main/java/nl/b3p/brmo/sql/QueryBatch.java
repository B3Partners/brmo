/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.sql;

import java.sql.SQLException;

public interface QueryBatch {
    boolean addBatch(Object[] params) throws Exception;

    void executeBatch() throws Exception;

    void close() throws SQLException;
}
