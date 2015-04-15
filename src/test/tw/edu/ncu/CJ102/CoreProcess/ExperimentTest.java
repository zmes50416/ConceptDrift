package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;

public class ExperimentTest{

	Experiment exp;
	AbstractUserProfile user;
	Path expPath;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		expPath = Files.createTempDirectory("ExperimentClass");
		exp = new Experiment(expPath.toString());
		user = new MemoryBasedUserProfile();
		exp.setUser(user);
		exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), 0.1);
		exp.setExperimentDays(14);
		exp.newsPopulater = new RouterNewsPopulator(expPath.toString()){
			@Override
			public void setGenarationRule() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(this.expPath.toFile());
	}

	@Test
	public void testExperiment() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUser() {
		AbstractUserProfile u = this.exp.getUser();
		boolean isRightUser = (u==user);
		assertTrue("user are not the right user",isRightUser);
	}

	@Test(expected = RuntimeException.class)
	public void testInitialize() {//What should we test? expect throw out Exception when user and experiment day are not set
		try {
			this.exp.initialize();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

}
