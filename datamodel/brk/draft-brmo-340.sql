/* 
 * Zakelijke rechten die bestemd zijn tot een mandeligheid koppelen per subject
 */

select 
		r2.identificatie as mandeligheid,
		/*
		Gebruik het zakelijkrechtidentificatie wanneer er geen 'heefthoofdzaak' attribuut
		beschikbaar is voor het zakelijkrecht
		*/
		r.identificatie as zakelijkrecht_bestemdtot,
		r3.identificatie as zakelijkrecht_heefthoofzaak,
		vuzrok.identificatie as zakelijkrecht_vuzrok,
		case 
			when vuzrok.identificatie is not null then vuzrok.identificatie
			else r.identificatie 
		end as zr_identif,
		r4.tennamevan as tennamevan1, 
		/*
		Wanneer het mandeligheid geen hoofzaak heeft, gebruik dan de r.rustop waarde
		voor het kadastraalobject
		*/
		r.rustop as kadastraalobject_isbestemdtot, 
		r3.rustop as kadastraalobject_heefthoofzaak,
		vuzrok.rustop_zak_recht as kadastraalobject_vuzrok,
		case 
			when vuzrok.rustop_zak_recht is not null then vuzrok.rustop_zak_recht
			else r.rustop
		end as koz_identif
from brk.recht r
join brk.recht r2 on r.isbestemdtot = r2.identificatie 
left join brk.recht r3 on r2.heefthoofdzaak = r3.rustop
left join brk.vb_util_zk_recht_op_koz vuzrok on r3.rustop = vuzrok.rustop_zak_recht
-- pak de tenaamstelling
left join brk.recht r4 on vuzrok.identificatie  = r4.van
order by vuzrok.identificatie;
--where r4.tennamevan is null;

/*
 * Mandelige zaken die geen subject hebben: 
 * 'where r4.tennamevan is null'
 * mandeligheid                    |zakelijkrecht_bestemdtot         |zakelijkrecht_heefthoofzaak|zakelijkrecht_vuzrok|zr_identif                       |tennamevan|kadastraalobject_isbestemdtot           |kadastraalobject_heefthoofzaak|kadastraalobject_vuzrok|koz_identif                             |
--------------------------------+---------------------------------+---------------------------+--------------------+---------------------------------+----------+----------------------------------------+------------------------------+-----------------------+----------------------------------------+
NL.IMKAD.Mandeligheid:1000000043|NL.IMKAD.ZakelijkRecht:90265326  |                           |                    |NL.IMKAD.ZakelijkRecht:90265326  |          |NL.IMKAD.KadastraalObject:21780774070000|                              |                       |NL.IMKAD.KadastraalObject:21780774070000|
NL.IMKAD.Mandeligheid:1000000043|NL.IMKAD.ZakelijkRecht:90264551  |                           |                    |NL.IMKAD.ZakelijkRecht:90264551  |          |NL.IMKAD.KadastraalObject:21780758270000|                              |                       |NL.IMKAD.KadastraalObject:21780758270000|
NL.IMKAD.Mandeligheid:90000090  |NL.IMKAD.ZakelijkRecht:90264520  |                           |                    |NL.IMKAD.ZakelijkRecht:90264520  |          |NL.IMKAD.KadastraalObject:21800350270000|                              |                       |NL.IMKAD.KadastraalObject:21800350270000|
NL.IMKAD.Mandeligheid:90000090  |NL.IMKAD.ZakelijkRecht:90264508  |                           |                    |NL.IMKAD.ZakelijkRecht:90264508  |          |NL.IMKAD.KadastraalObject:21800349070000|                              |                       |NL.IMKAD.KadastraalObject:21800349070000|
NL.IMKAD.Mandeligheid:90000085  |NL.IMKAD.ZakelijkRecht:90264317  |                           |                    |NL.IMKAD.ZakelijkRecht:90264317  |          |NL.IMKAD.KadastraalObject:21780754670000|                              |                       |NL.IMKAD.KadastraalObject:21780754670000|
NL.IMKAD.Mandeligheid:90000091  |NL.IMKAD.ZakelijkRecht:90261989  |                           |                    |NL.IMKAD.ZakelijkRecht:90261989  |          |NL.IMKAD.KadastraalObject:21820375370000|                              |                       |NL.IMKAD.KadastraalObject:21820375370000|
NL.IMKAD.Mandeligheid:90000086  |NL.IMKAD.ZakelijkRecht:90261927  |                           |                    |NL.IMKAD.ZakelijkRecht:90261927  |          |NL.IMKAD.KadastraalObject:21790764770000|                              |                       |NL.IMKAD.KadastraalObject:21790764770000|
NL.IMKAD.Mandeligheid:10001434  |NL.IMKAD.ZakelijkRecht:10601254  |                           |                    |NL.IMKAD.ZakelijkRecht:10601254  |          |NL.IMKAD.KadastraalObject:19800500970000|                              |                       |NL.IMKAD.KadastraalObject:19800500970000|
NL.IMKAD.Mandeligheid:10001431  |NL.IMKAD.ZakelijkRecht:10601152  |                           |                    |NL.IMKAD.ZakelijkRecht:10601152  |          |NL.IMKAD.KadastraalObject:19800492770000|                              |                       |NL.IMKAD.KadastraalObject:19800492770000|
NL.IMKAD.Mandeligheid:10001430  |NL.IMKAD.ZakelijkRecht:10600825  |                           |                    |NL.IMKAD.ZakelijkRecht:10600825  |          |NL.IMKAD.KadastraalObject:19800470370000|                              |                       |NL.IMKAD.KadastraalObject:19800470370000|
NL.IMKAD.Mandeligheid:10001429  |NL.IMKAD.ZakelijkRecht:10600739  |                           |                    |NL.IMKAD.ZakelijkRecht:10600739  |          |NL.IMKAD.KadastraalObject:19800467570000|                              |                       |NL.IMKAD.KadastraalObject:19800467570000|
NL.IMKAD.Mandeligheid:10001428  |NL.IMKAD.ZakelijkRecht:10600032  |                           |                    |NL.IMKAD.ZakelijkRecht:10600032  |          |NL.IMKAD.KadastraalObject:19800462070000|                              |                       |NL.IMKAD.KadastraalObject:19800462070000|
NL.IMKAD.Mandeligheid:10001442  |NL.IMKAD.ZakelijkRecht:10599802  |                           |                    |NL.IMKAD.ZakelijkRecht:10599802  |          |NL.IMKAD.KadastraalObject:19870083770000|                              |                       |NL.IMKAD.KadastraalObject:19870083770000|
NL.IMKAD.Mandeligheid:1000015865|NL.IMKAD.ZakelijkRecht:1002092905|                           |                    |NL.IMKAD.ZakelijkRecht:1002092905|          |NL.IMKAD.KadastraalObject:21140075870000|                              |                       |NL.IMKAD.KadastraalObject:21140075870000|
NL.IMKAD.Mandeligheid:1000015865|NL.IMKAD.ZakelijkRecht:1002092649|                           |                    |NL.IMKAD.ZakelijkRecht:1002092649|          |NL.IMKAD.KadastraalObject:21140074970000|                              |                       |NL.IMKAD.KadastraalObject:21140074970000|
NL.IMKAD.Mandeligheid:1000007585|NL.IMKAD.ZakelijkRecht:1001102477|                           |                    |NL.IMKAD.ZakelijkRecht:1001102477|          |NL.IMKAD.KadastraalObject:19890117870000|                              |                       |NL.IMKAD.KadastraalObject:19890117870000|
NL.IMKAD.Mandeligheid:1000000043|NL.IMKAD.ZakelijkRecht:1001051347|                           |                    |NL.IMKAD.ZakelijkRecht:1001051347|          |NL.IMKAD.KadastraalObject:21780780570000|                              |                       |NL.IMKAD.KadastraalObject:21780780570000|
