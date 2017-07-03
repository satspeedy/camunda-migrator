# Camunda-Migrator
This tool helps you to have continuously a current process state with the latest features.

Therefor all previous process versions will be migrated to the newest process definition version.

It can be used to interact with the process engine from outside the application server and deliver information for a automatic process instance migration. 
No running application server is required for this because its set up as a _SpringBoot_ application.

For example, you can integrate it into your CI/CD system to run the migration on all existing environments during software delivery.
And also it can be integrated into your actual BPM project as a Maven project submodule, too.

During the execution, all the information can be logged out in a central logging system (e.g. PapterTrail or Graylog) and locally on the console.
The logging system can be configured as a Logback Appender via ![logback.xml](/src/main/resources/logback.xml). The default is _PaperTrail_.

The main functions are:
* `Process Instance Migration`
* `Modifying Process Variables`

The main functions are described in more detail below.

## How to run it?
This chapter describes the usage of this tool. It recognizes by itself, which process versions are already inserted in the database, and which are not.

Generally the following procedure should be observed to prevent any issues:
1. Stop the running application server (e.g. WildFly)
2. Update the processes with this Tool
3. Copy the new released application artifacts (e.g. war-files) to the application server
4. Start the application server

### CI Flow Integration
* The Tool will build with each build process and executed on every stage (e.g. DEV, TEST, INT, UAT, PROD).
* Logging with a central logging system (e.g., PaperTrail or GrayLog) and additionally on console
* It should be executed after a database update to prevent any problems in combination of camunda version update and process migration!

### Executing beyond CI Flow
* The tool is build with each project build
* Logging in console

You can execute it with the `dev` profile for local usage with your development environment.
When you're in the folder of the jar file you can type:

    java -Dspring.profiles.active=dev -jar camunda-migrator.jar migrate
    
Also, if you do not pass the `migrate` argument it will start as Command Line Interface:
    
    java -Dspring.profiles.active=dev -jar camunda-migrator.jar
    ...
    Type migrate, info, help or exit as a command:
    ...

Alternatively you can override the arguments to pass specific environment variables to execute it, too:

    java -DDB_HOST=192.168.188.101 -DDB_PORT=5432 -DDB_SCHEMA=process-engine -DDB_USER=dev -DDB_PASS=dev -DLOG_HOST=udp:192.168.188.101 -DLOG_PORT=12211 -jar camunda-migrator.jar migrate
    
### Necessary Environment Variables
The following environment variables are necessary to execute the tool.

Otherwise an error occured for missing values and no defaults are defined. 

Primary they are used to connect to database and central logging system.

| Key           | Description                                            |
| ------------- |--------------------------------------------------------|
| DB_HOST	    | Database Host (e.g. 192.168.188.101)                   | 
| DB_PORT	    | Database Port (e.g. 5432)                              | 
| DB_SCHEMA	    | Database Schema (e.g. process-engine)                  | 
| DB_USER	    | Database User (e.g. dev)                               | 
| DB_PASS	    | Database User Password (e.g. dev)                      | 
| LOG_HOST	    | Central Logging System Host (e.g. udp:192.168.188.101) | 
| LOG_PORT	    | Central Logging System Port (e.g. 12211)               | 

### Log Results
If a the migration was successful the result output will contain the following line:

    11:31:25.014 [main] INFO  c.i.d.p.a.ProcessMigrationToolApplication - Migration successful!  
    
Otherwise the output will contain the following lines.

    11:57:00.112 [main] ERROR c.i.d.p.a.ProcessMigrationToolApplication - Migration error
    11:57:00.114 [main] INFO  c.i.d.p.a.ProcessMigrationToolApplication - Migration aborted! (Please see logging output)

## Process Migration with Camunda-Migrator
The rough procedure is visualized in the following figure

![Process Migration Process](/docs/processmigration.png "Process Migration Process")

### Rough Technical Steps
1. Determine Changelog entries and process in the given order
    1. Deploy the referenced zip archive in the Changelog entry 
        1. Condition: If no process model has been deployed to the `Version Tag` or at least one process model has changed
2. Modify the non-migratable instances using the referenced modification plan (modification_before.json) in the changelog entry 
    1. The modification plan is validated before modification
3. Migrate using the referenced migration plan (migration.json) in the changelog entry 
    1. Before migration, the migration plan is validated against the engine
4. Delete the _so called helper_ process variables used for the migration
5. Use the referenced modification plan (modification_after.json) in the changelog entry to set the new process variables

The migration runs in a transaction and primarily only synchronously. 
In the an error occurs, the complete migration is aborted.

### Preparation of process migration

* Adding `Version Tag` to modified process models
    * Important: Before the models are edited, it must be ensured that the initial state is already stored in the `Camunda-Migrator` for future migration as zip archive
    * ![Add VersionTag to Model](/docs/addVersionTagToModel.png "Add VersionTag to Model")
* Add changed processes as zip archive to migration tool  
    * Important: The files must be exactly the same as the files stored in the repository, otherwise duplicate deployments will occur. In order to suppress automatic conversions to the format of the line ends, for _Git_ a .gitattributes file can be stored in the bpmn directory, which deactivates automatic conversions of Git for all contents under it.
    * Add zip archiv in `Camunda-Migrator` to _src/main/resources/process/archive_ or upload somewhere else and notice the url
    * ![Add VersionTag to Model Example](/docs/addVersionTagToModel-Example.png "Add VersionTag to Model Example")
* Create a process migration plan
* Create a process modification plan
* Extend the process modification plan with _ProcessDefinitionKey_ and _ProcessVersionTag_
* Extend the process migration plan with _ProcessDefinitionKey_ and _ProcessVersionTag_
* Extend the `process-changelg.json` with the new migration
* Test the process migration locally

## Modifying Process Variables with Camunda-Migrator
In some cases, a migration requires modifying process variables.
Otherwise, it can be technically migratable but from the business perpective it can be a non-migratable instance.

* The setting of newly required process variables takes place as the last step in the process migration
* To do this, a `modification_after.json` file must be created
    * The instruction type is `addVariable`
    
In this step, process variables can also be set in non-migrated process instances,
so that they are still compatible with the current main/sub processes!

The modification_after.json` file can look like the following snippet:
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

## Contributing

You are __more than welcome__ to take part on the development of this project!

Clone the repository, add, fix or improve and send us a pull request.
But please take care about the commit messages and have a look at this [wiki entry](https://github.com/erlang/otp/wiki/Writing-good-commit-messages).

You can submit issues in the [Issues](https://github.com/satspeedy/camunda-migrator/issues/).

## Help and support

* Have a look at the [blog entry](https://thecattlecrew.net/2017/07/03/published-an-automation-tool-for-process-instance-migration-camunda-migrator/) at [CattleCrew Blog](https://thecattlecrew.net/)
* Contact us via [e-mail](mailto:halil.hancioglu@opitz-consulting.com) 
* Visit our website [http://www.opitz-consulting.com/](http://www.opitz-consulting.com/) to get more detailed info about us

## License

Copyright (c) 2017 Halil Hancioglu
Licensed under the [MIT license](./LICENSE).

## Requirements
Generally you need JDK 8+ and Maven 3.2.1+ for this project.    

## Environment Restrictions
Built and tested against Camunda BPM version 7.7.0 with PostgreSQL, MySql and H2.
