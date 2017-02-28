package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.*;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Process migration service.
 */
@Service
public class MigrationService {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationService.class);

  public static final String CLASSPATH_TO_MIGRATION_PLAN = "classpath:/process/migrationplan/";
  public static final String MIGRATION_JSON_FILE = "migration.json";

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ResourcePatternResolver resourceLoader;

  @Autowired
  private ProcessDefinitionService processDefinitionService;

  @Autowired
  private ObjectMapperService objectMapperService;

  @Autowired
  private MigrationPlanService migrationPlanService;

  /**
   * Migrate process instances ordered by changelog file.
   *
   * @param changelogVersion changelogVersion
   * @throws IllegalMigrationStateException Exception for illegal migration state
   */
  public void migrate(ChangelogVersion changelogVersion)  {
    String migrationFile = changelogVersion.getMigrationFolder() + "/" + MIGRATION_JSON_FILE;
    LOG.info("Starting migration for version tag {} with migration file {}", changelogVersion.getVersionTag(), migrationFile);
    if (StringUtils.isNotEmpty(changelogVersion.getMigrationFolder())) {
      final Resource resource = resourceLoader.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFile);
      if (resource.exists()) {
        try {
          this.migrateCollection(objectMapperService.convertMigrationCollection(resource));
        } catch (IllegalMigrationStateException e) {
          throw new IllegalMigrationStateException("Migrate failed for version tag " + changelogVersion.getVersionTag() + " with migration file " + migrationFile, e);
        } catch (IOException e) {
          throw new IllegalMigrationStateException("Could not load migration file " + migrationFile + " for version tag " + changelogVersion.getVersionTag(), e);
        }
      }
    }

    LOG.info("Migration for version tag {} with migration file {} ended successfully", changelogVersion.getVersionTag(), migrationFile);
  }

  private void migrateCollection(MigrationCollection migrationCollection) {
    List<MigrationPlanContainer> migrationPlanMap = new ArrayList<>();

    for (Migration migration : migrationCollection.getMigrations()) {
      migrationPlanMap.addAll(createMigrationPlan(migration));
    }

    migrationPlanMap.forEach(this::execute);
  }

  private boolean validate(Migration migration, ProcessDefinition sourceDefinition, ProcessDefinition targetDefinition) {
    MigrationPlan migrationPlan = migration.getMigrationPlan();

    validateProcessDefinitions(migrationPlan, sourceDefinition, targetDefinition);
    validateMigration(migration);
    validateMigrationActivities(migration.getProcessInstanceQuery());

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceDefinition.getId());
    if (!migration.getProcessInstanceQuery().getProcessActivityIds().isEmpty()) {
      processInstanceQuery.activityIdIn(migration.getProcessInstanceQuery().getProcessActivityIds().toArray(new String[0]));
    }
    if (processInstanceQuery.count() == 0) {
      LOG.info("\tNo process instance exists for SOURCE process definition key {} and version tag {} and version {}. Migration skipped!", migrationPlan.getSourceProcessDefinitionKey(), migrationPlan.getSourceProcessVersionTag(), sourceDefinition.getVersion());
      return false;
    }
    return true;
  }

  private void validateMigration(Migration migration) {
    MigrationPlan migrationPlan = migration.getMigrationPlan();
    ProcessInstanceQuery processInstanceQuery = migration.getProcessInstanceQuery();

    if (!migrationPlan.getSourceProcessDefinitionKey().equals(processInstanceQuery.getProcessDefinitionKey())) {
      throw new IllegalMigrationStateException("Defined source process definition key " + migrationPlan.getSourceProcessDefinitionKey() + " and process instance query " + processInstanceQuery.getProcessDefinitionKey() + " for migration are mismatching");
    }

    if (!migrationPlan.getSourceProcessVersionTag().equals(processInstanceQuery.getProcessVersionTag())) {
      throw new IllegalMigrationStateException("Defined source process version tag " + migrationPlan.getSourceProcessVersionTag() + " and process version tag " + processInstanceQuery.getProcessVersionTag() + " for migration are mismatching");
    }
  }

  private void validateMigrationActivities(ProcessInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.getProcessActivityIds().isEmpty()) {
      return;
    }
    List<ProcessDefinition> processDefinitions = processDefinitionService.fetchProcessDefinitionsByKeyAndVersionTag(processInstanceQuery.getProcessDefinitionKey(), processInstanceQuery.getProcessVersionTag());
    for (ProcessDefinition processDefinition : processDefinitions) {
      BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());
      for (String activityId : processInstanceQuery.getProcessActivityIds()) {
        if (bpmnModelInstance.getModelElementById(activityId) == null) {
          throw new IllegalMigrationStateException("Defined activityId " + activityId + " does not exist for the process instance query definition key " + processInstanceQuery.getProcessDefinitionKey() + " and process version tag " + processInstanceQuery.getProcessVersionTag());
        }
      }
    }
  }

  private void validateProcessDefinitions(MigrationPlan migrationPlan, ProcessDefinition sourceDefinition, ProcessDefinition targetDefinition) {
    if (sourceDefinition == null || targetDefinition == null) {
      if (sourceDefinition == null) {
        throw new IllegalMigrationStateException("No process definition found for SOURCE definition key " + migrationPlan.getSourceProcessDefinitionKey() + " and version tag " + migrationPlan.getSourceProcessVersionTag());
      } else  {
        throw new IllegalMigrationStateException("No process definition found for TARGET definition key " + migrationPlan.getTargetProcessDefinitionKey() + " and version tag " + migrationPlan.getTargetProcessVersionTag());
      }
    }
  }

  private List<MigrationPlanContainer> createMigrationPlan(Migration migration) {
    List<MigrationPlanContainer> result = new ArrayList<>();
    MigrationPlan migrationPlan = migration.getMigrationPlan();
    List<ProcessDefinition> sourceDefinitions = processDefinitionService.fetchProcessDefinitionsByKeyAndVersionTag(migrationPlan.getSourceProcessDefinitionKey(), migrationPlan.getSourceProcessVersionTag());
    ProcessDefinition targetDefinition = processDefinitionService.fetchLatestProcessDefinitionByKeyAndVersionTag(migrationPlan.getTargetProcessDefinitionKey(), migrationPlan.getTargetProcessVersionTag());

    for (ProcessDefinition sourceDefinition: sourceDefinitions) {
      if (!validate(migration, sourceDefinition, targetDefinition)) {
        continue;
      }
      LOG.info("\tCreating a migration plan from SOURCE process definition " + sourceDefinition.getId() + "(Version:" + sourceDefinition.getVersion() + ") " + " to TARGET process definition " + targetDefinition.getId());

      org.camunda.bpm.engine.migration.MigrationPlan engineMigrationPlan;
      try {
        engineMigrationPlan = migrationPlanService.createMigrationPlan(sourceDefinition, targetDefinition, migrationPlan);
      } catch (MigrationPlanValidationException e) {
        throw new IllegalMigrationStateException("Validation error occurred during creating a plan for migration from SOURCE process definition " + sourceDefinition.getId() + " to TARGET process definition " + targetDefinition.getId() + ": " + e.getValidationReport().toString(), e);
      }

      org.camunda.bpm.engine.runtime.ProcessInstanceQuery migrationProcessInstanceQuery = runtimeService.createProcessInstanceQuery()
        .processDefinitionId(sourceDefinition.getId());
      if (!migration.getProcessInstanceQuery().getProcessActivityIds().isEmpty()) {
        migrationProcessInstanceQuery.activityIdIn(migration.getProcessInstanceQuery().getProcessActivityIds().toArray(new String[0]));
      }

      MigrationPlanExecutionBuilder migrationPlanExecutionBuilder = runtimeService
        .newMigration(engineMigrationPlan)
        .processInstanceQuery(migrationProcessInstanceQuery);

      if (migration.isSkipIoMappings()) {
        migrationPlanExecutionBuilder.skipIoMappings();
      }

      if (migration.isSkipCustomListeners()) {
        migrationPlanExecutionBuilder.skipCustomListeners();
      }
      result.add(new MigrationPlanContainer(migrationPlanExecutionBuilder, sourceDefinition, targetDefinition));
    }

    return result;
  }

  private void execute(MigrationPlanContainer migrationPlanContainer) {
    ProcessDefinition sourceDefinition = migrationPlanContainer.getSourceDefinition();
    ProcessDefinition targetDefinition = migrationPlanContainer.getTargetDefinition();
    MigrationPlanExecutionBuilder migrationPlanExecutionBuilder = migrationPlanContainer.getMigrationPlanExecutionBuilder();

    LOG.info("\tMigrating all process instances from SOURCE process definition " + sourceDefinition.getId() + " to TARGET process definition " + targetDefinition.getId());
    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().processDefinitionId(sourceDefinition.getId()).list()) {
      LOG.info("\tMigrating process instance with id " + processInstance.getId());
    }

    try {
      migrationPlanExecutionBuilder.execute();
      // .executeAsync() for asynchronous execution in a batch (useful for large numbers of instances)
    } catch (MigratingProcessInstanceValidationException e) {
      throw new IllegalMigrationStateException("Validation error occurred during executing the migration from SOURCE process definition " + sourceDefinition.getId() + " to TARGET process definition " + targetDefinition.getId() + ": " + e.getValidationReport().toString(), e);
    }

    LOG.info("\tSuccessfully migrated all process instances from SOURCE process definition " + sourceDefinition.getId() + " to TARGET process definition " + targetDefinition.getId());
  }

  /**
   * Class to combine MigrationPlanExecutionBuilder with its sourceDefinition and targetDefinition.
   */
  class MigrationPlanContainer {
    private MigrationPlanExecutionBuilder migrationPlanExecutionBuilder;
    private ProcessDefinition sourceDefinition;
    private ProcessDefinition targetDefinition;

    public MigrationPlanExecutionBuilder getMigrationPlanExecutionBuilder() {
      return migrationPlanExecutionBuilder;
    }

    public ProcessDefinition getSourceDefinition() {
      return sourceDefinition;
    }

    public ProcessDefinition getTargetDefinition() {
      return targetDefinition;
    }

    MigrationPlanContainer(MigrationPlanExecutionBuilder migrationPlanExecutionBuilder, ProcessDefinition sourceDefinition, ProcessDefinition targetDefinition) {
      this.migrationPlanExecutionBuilder = migrationPlanExecutionBuilder;
      this.sourceDefinition = sourceDefinition;
      this.targetDefinition = targetDefinition;
    }
  }
}
