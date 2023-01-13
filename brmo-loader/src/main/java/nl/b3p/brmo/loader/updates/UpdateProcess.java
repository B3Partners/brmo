package nl.b3p.brmo.loader.updates;

/** @author Matthijs Laan */
public class UpdateProcess {

    private String name;

    private String soort;

    private String xsl;

    private boolean updateDbXml;

    public UpdateProcess(String name, String soort, String xsl) {
        this.name = name;
        this.soort = soort;
        this.xsl = xsl;
    }

    public UpdateProcess(String name, String soort, String xsl, boolean updateDbXml) {
        this.name = name;
        this.soort = soort;
        this.xsl = xsl;
        this.updateDbXml = updateDbXml;
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

    /**
     * of de DbXml moet "herberekend" en gebruikt moet worden in verdere verwerking.
     *
     * @return true als de DbXml opnieuw moet worden gemaakt en opgeslagen in het bericht.
     */
    public boolean isUpdateDbXml() {
        return updateDbXml;
    }

    /**
     * of de DbXml moet "herberekend" en gebruikt moet worden in verdere verwerking.
     *
     * @param updateDbXml {@code true} als de DbXml opnieuw moet worden gemaakt en opgeslagen in het
     *     bericht.
     */
    public void setUpdateDbXml(boolean updateDbXml) {
        this.updateDbXml = updateDbXml;
    }
}
