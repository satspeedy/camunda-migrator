package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.*;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.satspeedy.bpm.camuda.migrator.service.MigrationService.CLASSPATH_TO_MIGRATION_PLAN;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MigrationServiceTest {

  @InjectMocks
  private MigrationService migrationService;

  @Mock
  private ResourcePatternResolver resourceLoaderMock;

  @Mock
  private ChangelogService changelogServiceMock;

  @Mock
  private ObjectMapperService objectMapperServiceMock;

  @Mock
  private ProcessDefinitionService processDefinitionServiceMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RuntimeService runtimeServiceMock;

  @Mock
  private MigrationPlanService migrationPlanServiceMock;

  @Mock
  private RepositoryService repositoryServiceMock;

  @Test
  public void shouldSkipMigrationOnProcessDefinitionKeyMismatch() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String mismatchingSourceProcessDefinitionKey = "mismatchingSourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";
    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(mismatchingSourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    migration.setProcessInstanceQuery(processInstanceQuery);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Collections.singletonList(sourceProcessDefinitionMock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    // when
    Exception result = null;
    try {
      migrationService.migrate(changelogVersion);
      fail("Should fail");
    } catch (Exception e) {
      result = e;
    }

    // then
    assertThat(result, instanceOf(IllegalMigrationStateException.class));
    verify(processDefinitionServiceMock).fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag);
    verify(processDefinitionServiceMock).fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag);
    verify(runtimeServiceMock, never()).createProcessInstanceQuery();
    verify(migrationPlanServiceMock, never()).createMigrationPlan(eq(sourceProcessDefinitionMock), eq(targetProcessDefinitionMock), any(MigrationPlan.class));
    verify(runtimeServiceMock, never()).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
  }

  @Test
  public void shouldNotMigrateWhenNoSourceProcessInstancesExist() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";
    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(sourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    migration.setProcessInstanceQuery(processInstanceQuery);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Collections.singletonList(sourceProcessDefinitionMock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQueryMock = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.processDefinitionId(sourceProcessDefinitionId)).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.count()).thenReturn(0L);

    // when
    migrationService.migrate(changelogVersion);

    // then
    verify(processDefinitionServiceMock).fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag);
    verify(processDefinitionServiceMock).fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag);
    verify(runtimeServiceMock, times(1)).createProcessInstanceQuery();
    verify(migrationPlanServiceMock, never()).createMigrationPlan(eq(sourceProcessDefinitionMock), eq(targetProcessDefinitionMock), any(MigrationPlan.class));
    verify(runtimeServiceMock, never()).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
  }

  @Test
  public void shouldMigrateAndSkipIoMappingsAndCustomListenersWhenSourceProcessInstancesExist() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";

    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(sourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    final String firstProcessActivity = "firstProcessActivity";
    final String secondProcessActivity = "secondProcessActivity";
    processInstanceQuery.setProcessActivityIds(Arrays.asList(firstProcessActivity, secondProcessActivity));
    migration.setProcessInstanceQuery(processInstanceQuery);
    migration.setSkipCustomListeners(true);
    migration.setSkipIoMappings(true);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Collections.singletonList(sourceProcessDefinitionMock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQueryMock = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.processDefinitionId(anyString())).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.count()).thenReturn(1L);
    MigrationPlanExecutionBuilder migrationPlanExecutionBuilderMock = mock(MigrationPlanExecutionBuilder.class);
    when(runtimeServiceMock.newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class))).thenReturn(migrationPlanExecutionBuilderMock);
    when(migrationPlanExecutionBuilderMock.processInstanceQuery(any(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class))).thenReturn(migrationPlanExecutionBuilderMock);

    final BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
    when(bpmnModelInstance.getModelElementById(firstProcessActivity)).thenReturn(mock(ModelElementInstance.class));
    when(bpmnModelInstance.getModelElementById(secondProcessActivity)).thenReturn(mock(ModelElementInstance.class));
    when(repositoryServiceMock.getBpmnModelInstance(sourceProcessDefinitionMock.getId())).thenReturn(bpmnModelInstance);

    // when
    migrationService.migrate(changelogVersion);

    // then
    verify(processDefinitionServiceMock, times(2)).fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag);
    verify(processDefinitionServiceMock).fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag);
    verify(runtimeServiceMock, times(3)).createProcessInstanceQuery();
    verify(processInstanceQueryMock, times(2)).activityIdIn(firstProcessActivity, secondProcessActivity);
    verify(migrationPlanServiceMock).createMigrationPlan(eq(sourceProcessDefinitionMock), eq(targetProcessDefinitionMock), any(MigrationPlan.class));
    verify(runtimeServiceMock).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
    verify(migrationPlanExecutionBuilderMock).skipCustomListeners();
    verify(migrationPlanExecutionBuilderMock).skipIoMappings();
    verify(migrationPlanExecutionBuilderMock).execute();
    verify(bpmnModelInstance, times(2)).getModelElementById(anyString());
  }

  @Test
  public void shouldSkipMigrationWhenProcessInstanceQueryActivityIdDoesNotExistsInModel() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";

    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(sourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    final String firstProcessActivity = "firstProcessActivity";
    final String secondProcessActivity = "secondProcessActivity";
    processInstanceQuery.setProcessActivityIds(Arrays.asList(firstProcessActivity, secondProcessActivity));
    migration.setProcessInstanceQuery(processInstanceQuery);
    migration.setSkipCustomListeners(true);
    migration.setSkipIoMappings(true);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Collections.singletonList(sourceProcessDefinitionMock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQueryMock = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.processDefinitionId(anyString())).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.count()).thenReturn(1L);
    MigrationPlanExecutionBuilder migrationPlanExecutionBuilderMock = mock(MigrationPlanExecutionBuilder.class);
    when(runtimeServiceMock.newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class))).thenReturn(migrationPlanExecutionBuilderMock);
    when(migrationPlanExecutionBuilderMock.processInstanceQuery(any(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class))).thenReturn(migrationPlanExecutionBuilderMock);

    final BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
    when(bpmnModelInstance.getModelElementById(firstProcessActivity)).thenReturn(mock(ModelElementInstance.class));
    when(repositoryServiceMock.getBpmnModelInstance(sourceProcessDefinitionMock.getId())).thenReturn(bpmnModelInstance);

    // when
    Exception result = null;
    try {
      migrationService.migrate(changelogVersion);
      fail("Should fail");
    } catch (Exception e) {
      result = e;
    }

    // then
    assertThat(result, instanceOf(IllegalMigrationStateException.class));
    verify(repositoryServiceMock).getBpmnModelInstance(any());

  }

  @Test
  public void shouldMigrateAndExecuteIoMappingsAndCustomListenersWhenSourceProcessInstancesExist() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";

    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(sourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    migration.setProcessInstanceQuery(processInstanceQuery);
    migration.setSkipCustomListeners(false);
    migration.setSkipIoMappings(false);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Collections.singletonList(sourceProcessDefinitionMock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQueryMock = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.processDefinitionId(anyString())).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.count()).thenReturn(1L);
    MigrationPlanExecutionBuilder migrationPlanExecutionBuilderMock = mock(MigrationPlanExecutionBuilder.class);
    when(runtimeServiceMock.newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class))).thenReturn(migrationPlanExecutionBuilderMock);
    when(migrationPlanExecutionBuilderMock.processInstanceQuery(any(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class))).thenReturn(migrationPlanExecutionBuilderMock);

    // when
    migrationService.migrate(changelogVersion);

    // then
    verify(processDefinitionServiceMock).fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag);
    verify(processDefinitionServiceMock).fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag);
    verify(runtimeServiceMock, times(3)).createProcessInstanceQuery();
    verify(migrationPlanServiceMock).createMigrationPlan(eq(sourceProcessDefinitionMock), eq(targetProcessDefinitionMock), any(MigrationPlan.class));
    verify(runtimeServiceMock).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
    verify(migrationPlanExecutionBuilderMock, never()).skipCustomListeners();
    verify(migrationPlanExecutionBuilderMock, never()).skipIoMappings();
    verify(migrationPlanExecutionBuilderMock).execute();
  }

  @Test
  public void shouldSkipMigrationWhenNoMigrationFileExists() throws IOException, IllegalMigrationStateException {
    // given
    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);

    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.FALSE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    // when
    migrationService.migrate(changelogVersion);

    // then
    verify(runtimeServiceMock, never()).createProcessInstanceQuery();
    verify(migrationPlanServiceMock, never()).createMigrationPlan(any(), any(), any(MigrationPlan.class));
    verify(runtimeServiceMock, never()).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
  }

  @Test
  public void shouldMigrateTwoSourceProcessDefinitionsWithTheSameKey() throws IOException, IllegalMigrationStateException {
    // given
    String sourceProcessDefinitionKey = "sourceProcessDefinitionKey";
    String sourceProcessVersionTag = "sourceProcessVersionTag";
    String targetProcessDefinitionKey = "targetProcessDefinitionKey";
    String targetProcessVersionTag = "targetProcessVersionTag";
    String sourceProcessDefinitionId = "sourceProcessDefinitionId";
    String targetProcessDefinitionId = "targetProcessDefinitionId";

    String migrationFolder = "version1";

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder(migrationFolder);


    Resource resource = mock(Resource.class);
    when(resource.exists()).thenReturn(Boolean.TRUE);
    when(resourceLoaderMock.getResource(CLASSPATH_TO_MIGRATION_PLAN + migrationFolder + "/migration.json")).thenReturn(resource);

    ProcessDefinition sourceProcessDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition sourceProcessDefinition2Mock = mock(ProcessDefinition.class);
    ProcessDefinition targetProcessDefinitionMock = mock(ProcessDefinition.class);

    MigrationCollection migrationCollection = new MigrationCollection();
    Migration migration = new Migration();
    migrationCollection.getMigrations().add(migration);
    ProcessInstanceQuery processInstanceQuery = new ProcessInstanceQuery();
    processInstanceQuery.setProcessDefinitionKey(sourceProcessDefinitionKey);
    processInstanceQuery.setProcessVersionTag(sourceProcessVersionTag);
    migration.setProcessInstanceQuery(processInstanceQuery);
    migration.setSkipCustomListeners(true);
    migration.setSkipIoMappings(true);

    MigrationPlan migrationPlan = new MigrationPlan();
    migrationPlan.setSourceProcessDefinitionKey(sourceProcessDefinitionKey);
    migrationPlan.setTargetProcessDefinitionKey(targetProcessDefinitionKey);
    migrationPlan.setSourceProcessVersionTag(sourceProcessVersionTag);
    migrationPlan.setTargetProcessVersionTag(targetProcessVersionTag);
    migration.setMigrationPlan(migrationPlan);

    when(objectMapperServiceMock.convertMigrationCollection(resource)).thenReturn(migrationCollection);

    when(processDefinitionServiceMock.fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag)).thenReturn(Arrays.asList(sourceProcessDefinitionMock, sourceProcessDefinition2Mock));
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag)).thenReturn(targetProcessDefinitionMock);
    when(sourceProcessDefinitionMock.getId()).thenReturn(sourceProcessDefinitionId);
    when(targetProcessDefinitionMock.getId()).thenReturn(targetProcessDefinitionId);

    org.camunda.bpm.engine.runtime.ProcessInstanceQuery processInstanceQueryMock = mock(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.processDefinitionId(anyString())).thenReturn(processInstanceQueryMock);
    when(processInstanceQueryMock.count()).thenReturn(1L);
    MigrationPlanExecutionBuilder migrationPlanExecutionBuilderMock = mock(MigrationPlanExecutionBuilder.class);
    when(runtimeServiceMock.newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class))).thenReturn(migrationPlanExecutionBuilderMock);
    when(migrationPlanExecutionBuilderMock.processInstanceQuery(any(org.camunda.bpm.engine.runtime.ProcessInstanceQuery.class))).thenReturn(migrationPlanExecutionBuilderMock);

    // when
    migrationService.migrate(changelogVersion);

    // then
    verify(processDefinitionServiceMock).fetchProcessDefinitionsByKeyAndVersionTag(sourceProcessDefinitionKey, sourceProcessVersionTag);
    verify(processDefinitionServiceMock).fetchLatestProcessDefinitionByKeyAndVersionTag(targetProcessDefinitionKey, targetProcessVersionTag);
    verify(runtimeServiceMock, times(6)).createProcessInstanceQuery();
    verify(migrationPlanServiceMock).createMigrationPlan(eq(sourceProcessDefinitionMock), eq(targetProcessDefinitionMock), any(MigrationPlan.class));
    verify(runtimeServiceMock, times(2)).newMigration(any(org.camunda.bpm.engine.migration.MigrationPlan.class));
    verify(migrationPlanExecutionBuilderMock, times(2)).skipCustomListeners();
    verify(migrationPlanExecutionBuilderMock, times(2)).skipIoMappings();
    verify(migrationPlanExecutionBuilderMock, times(2)).execute();
  }
}