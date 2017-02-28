package com.satspeedy.bpm.camuda.migrator.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Type of modification instruction.
 */
public enum ModificationInstructionType {
  CANCEL("cancel"),
  START_BEFORE_ACTIVITY("startBeforeActivity"),
  START_AFTER_ACTIVITY("startAfterActivity"),
  ADD_VARIABLE("addVariable");

  private String type;

  ModificationInstructionType(String type) {
    this.type = type;
  }

  /**
   * create enum instance from json string.
   *
   * @param type type
   * @return ModificationInstructionType instance
   */
  @JsonCreator
  public static ModificationInstructionType fromString(String type) {
    if (type != null) {
      for (ModificationInstructionType modificationInstructionType : ModificationInstructionType.values()) {
        if (modificationInstructionType.type.equals(type)) {
          return modificationInstructionType;
        }
      }
    }
    return null;
  }

}
