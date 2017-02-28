package com.satspeedy.bpm.camuda.migrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satspeedy.bpm.camuda.migrator.domain.MigrationCollection;
import com.satspeedy.bpm.camuda.migrator.domain.ModificationCollection;
import com.satspeedy.bpm.camuda.migrator.service.ObjectMapperService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class ResourceValidationTest {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceValidationTest.class);

  @Autowired
  private ObjectMapperService objectMapperService;

  private ResourceLoader resourceLoader = new DefaultResourceLoader();

  @Bean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

  @Test
  public void shouldValidateAllModificationFiles() throws IOException {
    // given
    List<Resource> resources = this.loadResources("classpath*:process/migrationplan/*/modification_before.json");

    // when
    for (Resource resource : resources) {
      LOG.info("Validating file " + resource.getURL());
      ModificationCollection modificationCollection = objectMapperService.convertModificationCollection(resource);
      assertThat(modificationCollection, notNullValue());
    }

  }

  @Test
  public void shouldValidateAllMigrationFiles() throws IOException {
    // given
    List<Resource> resources = this.loadResources("classpath*:process/migrationplan/*/migration.json");

    // when
    for (Resource resource : resources) {
      LOG.info("Validating file " + resource.getURL());
      MigrationCollection migrationCollection = objectMapperService.convertMigrationCollection(resource);
      assertThat(migrationCollection, notNullValue());
    }

  }

  List<Resource> loadResources(String pattern) throws IOException {
    return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
  }
}