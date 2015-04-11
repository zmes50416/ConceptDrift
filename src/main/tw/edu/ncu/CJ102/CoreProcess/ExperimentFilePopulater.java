package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;

/**
 * To populate file for Experiment and imp
 * @author TingWen
 *
 */
public interface ExperimentFilePopulater {
	static final String TRAININGPATH ="training";
	static final String TESTINGPATH = "testing";
	/**
	 * generate the file from specification sources
	 * @return whether the file dir have been successfully populated
	 */
	public boolean populateExperiment(int days);
	/**
	 * get the file for topics
	 * @param document
	 * @return the document's topic
	 */
	public String getTopics(File document);
}
