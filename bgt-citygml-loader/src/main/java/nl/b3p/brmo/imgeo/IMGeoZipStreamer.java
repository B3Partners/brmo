package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.AttributeColumnMapping;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class IMGeoZipStreamer {

    public static void main(String[] args) throws IOException {
        ZipFile file = new ZipFile(new File("/media/ssd/files/bgt/2021/bgt-citygml-nl-nopbp.zip"));
        // XXX Java 8 geen takeUntil() etc.
        file.stream().forEach(entry -> {
            final double sizeMB = entry.getSize() / 1024.0 / 1024;
            System.out.printf("Processing ZIP entry: %s (%.0f MiB)\n", entry.getName(), sizeMB);

            try {
                IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(file.getInputStream(entry));

                int objects = 0;
                boolean log = true;
                long startTime = System.currentTimeMillis();
                for(IMGeoObject object: streamer) {
                    objects++;

                    if(log) System.out.printf("cityObjectMember #%d: %s\n",
                            objects,
                            object);

                    System.out.printf("Table %s:\n", object.getName());
                    for(String attribute: object.getAttributes().keySet()) {
                        boolean isBaseColumn = false;
                        for(AttributeColumnMapping baseColumn: IMGeoSchema.baseAttributes) {
                            if (attribute.equals(baseColumn.getName())) {
                                isBaseColumn = true;
                                break;
                            }
                        }
                        if (!isBaseColumn) {
                            System.out.printf("  %s\n", attribute);
                        }
                    }

                    for(AttributeColumnMapping baseColumn: IMGeoSchema.baseAttributes) {
                        if (!object.getAttributes().containsKey(baseColumn.getName())) {
                            System.out.println("  Does not have baseColumn " + baseColumn.getName());
                        }
                    }
                    break;

/*
                    if (objects % 1000000 == 0) {
                        System.out.printf("Parsed %d objects\n", objects);
                    }
*/
                    //if (objects == 100000) {
                    //    break;
                    //}
                }
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                double zippedSizeMB = entry.getCompressedSize() / 1024.0 / 1024;
                System.out.printf("Finished streaming: %d objects, %.1f s, %.0f objects/s, zipped %.1f MiB/s, unzipped %.1f MiB/s\n", objects, time, objects / time, zippedSizeMB / time, sizeMB / time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

/*
        try(ZipInputStream zip = new ZipInputStream(new FileInputStream())) {
            ZipEntry entry = zip.getNextEntry();
            while(entry != null) {
                System.out.println("Processing zip entry: " + entry.getName());
                entry = zip.getNextEntry();
            }
        }
*/
    }
}
