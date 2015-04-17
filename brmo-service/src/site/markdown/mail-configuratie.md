# Mail configuratie

Er wordt een JNDI bron voor de mail configuratie gebruikt.

In de `context.xml` voor de webapplicatie is de volgende global resource opgenomen:

```xml
<ResourceLink global="mail/session"
              name="mail/session"
              type="javax.mail.Session"/>
```

Voor een Tomcat server dient deze resource in de `server.xml` te worden aangemaakt, bijvoobeeld:

```xml
<Resource name="mail/session"
               auth="Container"
               type="javax.mail.Session"
               mail.smtp.host="mail.b3partners.nl"
               mail.smtp.from="brmo-no-reply@b3partners.nl"
/>
```
Uitgangspunt hierbij is dat de server vrij SMTP kan gebruiken. Daarnaast de juiste mail
library ([bijvoorbeeld javax.mail-1.5.2.jar](http://search.maven.org/#artifactdetails%7Cjavax.mail%7Cjavax.mail-api%7C1.5.2%7Cjar))
aan Tomcat toevoegen in de `lib` directory en de server herstarten.

Jobs kunnen in de BRMO dashboard worden gemaakt.
