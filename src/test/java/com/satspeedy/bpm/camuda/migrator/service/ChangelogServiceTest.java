package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import org.camunda.bpm.engine.RepositoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangelogServiceTest {

  @InjectMocks
  private ChangelogService changelogService;

  @Mock
  private ResourcePatternResolver resourceLoaderMock;

  @Mock
  private ObjectMapperService objectMapperServiceMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryServiceMock;

  @Test
  public void shouldFetchChangelog() throws IOException {
    //given

    final Resource resourceChangelog = mock(Resource.class);
    final Changelog changelog = new Changelog();

    when(resourceLoaderMock.getResource(ChangelogService.CLASSPATH_TO_CHANGELOG)).thenReturn(resourceChangelog);
    when(objectMapperServiceMock.convertChangelog(resourceChangelog)).thenReturn(changelog);

    //when
    final Changelog result = changelogService.loadChangelog();

    //then
    assertThat(result, equalTo(changelog));
  }
}