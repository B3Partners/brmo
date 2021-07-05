package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.bgt.download.api.DeltaApi;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.GetDeltasResponse;
import picocli.CommandLine.Command;

@Command(name = "downloadservice", description = "de downloadservice subcommand")
public class DownloadServiceCommand {
    @Command(name="init")
    public void init() throws ApiException {
        GetDeltasResponse response = new DeltaApi().getDeltas(null, 1, 100);
        System.out.println("Deltas: " + response);
    }

}
