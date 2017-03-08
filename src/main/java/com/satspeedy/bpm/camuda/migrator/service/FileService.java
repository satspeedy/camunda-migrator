package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Service for handling file resources.
 */
@Service
public class FileService {

  /**
   * Create a temporary file in system temp directory.
   *
   * @param fileNamePrefix file name prefix (e.g. 'filename')
   * @param fileNameSuffix file name suffix (e.g. '.zip')
   * @param deleteOnExit delete file on exit jvm
   * @return new temp file
   */
  protected File createTempFile(String fileNamePrefix, String fileNameSuffix, boolean deleteOnExit) {
    File file;
    try {
      file = File.createTempFile(fileNamePrefix, fileNameSuffix);
    } catch (IOException e) {
      throw new IllegalMigrationStateException("Could not create temp file " + fileNamePrefix + "." + fileNameSuffix, e);
    }
    if (deleteOnExit) {
      file.deleteOnExit();
    }
    return file;
  }

  /**
   * Copy bytes from the URL source to a file destination.
   * The directories up to destination will be created if they don't already exist.
   *
   * @param url url (e.g. https://www.dropbox.com/8cqkt0aieim37/Release_1_0-Sprint_2-1.zip?dl=1)
   * @param file File to write bytes
   * @return given file
   */
  protected File copyURLToFile(String url, File file) {
    try {
      FileUtils.copyURLToFile(new URL(url), file);
    } catch (IOException e) {
      throw new IllegalMigrationStateException("Could not download resource from " + url + " and save it under " + file.getPath(), e);
    }
    return file;
  }

}
