package com.satspeedy.bpm.camuda.migrator.domain;

/**
 *  Changelog version.
 */
public class ChangelogVersion {
  private String versionTag;
  private String archiveFile;
  private String migrationFolder;

  public String getVersionTag() {
    return versionTag;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  public String getArchiveFile() {
    return archiveFile;
  }

  public void setArchiveFile(String archiveFile) {
    this.archiveFile = archiveFile;
  }

  public String getMigrationFolder() {
    return migrationFolder;
  }

  public void setMigrationFolder(String migrationFolder) {
    this.migrationFolder = migrationFolder;
  }
}
