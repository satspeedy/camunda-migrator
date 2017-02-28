package com.satspeedy.bpm.camuda.migrator.exception;

/**
 * Exception for illegal migration state.
 */
public class IllegalMigrationStateException extends RuntimeException {

  /**
   * Constructs an {@code IllegalMigrationStateException} with the specified detail message.
   *
   * @param message
   *        The detail message (which is saved for later retrieval
   *        by the {@link #getMessage()} method)
   */
  public IllegalMigrationStateException(String message) {
    super(message);
  }

  /**
   * Constructs an {@code IllegalMigrationStateException} with the specified detail message
   * and cause.
   *
   * @param message
   *        The detail message (which is saved for later retrieval
   *        by the {@link #getMessage()} method)
   *
   * @param cause
   *        The cause (which is saved for later retrieval by the
   *        {@link #getCause()} method).  (A null value is permitted,
   *        and indicates that the cause is nonexistent or unknown.)
   */
  public IllegalMigrationStateException(String message, Throwable cause) {
    super(message, cause);
  }
}
