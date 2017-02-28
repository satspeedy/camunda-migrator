package com.satspeedy.bpm.camuda.migrator;

import com.satspeedy.bpm.camuda.migrator.service.ProcessInstanceMigrationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class ProcessMigrationToolApplicationTest {

	@InjectMocks
	private ProcessMigrationToolApplication processMigrationToolApplication;

	@Mock
	private ProcessInstanceMigrationService processInstanceMigrationServiceMock;

	@Test
	public void shouldStartMigration() {
		// when
		processMigrationToolApplication.doPostConstruct();
		// then
		verify(processInstanceMigrationServiceMock).migrate();
	}

	@Test(expected = RuntimeException.class)
	public void shouldEndMigrationWithExitCode() {
		//given
		doThrow(new RuntimeException("Aborted")).when(processInstanceMigrationServiceMock).migrate();

		// when
		ProcessMigrationToolApplication.main(new String[]{});
	}


}
