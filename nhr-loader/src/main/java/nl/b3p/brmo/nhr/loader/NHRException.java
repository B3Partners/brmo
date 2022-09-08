/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.nhr.loader;

import nl.b3p.brmo.loader.util.BrmoException;
import java.util.Map;

/**
 * Specifieke exception voor NHR foutmeldingen.
 */
public class NHRException extends BrmoException {
    private Map<String, String> errors;
    private String message;

    public NHRException(Map<String, String> errors) {
        this.errors = errors;

        StringBuilder errorList = new StringBuilder();
        for (String key : errors.keySet()) {
            if (errorList.length() > 0) {
                errorList.append(", ");
            }

            errorList.append(key);
            errorList.append(": ");
            errorList.append(errors.get(key));
        }

        message = errorList.toString();
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }
}

