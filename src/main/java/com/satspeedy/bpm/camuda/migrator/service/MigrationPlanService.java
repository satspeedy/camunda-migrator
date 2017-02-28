package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.MigrationInstruction;
import com.satspeedy.bpm.camuda.migrator.domain.MigrationPlan;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  Service to create an engine specific migration plan.
 */
@Service
public class MigrationPlanService {

  @Autowired
  private RuntimeService runtimeService;

  /**
   * Creates an engine specific migration plan.
   *
   * @param sourceProcessDefinition source process definition
   * @param targetProcessDefinition target process definition
   * @param migrationPlan {@link MigrationPlan}
   * @return a {@link org.camunda.bpm.engine.migration.MigrationPlan}
   */
  protected org.camunda.bpm.engine.migration.MigrationPlan createMigrationPlan(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition, MigrationPlan migrationPlan) {
    MigrationPlanBuilder migrationPlanBuilder = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId());
    migrationPlanBuilder.mapEqualActivities();

    for (MigrationInstruction migrationInstruction : migrationPlan.getInstructions()) {
      List<String> sourceActivityIds = migrationInstruction.getSourceActivityIds();
      List<String> targetActivityIds = migrationInstruction.getTargetActivityIds();
      for (int idx = 0; idx < sourceActivityIds.size(); idx++) {
        MigrationInstructionBuilder migrationInstructionBuilder = migrationPlanBuilder.mapActivities(sourceActivityIds.get(idx), targetActivityIds.get(idx));
        if (migrationInstruction.isUpdateEventTrigger()) {
          migrationInstructionBuilder.updateEventTrigger();
        }
      }
    }
    return migrationPlanBuilder.build();
  }

}
