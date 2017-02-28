package com.satspeedy.bpm.camuda.migrator.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

  @InjectMocks
  private ProcessDefinitionService processDefinitionService;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryServiceMock;

  @Mock
  private ZipResourceService zipResourceServiceMock;

  @Ignore
  @Test
  public void shouldReturnLatestProcessDefinition() {
    // given
    String processDefinitionKey = "processDefinitionKey";
    String processVersionTag = "processVersionTag";

    ProcessDefinition processDefinitionMock = mock(ProcessDefinition.class);

    ProcessDefinitionQuery processDefinitionQueryMock = mock(ProcessDefinitionQuery.class);
//    Query<ProcessDefinitionQuery, ProcessDefinition> processDefinitionQueryMock = (Query<ProcessDefinitionQuery, ProcessDefinition>) mock(Query.class);

    when(repositoryServiceMock.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .versionTag(processVersionTag)
      .orderByProcessDefinitionId()
      .desc()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.list()).thenReturn(Collections.singletonList(processDefinitionMock));

    // when
    ProcessDefinition processDefinition = processDefinitionService.fetchLatestProcessDefinitionByKeyAndVersionTag(processDefinitionKey, processVersionTag);

    // then
    assertThat(processDefinition, equalTo(processDefinitionMock));
  }

  @Ignore
  @Test
  public void shouldReturnProcessDefinitions() {
    // given
    String processDefinitionKey = "processDefinitionKey";
    String processVersionTag = "processVersionTag";

    ProcessDefinition processDefinitionMock = mock(ProcessDefinition.class);
    ProcessDefinition processDefinition2Mock = mock(ProcessDefinition.class);

    ProcessDefinitionQuery processDefinitionQueryMock = mock(ProcessDefinitionQuery.class);
    when(repositoryServiceMock.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey)
      .versionTag(processVersionTag)
      .orderByProcessDefinitionId()
      .desc()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.list()).thenReturn(Arrays.asList(processDefinitionMock, processDefinition2Mock));

    // when
    final List<ProcessDefinition> processDefinitions = processDefinitionService.fetchProcessDefinitionsByKeyAndVersionTag(processDefinitionKey, processVersionTag);

    // then
    assertThat(processDefinitions, hasSize(2));
    assertThat(processDefinitions, hasItems(processDefinitionMock, processDefinition2Mock));
  }

  @Test
  public void shouldExtractBPMNHashValuesFromEngine() {
    //given
    final String fileName1 = "fileName1";
    final String fileName2 = "fileName2";
    final String processDefinitionId1 = "processDefinition1";
    final String processDefinitionId2 = "processDefinition2";
    final String hash1 = "hash1";
    final String hash2 = "hash2";
    final ProcessDefinition processDefinition1 = mock(ProcessDefinition.class);
    final ProcessDefinition processDefinition2 = mock(ProcessDefinition.class);
    when(processDefinition1.getId()).thenReturn(processDefinitionId1);
    when(processDefinition2.getId()).thenReturn(processDefinitionId2);
    when(processDefinition1.getName()).thenReturn(fileName1);
    when(processDefinition2.getName()).thenReturn(fileName2);
    final InputStream inputStreamProcessDefinition1 = mock(InputStream.class);
    final InputStream inputStreamProcessDefinition2 = mock(InputStream.class);
    when(repositoryServiceMock.getProcessModel(processDefinitionId1)).thenReturn(inputStreamProcessDefinition1);
    when(repositoryServiceMock.getProcessModel(processDefinitionId2)).thenReturn(inputStreamProcessDefinition2);
    when(repositoryServiceMock.createProcessDefinitionQuery().list()).thenReturn(Arrays.asList(processDefinition1, processDefinition2));
    when(zipResourceServiceMock.createHashForFile(inputStreamProcessDefinition1, fileName1)).thenReturn(hash1);
    when(zipResourceServiceMock.createHashForFile(inputStreamProcessDefinition2, fileName2)).thenReturn(hash2);

    //when
    final List<String> result = processDefinitionService.extractBPMNHashValuesFromEngine();

    //then
    assertThat(result, hasItems(hash1, hash2));
  }
}