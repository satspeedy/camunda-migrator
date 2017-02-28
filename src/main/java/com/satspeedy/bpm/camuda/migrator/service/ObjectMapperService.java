package com.satspeedy.bpm.camuda.migrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import com.satspeedy.bpm.camuda.migrator.domain.MigrationCollection;
import com.satspeedy.bpm.camuda.migrator.domain.ModificationCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for Object mapping.
 */
@Service
public class ObjectMapperService {

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Converts a JSON object to a {@link MigrationCollection}.
   * @param resource resource to read
   * @return a new MigrationCollection
   * @throws IOException IOException
   */
  public MigrationCollection convertMigrationCollection(Resource resource) throws IOException {
    return objectMapper.readValue(resource.getInputStream(), MigrationCollection.class);
  }

  /**
   * Converts a JSON object to a {@link ModificationCollection}.
   *
   * @param resource resource to read
   * @return a new ModificationCollection
   * @throws IOException IOException
   */
  public ModificationCollection convertModificationCollection(Resource resource) throws IOException {
    return objectMapper.readValue(resource.getInputStream(), ModificationCollection.class);
  }

  /**
   * Converts a JSON object to a Changelog.
   * @param resource resource to read
   * @return new ChangelogRO
   * @throws IOException IOException
   */
  public Changelog convertChangelog(Resource resource) throws IOException {
    return objectMapper.readValue(resource.getInputStream(), Changelog.class);
  }

}
