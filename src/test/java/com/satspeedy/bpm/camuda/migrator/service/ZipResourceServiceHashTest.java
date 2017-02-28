package com.satspeedy.bpm.camuda.migrator.service;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.DigestUtils;

import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IoUtil.class, DigestUtils.class})
public class ZipResourceServiceHashTest {
  
  private ZipResourceService zipResourceService = new ZipResourceService();

  @Test
  public void shouldCreateHashForFile() {
    //given
    PowerMockito.mockStatic(IoUtil.class);
    PowerMockito.mockStatic(DigestUtils.class);
    byte[] bytes = new byte[]{1,2,3};
    final String fileEntry = "fileEntry";
    final ZipInputStream zipInputStream = mock(ZipInputStream.class);
    when(IoUtil.readInputStream(zipInputStream, fileEntry)).thenReturn(bytes);
    final String hash = "hash";
    when(DigestUtils.md5DigestAsHex(bytes)).thenReturn(hash);

    //when
    final String result = zipResourceService.createHashForFile(zipInputStream, fileEntry);

    //then
    assertThat(result, equalTo(hash));
  }
}