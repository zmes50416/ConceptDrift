package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExperimentTest {
		Tom_exp exp;
		Path tempDir;
		MockRouterNewsPopulator r;
	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("TomEXP_");
		exp = new Tom_exp(tempDir.toString());
		r = new MockRouterNewsPopulator(tempDir.toString());
		r.addTestingTopics("acq");
		r.addTrainingTopics("acq");
		exp.setExperementSource(r);
		exp.setmUserProfile(new UserProfile(true));

	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(tempDir.toFile());
	}

	@Test(expected=RuntimeException.class)
	public void testTom_expLockDetection() {
		Tom_exp exp2 = new Tom_exp(tempDir.toString());
	}

	@Test
	public void testStart() {
		fail("Not yet implement");
		//exp.start();
		
		
	}


	@Test
	public void testStartAnotherTraining() {
		//TODO implement exp.startAnotherTraining(1);
		r.populateExperiment(1);
		exp.startAnotherTraining(1);
		
		fail("Not yet implemented");
	}

	@Test
	public void testStartTraining() {
		fail("Not yet implemented");
	}

	@Test
	public void testStartTesting() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDynamicDecayMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetExperimentDays() {
		fail("Not yet implemented");
	}

	@Test
	public void testReRandomize() {
		fail("Not yet implemented");
	}

	@Test
	public void testMUserProfile() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetmUserProfile() {
		fail("Not yet implemented");
	}

	class MockRouterNewsPopulator extends RouterNewsPopulator{

		public MockRouterNewsPopulator(String dir) {
			super(dir);
		}

		@Override
		public void setGenarationRule() {
			this.setTestSize(2);
			this.setTrainSize(2);
		}
		
	}
}
