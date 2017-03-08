package com.satspeedy.bpm.camuda.migrator.domain;

/**
 *  Changelog version.
 */
public class ChangelogVersion {

  private String deploymentName;
  private String deploymentSource;
  private boolean deployChangedOnly;
  private String versionTag;
  private String archiveFile;
  private String migrationFolder;

  public String getDeploymentName() {
    return deploymentName;
  }

  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public String getDeploymentSource() {
    return deploymentSource;
  }

  public void setDeploymentSource(String deploymentSource) {
    this.deploymentSource = deploymentSource;
  }

  public boolean isDeployChangedOnly() {
    return deployChangedOnly;
  }

  public void setDeployChangedOnly(boolean deployChangedOnly) {
    this.deployChangedOnly = deployChangedOnly;
  }

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
