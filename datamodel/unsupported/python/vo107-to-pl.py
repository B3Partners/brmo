#!/usr/bin/python
# coding=utf-8
import os, sys, re, logging, csv, datetime
from collections import defaultdict

logging.basicConfig(
    # level=logging.DEBUG,
    level=logging.INFO,
    filename='vo107-to-pl.log',
    format='%(asctime)-15s %(levelname)-6s %(message)s'
)

vo107key = re.compile(u"[B][0-9]{6}")
# waarde "Iwoord woord woord "
vo107value = re.compile(u"[I]{1}(?:[\w]+\s{1})*")
# gemeente code
vo107gemcode = re.compile(u"[\s][0-9]{4}[\s]")
# afkorting code ergens op de regel
vo107afkcode = re.compile(u"[\s][A-Z]{1}[\s]{3}")

# WPL tabel uit de bag, maar dan andersom (gemeentecode:naam:code)
_bagWoonplaatsen = {}

'''zoekt BAG woonplaats code op in het csv bestand'''
def lookupWPL(gemcode, plaats):
    logging.debug("opzoeken gemeente: " + gemcode + ", plaatsnaam: " + plaats)
    try:
        return _bagWoonplaatsen[gemcode][plaats]
    except KeyError:
        return ""

'''verwerkt een peroon blok uit vo107'''
def parse_block(lines, outFile):
    logging.debug('verwerken BLOK\n' + ''.join(lines))
    pl = defaultdict(dict)
    gemcode = ""
    for line in lines:
        logging.debug("verwerken regel: " + line)
        key = vo107key.search(line).group()
        categorieNummer = key[1:3]
        rubriekNummer = key[3:]
        waarde = vo107value.search(line).group()[1:].strip()
        # gemeente code
        if rubriekNummer == '0910':
            code = vo107gemcode.search(line).group().strip().lstrip('0')
            gemcode = code
            waarde = waarde + '#' + code
        if rubriekNummer in {'0410', '1010'}:
            code = vo107afkcode.search(line).group().strip()
            waarde = waarde + '#' + code
        if rubriekNummer in {'1170'}:
            code = lookupWPL(gemcode, waarde)
            waarde = waarde + '#' + str(code)

        logging.debug(
            "verwerken key: " + key
            + ", categorieNummer: " + categorieNummer
            + " rubriekNummer: "
            + rubriekNummer
            + ", waarde: " + waarde
        )
        pl[categorieNummer][rubriekNummer] = waarde

    # print pl

    outFile.write('<persoon>')
    outFile.write('<categorieen>')

    for cat in sorted(pl.iterkeys()):

        outFile.write('<categorie>')
        outFile.write('<nummer>')
        outFile.write(cat)
        outFile.write('</nummer>')
        outFile.write('<rubrieken>')

        data = pl[cat]

        for rubr, waarde in data.items():
            outFile.write('<rubriek>')
            outFile.write('<nummer>')
            outFile.write(rubr)
            outFile.write('</nummer>')
            # naam is omschrijving van de code, bijv 1020==Gemeentedeel
            # outFile.write('<naam />')
            outFile.write('<waarde>')
            # TODO mogelijk moet waarde nog escaped worden
            if rubr in {'0410', '1010', '0910', '1170'}:
                waarde, code = waarde.split('#')
                # voorloop 0 gemeentecode verwijderen
                outFile.write(code)
            else:
                outFile.write(waarde)
            outFile.write('</waarde>')
            outFile.write('<omschrijving>')
            outFile.write(waarde)
            outFile.write('</omschrijving>')
            outFile.write('</rubriek>')

        outFile.write('</rubrieken>')
        outFile.write('</categorie>')

    outFile.write('</categorieen>')
    outFile.write('</persoon>\n')


def parse_file(inFile, outFile):
    lines = []
    startedPL = False

    f = open(inFile, 'r')
    line = f.readline()
    while line:
        if (not startedPL and line.startswith('A')):
            # start PL blok
            logging.info('begin PL voor: ' + line.strip())
            #DEBUG outFile.write('<!-- begin PL: ' + line.strip() + ' -->\n')
            startedPL = True
            lines = []
            line = f.readline()

        if (startedPL and (line.startswith('A'))):
            # einde PL blok is begin volgende PL blok
            logging.debug('einde/begin PL ' + line)
            startedPL = False
            parse_block(lines, outFile)
            continue

        logging.debug('toevoegen regel: ' + line)
        lines.append(line)
        line = f.readline()

    # nog 1 keer aan het eind van de file block parsen (want er is geen eind regel in vo107)
    parse_block(lines, outFile)
    f.close()


def main(*args, **kwargs):
    if len(sys.argv) < 2:
        logging.error("Argument ontbreekt.")
        exit(-1)
    inputFileName = sys.argv[1]
    if not os.path.exists(inputFileName):
        logging.error("Bestand '%(fn)s' bestaat niet" % {'fn': inputFileName})
        exit(-1)


    logging.info("Laden woonplaatsentabel")
    with open("plaatsnamen.csv", 'r') as data_file:
        data = csv.DictReader(data_file, delimiter=",")
        for row in data:
            item = _bagWoonplaatsen.get(row["GEMCODE"], dict())
            item[row["NAAM"]] = int(row["PLAATSCODE"])
            _bagWoonplaatsen[row["GEMCODE"]] = item

    logging.info("Begin verwerken bestand '%(fn)s'." % {'fn': inputFileName})
    _outFile = open(inputFileName + ".xml", 'w')

    _outFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    _outFile.write('<persoonlijsten>\n<metadata>\n')
    _outFile.write('  <desc>vo107 naar PL conversie</desc>\n')
    _outFile.write('  <bronbestand>' + inputFileName + '</bronbestand>\n')
    _outFile.write('  <datum>' + str(datetime.datetime.now()) + '</datum>\n')
    _outFile.write('</metadata>\n')
    _outFile.write('<resultaten>\n')

    parse_file(inputFileName, _outFile)

    _outFile.write('</resultaten>\n</persoonlijsten>')
    _outFile.close()

    logging.info("Verwerken bestand '%(fn)s' is afgerond." % {'fn': inputFileName})


if __name__ == "__main__": main()
