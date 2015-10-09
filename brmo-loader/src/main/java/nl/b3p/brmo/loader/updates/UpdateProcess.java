package nl.b3p.brmo.loader.updates;

/**
 *
 * @author Matthijs Laan
 */
public class UpdateProcess {

    private String name;

    private String soort;

    private String xsl;

    public UpdateProcess(String name, String soort, String xsl) {
        this.name = name;
        this.soort = soort;
        this.xsl = xsl;
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

    public String getXsl() {
        return xsl;
    }

    public void setXsl(String xsl) {
        this.xsl = xsl;
    }
}
