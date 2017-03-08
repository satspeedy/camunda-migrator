package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ZipResourceServiceTest {
  
  @InjectMocks
  @Spy
  private ZipResourceService zipResourceService;

  @Mock
  private ResourcePatternResolver resourceLoaderMock;

  @Mock
  private FileService fileServiceMock;

  @Test
  public void shouldLoadZipResource() throws IOException {
    //given
    final String file = "file";
    final Resource resource = mock(Resource.class);
    when(resourceLoaderMock.getResource(file)).thenReturn(resource);

    //when
    final Resource result = zipResourceService.loadZipResource(file);

    //then
    assertThat(result, equalTo(resource));
  }

  @Test
  public void shouldOpenZipResource() {
    //given
    final Resource resource = mock(Resource.class);

    //when
    final ZipInputStream result = zipResourceService.openZipResource(resource);

    //then
    assertThat(result, notNullValue());
  }

  @Test
  public void shouldExtractHashValuesFromZipEntries() throws IOException {
    //given
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    final Resource zipResource = mock(Resource.class);
    ZipInputStream zipInputStream = mock(ZipInputStream.class);
    final ZipEntry zipEntryDirectory = mock(ZipEntry.class);
    final ZipEntry zipEntry = mock(ZipEntry.class);
    when(zipInputStream.getNextEntry()).thenReturn(zipEntryDirectory).thenReturn(zipEntry).thenReturn(null);
    when(zipEntryDirectory.isDirectory()).thenReturn(true);
    when(zipEntry.isDirectory()).thenReturn(false);
    final String zipEntryName = "zipEntry";
    when(zipEntry.getName()).thenReturn(zipEntryName);
    final String hash = "hash";
    doReturn(zipInputStream).when(zipResourceService).openZipResource(zipResource);
    doReturn(hash).when(zipResourceService).createHashForFile(zipInputStream, zipEntryName);

    //when
    final List<String> result = zipResourceService.extractHashValuesFromZipEntries(changelogVersion, zipResource);

    //then
    verify(zipResourceService).createHashForFile(zipInputStream, zipEntryName);
    assertThat(result, hasSize(1));
    assertThat(result, hasItem(hash));
  }

  @Test
  public void shouldDownloadZipResource() {
    // given
    String url = "http://www.example.com/archive.zip";
    String fileName = "filename";
    String fileNameSuffix = ".zip";
    File file = mock(File.class);
    when(file.getPath()).thenReturn("C:/Users/TST/AppData/Local/Temp/filename.zip");

    when(fileServiceMock.createTempFile(fileName, fileNameSuffix, true)).thenReturn(file);
    when(fileServiceMock.copyURLToFile(url, file)).thenReturn(file);

    // when
    String filePath = zipResourceService.downloadZipResource(url, fileName);

    // then
    assertThat(filePath, equalTo("file:C:/Users/TST/AppData/Local/Temp/filename.zip"));
  }
}