package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tw.edu.ncu.CJ102.SettingManager;

public class BBCNewsPopulator implements ExperimentFilePopulater {

	private Path trainFileDir;
	private Path testFileDir;
	
	protected Set<String> trainTopics;
	protected Set<String> testTopics;
	public static final String[] TOPICS = {
			"sport", "business", "entertainment" , "politics" , "tech"
	};
	public final static String DEFAULT_TOPIC_PATH = SettingManager.getSetting("bbcDataSet");
	private TrainingTools trainerTom = new TrainingTools();
	protected Path topicPath;
	protected int trainSize = 0;
	protected int testSize = 0;
	int theDay;
	public BBCNewsPopulator(Path projectDir){
		this(projectDir, Paths.get(DEFAULT_TOPIC_PATH));
	}
	
	public BBCNewsPopulator(Path projectDir,Path topicPath) {
		this.trainFileDir = projectDir.resolve(this.TRAININGPATH);
		this.testFileDir = projectDir.resolve(this.TESTINGPATH);
		this.topicPath = topicPath;
		trainTopics = new HashSet<>();
		testTopics = new HashSet<>();
	}
	
	public boolean addTrainingTopics(String topic){
		return this.trainTopics.add(topic);
	}
	public boolean addTestingTopics(String topic){
		return this.testTopics.add(topic);
	}
	
	/**
	 * @return the testSize
	 */
	public int getTestSize() {
		return testSize;
	}

	/**
	 * @param testSize the testSize to set
	 */
	public void setTestSize(int testSize) {
		this.testSize = testSize;
	}

	/**
	 * @return the trainSize
	 */
	public int getTrainSize() {
		return trainSize;
	}

	/**
	 * @param trainSize the trainSize to set
	 */
	public void setTrainSize(int trainSize) {
		this.trainSize = trainSize;
	}

	/**
	 * inject populate rule into the algorithm
	 * Default to generate simple one
	 */
	public void setGenarationRule(){
		
	}
	@Override
	public boolean populateExperiment(int days) {
		try {

			for (theDay = 1; theDay <= days; theDay++) {
				Path todayTrainDir = this.trainFileDir.resolve("Day_" + theDay);
				Files.createDirectories(todayTrainDir);
				Path todayTestDir = this.testFileDir.resolve("Day_" + theDay);
				Files.createDirectories(todayTestDir);
				this.setGenarationRule();
				for (String topic : trainTopics) {
					trainerTom.point_topic_doc_generateSet(
							topicPath.toString(), todayTrainDir.toString(),
							topic, trainSize, theDay);
				}
				for (String topic : testTopics) {
					trainerTom.point_topic_doc_generateSet(
							topicPath.toString(), todayTestDir.toString(),
							topic, testSize, theDay);
				}

			}
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String identifyTopic(File document) {//Should have the topic name before file ex:sport_01.txt
		String fileName = document.getName();
		String topicName = fileName.split("_")[0];
		for(String name:TOPICS){
			if(topicName.equals(name)){
				return topicName;
			}
		}
		
		throw new IllegalArgumentException("can't find topic name patten in file: "+document.getName());
	}

}
