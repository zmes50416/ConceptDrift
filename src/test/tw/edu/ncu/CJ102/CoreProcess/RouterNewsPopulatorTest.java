package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RouterNewsPopulatorTest {
	
	RouterNewsPopulator	p;
	Path tempProject;
	
	@Before
	public void setUp() throws Exception {
		tempProject = Files.createTempDirectory("UnitTest_");
		p = new RouterNewsPopulator(tempProject.toString()+"/"){

			@Override
			public void setGenarationRule() {
				this.setTestSize(1);
				this.setTrainSize(1);
			}
			
		};
		
	}
	@After
	public void tear() throws Exception {
		FileUtils.deleteDirectory(tempProject.toFile());
		//Files.delete(tempProject);
	}
	

	@Test
	public void testAddTestingTopic() {
		
		p.addTestingTopics("acq");
		assertTrue("test if it contain topic",p.testTopics.contains("acq"));
		assertEquals("test if it all added sucessful",1,p.testTopics.size(),0);
	}
	
	@Test
	public void testAddTrainingTopic(){
		p.addTrainingTopics("acq");
		p.addTrainingTopics("earn");

		assertTrue("test if it contain topic",p.trainTopics.contains("acq"));
		assertEquals("test if it all added succesful",2, p.trainTopics.size(),0);
	}
	
	@Test
	public void testpopulateExperiment(){
		int day=1;
		p.addTestingTopics("acq");
		p.addTrainingTopics("acq");
		p.populateExperiment(day);
		File test = Paths.get(p.projectDir+"training/").toFile();

		assertEquals("",test.list().length,day);
		for(File dayDir:test.listFiles()){
			assertEquals("",dayDir.list().length,1);
		}
	}
	
	

}
