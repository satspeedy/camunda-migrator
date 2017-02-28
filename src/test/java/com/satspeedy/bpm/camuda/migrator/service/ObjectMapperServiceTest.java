package com.satspeedy.bpm.camuda.migrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import com.satspeedy.bpm.camuda.migrator.domain.MigrationCollection;
import com.satspeedy.bpm.camuda.migrator.domain.ModificationCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperServiceTest {

  @InjectMocks
  private ObjectMapperService objectMapperService;

  @Mock
  private ObjectMapper objectMapperMock;

  @Test
  public void shouldConvertMigrationCollection() throws IOException {
    // given
    Resource resource = mock(Resource.class);
    MigrationCollection migrationCollectionMock = mock(MigrationCollection.class);

    when(objectMapperMock.readValue(any(InputStream.class), eq(MigrationCollection.class))).thenReturn(migrationCollectionMock);

    // when
    MigrationCollection result = objectMapperService.convertMigrationCollection(resource);

    // then
    assertThat(result, equalTo(migrationCollectionMock));
  }

  @Test
  public void shouldConvertJsonToChangelog() throws IOException {
    // given
    Resource resource = mock(Resource.class);
    Changelog changelogMock = mock(Changelog.class);

    when(objectMapperMock.readValue(any(InputStream.class), eq(Changelog.class))).thenReturn(changelogMock);

    // when
    Changelog result = objectMapperService.convertChangelog(resource);

    // then
    assertThat(result, equalTo(changelogMock));
  }

  @Test
  public void shouldConvertModificationCollection() throws IOException {
    // given
    Resource resource = mock(Resource.class);
    ModificationCollection modificationCollectionMock = mock(ModificationCollection.class);

    when(objectMapperMock.readValue(any(InputStream.class), eq(ModificationCollection.class))).thenReturn(modificationCollectionMock);

    // when
    ModificationCollection result = objectMapperService.convertModificationCollection(resource);

    // then
    assertThat(result, equalTo(modificationCollectionMock));
  }
}