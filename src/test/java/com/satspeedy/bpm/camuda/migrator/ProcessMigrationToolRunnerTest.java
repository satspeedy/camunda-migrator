package com.satspeedy.bpm.camuda.migrator;

import com.satspeedy.bpm.camuda.migrator.service.ProcessInstanceMigrationService;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class ProcessMigrationToolRunnerTest {

    @Rule
    public final TextFromStandardInputStream systemInMock = emptyStandardInputStream();

    @InjectMocks
    private ProcessMigrationToolRunner processMigrationToolRunner;

    @Mock
    private ProcessInstanceMigrationService processInstanceMigrationServiceMock;

    @Test
    public void shouldStartMigration() {
        // when
        processMigrationToolRunner.migrate();
        // then
		verify(processInstanceMigrationServiceMock).migrate();
    }

    @Test(expected = RuntimeException.class)
    public void shouldRunDirectlyMigrationAndEndWithExitCode() throws Exception {
        //given
        doThrow(new RuntimeException("Aborted")).when(processInstanceMigrationServiceMock).migrate();

        // when
        processMigrationToolRunner.run(new String[]{"migrate"});
    }

    @Ignore
    @Test
    public void shouldDisplayMoreInfoOnCommandLine() throws Exception {
        //given
        systemInMock.provideLines("info");

        // when
        processMigrationToolRunner.run(new String[]{});
    }

}