package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.imgeo.IMGeoSchemaMapper;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest.FeaturetypesEnum.PLAATSBEPALINGSPUNT;
import static nl.b3p.brmo.imgeo.IMGeoSchema.getIMGeoPlusObjectTypes;
import static nl.b3p.brmo.imgeo.IMGeoSchema.getOnlyBGTObjectTypes;

public class FeatureTypeSelectionOptions {
    @Option(names={"--feature-types"}, split=",", defaultValue="all", paramLabel = "<name>")
    List<String> featureTypes;

    @Option(names={"--include-plaatsbepalingspunten"})
    boolean includePlaatsbepalingspunten = false;

    public Set<DeltaCustomDownloadRequest.FeaturetypesEnum> getFeatureTypesList() {
        Set<DeltaCustomDownloadRequest.FeaturetypesEnum> types = new HashSet<>();
        if (featureTypes.contains("all")) {
            types.addAll(Arrays.asList(DeltaCustomDownloadRequest.FeaturetypesEnum.values()));
        } else {
            if (featureTypes.contains("bgt")) {
                types.addAll(getOnlyBGTObjectTypes().stream().map(t -> {
                    try {
                        // Just add the table names as featureType enum value, these are the same
                        return DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(IMGeoSchemaMapper.getTableNameForObjectType(t));
                    } catch(IllegalArgumentException e) {
                        // Ignore nummeraanduidingreeks as it is a one-to-many table for pand
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
            }
            if (featureTypes.contains("plus")) {
                types.addAll(getIMGeoPlusObjectTypes().stream().map(t ->
                        DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(IMGeoSchemaMapper.getTableNameForObjectType(t))
                ).collect(Collectors.toSet()));
            }
        }

        if (!includePlaatsbepalingspunten) {
            types.remove(PLAATSBEPALINGSPUNT);
        } else {
            types.add(PLAATSBEPALINGSPUNT);
        }

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
