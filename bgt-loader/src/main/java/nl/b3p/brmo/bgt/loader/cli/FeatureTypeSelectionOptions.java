/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.schema.BGTSchema;
import nl.b3p.brmo.bgt.schema.BGTSchemaMapper;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest.FeaturetypesEnum.PLAATSBEPALINGSPUNT;

public class FeatureTypeSelectionOptions {
    @Option(names={"--feature-types"}, split=",", defaultValue="all", paramLabel = "<name>")
    List<String> featureTypes;

    public List<String> getFeatureTypes() {
        return featureTypes;
    }

    public void setFeatureTypes(List<String> featureTypes) {
        this.featureTypes = featureTypes;
    }


    public Set<DeltaCustomDownloadRequest.FeaturetypesEnum> getFeatureTypesList() {
        Set<DeltaCustomDownloadRequest.FeaturetypesEnum> types = new HashSet<>();
        if (featureTypes.contains("all")) {
            types.addAll(Arrays.asList(DeltaCustomDownloadRequest.FeaturetypesEnum.values()));
        } else {
            BGTSchema bgtSchema = BGTSchema.getInstance();
            BGTSchemaMapper bgtSchemaMapper = BGTSchemaMapper.getInstance();
            if (featureTypes.contains("bgt")) {
                types.addAll(BGTSchema.getInstance().getOnlyBGTObjectTypes().map(objectType -> {
                    try {
                        // Just add the table names as featureType enum value, these are the same
                        return DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(bgtSchemaMapper.getTableNameForObjectType(objectType, ""));
                    } catch(IllegalArgumentException e) {
                        // Ignore nummeraanduidingreeks as it is a one-to-many table for pand
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
            }
            if (featureTypes.contains("plus")) {
                types.addAll(bgtSchema.getIMGeoPlusObjectTypes().map(objectType ->
                        DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(bgtSchemaMapper.getTableNameForObjectType(objectType, ""))
                ).collect(Collectors.toSet()));
            }
        }

        // This feature type is mostly not useful and very big so must be added explicitly, not included with 'all' or 'bgt'
        types.remove(PLAATSBEPALINGSPUNT);

        for(String featureType: featureTypes) {
            if (Stream.of("all", "bgt", "plus").noneMatch(t -> t.equals(featureType))) {
                try {
                    types.add(DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(featureType));
                } catch(IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format("Invalid feature type: \"%s\"", featureType));
                }
            }
        }
        return types;
    }
}
