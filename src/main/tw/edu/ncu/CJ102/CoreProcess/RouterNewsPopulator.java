package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * For Router News DataSet.
 * should Override the simulateNextDay for different Experiment requirment.
 * @author TingWen
 *
 */
public abstract class RouterNewsPopulator implements ExperimentFilePopulater {
	Path projectDir;
	Collection<String> topics;
	Set<String> trainTopics = new HashSet<String>();
	Set<String> testTopics = new HashSet<String>() ;
	TrainingTools trainerTom = new TrainingTools();
	public String topicPath;
	public static final String DEFAULT_TOPIC_PATH = "Tom_reuters_0.4/single";
	private int trainSize,testSize;
	static final String test[] = { "acq", "earn", "crude", "coffee", "sugar",
			"trade", "cocoa" };

	
	int theDay;
	public RouterNewsPopulator(String dir){
		this(dir,DEFAULT_TOPIC_PATH);
	}
	/**
	 * 
	 * @param dir project itself
	 * @param topicPath source of files,Should be relative to eclipse's project path
	 */
	public RouterNewsPopulator(String dir,String topicPath){
		this.projectDir = Paths.get(dir);
		this.topicPath = topicPath;
		init();
		this.theDay =1;
	}
	protected void init(){
		File topicDir= new File(topicPath);
		if(!topicDir.isDirectory()||topicDir.list().length==0){
			throw new IllegalArgumentException("Can't Find the Topics Dir");//Nothing to add will break everythings
		}
		topics = new HashSet<String>();
		for(String topic:topicDir.list()){
			this.topics.add(topic);
		}
	}

	public boolean addTrainingTopics(String topic){
		if(this.topics.contains(topic)){
			return this.trainTopics.add(topic);
		}else{
			throw new IllegalArgumentException("train topic "+topic+" are not in the collection");
		}
	}
	public boolean addTestingTopics(String topic){
		if(this.topics.contains(topic)){
			return this.testTopics.add(topic);
		}else{
			throw new IllegalArgumentException("test topic "+topic+" are not in the collection");
		}
	}

	@Override
	public boolean populateExperiment(int days) {
		if(this.trainTopics.isEmpty()||this.testTopics.isEmpty()){
			throw new RuntimeException("topic is empty, nothing will be generate!");
		}
		for (int i = 1; i <= days; i++) {
			this.setGenarationRule();
			// 創造出實驗訓練集,測試集第i天資料匣
			Path traingPath = projectDir.resolve(Paths.get("training" ,"day_"
					+ theDay)) ;
			Path testingPath = projectDir.resolve(Paths.get("testing" , "day_"
					+ theDay));
			try {
				Files.createDirectories(traingPath);
				Files.createDirectories(testingPath);
			} catch (IOException e) {
				System.err.println("System Can't Create Dir at:" + projectDir
						+ "training/" + "day_" + theDay);
				e.printStackTrace();
			}
			
			for (String topic : this.trainTopics) {
				trainerTom.point_topic_doc_generateSet(
						topicPath, traingPath.toString(), topic, trainSize, theDay);
			}

			for (String topic : this.testTopics) {
				trainerTom.point_topic_doc_generateSet(
						topicPath, testingPath.toString(), topic, testSize, days
								+ theDay);
			}

			theDay++;
		}

		return true;
	}
	/**
	 * Simulate the Next day's Experiment requirment. 
	 * Please Implements Today's trainSize and testSize
	 */
	public abstract void setGenarationRule();
	
	@Override
	public String getTopics(File document){
		
		if(document.isFile()){
			String topicName = document.getName().split("_")[0];
			if(this.testTopics.contains(topicName)||this.trainTopics.contains(topicName)){
				return topicName;
			}else{
				throw new IllegalArgumentException("File isn't in the topics or File Name are not in correct Format");
			}
		}else{
			throw new IllegalArgumentException("can't use File: "+document);
		}
	}
	public void setTrainSize(int size){
		this.trainSize = size;
	}
	public void setTestSize(int size){
		this.testSize = size;
	}
	public int getTrainSize(){
		return this.trainSize;
	}
	public int getTestSize(){
		return this.testSize;
	}

}
