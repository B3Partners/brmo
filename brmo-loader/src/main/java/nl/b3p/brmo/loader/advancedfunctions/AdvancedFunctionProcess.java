package nl.b3p.brmo.loader.advancedfunctions;

/**
 *
 * @author Chris van Lith
 */
public class AdvancedFunctionProcess {

    private String name;

    private String soort;

    private String config;

    /**
     * constructor.
     *
     * @param name beschrijving / naam van het proces.
     * @param soort soort berichten
     * @param config extra gegevens voor dit proces
     */
    public AdvancedFunctionProcess(String name, String soort, String config) {
        this.name = name;
        this.soort = soort;
        this.config = config;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
