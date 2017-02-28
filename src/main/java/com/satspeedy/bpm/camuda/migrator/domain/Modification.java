package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.List;

/**
 *  Represent a modification.
 */
public class Modification {
  private String sourceProcessDefinitionKey;
  private String sourceProcessVersionTag;
  private List<ModificationInstruction> instructions;

  private boolean skipCustomListeners;
  private boolean skipIoMappings;

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

  public List<ModificationInstruction> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<ModificationInstruction> instructions) {
    this.instructions = instructions;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }
}
