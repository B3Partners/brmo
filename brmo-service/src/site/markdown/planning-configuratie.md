## Geplande taken configuratie

De BRMO service gebruikt de [Quartz Scheduler](http://quartz-scheduler.org/) om taken te plannen.
Quatz heeft uitgebreide doumentatie en een beknopte [cookbook](http://quartz-scheduler.org/documentation/quartz-2.x/cookbook/) site.


### Taak planning toevoegen

  - Maak een taak aan in het dashboard
  - gebruik de id uit het dashboard om de taak in de quartz.xml toe te voegen.    
    Voeg een `job` en een (of meer) `trigger` element toe, zoals hieronder voor een proces met id 50.
    
    ```xml
    <schedule>
        <job>
            <name>BRKScanner_50</name>
            <description>BRK scanner proces met ID 50</description>
            <job-class>nl.b3p.brmo.service.jobs.AutomatischProcesJob</job-class>
            <job-data-map>
                <entry>
                    <key>id</key>
                    <value>50</value>
                </entry>
            </job-data-map>
        </job>
        <trigger>
            <cron>
                <name>periodiek</name>
                <!-- de naam van de job hierboven -->
                <job-name>BRKScanner_50</job-name>
                <!-- iedere nacht om 4:30 -->
                <cron-expression>0 30 4 * * ?</cron-expression>
            </cron>
        </trigger>
    </schedule>
    ```

De planner pakt zelf de aanpassingen in het quartz.xml bestand op (dat kan een paar minuten duren).

Als alternatief kunnen er meerdere Quartz xml bestanden worden gebruikt, hiervoor 
dient de regel `org.quartz.plugin.jobInitializer.fileNames` in het bestand quartz.properties
te worden aangepast, tevens is na deze aanpassing een service herstart nodig.

zie ook: [How-To: Initializing Job Data With Scheduler Initialization](http://quartz-scheduler.org/documentation/quartz-2.x/cookbook/JobInitPlugin)
