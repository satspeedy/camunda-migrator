package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.*;
import com.satspeedy.bpm.camuda.migrator.domain.execution.ProcessInstanceModification;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.*;

import static com.satspeedy.bpm.camuda.migrator.service.ModificationService.AFTER_MIGRATION_RETURN_POINT;
import static com.satspeedy.bpm.camuda.migrator.service.ModificationService.AFTER_MIGRATION_SKIP_IO_MAPPINGS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModificationServiceTest {

  @Spy
  @InjectMocks
  public ModificationService modificationService;

  @Mock
  private ProcessDefinitionService processDefinitionServiceMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RuntimeService runtimeService;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryServiceMock;

  @Mock
  private ResourcePatternResolver resourcePatternResolver;

  @Mock
  private ObjectMapperService objectMapperService;

  @Test
  public void shouldFindProcessInstancesToModify() {
    //given
    String activityId = "activityId";
    Modification modification = new Modification();
    modification.setSourceProcessDefinitionKey("processDefinitionKey");
    modification.setSourceProcessVersionTag("versionTag");
    ModificationInstruction modificationInstruction = new ModificationInstruction();
    modificationInstruction.setType(ModificationInstructionType.CANCEL);
    modificationInstruction.setActivityId(activityId);
    ModificationInstruction modificationInstructionForAddVariable = new ModificationInstruction();
    modificationInstruction.setType(ModificationInstructionType.ADD_VARIABLE);
    modificationInstruction.setActivityId(activityId);

    List<ModificationInstruction> instructions = Arrays.asList(modificationInstruction, modificationInstructionForAddVariable);
    modification.setInstructions(instructions);

    String processDefinitionId = "processDefinitionId";
    ProcessDefinition processDefinitionMock = mock(ProcessDefinition.class);
    when(processDefinitionMock.getId()).thenReturn(processDefinitionId);
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(modification.getSourceProcessDefinitionKey(), modification.getSourceProcessVersionTag())).thenReturn(processDefinitionMock);

    List<ProcessInstance> processInstances = mock(List.class);
    when(runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinitionId).activityIdIn(activityId).list()).thenReturn(processInstances);

    //when
    List<ProcessInstance> processInstancesToModify = modificationService.findProcessInstancesToModify(modification);

    //then
    assertThat(processInstancesToModify, equalTo(processInstances));
  }

  @Test(expected = IllegalMigrationStateException.class)
  public void shouldThrowExceptionOnFindProcessInstancesToModifyIfNoCancelDefined() {
    //given
    String activityId = "activityId";
    Modification modification = new Modification();
    modification.setSourceProcessDefinitionKey("processDefinitionKey");
    modification.setSourceProcessVersionTag("versionTag");
    ModificationInstruction modificationInstruction = new ModificationInstruction();
    modificationInstruction.setType(ModificationInstructionType.START_AFTER_ACTIVITY);
    modificationInstruction.setActivityId(activityId);
    List<ModificationInstruction> instructions = Collections.singletonList(modificationInstruction);
    modification.setInstructions(instructions);

    String processDefinitionId = "processDefinitionId";
    ProcessDefinition processDefinitionMock = mock(ProcessDefinition.class);
    when(processDefinitionMock.getId()).thenReturn(processDefinitionId);
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(modification.getSourceProcessDefinitionKey(), modification.getSourceProcessVersionTag())).thenReturn(processDefinitionMock);

    //when
    modificationService.findProcessInstancesToModify(modification);

    //then
    //exception expected
  }

  @Test
  public void shouldCreateProcessInstanceModificationAndExecuteThem() {
    //given
    ProcessInstance processInstance = mock(ProcessInstance.class);
    final String processInstanceId = "processInstanceId";
    Modification modification = new Modification();
    String cancelActivityId = "cancelActivityId";
    String startAfterActivityId = "startAfterActivityId";
    String startBeforeActivityId = "startBeforeActivityId";
    String addVariableActivityId = "addVariableActivityId";
    Map<String, ModificationVariable> startAfterAcitivityVariables = new HashMap<>();
    startAfterAcitivityVariables.put("startAfterAcitivityKey", new ModificationVariable("String", "startAfterAcitivityValue"));
    Map<String, ModificationVariable> startBeforeAcitivityVariables = new HashMap<>();
    startBeforeAcitivityVariables.put("startBeforeAcitivityKey1", new ModificationVariable("String", "startBeforeAcitivityValue1"));
    startBeforeAcitivityVariables.put("startBeforeAcitivityKey2", new ModificationVariable("String", "startBeforeAcitivityValue2"));
    Map<String, ModificationVariable> addVariableAcitivityVariables = new HashMap<>();
    addVariableAcitivityVariables.put("addVariableAcitivityKey1", new ModificationVariable("String", "addVariableAcitivityValue1"));
    addVariableAcitivityVariables.put("addVariableAcitivityKey2", new ModificationVariable("String", "addVariableAcitivityValue2"));
    ModificationInstruction cancelInstruction = createModificationInstruction(ModificationInstructionType.CANCEL, cancelActivityId, null);
    ModificationInstruction startAfterInstruction = createModificationInstruction(ModificationInstructionType.START_AFTER_ACTIVITY, startAfterActivityId, startAfterAcitivityVariables);
    ModificationInstruction startBeforeInstruction = createModificationInstruction(ModificationInstructionType.START_BEFORE_ACTIVITY, startBeforeActivityId, startBeforeAcitivityVariables);
    ModificationInstruction addVariableInstruction = createModificationInstruction(ModificationInstructionType.ADD_VARIABLE, addVariableActivityId, addVariableAcitivityVariables);
    modification.setInstructions(Arrays.asList(cancelInstruction, startAfterInstruction, startBeforeInstruction, addVariableInstruction));

    ProcessInstanceModificationBuilder processInstanceModificationBuilderMock = mock(ProcessInstanceModificationBuilder.class);
    ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilderMock = mock(ProcessInstanceModificationInstantiationBuilder.class);

    when(processInstance.getProcessInstanceId()).thenReturn(processInstanceId);
    when(runtimeService.createProcessInstanceModification(processInstance.getProcessInstanceId())).thenReturn(processInstanceModificationBuilderMock);
    when(processInstanceModificationBuilderMock.cancelAllForActivity(cancelActivityId)).thenReturn(processInstanceModificationBuilderMock);
    when(processInstanceModificationBuilderMock.startAfterActivity(startAfterActivityId)).thenReturn(processInstanceModificationInstantiationBuilderMock);
    when(processInstanceModificationInstantiationBuilderMock.startBeforeActivity(startBeforeActivityId)).thenReturn(processInstanceModificationInstantiationBuilderMock);
    when(processInstanceModificationInstantiationBuilderMock.setVariable(anyString(), any())).thenReturn(processInstanceModificationInstantiationBuilderMock);

    //when
    ProcessInstanceModification result = modificationService.createProcessInstanceModification(processInstance, modification);

    //then
    assertThat(result.getProcessInstanceModificationBuilder(), equalTo(processInstanceModificationInstantiationBuilderMock));
    InOrder inOrder = inOrder(processInstanceModificationBuilderMock, processInstanceModificationInstantiationBuilderMock);
    inOrder.verify(processInstanceModificationBuilderMock).cancelAllForActivity(cancelActivityId);
    inOrder.verify(processInstanceModificationBuilderMock).startAfterActivity(startAfterActivityId);
    inOrder.verify(processInstanceModificationInstantiationBuilderMock).setVariable("startAfterAcitivityKey", "startAfterAcitivityValue");
    inOrder.verify(processInstanceModificationInstantiationBuilderMock).startBeforeActivity(startBeforeActivityId);
    inOrder.verify(processInstanceModificationInstantiationBuilderMock, times(2)).setVariable(anyString(), any());
    verify(processInstanceModificationInstantiationBuilderMock).setVariable("startBeforeAcitivityKey1", "startBeforeAcitivityValue1");
    verify(processInstanceModificationInstantiationBuilderMock).setVariable("startBeforeAcitivityKey2", "startBeforeAcitivityValue2");
    assertThat(result.getProcessInstanceModificationExecutorList(), hasSize(2));

    //when we execute the processInstanceModification
    result.execute();

    //then
    verify(result.getProcessInstanceModificationBuilder()).execute(false, false);
    verify(runtimeService).setVariable(processInstanceId, "addVariableAcitivityKey1", "addVariableAcitivityValue1");
    verify(runtimeService).setVariable(processInstanceId, "addVariableAcitivityKey2", "addVariableAcitivityValue2");
  }

  @Test
  public void shouldModifyBeforeMigrationIfFileExists() throws Exception {
    //given
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder("test");
    Resource resourceMock = mock(Resource.class);
    when(resourceMock.exists()).thenReturn(true);
    when(resourcePatternResolver.getResource("classpath:/process/migrationplan/test/modification_before.json")).thenReturn(resourceMock);
    ModificationCollection modificationCollectionMock = mock(ModificationCollection.class);
    when(objectMapperService.convertModificationCollection(resourceMock)).thenReturn(modificationCollectionMock);
    Modification modificationMock = mock(Modification.class);
    when(modificationCollectionMock.getModifications()).thenReturn(Collections.singletonList(modificationMock));
    ProcessInstance processInstanceMock = mock(ProcessInstance.class);
    doReturn(Collections.singletonList(processInstanceMock)).when(modificationService).findProcessInstancesToModify(modificationMock);
    ProcessInstanceModification processInstanceModificationMock = mock(ProcessInstanceModification.class);
    doReturn(processInstanceModificationMock).when(modificationService).createProcessInstanceModification(processInstanceMock, modificationMock);
    doNothing().when(modificationService).validate(modificationMock);

    //when
    modificationService.modifyBeforeMigration(changelogVersion);

    //then
    verify(processInstanceModificationMock).execute();
  }

  @Test
  public void shouldModifyAfterMigrationIfFileExists() throws Exception {
    //given
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder("test");
    Resource resourceMock = mock(Resource.class);
    when(resourceMock.exists()).thenReturn(true);
    when(resourcePatternResolver.getResource("classpath:/process/migrationplan/test/modification_after.json")).thenReturn(resourceMock);
    ModificationCollection modificationCollectionMock = mock(ModificationCollection.class);
    when(objectMapperService.convertModificationCollection(resourceMock)).thenReturn(modificationCollectionMock);
    Modification modificationMock = mock(Modification.class);
    when(modificationCollectionMock.getModifications()).thenReturn(Collections.singletonList(modificationMock));
    ProcessInstance processInstanceMock = mock(ProcessInstance.class);
    doReturn(Collections.singletonList(processInstanceMock)).when(modificationService).findProcessInstancesToModify(modificationMock);
    ProcessInstanceModification processInstanceModificationMock = mock(ProcessInstanceModification.class);
    doReturn(processInstanceModificationMock).when(modificationService).createProcessInstanceModification(processInstanceMock, modificationMock);
    doNothing().when(modificationService).validate(modificationMock);

    //when
    modificationService.modifyAfterMigration(changelogVersion);

    //then
    verify(processInstanceModificationMock).execute();
  }

  @Test
  public void shouldNotModifyIfNoCancelActivityExists() throws Exception {
    //given
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder("test");
    Resource resourceMock = mock(Resource.class);
    when(resourceMock.exists()).thenReturn(true);
    when(resourcePatternResolver.getResource("classpath:/process/migrationplan/test/modification_before.json")).thenReturn(resourceMock);
    ModificationCollection modificationCollectionMock = mock(ModificationCollection.class);
    when(objectMapperService.convertModificationCollection(resourceMock)).thenReturn(modificationCollectionMock);
    Modification modificationMock = mock(Modification.class);
    ModificationInstruction modificationInstruction = new ModificationInstruction();
    modificationInstruction.setType(ModificationInstructionType.START_AFTER_ACTIVITY);
    String testActivityId = "testActivity";
    modificationInstruction.setActivityId(testActivityId);
    when(modificationMock.getInstructions()).thenReturn(Collections.singletonList(modificationInstruction));
    when(modificationCollectionMock.getModifications()).thenReturn(Collections.singletonList(modificationMock));

    //when
    Exception result = null;
    try {
      modificationService.modifyBeforeMigration(changelogVersion);
      fail("Should fail!");
    } catch (IllegalMigrationStateException e) {
      result = e;
    }

    //then
    assertThat(result, instanceOf(IllegalMigrationStateException.class));
    verify(modificationService, never()).createProcessInstanceModification(any(), any());
  }

  @Test
  public void shouldNotModifyIfReturnPointActivityNotExists() throws Exception {
    //given
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder("test");
    Resource resourceMock = mock(Resource.class);
    when(resourceMock.exists()).thenReturn(true);
    when(resourcePatternResolver.getResource("classpath:/process/migrationplan/test/modification_before.json")).thenReturn(resourceMock);
    ModificationCollection modificationCollectionMock = mock(ModificationCollection.class);
    when(objectMapperService.convertModificationCollection(resourceMock)).thenReturn(modificationCollectionMock);
    Modification modificationMock = mock(Modification.class);
    ModificationInstruction modificationInstruction = new ModificationInstruction();
    modificationInstruction.setType(ModificationInstructionType.CANCEL);
    String testActivityId = "testActivity";
    modificationInstruction.setActivityId(testActivityId);
    when(modificationMock.getInstructions()).thenReturn(Collections.singletonList(modificationInstruction));
    when(modificationCollectionMock.getModifications()).thenReturn(Collections.singletonList(modificationMock));
    ProcessInstance processInstanceMock = mock(ProcessInstance.class);
    doReturn(Collections.singletonList(processInstanceMock)).when(modificationService).findProcessInstancesToModify(modificationMock);
    ProcessInstanceModification processInstanceModificationMock = mock(ProcessInstanceModification.class);
    doReturn(processInstanceModificationMock).when(modificationService).createProcessInstanceModification(processInstanceMock, modificationMock);
    ProcessDefinition processDefinitionMock = mock(ProcessDefinition.class);
    String processDefinitionId = "processDefinitionId";
    when(processDefinitionMock.getId()).thenReturn(processDefinitionId);
    when(processDefinitionServiceMock.fetchLatestProcessDefinitionByKeyAndVersionTag(anyString(), anyString())).thenReturn(processDefinitionMock);

    BpmnModelInstance bpmnModelInstanceMock = mock(BpmnModelInstance.class);
    when(repositoryServiceMock.getBpmnModelInstance(processDefinitionId)).thenReturn(bpmnModelInstanceMock);
    when(bpmnModelInstanceMock.getModelElementById(testActivityId)).thenReturn(null);

    //when
    Exception result = null;
    try {
      modificationService.modifyBeforeMigration(changelogVersion);
      fail("Should fail!");
    } catch (IllegalMigrationStateException e) {
      result = e;
    }

    //then
    assertThat(result, instanceOf(IllegalMigrationStateException.class));
    verify(modificationService, never()).createProcessInstanceModification(any(), any());
    verify(processInstanceModificationMock, never()).execute();
  }

  @Test
  public void shouldNotModifyIfFileNotExists() throws Exception {
    //given
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setMigrationFolder("test");
    Resource resourceMock = mock(Resource.class);
    when(resourceMock.exists()).thenReturn(false);
    when(resourcePatternResolver.getResource("classpath:/process/migrationplan/test/modification_before.json")).thenReturn(resourceMock);
    //when
    modificationService.modifyBeforeMigration(changelogVersion);

    //then
    verify(objectMapperService, never()).convertModificationCollection(any());
    verify(modificationService, never()).createProcessInstanceModification(any(), any());
  }

  @Test
  public void shouldCleanUpModifiedProcessInstances() {
    //given
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    final VariableInstance variableInstance1 = createVariableInstanceMock("v1");
    final VariableInstance variableInstance2 = createVariableInstanceMock("v2");
    final List<VariableInstance> variableInstances = Arrays.asList(variableInstance1, variableInstance2);
    final ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilder1 = mock(ProcessInstanceModificationInstantiationBuilder.class);
    final ProcessInstanceModificationInstantiationBuilder processInstanceModificationInstantiationBuilder2 = mock(ProcessInstanceModificationInstantiationBuilder.class);
    when(runtimeService.createProcessInstanceModification(variableInstance1.getProcessInstanceId()).cancelActivityInstance(variableInstance1.getActivityInstanceId()).startBeforeActivity(variableInstance1.getValue().toString())).thenReturn(processInstanceModificationInstantiationBuilder1);
    when(runtimeService.createProcessInstanceModification(variableInstance2.getProcessInstanceId()).cancelActivityInstance(variableInstance2.getActivityInstanceId()).startBeforeActivity(variableInstance2.getValue().toString())).thenReturn(processInstanceModificationInstantiationBuilder2);
    final VariableInstance skipIoMappingsVariableInstance = mock(VariableInstance.class);
    when(skipIoMappingsVariableInstance.getValue()).thenReturn(false);
    final VariableInstance skipCustomListenersVariableInstance = mock(VariableInstance.class);
    when(skipCustomListenersVariableInstance.getValue()).thenReturn(false);
    when(runtimeService.createVariableInstanceQuery().processInstanceIdIn(any()).variableName(AFTER_MIGRATION_SKIP_IO_MAPPINGS).singleResult()).thenReturn(skipIoMappingsVariableInstance);

    when(runtimeService.createVariableInstanceQuery().variableName(AFTER_MIGRATION_RETURN_POINT).list()).thenReturn(variableInstances);

    //when
    modificationService.cleanUpAfterMigration(changelogVersion);

    //then
    verify(runtimeService.createProcessInstanceModification(any()).cancelActivityInstance(any())).startBeforeActivity(any());
    verify(processInstanceModificationInstantiationBuilder1).execute(true ,false);
    verify(processInstanceModificationInstantiationBuilder2).execute(true ,false);

    verify(runtimeService).removeVariables(variableInstance1.getExecutionId(), Arrays.asList(AFTER_MIGRATION_RETURN_POINT, AFTER_MIGRATION_SKIP_IO_MAPPINGS));
    verify(runtimeService).removeVariables(variableInstance2.getExecutionId(), Arrays.asList(AFTER_MIGRATION_RETURN_POINT, AFTER_MIGRATION_SKIP_IO_MAPPINGS));
  }

  private VariableInstance createVariableInstanceMock(String prefix) {
    final VariableInstance variableInstance = mock(VariableInstance.class);
    when(variableInstance.getName()).thenReturn(AFTER_MIGRATION_RETURN_POINT);
    when(variableInstance.getValue()).thenReturn(prefix + "ReturnPointTask");
    when(variableInstance.getExecutionId()).thenReturn(prefix + "ExecutionId");
    when(variableInstance.getActivityInstanceId()).thenReturn(prefix + "AcitivityInstanceId");
    when(variableInstance.getProcessInstanceId()).thenReturn(prefix + "ProcessInstanceId");
    return variableInstance;
  }

  private ModificationInstruction createModificationInstruction(ModificationInstructionType modificationInstructionType, String activityId, Map<String, ModificationVariable> variables) {
    ModificationInstruction modificationInstruction = new ModificationInstruction();
    modificationInstruction.setType(modificationInstructionType);
    modificationInstruction.setActivityId(activityId);
    modificationInstruction.setVariables(variables);
    return modificationInstruction;
  }
}