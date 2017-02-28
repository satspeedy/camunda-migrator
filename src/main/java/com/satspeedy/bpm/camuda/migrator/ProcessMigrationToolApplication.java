package com.satspeedy.bpm.camuda.migrator;

import com.satspeedy.bpm.camuda.migrator.service.ProcessInstanceMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * Process Migration Tool Application.
 */
@SpringBootApplication
public class ProcessMigrationToolApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessMigrationToolApplication.class);

  @Autowired
  private ProcessInstanceMigrationService processInstanceMigrationService;

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
      //DO NOT REMOVE! Exception must be thrown to inidicate jenkins, that the application didnt return with RETURN_CODE 0!
      throw e;
    }
  }

  /**
   * Do post construct.
   */
  @PostConstruct
   public void doPostConstruct() {
    processInstanceMigrationService.migrate();
  }

}
