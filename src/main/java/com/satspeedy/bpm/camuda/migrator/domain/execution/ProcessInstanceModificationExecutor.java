package com.satspeedy.bpm.camuda.migrator.domain.execution;

/**
 * Process instance modification executor interface to differentiate between engine and own impl.
 */
public interface ProcessInstanceModificationExecutor {

  /**
   * Execute engine or own impl.
   */
  void execute();

}
