package nl.b3p.brmo.persistence.staging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;

/**
 * Proces configuratie voor het ophalen van beschikbare PDOK Download Service
 * delta's.
 * 
 * @author matthijsln
 */
@Entity
public class PDOKDownloadServiceProces extends AutomatischProces {

    private static final String PDOK_SERVICE_URL = "pdok_service_url";
    public static final String PDOK_SERVICE_URL_DEFAULT = "https://test.downloads.pdok.nl/";

    private static final String DOWNLOAD_DIR = "downloaddir";
    private static final String DATASET = "dataset";
    private static final String FORMAT = "format";
    private static final String PARAMETERS = "parameters";

    private static final String MODE = "mode";
    public static final String MODE_CHECK_DOWNLOADS = "check_downloads";
    public static final String MODE_DOWNLOAD = "download";
    
    private static final String GET_FIRST_DELTA = "get_first_deltaId";
    private static final String START_DELTA_ID = "start_deltaId";

    private static final String SSL_VALIDATION = "ssl_validation";

    public PDOKDownloadServiceProces() {
        this.getConfig().put(DATASET, new ClobElement("bgtv3"));
        this.getConfig().put(FORMAT, new ClobElement("citygml"));
        this.getConfig().put(PDOK_SERVICE_URL, new ClobElement(PDOK_SERVICE_URL_DEFAULT));
    }

    public String getPDOKServiceURL() {
        String s = ClobElement.nullSafeGet(this.getConfig().get(PDOK_SERVICE_URL));
        return s == null ? PDOK_SERVICE_URL_DEFAULT : s;
    }

    public void setPDOKServiceUR(String url) {
        if (url == null) {
            this.getConfig().put(PDOK_SERVICE_URL, null);
        } else {
            this.getConfig().put(PDOK_SERVICE_URL, new ClobElement(url));
        }
    }

    public String getDownloadDirectory() {
        return ClobElement.nullSafeGet(this.getConfig().get(DOWNLOAD_DIR));
    }

    public void setDownloadDirectory(String downloadDirectory) {
        if (downloadDirectory == null) {
            this.getConfig().put(DOWNLOAD_DIR, null);
        } else {
            this.getConfig().put(DOWNLOAD_DIR, new ClobElement(downloadDirectory));
        }
    }

    public String getDataset() {
        return ClobElement.nullSafeGet(this.getConfig().get(DATASET));
    }

    public void setDataset(String dataset) {
        if (dataset == null) {
            this.getConfig().put(DATASET, null);
        } else {
            this.getConfig().put(DATASET, new ClobElement(dataset));
        }
    }

    public String getFormat() {
        return ClobElement.nullSafeGet(this.getConfig().get(FORMAT));
    }

    public void setFormat(String format) {
        if (format == null) {
            this.getConfig().put(FORMAT, null);
        } else {
            this.getConfig().put(FORMAT, new ClobElement(format));
        }
    }

    private static final String CONFIG_PARAM_PREFIX = "param_";

    public Map<String, String> getParameters() {
        Map<String,String> params = new HashMap();
        for(Map.Entry<String,ClobElement> c: this.getConfig().entrySet()) {
            if(c.getKey().startsWith(CONFIG_PARAM_PREFIX) && c.getValue() != null) {
                params.put(c.getKey().substring(CONFIG_PARAM_PREFIX.length()), c.getValue().getValue());
            }
        }
        return params;
    }

    public void setParameters(Map<String,String> params) {
        for(Map.Entry<String,String> p: params.entrySet()) {
            this.getConfig().put(CONFIG_PARAM_PREFIX + p.getKey(), new ClobElement(p.getValue()));
        }

        List<String> toRemove = new ArrayList();
        for(Map.Entry<String,ClobElement> c: this.getConfig().entrySet()) {
            if(c.getKey().startsWith(CONFIG_PARAM_PREFIX) && !params.containsKey(c.getKey().substring(CONFIG_PARAM_PREFIX.length()))) {
                toRemove.add(c.getKey());
            }
        }
        for(String k: toRemove) {
            this.getConfig().remove(k);
        }
    }

    public String getMode() {
        String m = ClobElement.nullSafeGet(this.getConfig().get(MODE));
        return MODE_DOWNLOAD.equals(m) ? MODE_DOWNLOAD : MODE_CHECK_DOWNLOADS;
    }

    public void setMode(String mode) {
        this.getConfig().put(MODE, new ClobElement(mode));
    }

    public boolean isGetFirstDelta() {
        return "true".equals(ClobElement.nullSafeGet(this.getConfig().get(GET_FIRST_DELTA)));
    }

    public void setGetFirstDelta(boolean getFirstDelta) {
        this.getConfig().put(GET_FIRST_DELTA, new ClobElement(getFirstDelta ? "true" : "false"));
    }

    public String getStartDeltaId() {
        return ClobElement.nullSafeGet(this.getConfig().get(START_DELTA_ID));
    }

    public void setStartDeltaId(String deltaId) {
        if (deltaId == null) {
            this.getConfig().put(START_DELTA_ID, null);
        } else {
            this.getConfig().put(START_DELTA_ID, new ClobElement(deltaId));
        }
    }

    public boolean isSSLValidationEnabled() {
        return "true".equals(ClobElement.nullSafeGet(this.getConfig().get(SSL_VALIDATION)));
    }

    public void setSSLValidationEnabled(boolean b) {
        this.getConfig().put(SSL_VALIDATION, new ClobElement(b ? "true" : "false"));
    }
}
