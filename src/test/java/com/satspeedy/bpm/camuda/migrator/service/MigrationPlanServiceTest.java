package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.MigrationInstruction;
import com.satspeedy.bpm.camuda.migrator.domain.MigrationPlan;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationPlanServiceTest {

  @InjectMocks
  MigrationPlanService migrationPlanService;

  @Mock
  private RuntimeService runtimeServiceMock;

  @Test
  public void shouldCreateAMigrationPlanWithUpdatingEventTrigger() {
    // given
    ProcessDefinition sourceProcessDefinition = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinition = mock(ProcessDefinition.class);

    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    MigrationInstruction migrationInstruction = new MigrationInstruction();
    final String sourceActivityIds = "sourceActivityIds";
    final String targetActivityIds = "targetActivityIds";
    migrationInstruction.setSourceActivityIds(Collections.singletonList(sourceActivityIds));
    migrationInstruction.setTargetActivityIds(Collections.singletonList(targetActivityIds));
    migrationInstruction.setUpdateEventTrigger(true);
    migrationPlan.setInstructions(Collections.singletonList(migrationInstruction));

    MigrationPlanBuilder migrationPlanBuilderMock = mock(MigrationPlanBuilder.class);
    when(runtimeServiceMock.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())).thenReturn(migrationPlanBuilderMock);
    MigrationInstructionBuilder migrationInstructionBuilderMock = mock(MigrationInstructionBuilder.class);
    when(migrationPlanBuilderMock.mapActivities(anyString(), anyString())).thenReturn(migrationInstructionBuilderMock);

    // when
    migrationPlanService.createMigrationPlan(sourceProcessDefinition, targetProcessDefinition, migrationPlan);

    // then
    verify(migrationPlanBuilderMock).mapActivities(sourceActivityIds, targetActivityIds);
    verify(migrationPlanBuilderMock).mapEqualActivities();
    verify(migrationInstructionBuilderMock).updateEventTrigger();
    verify(migrationPlanBuilderMock).build();
  }

  @Test
  public void shouldCreateAMigrationPlanWithoutUpdatingEventTrigger() {
    // given
    ProcessDefinition sourceProcessDefinition = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinition = mock(ProcessDefinition.class);

    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    MigrationInstruction migrationInstruction = new MigrationInstruction();
    final String sourceActivityIds = "sourceActivityIds";
    final String targetActivityIds = "targetActivityIds";
    migrationInstruction.setSourceActivityIds(Collections.singletonList(sourceActivityIds));
    migrationInstruction.setTargetActivityIds(Collections.singletonList(targetActivityIds));
    migrationInstruction.setUpdateEventTrigger(false);
    migrationPlan.setInstructions(Collections.singletonList(migrationInstruction));

    MigrationPlanBuilder migrationPlanBuilderMock = mock(MigrationPlanBuilder.class);
    when(runtimeServiceMock.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())).thenReturn(migrationPlanBuilderMock);
    MigrationInstructionBuilder migrationInstructionBuilderMock = mock(MigrationInstructionBuilder.class);
    when(migrationPlanBuilderMock.mapActivities(anyString(), anyString())).thenReturn(migrationInstructionBuilderMock);

    // when
    migrationPlanService.createMigrationPlan(sourceProcessDefinition, targetProcessDefinition, migrationPlan);

    // then
    verify(migrationPlanBuilderMock).mapActivities(sourceActivityIds, targetActivityIds);
    verify(migrationInstructionBuilderMock, never()).updateEventTrigger();
    verify(migrationPlanBuilderMock).build();
  }


}