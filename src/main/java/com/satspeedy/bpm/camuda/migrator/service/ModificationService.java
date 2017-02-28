package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.*;
import com.satspeedy.bpm.camuda.migrator.domain.execution.ProcessInstanceModification;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Process modification service.
 */
@Service
public class ModificationService {

  private static final Logger LOG = LoggerFactory.getLogger(ModificationService.class);

  public static final String CLASSPATH_TO_MIGRATION_PLAN = "classpath:/process/migrationplan/";
  public static final String MODIFICATION_BEFORE_MIGRATION_JSON_FILE = "modification_before.json";
  public static final String MODIFICATION_AFTER_MIGRATION_JSON_FILE = "modification_after.json";
  public static final String AFTER_MIGRATION_RETURN_POINT = "_afterMigrationReturnPoint";
  public static final String AFTER_MIGRATION_SKIP_IO_MAPPINGS = "_afterMigrationSkipIoMappings";

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ResourcePatternResolver resourcePatternResolver;

  @Autowired
  private ProcessDefinitionService processDefinitionService;

  @Autowired
  private ObjectMapperService objectMapperService;

  /**
   * Migrate process instances ordered by changelog file.
   *
   * @param changelogVersion changelogVersion
   * @throws IllegalMigrationStateException Exception for illegal migration state
   */
  public void modifyBeforeMigration(ChangelogVersion changelogVersion) {
    LOG.info("Starting modification before migration for version tag {}", changelogVersion.getVersionTag());
    ModificationCollection modificationCollection = loadModificationCollection(changelogVersion, MODIFICATION_BEFORE_MIGRATION_JSON_FILE);
    modify(changelogVersion, modificationCollection);
    LOG.info("Modification for version tag {} with modification file {} ended successfully", changelogVersion.getVersionTag());
  }

  /**
   * Migrate process instances ordered by changelog file.
   *
   * @param changelogVersion changelogVersion
   * @throws IllegalMigrationStateException Exception for illegal migration state
   */
  public void modifyAfterMigration(ChangelogVersion changelogVersion) {
    LOG.info("Starting modification after migration for version tag {}", changelogVersion.getVersionTag());
    ModificationCollection modificationCollection = loadModificationCollection(changelogVersion, MODIFICATION_AFTER_MIGRATION_JSON_FILE);
    modify(changelogVersion, modificationCollection);
    LOG.info("Modification for version tag {} with modification file {} ended successfully", changelogVersion.getVersionTag());
  }

  private void modify(ChangelogVersion changelogVersion, ModificationCollection modificationCollection) {
    if (modificationCollection != null) {
      try {
        this.modifyCollection(modificationCollection);
      } catch (IllegalMigrationStateException e) {
        throw new IllegalMigrationStateException("Modification failed for version tag " + changelogVersion.getVersionTag(), e);
      }
    } else {
      LOG.info("No modification found for version tag {}.", changelogVersion.getVersionTag());
    }
  }

  private ModificationCollection loadModificationCollection(ChangelogVersion changelogVersion, String modificationFileName) {
    String modificationFile = changelogVersion.getMigrationFolder() + "/" + modificationFileName;
    LOG.info("Reading file {}", modificationFile);
    if (StringUtils.isNotEmpty(changelogVersion.getMigrationFolder())) {
      final Resource resource = resourcePatternResolver.getResource(CLASSPATH_TO_MIGRATION_PLAN + modificationFile);
      if (resource.exists()) {
        try {
          return objectMapperService.convertModificationCollection(resource);
        } catch (IOException e) {
          throw new IllegalMigrationStateException("Could not load modification file " + modificationFile + " for version tag " + changelogVersion.getVersionTag(), e);
        }
      } else {
        LOG.info("No modification necessary for version tag {}.", changelogVersion.getVersionTag());
      }
    }
    return null;
  }

  private void modifyCollection(ModificationCollection modificationCollection) {
    List<ProcessInstanceModification> processInstanceModifications = new ArrayList<>();
    List<Modification> modifications = modificationCollection.getModifications();
    for (Modification modification : modifications) {
      validate(modification);
      List<ProcessInstance> processInstancesToModify = findProcessInstancesToModify(modification);
      processInstancesToModify.forEach(processInstance -> processInstanceModifications.add(createProcessInstanceModification(processInstance, modification)));
      LOG.info("Modification for " + modification.getSourceProcessDefinitionKey() + " applied on " + processInstancesToModify.size() + " process instance(s)");
    }
    processInstanceModifications.forEach(modification -> modification.execute());


  }

  /**
   * Validate the modification and throws an exception in case of a violation.
   *
   * @param modification modification
   */
  protected void validate(Modification modification) {
    ProcessDefinition sourceDefinition = processDefinitionService.fetchLatestProcessDefinitionByKeyAndVersionTag(modification.getSourceProcessDefinitionKey(), modification.getSourceProcessVersionTag());
    ModificationInstruction firstCancelModificationInstruction = modification.getInstructions().stream()
      .filter(i -> ModificationInstructionType.CANCEL.equals(i.getType()) || ModificationInstructionType.ADD_VARIABLE.equals(i.getType()))
      .findFirst()
      .orElseThrow(() -> new IllegalMigrationStateException("No cancel instruction defined for SOURCE process definition key " + modification.getSourceProcessDefinitionKey() + " and process version tag " + modification.getSourceProcessVersionTag()));

    BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(sourceDefinition.getId());
    if (bpmnModelInstance.getModelElementById(firstCancelModificationInstruction.getActivityId()) == null) {
      throw new IllegalMigrationStateException("Defined activityId " + firstCancelModificationInstruction.getActivityId() + " in first cancel instruction does not exist for the SOURCE process definition key " + modification.getSourceProcessDefinitionKey() + " and process version tag " + modification.getSourceProcessVersionTag());
    }
  }

  /**
   * Creates a process instance modification builder for the given process instance and given modification definition.
   *
   * @param processInstance processInstance
   * @param modification    modification
   * @return ProcessInstanceModificationBuilder
   */
  protected ProcessInstanceModification createProcessInstanceModification(ProcessInstance processInstance, Modification modification) {
    ProcessInstanceModification processInstanceModification = new ProcessInstanceModification();
    for (ModificationInstruction modificationInstruction : modification.getInstructions()) {
      processInstanceModification = addInstruction(processInstanceModification, modificationInstruction, processInstance, modification.isSkipCustomListeners(), modification.isSkipIoMappings());
    }
    return processInstanceModification;
  }

  private ProcessInstanceModification addInstruction(ProcessInstanceModification processInstanceModification, ModificationInstruction modificationInstruction, ProcessInstance processInstance, boolean skipCustomListeners, boolean skipIoMappings) {
    ProcessInstanceModification result = processInstanceModification;
    switch (modificationInstruction.getType()) {
      case CANCEL:
        ProcessInstanceModificationBuilder processInstanceModificationBuilder = retrieveProcessInstanceModificationBuilder(processInstanceModification, processInstance, skipCustomListeners, skipIoMappings);
        result.setProcessInstanceModificationBuilder(processInstanceModificationBuilder.cancelAllForActivity(modificationInstruction.getActivityId()));
        break;
      case START_AFTER_ACTIVITY:
        ProcessInstanceModificationBuilder processInstanceModificationBuilderAfter = retrieveProcessInstanceModificationBuilder(processInstanceModification, processInstance, skipCustomListeners, skipIoMappings);
        ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilderAfter = processInstanceModificationBuilderAfter.startAfterActivity(modificationInstruction.getActivityId());
        modificationInstruction.getVariables().entrySet().forEach(entry -> processInstanceModificationInstantiationBuilderAfter.setVariable(entry.getKey(), entry.getValue().getValueAsObject()));
        result.setProcessInstanceModificationBuilder(processInstanceModificationInstantiationBuilderAfter);
        break;
      case START_BEFORE_ACTIVITY:
        ProcessInstanceModificationBuilder processInstanceModificationBuilderBefore = retrieveProcessInstanceModificationBuilder(processInstanceModification, processInstance, skipCustomListeners, skipIoMappings);
        ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilderBefore = processInstanceModificationBuilderBefore.startBeforeActivity(modificationInstruction.getActivityId());
        modificationInstruction.getVariables().entrySet().forEach(entry -> processInstanceModificationInstantiationBuilderBefore.setVariable(entry.getKey(), entry.getValue().getValueAsObject()));
        result.setProcessInstanceModificationBuilder(processInstanceModificationInstantiationBuilderBefore);
        break;
      case ADD_VARIABLE:
        result.addProcessInstanceModificationExecutor(() -> modificationInstruction.getVariables().entrySet().forEach(entry -> runtimeService.setVariable(processInstance.getProcessInstanceId(), entry.getKey(), entry.getValue().getValueAsObject())));
        break;
      default:
        throw new IllegalMigrationStateException("Unsupported modificationInstruction: " + modificationInstruction);
    }
    return result;
  }

  private ProcessInstanceModificationBuilder retrieveProcessInstanceModificationBuilder(ProcessInstanceModification processInstanceModification, ProcessInstance processInstance, boolean skipCustomListeners, boolean skipIoMappings) {
    ProcessInstanceModificationBuilder processInstanceModificationBuilder = processInstanceModification.getProcessInstanceModificationBuilder();
    if (processInstanceModificationBuilder == null) {
      processInstanceModificationBuilder = runtimeService.createProcessInstanceModification(processInstance.getProcessInstanceId());
      processInstanceModification.setProcessInstanceModificationBuilder(processInstanceModificationBuilder);
      processInstanceModification.addProcessInstanceModificationExecutor(() -> processInstanceModification.getProcessInstanceModificationBuilder().execute(skipCustomListeners, skipIoMappings));
    }
    return processInstanceModificationBuilder;
  }

  /**
   * Find all affected process instances to modifyBeforeMigration.
   *
   * @param modification modification
   * @return affected process instances
   */
  protected List<ProcessInstance> findProcessInstancesToModify(Modification modification) {
    ProcessDefinition sourceDefinition = processDefinitionService.fetchLatestProcessDefinitionByKeyAndVersionTag(modification.getSourceProcessDefinitionKey(), modification.getSourceProcessVersionTag());

    ModificationInstruction modificationInstruction = modification.getInstructions().stream()
      .filter(i -> ModificationInstructionType.CANCEL.equals(i.getType()) || ModificationInstructionType.ADD_VARIABLE.equals(i.getType()))
      .findFirst().orElseThrow(() -> new IllegalMigrationStateException("No cancel instruction defined for " + modification.getSourceProcessDefinitionKey()));
    return runtimeService.createProcessInstanceQuery().processDefinitionId(sourceDefinition.getId()).activityIdIn(modificationInstruction.getActivityId()).list();
  }

  /**
   * clean up modified ProcessInstances.
   *
   * @param changelogVersion changelogVersion
   */
  public void cleanUpAfterMigration(ChangelogVersion changelogVersion) {
    LOG.info("Cleaning up modified process instances for {}", changelogVersion.getVersionTag());
    final List<VariableInstance> variableInstanceList = runtimeService.createVariableInstanceQuery().variableName(AFTER_MIGRATION_RETURN_POINT).list();
    LOG.info("\tFound {} processes to clean up", variableInstanceList.size());
    for (VariableInstance variableInstance : variableInstanceList) {
      List<String> activeActivityIds = runtimeService.getActiveActivityIds(variableInstance.getProcessInstanceId());
      LOG.info("\tProcess instance {} is positioned at {} and will be moved to position {}", variableInstance.getProcessInstanceId(), activeActivityIds, variableInstance.getValue());
      final ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilder = runtimeService.createProcessInstanceModification(variableInstance.getProcessInstanceId()).cancelActivityInstance(variableInstance.getActivityInstanceId()).startBeforeActivity(variableInstance.getValue().toString());
      final boolean skipIoMappings = retrieveBooleanFromVariableInstance(variableInstance.getProcessInstanceId(), AFTER_MIGRATION_SKIP_IO_MAPPINGS, true);
      runtimeService.removeVariables(variableInstance.getExecutionId(), Arrays.asList(AFTER_MIGRATION_RETURN_POINT, AFTER_MIGRATION_SKIP_IO_MAPPINGS));
      processInstanceModificationInstantiationBuilder.execute(true, skipIoMappings);
    }
    LOG.info("Done cleaning up modified process instances for {}", changelogVersion.getVersionTag());
  }

  private boolean retrieveBooleanFromVariableInstance(String processInstanceId, String variableName, boolean defaultValue) {
    final VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstanceId).variableName(variableName).singleResult();
    return variableInstance != null ? (Boolean) variableInstance.getValue() : defaultValue;
  }

}
