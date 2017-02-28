package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for handling zip resources.
 */
@Service
public class ZipResourceService {

  @Autowired
  private ResourcePatternResolver resourceLoader;

  /**
   * Load zip resource.
   * @param file file
   * @return Resource
   * @throws IOException IOException
   */
  protected Resource loadZipResource(String file) throws IOException {
    return resourceLoader.getResource(file);
  }

  /**
   * Open the given resource as a a ZipInputStream.
   * @param resource resource
   * @return ZipInputStream
   */
  protected ZipInputStream openZipResource(Resource resource) {
    try {
      return new ZipInputStream(new BufferedInputStream(resource.getInputStream()));
    } catch (IOException e) {
      throw new IllegalMigrationStateException("Could not deploy zip resource for version tag " + resource.getFilename(), e);
    }
  }

  /**
   * Retrieves a Hash for each file in the given zipResource.
   * @param changelogVersion changelogVersion
   * @param zipResource zipResource
   * @return list of Hashes as String
   */
  protected List<String> extractHashValuesFromZipEntries(ChangelogVersion changelogVersion, Resource zipResource) {
    final ZipInputStream zipInputStream = openZipResource(zipResource);
    List<String> result = new ArrayList<>();
    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory()) {
          String entryName = entry.getName();
          result.add(createHashForFile(zipInputStream, entryName));
        }
        entry = zipInputStream.getNextEntry();
      }
    } catch (IOException e) {
      throw new IllegalMigrationStateException("Could not read from zip resource for version tag " + changelogVersion.getVersionTag(), e);
    }
    return result;
  }

  /**
   * Creates a hash value for the given fileEntry in the zipInputStream.
   * @param inputStream inputStream
   * @param fileEntry fileEntry
   * @return hash value for the given fileEntry
   */
  protected String createHashForFile(InputStream inputStream, String fileEntry) {
    byte[] bytes = IoUtil.readInputStream(inputStream, fileEntry);
    return DigestUtils.md5DigestAsHex(bytes);
  }
}
