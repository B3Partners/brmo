# Geplande taken configuratie

De BRMO service gebruikt de [Quartz Scheduler](http://quartz-scheduler.org/) om taken te plannen.

Op dit moment kunnen cron expressies worden gebruikt om taken te plannen.

## Een aantal voorbeelden:

|schema		|expressie |
|---|---|
|iedere 5 minuten	|`0 0/5 * * * ?` |
|ieder uur			|`0 0 0/1 * * ?` |


Een handige tool om cron expressie te maken is te vinden op [cronmaker.com](http://www.cronmaker.com/)
