package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipInputStream;

import static com.satspeedy.bpm.camuda.migrator.service.DeploymentService.CLASSPATH_TO_ARCHIVE_FILES;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentServiceTest {

  @InjectMocks
  private DeploymentService deploymentService;

  @Mock
  private ZipResourceService zipResourceServiceMock;

  @Mock
  private ProcessDefinitionService processDefinitionServiceMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryServiceMock;

  @Mock
  private ObjectMapperService objectMapperServiceMock;

  @Mock
  private DeploymentBuilder deploymentBuilderMock;

  @Mock
  private ChangelogService changelogServiceMock;

  @Test
  public void shouldDeployWhenResourcesExistAndVersionNotDeployedBefore() throws IOException {
    // given
    final String resourceFileName = "resource.zip";
    final Resource resource = mock(Resource.class);
    InputStream inputStreamResource = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(inputStreamResource);
    when(resource.getFilename()).thenReturn(resourceFileName);

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setArchiveFile(resourceFileName);

    when(zipResourceServiceMock.loadZipResource(CLASSPATH_TO_ARCHIVE_FILES + resourceFileName)).thenReturn(resource);
    when(repositoryServiceMock.createDeployment().addZipInputStream(any(ZipInputStream.class)).name(DeploymentService.DEPLOYMENT_NAME).source(DeploymentService.DEPLOYMENT_SOURCE).enableDuplicateFiltering(DeploymentService.DEPLOY_CHANGED_ONLY)).thenReturn(deploymentBuilderMock);
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag(any()).list()).thenReturn(Collections.emptyList());

    // when
    deploymentService.deploy(changelogVersion);

    // then
    verify(deploymentBuilderMock, times(1)).deploy();
  }

  @Test
  public void shouldSkipDeployingWhenNoResourcesExist() throws IOException {
    // given
    final String resourceFileName = "resource1.zip";
    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setVersionTag("V1");
    changelogVersion.setArchiveFile(resourceFileName);

    when(zipResourceServiceMock.loadZipResource(CLASSPATH_TO_ARCHIVE_FILES + resourceFileName)).thenThrow(IOException.class);
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag("V1").list()).thenReturn(Collections.emptyList());

    // when
    Exception resultedException = null;
    try {
      deploymentService.deploy(changelogVersion);
      fail("Exception expected!");
    } catch (Exception e) {
      resultedException = e;
    }

    // then
    assertThat(resultedException, instanceOf(IllegalMigrationStateException.class));
    verify(repositoryServiceMock, never()).createDeployment();
    verify(deploymentBuilderMock, never()).addZipInputStream(any(ZipInputStream.class));
    verify(deploymentBuilderMock, never()).deploy();
  }

  @Test
  public void shouldDownloadZipResourceWhenArchiveFileContainsAnUrl() throws IOException {
    // given
    final String resourcePath = "file:C:/Users/TST/AppData/Local/Temp/Release_1_0-Sprint_1-1.zip";
    final String resourceFileName = "http://www.example.com/Release_1_0-Sprint_1-1.zip";
    final Resource resource = mock(Resource.class);
    InputStream inputStreamResource = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(inputStreamResource);
    when(resource.getFilename()).thenReturn(resourceFileName);

    ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setArchiveFile(resourceFileName);

    when(zipResourceServiceMock.downloadZipResource(changelogVersion.getArchiveFile(), changelogVersion.getVersionTag())).thenReturn(resourcePath);
    when(zipResourceServiceMock.loadZipResource(resourcePath)).thenReturn(resource);
    when(repositoryServiceMock.createDeployment().addZipInputStream(any(ZipInputStream.class)).name(DeploymentService.DEPLOYMENT_NAME).source(DeploymentService.DEPLOYMENT_SOURCE).enableDuplicateFiltering(DeploymentService.DEPLOY_CHANGED_ONLY)).thenReturn(deploymentBuilderMock);
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag(any()).list()).thenReturn(Collections.emptyList());

    // when
    deploymentService.deploy(changelogVersion);

    // then
    verify(deploymentBuilderMock, times(1)).deploy();
  }

  @Test
  public void shouldAssertVersionIsAlreadyDeployedAndZipFileHasNoChanges() {
    //given
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    final Resource zipResource = mock(Resource.class);
    changelogVersion.setVersionTag("V1");
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag("V1").list()).thenReturn(Collections.singletonList(mock(ProcessDefinition.class)));
    final String bpmnHash = "111";
    when(processDefinitionServiceMock.extractBPMNHashValuesFromEngine()).thenReturn(Collections.singletonList(bpmnHash));
    when(zipResourceServiceMock.extractHashValuesFromZipEntries(changelogVersion, zipResource)).thenReturn(Collections.singletonList(bpmnHash));

    //when
    final boolean result = deploymentService.isVersionAlreadyDeployed(changelogVersion, zipResource);

    //then
    assertThat(result, is(true));
  }

  @Test
  public void shouldAssertVersionIsNotAlreadyDeployedWhenZipFileHasChanges() {
    //given
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    final Resource zipResource = mock(Resource.class);
    changelogVersion.setVersionTag("V1");
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag("V1").list()).thenReturn(Collections.singletonList(mock(ProcessDefinition.class)));
    final String bpmnHash = "111";
    final String newBpmnHash = "222";
    when(processDefinitionServiceMock.extractBPMNHashValuesFromEngine()).thenReturn(Collections.singletonList(bpmnHash));
    when(zipResourceServiceMock.extractHashValuesFromZipEntries(changelogVersion, zipResource)).thenReturn(Collections.singletonList(newBpmnHash));

    //when
    final boolean result = deploymentService.isVersionAlreadyDeployed(changelogVersion, zipResource);

    //then
    assertThat(result, is(false));
  }

  @Test
  public void shouldAssertVersionIsNotAlreadyDeployed() {
    //given
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    changelogVersion.setVersionTag("V1");
    when(repositoryServiceMock.createProcessDefinitionQuery().versionTag("V1").list()).thenReturn(Collections.emptyList());
    final Resource zipResource = mock(Resource.class);

    //when
    final boolean result = deploymentService.isVersionAlreadyDeployed(changelogVersion, zipResource);

    //then
    assertThat(result, is(false));
  }
}