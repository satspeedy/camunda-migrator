package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.List;

/**
 *  Contains planned information to migrate a process.
 */
public class MigrationPlan {

  private String sourceProcessDefinitionKey;
  private String sourceProcessVersionTag;
  private String targetProcessDefinitionKey;
  private String targetProcessVersionTag;
  private List<MigrationInstruction> instructions;

  public String getSourceProcessDefinitionKey() {
    return sourceProcessDefinitionKey;
  }

  public void setSourceProcessDefinitionKey(String sourceProcessDefinitionKey) {
    this.sourceProcessDefinitionKey = sourceProcessDefinitionKey;
  }

  public String getSourceProcessVersionTag() {
    return sourceProcessVersionTag;
  }

  public void setSourceProcessVersionTag(String sourceProcessVersionTag) {
    this.sourceProcessVersionTag = sourceProcessVersionTag;
  }

  public String getTargetProcessDefinitionKey() {
    return targetProcessDefinitionKey;
  }

  public void setTargetProcessDefinitionKey(String targetProcessDefinitionKey) {
    this.targetProcessDefinitionKey = targetProcessDefinitionKey;
  }

  public String getTargetProcessVersionTag() {
    return targetProcessVersionTag;
  }

  public void setTargetProcessVersionTag(String targetProcessVersionTag) {
    this.targetProcessVersionTag = targetProcessVersionTag;
  }

  public List<MigrationInstruction> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<MigrationInstruction> instructions) {
    this.instructions = instructions;
  }
}
