package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExperimentCaseTest {
	AbstractExperimentCase expController;
	Path temp;
	@Before
	public void setUp() throws Exception {
		this.temp = Files.createTempDirectory("TestExperiment");
		expController = new RouterExperimentCase(temp);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(temp.toFile());
	}

	@Test
	public void testRecordThisRound() {
		for(int i = 0;i<=3;i++){
			PerformanceMonitor mockMonitor = new PerformanceMonitor();
			expController.totalMonitor = mockMonitor;
			mockMonitor.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
			mockMonitor.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
			mockMonitor.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
			expController.sumTime += 100L;
		}
		//TODO check whether the data excel are really exist
		
	}
	@Test
	public void testBBC(){
		BBCExperimentsCase exp = new BBCExperimentsCase(this.temp);
		for(int i = 0;i<=3;i++){
			PerformanceMonitor mockMonitor = new PerformanceMonitor();
			exp.totalMonitor = mockMonitor;
			mockMonitor.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
			mockMonitor.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
			mockMonitor.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
			exp.sumTime += 100L;
		}
		fail("Not yet implement");
	}
}
