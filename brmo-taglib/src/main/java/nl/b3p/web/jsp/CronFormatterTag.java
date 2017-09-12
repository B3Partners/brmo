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

import java.util.Locale;
import net.redhogs.cronparser.CronExpressionDescriptor;
import java.text.ParseException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import net.redhogs.cronparser.Options;

/**
 * Formatter voor cron expressies. The default locale for this tag is
 * {@code nl}.
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
        final Options options = new Options();
        options.setVerbose(true);
        options.setTwentyFourHourTime(true);
        if (cronExpression != null && !cronExpression.isEmpty()) {
            try {
                if (locale != null) {
                    formatted = CronExpressionDescriptor.getDescription(cronExpression, options, locale);
                } else {
                    formatted = CronExpressionDescriptor.getDescription(cronExpression, options);
                }
            } catch (ParseException e) {
                formatted = e.getLocalizedMessage();
            }
        }
        return formatted;
    }

}
