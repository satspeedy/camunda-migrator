package com.satspeedy.bpm.camuda.migrator.domain.execution;

import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Process instance modification wrapper class to differentiate between engine and own impl executions.
 */
public class ProcessInstanceModification {

  private ProcessInstanceModificationBuilder processInstanceModificationBuilder;

  private List<ProcessInstanceModificationExecutor> processInstanceModificationExecutorList = new ArrayList<>();

  public ProcessInstanceModificationBuilder getProcessInstanceModificationBuilder() {
    return processInstanceModificationBuilder;
  }

  public void setProcessInstanceModificationBuilder(ProcessInstanceModificationBuilder processInstanceModificationBuilder) {
    this.processInstanceModificationBuilder = processInstanceModificationBuilder;
  }

  public List<ProcessInstanceModificationExecutor> getProcessInstanceModificationExecutorList() {
    return processInstanceModificationExecutorList;
  }

  /**
   * Adds given {@link ProcessInstanceModificationExecutor}.
   *
   * @param processInstanceModificationExecutor processInstanceModificationExecutor
   */
  public void addProcessInstanceModificationExecutor(ProcessInstanceModificationExecutor processInstanceModificationExecutor) {
    this.processInstanceModificationExecutorList.add(processInstanceModificationExecutor);
  }

  /**
   * Execute engine or own impl.
   */
  public void execute() {
    processInstanceModificationExecutorList.forEach(ProcessInstanceModificationExecutor::execute);
  }

}
