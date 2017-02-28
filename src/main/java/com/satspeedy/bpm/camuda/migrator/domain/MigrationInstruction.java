package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.List;

/**
 * Represent a single instruction to migrate.
 */
public class MigrationInstruction {

  private List<String> sourceActivityIds;
  private List<String> targetActivityIds;
  private Boolean updateEventTrigger;

  public List<String> getSourceActivityIds() {
    return sourceActivityIds;
  }

  public void setSourceActivityIds(List<String> sourceActivityIds) {
    this.sourceActivityIds = sourceActivityIds;
  }

  public List<String> getTargetActivityIds() {
    return targetActivityIds;
  }

  public void setTargetActivityIds(List<String> targetActivityIds) {
    this.targetActivityIds = targetActivityIds;
  }

  public void setUpdateEventTrigger(Boolean isUpdateEventTrigger) {
    this.updateEventTrigger = isUpdateEventTrigger;
  }

  public Boolean isUpdateEventTrigger() {
    return updateEventTrigger;

  }
}
