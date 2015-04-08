package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tw.edu.ncu.CJ102.algorithm.NgdReverseTfTopicSimilarity;

public class ExperimentTest{

	Experiment exp;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Path p = Files.createTempDirectory("ExperimentClass");
		exp = new Experiment(p.toString());
		this.
		maper.algorithm = new NgdReverseTfTopicSimilarity();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExperiment() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProjectPath() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitialize() {
		fail("Not yet implemented");
	}

	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

}
