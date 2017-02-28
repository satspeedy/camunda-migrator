package com.satspeedy.bpm.camuda.migrator.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represent a query to determine the process instances to migrate.
 */
public class ProcessInstanceQuery {

  private String processDefinitionKey;
  private String processVersionTag;
  private List<String> processActivityIds = new ArrayList<>();

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessVersionTag() {
    return processVersionTag;
  }

  public void setProcessVersionTag(String processVersionTag) {
    this.processVersionTag = processVersionTag;
  }

  public List<String> getProcessActivityIds() {
    return processActivityIds;
  }

  public void setProcessActivityIds(List<String> processActivityIds) {
    this.processActivityIds = processActivityIds;
  }
}
