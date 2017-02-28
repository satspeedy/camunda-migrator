package com.satspeedy.bpm.camuda.migrator.domain;

/**
 *  Represent a migration.
 */
public class Migration {

  private MigrationPlan migrationPlan;
  private boolean skipCustomListeners;
  private boolean skipIoMappings;
  private ProcessInstanceQuery processInstanceQuery;

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  public void setMigrationPlan(MigrationPlan migrationPlan) {
    this.migrationPlan = migrationPlan;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
  }
}
