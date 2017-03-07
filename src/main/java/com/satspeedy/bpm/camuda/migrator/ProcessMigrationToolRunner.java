package com.satspeedy.bpm.camuda.migrator;

import com.satspeedy.bpm.camuda.migrator.service.ProcessInstanceMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Process Migration Tool Runner.
 */
@Component
public class ProcessMigrationToolRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessMigrationToolRunner.class);

    private static final String COMMAND_MIGRATE = "migrate";
    private static final String COMMAND_INFO = "info";
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_EXIT = "exit";

    @Autowired
    private ProcessInstanceMigrationService processInstanceMigrationService;

    /**
     * Run method.
     * Differentiate between direct migration (pass arg 'migrate') and command line tool.
     *
     * @param args list of typed arguments
     */
    @Override
    public void run(String... args) throws Exception {
        boolean migrateDirectly = false;

        for (String arg : args) {
            LOG.info(arg);
            if (COMMAND_MIGRATE.equals(arg)) {
                migrateDirectly = true;
                break;
            }
        }

        if (migrateDirectly) {
            migrate();
        } else {
            runOnCommandLine();
        }

    }

    private void runOnCommandLine() {
        try (Scanner scanner = new Scanner(System.in)) {
            String text;
            do {
                LOG.info(String.format("%nType %s, %s, %s or %s as a command:", COMMAND_MIGRATE, COMMAND_INFO, COMMAND_HELP, COMMAND_EXIT));
                text = scanner.nextLine();
                text = performCommand(text);
            } while (!text.equals(COMMAND_EXIT));
            LOG.info("Closing the application!");
        }
    }

    private String performCommand(String text) {
        switch (text) {
            case COMMAND_MIGRATE:
                migrate();
                break;
            case COMMAND_INFO:
                LOG.info(String.format("Which kind of info do you need?", text));
                break;
            case COMMAND_HELP:
                break;
            case COMMAND_EXIT:
                break;
            default:
                LOG.error("Command does not exist!");
                break;
        }
        return text;
    }

    /**
     * Run the process instance migration.
     */
    public void migrate() {
        processInstanceMigrationService.migrate();
    }

}
