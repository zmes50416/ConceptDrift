package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * For Router News DataSet.
 * should Override the simulateNextDay for different Experiment requirment.
 * @author TingWen
 *
 */
public abstract class RouterNewsPopulator implements ExperimentFilePopulater {
	String projectDir;
	Collection<String> topics;
	Collection<String> trainTopics = new ArrayList<String>();
	Collection<String> testTopics = new ArrayList<String>() ;
	TrainingTools trainerTom = new TrainingTools();
	int trainSize,testSize;
	static final String test[] = { "acq", "earn", "crude", "coffee", "sugar",
			"trade", "cocoa" };
	
	int theDay;
	public RouterNewsPopulator(String dir){
		this.projectDir = dir;
		File topicDir= new File("Tom_reuters_0.4/single");
		if(!topicDir.isDirectory()||topicDir.list().length==0){
			throw new IllegalArgumentException();//Nothing to add will break everythings
		}
		topics = new ArrayList<String>();
		for(String topic:topicDir.list()){
			this.topics.add(topic);
		}
		this.theDay =1;
	}
	//TODO Collection's contains problem
	public void addTrainingTopics(String topic){
		//if(!this.topics.contains(topic)){
			this.trainTopics.add(topic);
		//}else{
		//	throw new IllegalArgumentException("train topic "+topic+" are not in the list");
		//}
	}
	public void addTestingTopics(String topic){
		//if(!this.topics.contains(topic)){
			this.testTopics.add(topic);
		//}else{
		//	throw new IllegalArgumentException("test topic "+topic+" are not in the list");
		//}
	}

	@Override
	public boolean populateExperiment(int days){
		for(int i=1;i<=days;i++){
			System.out.println("第" + theDay + "天");	
			this.setGenarationRule();
				// 創造出實驗訓練集,測試集第i天資料匣
				if(new File(projectDir + "training/" + "day_" + theDay).mkdirs() && 
						new File(projectDir + "testing/" + "day_" + theDay).mkdirs()){
							System.err.println("System Can't Create Dir at:" +projectDir + "training/" + "day_" + theDay);
							System.exit(1);
					}
				for (String topic : this.trainTopics) {
					trainerTom.point_topic_doc_generateSet(
							"Tom_reuters_0.4/single", projectDir + "training/"
									+ "day_" + theDay, topic, trainSize, theDay);
				}

				for (String topic : this.testTopics) {
					trainerTom.point_topic_doc_generateSet(
							"Tom_reuters_0.4/single", projectDir + "testing/"
									+ "day_" + theDay, topic, testSize, days + theDay);
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
