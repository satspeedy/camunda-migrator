package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of modifications.
 */
public class ModificationCollection {

  private List<Modification> modifications = new ArrayList<>();

  public List<Modification> getModifications() {
    return modifications;
  }

  public void setModifications(List<Modification> modifications) {
    this.modifications = modifications;
  }
}
