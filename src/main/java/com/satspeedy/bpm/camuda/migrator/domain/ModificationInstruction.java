package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent a single modification instruction.
 */
public class ModificationInstruction {

  private ModificationInstructionType type;
  private String activityId;
  private Map<String, ModificationVariable> variables = new HashMap<>();

  public ModificationInstructionType getType() {
    return type;
  }

  public void setType(ModificationInstructionType type) {
    this.type = type;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Map<String, ModificationVariable> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, ModificationVariable> variables) {
    this.variables = variables;
  }
}
