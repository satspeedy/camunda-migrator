package com.satspeedy.bpm.camuda.migrator.service;

import com.satspeedy.bpm.camuda.migrator.domain.Changelog;
import com.satspeedy.bpm.camuda.migrator.domain.ChangelogVersion;
import com.satspeedy.bpm.camuda.migrator.exception.IllegalMigrationStateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceMigrationServiceTest {

  @InjectMocks
  private ProcessInstanceMigrationService processInstanceMigrationService;

  @Mock
  private DeploymentService deploymentServiceMock;

  @Mock
  private MigrationService migrationServiceMock;

  @Mock
  private ChangelogService changelogServiceMock;

  @Mock
  private ModificationService modificationServiceMock;

  @Test
  public void shouldMigrateSuccessfulMultipleTimes() throws IOException, IllegalMigrationStateException {
    //given
    Changelog changelog = new Changelog();
    final ChangelogVersion changelogVersion1 = new ChangelogVersion();
    final ChangelogVersion changelogVersion2 = new ChangelogVersion();
    changelog.setVersionsOrdered(Arrays.asList(changelogVersion1, changelogVersion2));
    when(changelogServiceMock.loadChangelog()).thenReturn(changelog);

    // when
    processInstanceMigrationService.migrate();
    // then
    InOrder inOrder = inOrder(deploymentServiceMock, modificationServiceMock, migrationServiceMock);
    inOrder.verify(deploymentServiceMock).deploy(changelogVersion1);
    inOrder.verify(modificationServiceMock).modifyBeforeMigration(changelogVersion1);
    inOrder.verify(migrationServiceMock).migrate(changelogVersion1);
    inOrder.verify(modificationServiceMock).cleanUpAfterMigration(changelogVersion1);
    inOrder.verify(deploymentServiceMock).deploy(changelogVersion2);
    inOrder.verify(modificationServiceMock).modifyBeforeMigration(changelogVersion2);
    inOrder.verify(migrationServiceMock).migrate(changelogVersion2);
    inOrder.verify(modificationServiceMock).cleanUpAfterMigration(changelogVersion2);
  }

  @Test
  public void shouldSkipFollowingMigrationsWhenErrorOccurredDuringMigration() throws IOException, IllegalMigrationStateException {
    //given
    Changelog changelog = new Changelog();
    final ChangelogVersion changelogVersion = new ChangelogVersion();
    changelog.setVersionsOrdered(Collections.singletonList(changelogVersion));
    when(changelogServiceMock.loadChangelog()).thenReturn(changelog);
    doThrow(IllegalMigrationStateException.class).when(deploymentServiceMock).deploy(changelogVersion);

    // when
    try {
      processInstanceMigrationService.migrate();
    } catch (IllegalMigrationStateException e) {
      assertThat(e, notNullValue());
    }
    // then
    verify(deploymentServiceMock).deploy(changelogVersion);
    verify(migrationServiceMock, never()).migrate(changelogVersion);
  }
}