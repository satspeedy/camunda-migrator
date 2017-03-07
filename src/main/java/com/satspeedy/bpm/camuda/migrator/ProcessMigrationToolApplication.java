package com.satspeedy.bpm.camuda.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Process Migration Tool Application.
 */
@SpringBootApplication //NOCHECKSTYLE allow default constructor
public class ProcessMigrationToolApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessMigrationToolApplication.class);

    /**
     * Main method.
     *
     * @param args list
     */
    public static void main(String[] args) {
        try {
            final ConfigurableApplicationContext applicationContext = SpringApplication.run(ProcessMigrationToolApplication.class, args);
            applicationContext.close();
        } catch (Exception e) { //NOCHECKSTYLE allow central catch for logging
            LOG.error("Migration error", e);
            LOG.info("Migration aborted!");
            //DO NOT REMOVE! Exception must be thrown to indicate CI Tool (e.g. Jenkins), that the application did not return with RETURN_CODE 0!
            throw e;
        }
    }

}
