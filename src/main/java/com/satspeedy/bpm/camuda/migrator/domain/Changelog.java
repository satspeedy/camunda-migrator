package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *  Changelog.
 */
public class Changelog {

  private List<ChangelogVersion> versionsOrdered = new ArrayList<>();

  public List<ChangelogVersion> getVersionsOrdered() {
    return versionsOrdered;
  }

  public void setVersionsOrdered(List<ChangelogVersion> versionsOrdered) {
    this.versionsOrdered = versionsOrdered;
  }
}
