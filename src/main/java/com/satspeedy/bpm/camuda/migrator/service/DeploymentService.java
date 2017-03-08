package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Process definition deployment service.
 */
@Service
public class DeploymentService {

  private static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);

  public static final String DEPLOYMENT_NAME = "pcs";
  public static final String DEPLOYMENT_SOURCE = "process application";
  public static final boolean DEPLOY_CHANGED_ONLY = true;
  public static final String CLASSPATH_TO_ARCHIVE_FILES = "classpath:/process/archive/";

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ZipResourceService zipResourceService;

  @Autowired
  private ProcessDefinitionService processDefinitionService;

  /**
   * Deploy all process models included in zip file for the given changelogVersion.
   *
   * @param changelogVersion changelogVersion
   */
  public void deploy(ChangelogVersion changelogVersion) {
    LOG.info("Starting deployment for version tag {} with migration file {}", changelogVersion.getVersionTag(), changelogVersion.getArchiveFile());

    String zipResourcePath;
    if (changelogVersion.getArchiveFile().toLowerCase().contains("http") || changelogVersion.getArchiveFile().toLowerCase().contains("ftp")) {
      zipResourcePath = zipResourceService.downloadZipResource(changelogVersion.getArchiveFile(), changelogVersion.getVersionTag());
    } else {
      zipResourcePath = CLASSPATH_TO_ARCHIVE_FILES + changelogVersion.getArchiveFile();
    }

    Resource zipResource;
    try {
      zipResource = zipResourceService.loadZipResource(zipResourcePath);
    } catch (IOException e) {
      throw new IllegalMigrationStateException("zip resource loading failed for version tag " + changelogVersion.getVersionTag(), e);
    }

    if (!isVersionAlreadyDeployed(changelogVersion, zipResource)) {
      deployZipResource(zipResource, changelogVersion);
    } else {
      LOG.info("\tDeployment for version tag {} was already deployed", changelogVersion.getVersionTag());
    }
    LOG.info("Deployment for version tag {} with migration file {} ended successfully", changelogVersion.getVersionTag(), changelogVersion.getArchiveFile());
  }

  private void deployZipResource(Resource zipResource, ChangelogVersion changelogVersion) {
    Objects.requireNonNull(zipResource);
    repositoryService.createDeployment()
      .addZipInputStream(zipResourceService.openZipResource(zipResource))
      .name(DEPLOYMENT_NAME)
      .source(DEPLOYMENT_SOURCE)
      .enableDuplicateFiltering(DEPLOY_CHANGED_ONLY)
      .deploy();
  }

  /**
   * Checks if the given version is already deployment.
   *
   * @param changelogVersion changelogVersion
   * @param zipResource zipResource
   * @return true, if already deployed
   * @throws IOException exception
   */
  protected boolean isVersionAlreadyDeployed(ChangelogVersion changelogVersion, Resource zipResource) {
    final List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().versionTag(changelogVersion.getVersionTag()).list();
    if (processDefinitions.isEmpty()) {
      return false;
    }

    final List<String> hashValuesFromDB = processDefinitionService.extractBPMNHashValuesFromEngine();
    final List<String> hashValuesFromFile = zipResourceService.extractHashValuesFromZipEntries(changelogVersion, zipResource);
    return hashValuesFromDB.containsAll(hashValuesFromFile);
  }

}
