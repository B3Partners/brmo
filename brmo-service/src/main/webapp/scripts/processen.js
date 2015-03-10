/*
 * formulieren voor de automatische processen.
 */
// iteratie variabelen, worden eventueel aangepast door de processen iteraties
var nextBrk = 0, nextBag = 0, nextMail = 0, nextGDS = 0, nextBRMO = 0;

function addBRKScanner() {
    var formHTML = '<form><fieldset>' +
            '<label>Label' +
            '<input name="brkProcessen[' + nextBrk + '].config.label" value="" class="halfTxt" />' +
            '</label><br><label>Scan directory' +
            '<input name="brkProcessen[' + nextBrk + '].scanDirectory" value="" class="longTxt">' +
            '</label><br><label>Archief directory' +
            '<input name="brkProcessen[' + nextBrk + '].archiefDirectory" class="longTxt">' +
            '</label><br/><label>Planning (cron expressie)' +
            '<input name="brkProcessen[' + nextBrk + '].cron_expressie" value="" class="halfTxt" />' +
            '</label><br/></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('brkScannerAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function addBAGScanner() {
    var formHTML = '<form><fieldset>' +
            '<label>Label' +
            '<input name="bagProcessen[' + nextBag + '].config.label" value="" class="halfTxt" />' +
            '</label><br><label>Scan directory' +
            '<input name="bagProcessen[' + nextBag + '].scanDirectory" value="" class="longTxt">' +
            '</label><br><label>Archief directory' +
            '<input name="bagProcessen[' + nextBag + '].archiefDirectory" class="longTxt">' +
            '</label><br/><label>Planning (cron expressie)' +
            '<input name="bagProcessen[' + nextBag + '].cron_expressie" value="" class="halfTxt" />' +
            '</label><br/></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('bagScannerAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function addMailRapportage() {
    var formHTML = '<form><fieldset>' +
            '<label>Label' +
            '<input name="mailProcessen[' + nextMail + '].config.label" value="" class="halfTxt" />' +
            '</label><br><label>Geaddresseerde(n)' +
            '<input name = "mailProcessen[' + nextMail + '].mailAdressen" value="" class="longTxt">' +
            '</label><label>Proces ID\'s voor rapportage' +
            '<input name="mailProcessen[' + nextMail + '].config.pIDS" value="" class="longTxt">' +
            '</label><br/><label>Planning (cron expressie)' +
            '<input name="mailProcessen[' + nextMail + '].cron_expressie" value="" class="halfTxt" />' +
            '</label><br/></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('mailRapportAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function addGDS2Ophalen() {
    var formHTML = '<form><fieldset>' +
            '<label>Label' +
            '<input name="gds2Processen[' + nextGDS + '].config.label" value="" class="halfTxt" />' +
            '</label><br><label>Afleveringsendpoint' +
            '<input name="gds2Processen[' + nextGDS + '].config.delivery_endpoint" value="" class="longTxt" />' +
            '</label><label>Contractnummer' +
            '<input name="gds2Processen[' + nextGDS + '].config.gds2_contractnummer" value="" size="20" />' +
            '</label><br/><label>Pad naar keystore:' +
            '<input name="gds2Processen[' + nextGDS + '].config.keystore_path" value="" size="80" />' +
            '</label><label>Wachtwoord' +
            '<input name="gds2Processen[' + nextGDS + '].config.keystore_password" value="" type="password" size="20" />' +
            '</label><br/><label>Planning (cron expressie)' +
            '<input name="gds2Processen[' + nextGDS + '].cron_expressie" value="" class="halfTxt" />' +
            '</label><br/></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit" /><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('gds2OphalenAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function transformatieAdd() {
    var formHTML = '<form><fieldset>' +
            '<label>Label' +
            '<input name="brmoProcessen[' + nextBRMO + '].config.label" value="" class="halfTxt" />' +
            '</label><br/><label>Planning (cron expressie)' +
            '<input name="brmoProcessen[' + nextBRMO + '].cron_expressie" value="" class="halfTxt" />' +
            '</label></fieldset>' +
            '<input name="brmoProcessen[' + nextBRMO + '].config.transformAll" value="true" type="hidden" />'+
            '<input name="addNew" value="Opslaan" type="submit" /><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('transformatieAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}