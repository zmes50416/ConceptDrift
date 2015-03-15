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
		p.addTrainingTopics("acq");
		p.addTestingTopics("acq");
	}
	@After
	public void tear() throws Exception {
		FileUtils.deleteDirectory(tempProject.toFile());
		//Files.delete(tempProject);
	}
	

	@Test
	public void testAddTestingTopic() {
		assertTrue("test if it contain topic",p.testTopics.contains("acq"));
		assertEquals("test if it all added sucessful",1,p.testTopics.size(),0);
	}
	
	@Test
	public void testAddTrainingTopic(){
		p.addTrainingTopics("earn");
		assertTrue("test if it contain topic",p.trainTopics.contains("acq"));
		assertEquals("test if it all added succesful",2, p.trainTopics.size(),0);
	}
	
	@Test
	public void testpopulateExperiment(){
		int day=1;
		p.populateExperiment(day);
		File test = p.projectDir.resolve("training/").toFile();

		assertEquals("",test.list().length,day);
		for(File dayDir:test.listFiles()){
			assertEquals("",dayDir.list().length,1);
		}
	}
	
	@Test
	public void testgetTopics(){
		File testDocument = new File("/Tom_reuters_0.4/single/acq/acq_0000005_concepts.txt"); 
		assertEquals("Should be the acq tag",p.getTopics(testDocument),"acq");

	}
	
	

}
