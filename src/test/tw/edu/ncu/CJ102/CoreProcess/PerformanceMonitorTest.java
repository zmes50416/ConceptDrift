package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerformanceMonitorTest {
	PerformanceMonitor monitor;

	@Before
	public void setUp() throws Exception {
		monitor = new PerformanceMonitor();
		monitor.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
		monitor.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
		monitor.set_EfficacyMeasure(PerformanceType.TRUENEGATIVE);
		monitor.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGet_precision() {
		assertEquals(monitor.computePrecision(),0.5,0);
	}

	@Test
	public void testGet_recall() {
		assertEquals(monitor.computeRecall(),0.5,0);
	}

	@Test
	public void testGet_f_measure() {
		assertEquals("Fmeasure should be ",0.5,monitor.computeFmeasure(),0);
		this.monitor.saveRecord();
		assertEquals("Should be zero", 0,monitor.computeFmeasure(),0);
	}

	@Test
	public void testGet_accuracy() {
		assertEquals("",monitor.computeAccuracy(),0.5,0);
	}

	@Test
	public void testGet_error() {
		assertEquals("",0.5,monitor.computeError(),0);
	}

	@Test
	public void testPhTest() {
		
		assertTrue("PH test should be flaged?",monitor.phTest());
	}

}
