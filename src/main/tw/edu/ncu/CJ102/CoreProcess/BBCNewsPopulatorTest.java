package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BBCNewsPopulatorTest {
	BBCNewsPopulator testSubject;
	Path project;
	@Before
	public void setUp() throws Exception {
		project = Files.createTempDirectory("BBCNews_");
		testSubject = new BBCNewsPopulator(project);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(project.toFile());
	}

	@Test
	public void testPopulateExperiment() {
		int day=1;
		testSubject.setTrainSize(1);
		testSubject.setTestSize(1);
		testSubject.addTrainingTopics("tech");
		testSubject.addTestingTopics("tech");
		testSubject.populateExperiment(day);
		
		Path traingDir = project.resolve(ExperimentFilePopulater.TRAININGPATH);
		File testingDir = project.resolve(ExperimentFilePopulater.TESTINGPATH).toFile();
		assertTrue(" should have create the training directory",Files.isDirectory(traingDir));
		assertTrue(" should have create the testing directory",Files.isDirectory(testingDir.toPath()));
		
		assertEquals("Should have create train days dir",traingDir.toFile().list().length,day);
		assertEquals("Should have create train days dir",testingDir.list().length,day);
		for(File dayDir:testingDir.listFiles()){
			assertEquals("Should have generate right number of file",dayDir.list().length,1);
			for(File file:dayDir.listFiles()){
				String topic = testSubject.identifyTopic(file);
				File topicDir = testSubject.topicPath.resolve(topic).toFile();
				List<String> topicList = Arrays.asList(topicDir.list());
				assertTrue("Should have the same File",topicList.contains(file.getName()));
			}
		}
		
	}

	@Test
	public void testIdentifyTopic() {
			File dummyFile = new File("sport_(1).txt");
			String topic = this.testSubject.identifyTopic(dummyFile);
			assertEquals("should get the right topic name","sport", topic);
			
	}

}
