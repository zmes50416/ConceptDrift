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
	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("TomEXP_");
		exp = new Tom_exp(tempDir.toString()+"\\");
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
		fail("Not yet implemented");
	}

	@Test
	public void testSetExperementSource() {
		
		fail("Not yet implemented");
	}

	@Test
	public void testStartAnotherTraining() {
		RouterNewsPopulator r = new RouterNewsPopulator(tempDir.toString()){

			@Override
			public void setGenarationRule() {
				this.setTestSize(2);
				this.setTrainSize(2);
			}
			
		};
		r.addTestingTopics("acq");
		r.addTrainingTopics("acq");
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

}
