/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.service.stripes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author meine
 */
public class ControleActionBean implements ActionBean {

    private ActionBeanContext context;

    private static final String JSP = "/WEB-INF/jsp/controle/afgiftelijst.jsp";

    private static final Log LOG = LogFactory.getLog(ControleActionBean.class);

    @Validate
    private FileBean file;

    // <editor-fold desc="Getters en setters" defaultstate="collapsed">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    // </editor-fold>
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(JSP);
    }

    public Resolution check() {
        BrmoFramework brmo = null;
        try {

            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            brmo = new BrmoFramework(dataSourceStaging, null);
            File temp = File.createTempFile("afgiftelijst", "report.pdf");
            temp.deleteOnExit();

            LOG.info("Afgiftelijst controleren.");
            File response = brmo.checkAfgiftelijst(file.getFileName(), file.getInputStream(), temp);
            LOG.info("Afgifte gecontroleerd:");
            brmo.closeBrmoFramework();

            final FileInputStream fis = new FileInputStream(response);
            try {
                StreamingResolution res = new StreamingResolution(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(response)) {
                    @Override
                    public void stream(HttpServletResponse response) throws Exception {
                        OutputStream out = response.getOutputStream();
                        IOUtils.copy(fis, out);
                        fis.close();
                    }
                };
                String extension = "pdf";
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdddHHmmss");
                String newName = "Afgiftelijst_rapport_"+sdf.format(new Date()) + extension;
                res.setFilename(newName);
                res.setAttachment(true);
                return res;
            } finally {
                response.delete();
            }
        } catch (IOException | BrmoException ex) {
            LOG.error("Error reading afgiftelijst: " + ex.getLocalizedMessage(), ex);
            context.getValidationErrors().addGlobalError(new SimpleError("Kan afgiftelijst niet verwerken: " + ex.getLocalizedMessage()));
        }finally{
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
        }

        return new ForwardResolution(JSP);
    }

}
