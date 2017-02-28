package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to migrate process instances.
 */
@Service
public class ProcessInstanceMigrationService {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceMigrationService.class);

  @Autowired
  private ChangelogService changelogService;

  @Autowired
  private DeploymentService deploymentService;

  @Autowired
  private ModificationService modificationService;

  @Autowired
  private MigrationService migrationService;

  /**
   * Start process instance migration in a transaction.
   */
  @Transactional
  public void migrate() {
    LOG.info("Starting migration ...");
    final Changelog changelog = changelogService.loadChangelog();
    for (ChangelogVersion changelogVersion : changelog.getVersionsOrdered()) {
      deploymentService.deploy(changelogVersion);
      modificationService.modifyBeforeMigration(changelogVersion);
      migrationService.migrate(changelogVersion);
      modificationService.cleanUpAfterMigration(changelogVersion);
      modificationService.modifyAfterMigration(changelogVersion);
    }
    LOG.info("Migration successful!");
  }

}
