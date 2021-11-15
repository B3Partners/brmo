/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import nl.b3p.brmo.bag2.loader.BAG2ProgressReporter;
import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.schema.ObjectTableWriter;

public class BAG2ConsoleProgressReporter extends BAG2ProgressReporter {

    @Override
    public void startNextSplitFile(String entry) {
        super.startNextSplitFile(entry);
        System.out.print("\r" + currentFileName + ": " + entry);
    }

    @Override
    public void accept(ObjectTableWriter.Progress progress) {
        BAG2ObjectTableWriter.BAG2Progress bag2Progress = (BAG2ObjectTableWriter.BAG2Progress) progress;

        switch(progress.getStage()) {
            case LOAD_OBJECTS:
                if (progress.getObjectCount() == 0) {
                    System.out.print("\r" + currentFileName);
                }
                break;
            case FINISHED:
                System.out.print("\r");
                super.accept(progress);
                break;
            default:
                super.accept(progress);
        }
    }
}
