package tw.edu.ncu.CJ102.CoreProcess;
/**
 * To populate file for Experiment and imp
 * @author TingWen
 *
 */
public interface ExperimentFilePopulater {
	/**
	 * generate the file from specification sources
	 * @return whether the file dir have been successfully populated
	 */
	public boolean populateExperiment(int days);

}
