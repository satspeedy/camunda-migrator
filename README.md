# Camunda-Migrator
Camunda-Migrator dient zum Administrieren der Process Engine von außerhalb des Applikationsservers. 
Hierfür ist kein laufender Applikationsserver erforderlich, da es eine Spring Boot Applikation ist.

Das Tool kann in den CI Flow für alle Umgebungen eingebunden werden und wird dann währenddessen ausgeführt. 
Der Funktionsumfang umfasst:
* die Prozessmigration und
* das setzen von Prozessvariablen. 

Weiter unten sind die Funktionen detaillierter beschrieben. 
Es kann als Maven-Projektsubmodul in das entsprechende Projekt eingebunden werden.

Während der Ausführung werden die Informationen im zentralen Log-System (z.B. Graylog) ausgeloggt, 
lokal wird es auf der Konsole ausgegeben.

Der Tooleinsatz ist unter `How to use it?` dokumentiert.

## Process Migration with Camunda-Migrator

### Introduction
Um fortlaufend einen aktuellen Prozesszustand mit allen neuen Features zu haben, 
werden alle vorhergehenden Prozessversionen auf die neueste Prozessversion migriert.
Als Ausgangsbasis wird PROD bzw. PREPROD verwendet, auf die die neuen Änderungen iterativ hinzukommen.
Aus Gründen der Administrierbarkeit, wird die Prozessmigration weitestgehend für alle Umgebungen automatisiert erfolgen.
Die primäre Herausforderung für die Migration sind die langlaufenden und sich ständig ändernden Prozesse. 
Diese Restriktionen werden im `Camunda-Migrator` Tool berücksichtigt und einheitlich behandelt.
Der grobe Ablauf ist in nachfolgenden  Abbildung visualisiert und wird im weiteren Verlauf im Detail erörtert.

![Process Migration Process](/docs/processmigration.png "Process Migration Process")

### FAQ
* Wann muss eine Prozessmigration erstellt werden? 
    * Für jede Änderung am Prozessmodell (*.bpmn-Dateien) und bei langlaufenden Prozessen
* Muss ich für einen neuen Prozess eine Migration erstellen?  
    * Nein, eine Migration ist initial nicht erforderlich. Es muss aber das versionTag im Model gesetzt werden
* Wie & Wo wird die Migration getestet?  
    * Auf allen Umgebung sollte das Tool nach erfolgreichem Master Build automatisch ausgeführt werden
* Wird die Migration in DEV, TEST, UAT für existierende Prozesse ausgeführt? 
    * Ja, zum Testen sollten dort ebenfalls vorhandene Prozesse migriert. Sollte aber keine fachliche Anforderung sein.
* Von welcher Umgebung werden die Prozesse als Ausgangsbasis verwendet?  
    * Von der produktiven Umgebung.
* Welches Zip-Archiv & welcher Migrationsplan ist die initiale Auslieferung (Anfangszustand)? 
    * Release_1_0-Sprint_1-1.zip & Release_1_0-Sprint_1-1_TO_Release_2_0-Sprint_2-1.json
* Ist für ein Prozess Hotfix auch eine Migration erforderlich? 
    * Ja

### Rough Technical Steps
1. Ermittle Changelog Einträge und verarbeite diese in gegebener Reihenfolge
    1. Deploy das referenzierte Zip-Archiv im Changelog Eintrag 
        1. Condition: Wenn zu dem Version Tag noch kein Prozessmodell deployed ist oder mind. ein Prozessmodell sich verändert hat
2. Modifiziere anhand des referenzierten Modifikationsplans (modification_before.json) im Changelog Eintrag die nicht migrierbaren Instanzen 
    1. Vor der Modifizierung wird der Modifikationsplan validiert
3. Migriere anhand des referenzierten Migrationsplans (migration.json) im Changelog Eintrag 
    1. Vor der Migration wird der Migrationsplan nochmal gegen die Engine validiert
4. Lösche die für die Migration verwendeten "Hilfs"-Prozessvariablen
5. Setze anhand des referenzierten Modifikationsplans (modification_after.json) im Changelog Eintrag die neuen Prozessvariablen. Siehe Details auf der rechten Seite!

Die Migration läuft in einer Transaktion und primär nur synchron. 
Im Fehlerfall wird die komplette Migration abgebrochen.

### Aufbereitung der Prozessmigration

#### Version Tag zu Prozessmodellen hinzufügen
Um die Prozessmodelle eindeutig auf verschiedenen Stages auffinden/erkennen zu können, 
bietet Camunda die Möglichkeit einen Version Tag in einem Modell zu hinterlegen.
Die Idee ist, den Version Tag für die Migration von Prozessmodellen zu nutzen.

Bevor die Modelle editiert werden, muss sichergestellt sein, dass der initiale Zustand bereits im 
`Camunda-Migrator` für die spätere Migration als Zip-Archiv hinterlegt ist
* im Verzeichnis: src/main/resources/process/archive 
* Namenskonvention: [Aktueller Release]-[Aktueller Sprint]-[Fortlaufende Nummerierung für Änderungen]

Folgende Version Tag Namenskonvention kann z.B. genutzt werden:
    
    [Aktueller Release]-[Aktueller Sprint]-[Fortlaufende Nummer für Änderungen] wie z.B. Release_1_0-Sprint_1-1
     
1. Über den Camunda Modeler kann ein Version Tag zum Prozess editiert werden
    * Pool im Prozessmodell auswählen und in das Properties Feld Version Tag den Version Tag nach o.g. Konvention hinzufügen
        * ![Add VersionTag to Model](/docs/addVersionTagToModel.png "Add VersionTag to Model")
 
#### Veränderte Prozesse als Zip-Archiv zum Admin Tool hinzufügen
Um die Änderungen in einem Prozessmodell für die Migration vorzuhalten, 
werden die veränderten Prozessmodelle als Zip-Archiv zum `Camunda-Migrator` Tool hinzugefügt.

Folgende Zip Archiv Namenskonvention kann z.B. genutzt werden:

    [Aktueller Release]-[Aktueller Sprint]-[Fortlaufende Nummer für Änderungen].zip wie z.B. Release_1_0-Sprint_1-1.zip
    
1. Im Projekt mit den Modellen nach src/main/resources navigieren
2. Das Verzeichnis bpmn zu einem Zip-Archiv hinzufügen (Namenskonvention s.o.) und die nicht veränderten Prozessmodelle, 
    sowie die Datei ".gitattributes" aus dem Zip-Archiv entfernen!  
    1. Wichtig: Die Dateien müssen exakt gleich sein wie die im Repository hinterlegten Dateien, 
        sonst kommt es zu doppelten Deployments. Um automatische Umwandlungen zum Format der Zeilenenden zu Unterdrücken, 
        ist im bpmn Verzeichnis eine .gitattributes Datei hinterlegt, die automatische Konvertierungen 
        von Git für alle Inhalte darunter deaktiviert. Diese Lösung wurde gewählt, da Dateien in einer zip-Datei 
        ebenfalls nicht von git konvertiert werden.
3. Das Zip-Archiv nach `Camunda-Migrator` nach src/main/resources/process/archive hinzufügen

##### Beispiel

![Add VersionTag to Model Example](/docs/addVersionTagToModel-Example.png "Add VersionTag to Model Example")


#### Prozess Migrationsplan erstellen
**TODO**

#### Prozess Modifikationsplan erstellen
**TODO**

#### Prozess Modifikationsplan überarbeiten
**TODO**

#### Prozess Migrationsplan überarbeiten
**TODO**

#### Prozess Changelog erweitern
**TODO**

#### Prozess Migrationsplan testen
**TODO**

### Zu beachtende Aspekte während der Entwicklung
**TODO**

## Modifying Process Variables with Camunda-Migrator
In manchen Fällen erfordert eine Migration das modifizieren von Prozessvariablen.
Andernfalls kann sie Technisch migrierbar aber Fachlch eine nicht migrierbare Instanz sein.

* Das setzen von neu benötigten Prozessvariablen erfolgt als letzter Schritt der Prozessmigration
* Hierfür muss eine modification_after.json Datei erstellt werden 
    * Als Instruction Type wird "addVariable" verwendet
    
In diesem Schritt können auch Prozessvariablen in nicht migrierte Prozessinstanzen gesetzt werden, 
damit diese weiterhin noch kompatibel zu den aktuellen Haupt/Sub-Prozessen sind!

Die Datei kann z. B. wie folgt aussehen:
```json
{
  "modifications": [
    {
      "sourceProcessDefinitionKey": "My_Process_Definition_Key",
      "sourceProcessVersionTag": "Release_2_1-Sprint_2-3",
      "skipCustomListeners": true,
      "skipIoMappings": true,
      "instructions": [
        {
          "type": "addVariable",
          "activityId": "ServiceTask_Create_E_Invoice",
          "variables": {
            "sendPrintDocuments": {
              "type": "Boolean",
              "value": "True"
            }
          }
        }
      ]
    }
  ]
}
```

## How to use it?
This chapter describes the usage of the `Camunda-Migrator` to migrate processes. 
The Tool recognizes by itself, which process versions are already inserted in the database, and which are not.

In every release a new version of this tool will be delivered. 
How to define a process migration is described detailled in `Process migration`.

Generally the following procedure should be observed to prevent any issues:
1. Stop the running application server (e.g. WildFly)
2. Update the databases with the tool of your choice 
3. Update the processes with this Tool
4. Copy the new released application artifacts (e.g. war-files) to the application server
5. Start the application server

### CI Flow Integration
* The Tool will build with each build process and executed on every stage (e.g. DEV, TEST, UAT, PREPROD, PROD).
* Logging with a central logging system (e.g. GrayLog and additionally a local *.log file is created)
* It should be executed after the database update to prevent any problems in combination of camunda version update and process migration!

### Executing beyond CI Flow
* The tool is build with each project build
* Logging in console (additionally a local *.log file is created)

You can execute it with the `dev` profile for local usage within your development environment

    java -Dspring.profiles.active=dev -jar camunda-migrator.jar

or pass specific environment variables to execute it:

    java -DDB_HOST=192.168.50.10 -DDB_PORT=3306 -DDB_SCHEMA=ProcessEngine -DDB_USER=dev -DDB_PASS=dev -DLOG_HOST=udp:10.20.30.6 -DLOG_PORT=12211 -jar camunda-migrator.jar

### Necessary Environment Variables
The following environment variables are necessary to execute the tool. 
Otherwise an error occured for missing values and no defaults are defined. Primary they are used to connect to database and central logging system.

| Key           | Description                                       |
| ------------- |---------------------------------------------------|
| DB_HOST	    | Database Host (e.g. 192.168.50.10)                | 
| DB_PORT	    | Database Port (e.g. 3306)                         | 
| DB_SCHEMA	    | Database Schema (e.g. ProcessEngine)              | 
| DB_USER	    | Database User (e.g. dev)                          | 
| DB_PASS	    | Database User Password (e.g. dev)                 | 
| LOG_HOST	    | Central Logging System Host (e.g. udp:10.20.30.6) | 
| LOG_PORT	    | Central Logging System Port (e.g. 12211)          | 

### Log Results
If a the migration was successful the result output will contain the following line:

    11:31:25.014 [main] INFO  c.i.d.p.a.ProcessMigrationToolApplication - Migration successful!  
    
Otherwise the output will contain the following lines.

    11:57:00.112 [main] ERROR c.i.d.p.a.ProcessMigrationToolApplication - Migration error
    11:57:00.114 [main] INFO  c.i.d.p.a.ProcessMigrationToolApplication - Migration aborted! (Please see logging output)

## Requirements
Generally you need JDK 8+ and Maven 3.2.1+ for this project.    

## Environment Restrictions
Built and tested against Camunda BPM version 7.6.0.