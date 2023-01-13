/*
 * Copyright (C) 2016 B3Partners B.V.
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
package nl.b3p.web.jsp;

import static com.cronutils.model.CronType.QUARTZ;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Formatter voor cron expressies. The default locale for this tag is {@code nl}.
 *
 * @author mprins
 */
public class CronFormatterTag extends SimpleTagSupport {

    private String cronExpression;
    private Locale locale;

    public CronFormatterTag() {
        setLocale("nl");
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setLocale(String locale) {
        try {
            this.locale = new Locale(locale);
        } catch (NullPointerException npe) {
            this.locale = null;
        }
    }

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().write(buildOutput());
    }

    public String buildOutput() {
        String formatted = "";
        if (cronExpression != null && !cronExpression.isEmpty()) {
            try {
                CronParser cronParser =
                        new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
                if (locale != null) {
                    formatted =
                            CronDescriptor.instance(locale)
                                    .describe(cronParser.parse(cronExpression));
                } else {
                    formatted =
                            CronDescriptor.instance(Locale.getDefault())
                                    .describe(cronParser.parse(cronExpression));
                }
            } catch (Exception e) {
                formatted = e.getLocalizedMessage();
            }
        }
        return formatted;
    }
}
