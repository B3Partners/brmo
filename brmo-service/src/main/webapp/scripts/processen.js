/*
 * formulieren voor de automatische processen.
 */
// iteratie variabelen, worden eventueel aangepast door de processen iteraties
var nextBrk = 0, nextBag = 0, nextMail = 0;
function addBRKScanner() {
    var formHTML = '<form><fieldset><label>Scan directory' +
            '<input name="brkProcessen[' + nextBrk + '].scanDirectory" value="" class="longTxt"></label><br>' +
            '<label>Archief directory<input name="brkProcessen[' + nextBrk + '].archiefDirectory" class="longTxt"></label>' +
            ' </fieldset><input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('brkScannerAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function addBAGScanner() {
    var formHTML = '<form><fieldset><label>Scan directory<input name="bagProcessen[' +
            nextBag + '].scanDirectory" value="" class="longTxt"></label><br>' +
            '<label>Archief directory<input name="bagProcessen[' +
            nextBag + '].archiefDirectory" class="longTxt"></label></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('bagScannerAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}

function addMailRapportage() {
    var formHTML = '<form><fieldset><label>Geaddresseerde(n)<input name = "mailProcessen[' +
            nextMail + '].mailAdressen" value="" class="longTxt"></label>' +
            '<label>Proces ID\'s voor rapportage<input name="mailProcessen[' +
            nextMail + '].config.pIDS" value="" class="longTxt"></label></fieldset>' +
            '<input name="addNew" value="Opslaan" type="submit"/><input value="Annuleren" type="reset"/></form>';
    var btn = document.getElementById('mailRapportAdd');
    btn.insertAdjacentHTML('afterend', formHTML);
}