<%--
  ~ Copyright (C) 2021 B3Partners B.V.
  ~
  ~ SPDX-License-Identifier: MIT
  ~
  --%>
<%@include file="/WEB-INF/taglibs.jsp" %>
<script>
    function updateFormVisibility() {
        const selectedMode = document.querySelector('select[name="config[\'mode\']"]').value;
        document.querySelectorAll('[data-mode]').forEach(e => {
            const visible = e.dataset.mode.split(',').includes(selectedMode);
            e.classList.toggle('visible', visible);
            e.querySelectorAll('input').forEach(input => {
                // Don't clear default URL value when saving a mode which disables that input, so it isn't empty
                // switching back to default mode later
                if (input.name != 'config[\'url\']') {
                    // Disable hidden fields so for example the password does not remain in the saved config when the
                    // mode is changed to one which doesn't need the password
                    input.disabled = !visible;
                }
            });
        });
    }

    window.addEventListener('load', e => updateFormVisibility());
</script>
<style>
    tr[data-mode].visible {
        display: table-row !important;
    }

    tr[data-mode] {
        display: none;
    }
</style>
<table style="width: 100%">
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Modus</stripes:label></td>
        <td>
            <stripes:select name="config['mode']" onchange="updateFormVisibility()">
                <stripes:option value="applyFromMirror">Landelijke dagmutaties verwerken van publieke mirror (aanbevolen)</stripes:option>
                <stripes:option value="apply">Mutaties verwerken van BAG Bestanden service van het Kadaster (abonnement vereist)</stripes:option>
                <stripes:option value="download">Mutaties downloaden van BAG Bestanden service naar directory (abonnement vereist)</stripes:option>
                <stripes:option value="load">Mutaties verwerken vanuit directory</stripes:option>
            </stripes:select>
        </td>
    </tr>
    <tr data-mode="applyFromMirror">
        <td colspan="2">
            <i>In deze modus worden de landelijke dagmutaties gedownload en verwerkt vanaf een publieke mirror zonder authenticatie.
            Geen abonnement op mutaties bij het Kadaster vereist.</i>
        </td>
    </tr>
    <tr data-mode="applyFromMirror">
        <td style="vertical-align: top; white-space: nowrap"><stripes:label name="">URL</stripes:label></td>
        <td>
            <stripes:text name="config['url']" style="width: 43em;"/><br>
            <i>Geef de URL van de publieke mirror op. Standaard is de <a href="https://bag.b3p.nl/dagmutaties/bestanden.json" target="_blank">publieke mirror van B3Partners</a>
            ingevuld.</i>
        </td>
    </tr>
    <tr data-mode="apply">
        <td colspan="2">
            <i>In deze modus worden de mutaties gedownload en verwerkt vanaf de BAG Bestanden service van het Kadaster.
            Vul hieronder de gebruikersnaam en wachtwoord van het account in dat toegang heeft tot "BAG Bestanden" via
            "Mijn Kadaster".</i>
        </td>
    </tr>
    <tr data-mode="apply,download">
        <td style="white-space: nowrap"><stripes:label name="">BAG Bestanden gebruikersnaam</stripes:label></td>
        <td><stripes:text name="config['kadaster-username']"/></td>
    </tr>
    <tr data-mode="apply,download">
        <td><stripes:label name="">BAG Bestanden wachtwoord</stripes:label></td>
        <td><stripes:password name="config['kadaster-password']" value="${actionBean.config['kadaster-password']}"/></td>
    </tr>
    <tr data-mode="apply,download">
        <td style="vertical-align: top"><stripes:label name="">Query parameter voor bestandenlijst</stripes:label></td>
        <td><stripes:text name="config['query']" style="width: 43em;"/><br>
            <i>Laat leeg om de landelijke dagmutaties op te halen. Om maandelijkse gemeentemutaties op te halen
            dient hier wat ingevuld te worden, zie <a href="https://github.com/B3Partners/brmo/tree/master/bag2-loader#met-een-mijn-kadaster-account">hier</a> voor meer informatie.</i>
        </td>
    </tr>
    <tr data-mode="download">
        <td style="vertical-align: top"><stripes:label name="">Directory om mutatiebestanden naar te downloaden</stripes:label></td>
        <td><stripes:text name="config['directory']" style="width: 43em;"/><br>
            <i>De directory waarnaar alle beschikbare mutatiebestanden naar moeten worden gedownload. Ze worden niet
            verwerkt in de database. Deze kunnen via een share gedeeld worden met een andere brmo-service instantie met
            een proces om bestanden uit de share te verwerken.</i>
        </td>
    </tr>
    <tr data-mode="load">
        <td style="vertical-align: top"><stripes:label name="">Directory om mutatiebestanden uit te verwerken</stripes:label></td>
        <td><stripes:text name="config['directory']" style="width: 43em;"/><br>
            <i>De directory waarnaar alle beschikbare mutatiebestanden naar zijn gedownload. Deze worden geanalyseerd en
            mutatiebestanden die aansluiten op de huidige technische stand van de database worden verwerkt. Let bij
            gebruik van een share op het goed instellen van de rechten.</i>
        </td>
    </tr>

    <tr>
        <td style="vertical-align: top"><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron
            expressie)</a></stripes:label>
        </td>
        <td>
            <stripes:text name="proces.cronExpressie"/><br>
            Geadviseerd wordt dagelijks om 2:30 met de expressie <code>0 30 2 * * ? *</code>.<br>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}"/>
        </td>
    </tr>

    <script>
        console.log('test');
    </script>
</table>


