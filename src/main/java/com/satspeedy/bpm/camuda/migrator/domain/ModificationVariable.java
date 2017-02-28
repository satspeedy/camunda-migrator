package com.satspeedy.bpm.camuda.migrator.domain;

import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * Represents a modificationVariable.
 */
public class ModificationVariable {

  private String type;
  private String value;

  public ModificationVariable() {
  }

  public ModificationVariable(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  /**
   * Get value as object.
   * @return value as object
   */
  public Object getValueAsObject() {
    if (ValueType.STRING.getName().equalsIgnoreCase(type)) {
      return value;
    } else if (ValueType.BOOLEAN.getName().equalsIgnoreCase(type)) {
      return Boolean.valueOf(value);
    } else {
      throw new IllegalMigrationStateException("Unsupported value type:" + type);
    }

  }
}
