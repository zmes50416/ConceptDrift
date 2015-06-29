package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;

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
		TopicMappingTool maper = createMock(TopicMappingTool.class);
		TopicTermGraph t = new TopicTermGraph(0);
		expect(maper.map(notNull(TopicTermGraph.class), notNull(AbstractUserProfile.class))).andReturn(t).anyTimes();
		replay(maper);
		user = new MemoryBasedUserProfile();

		exp = new Experiment(expPath.toString(),maper,user);
		exp.setUser(user);
		exp.setExperimentDays(14);
		RouterNewsPopulator mPopulater = new RouterNewsPopulator(this.exp.getProjectPath().toString()){

			@Override
			public void setGenarationRule() {
				this.setTrainSize(1);
				this.setTestSize(1);
			}
			
		};
		mPopulater.addTrainingTopics("acq");
		mPopulater.addTestingTopics("acq");
		this.exp.newsPopulater = mPopulater;
	}

	@After
	public void tearDown() throws Exception {
//		FileUtils.deleteDirectory(this.expPath.toFile());
	}

	@Test
	public void testGetUser() {
		AbstractUserProfile u = this.exp.getUser();
		boolean isRightUser = (u==user);
		assertTrue("user are not the right user",isRightUser);
	}

	@Test
	public void testRun(){
		try {
			this.exp.initialize();
		
		for (int dayN = 1; dayN <= this.exp.experimentDays; dayN++) { //execute every day and record performance
			Long time = System.currentTimeMillis();
			this.exp.run(dayN);
			Long spendedTime = System.currentTimeMillis() - time;
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
