package com.satspeedy.bpm.camuda.migrator.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for process definitions.
 */
@Service
public class ProcessDefinitionService {

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ZipResourceService zipResourceService;

  /**
   * Fecth latest definition by key and version tag and return first (sort order desc).
   *
   * @param processDefinitionKey processDefinitionKey
   * @param processVersionTag processVersionTag
   * @return latest definition or null if no one exist
   */
  protected ProcessDefinition fetchLatestProcessDefinitionByKeyAndVersionTag(String processDefinitionKey, String processVersionTag) {
    return repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .versionTag(processVersionTag)
      .orderByProcessDefinitionId()
      .desc()
      .list()
      .stream()
      .findFirst()
      .orElse(null);
  }

  /**
   * Fecth definitions by key and version tag.
   *
   * @param processDefinitionKey processDefinitionKey
   * @param processVersionTag processVersionTag
   * @return definitions
   */
  protected List<ProcessDefinition> fetchProcessDefinitionsByKeyAndVersionTag(String processDefinitionKey, String processVersionTag) {
    return repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .versionTag(processVersionTag)
      .orderByProcessDefinitionId()
      .desc()
      .list();
  }

  /**
   * Extract the HashValues from all deployed bpmn models in the engine.
   * @return List of Hashes as String
   */
  protected List<String> extractBPMNHashValuesFromEngine() {
    List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
    return list.stream().map(processDefinition -> {
      InputStream processModelIn = repositoryService.getProcessModel(processDefinition.getId());
      return zipResourceService.createHashForFile(processModelIn, processDefinition.getName());
    }).collect(Collectors.toList());
  }

}
