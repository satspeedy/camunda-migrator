package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Changelog Service.
 */
@Service
public class ChangelogService {

  private static final Logger LOG = LoggerFactory.getLogger(ChangelogService.class);
  public static final String CLASSPATH_TO_CHANGELOG = "classpath:/process/process-changelog.json";

  @Autowired
  private ResourcePatternResolver resourceLoader;

  @Autowired
  private ObjectMapperService objectMapperService;

  @Autowired
  private RepositoryService repositoryService;

  /**
   * Load changelog resource and map it to Changelog.
   *
   * @return newly created Changelog, otherwise null in case of an error
   */
  public Changelog loadChangelog() {
    try {
      final Resource changelogResource = resourceLoader.getResource(CLASSPATH_TO_CHANGELOG);
      final Changelog changelog = objectMapperService.convertChangelog(changelogResource);
      LOG.info("Found changelog file with {} entries:", changelog.getVersionsOrdered().size());
      changelog.getVersionsOrdered().forEach(v -> LOG.info("\t" + v.getVersionTag()));
      return changelog;
    } catch (IOException e) {
      throw new IllegalMigrationStateException("Error during loading changelog file '" + CLASSPATH_TO_CHANGELOG + "'", e);
    }
  }
}
