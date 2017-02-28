package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of migration.
 */
public class MigrationCollection {

  private List<Migration> migrations;

  /**
   * Return a collection of migrations.
   *
   * @return migrations
   */
  public List<Migration> getMigrations() {
    if (migrations == null) {
      migrations = new ArrayList<>();
    }
    return migrations;
  }

  public void setMigrations(List<Migration> migrations) {
    this.migrations = migrations;
  }
}
