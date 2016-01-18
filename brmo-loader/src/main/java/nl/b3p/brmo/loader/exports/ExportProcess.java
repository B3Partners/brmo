package nl.b3p.brmo.loader.exports;

/**
 *
 * @author Chris van Lith
 */
public class ExportProcess {

    private String name;

    private String soort;

    private String exportpad;

    public ExportProcess(String name, String soort, String exportpad) {
        this.name = name;
        this.soort = soort;
        this.exportpad = exportpad;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSoort() {
        return soort;
    }

    public void setSoort(String soort) {
        this.soort = soort;
    }

    public String getExportpad() {
        return exportpad;
    }

    public void setExportpad(String exportpad) {
        this.exportpad = exportpad;
    }
}
