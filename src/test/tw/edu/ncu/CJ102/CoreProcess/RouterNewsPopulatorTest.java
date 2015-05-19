package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

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
	
	//Test the ability to generate test case, and test different souce dir can produce same result(But have to be exactly same file in topics)
	@Test
	public void testpopulateExperiment(){ 
		int day=1;
		p.populateExperiment(day);
		RouterNewsPopulator p2 = new RouterNewsPopulator(tempProject.toString()+"_dual/","Tom_reuters_noTolerance/single"){

			@Override
			public void setGenarationRule() {
				this.setTestSize(1);
				this.setTrainSize(1);
			}
			
		};
		p2.addTrainingTopics("acq");
		p2.addTestingTopics("acq");
		p2.populateExperiment(day);
		
		Path traingDir = this.p.projectDir.resolve(ExperimentFilePopulater.TRAININGPATH);
		Path testingDir = this.p.projectDir.resolve(ExperimentFilePopulater.TESTINGPATH);
		assertTrue("experiment should have create the training directory",Files.isDirectory(traingDir));
		assertTrue("experiment should have create the testing directory",Files.isDirectory(testingDir));
		
		File test = p.projectDir.resolve("training/").toFile();

		assertEquals("",test.list().length,day);
		for(File dayDir:test.listFiles()){
			assertEquals("",dayDir.list().length,day);
			File p2dayDir = p2.projectDir.resolve(ExperimentFilePopulater.TRAININGPATH).resolve(dayDir.getName()).toFile();
			List<String> fileList = Arrays.asList(p2dayDir.list());
			for(File file:dayDir.listFiles()){
				assertTrue("Should have the same File",fileList.contains(file.getName()));
			}
		}
		
		try {
			FileUtils.deleteDirectory(p2.projectDir.toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		
	}
	
	@Test
	public void testgetTopics() throws IOException{
		File testDocument = this.tempProject.resolve("acq_0000005_concepts.txt").toFile(); 
		BufferedWriter b = new BufferedWriter(new FileWriter(testDocument));
		b.write("test case");
		assertEquals("Should be the acq tag",p.identifyTopic(testDocument),"acq");
		b.close();
	}
	
	

}
