package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class RouterNewsPopulator implements ExperimentFilePopulater {
	String projectDir;
	Collection<String> topics;
	Collection<String> trainTopics;
	Collection<String> testTopics;
	Go_Training_Tom trainerTom = new Go_Training_Tom();

	public RouterNewsPopulator(String dir){
		this.projectDir = dir;
		File topicDir= new File("Tom_reuters_0.4/single");
		if(!topicDir.isDirectory()||topicDir.list().length==0){
			throw new IllegalArgumentException();//Nothing to add will break everythings
		}
		topics = new ArrayList<>();
		for(String topic:topicDir.list()){
			this.topics.add(topic);
		}
	}
	public void addTrainingTopics(String topic){
		if(!this.topics.contains(topic)){
			this.trainTopics.add(topic);
		}else{
			throw new IllegalArgumentException("train topic are not in the list");
		}
	}
	public void addTestingTopics(String topic){
		if(!this.topics.contains(topic)){
			this.testTopics.add(topic);
		}else{
			throw new IllegalArgumentException("test topic are not in the list");
		}
	}

	@Override
	public boolean populateExperiment(int days, int trainSize, int testSize){
		for (int i = 1; i <= days; i++) {
			System.out.println("第" + i + "天");
			new File(projectDir + "training/" + "day_" + i).mkdirs(); // 創造出實驗訓練集第i天資料匣
			new File(projectDir + "testing/" + "day_" + i).mkdirs(); // 創造出實驗測試集第i天資料匣
			
			for (String topic:this.trainTopics) {
				trainerTom.point_topic_doc_generateSet("Tom_reuters_0.4/single",
					projectDir + "training/" + "day_" + i,
					topic, trainSize, i);
			}

			for (String topic:this.testTopics) {
				trainerTom.point_topic_doc_generateSet("Tom_reuters_0.4/single",
						projectDir + "testing/" + "day_" + i, topic,
						testSize, days + i);
			}
		}		
		return true;
	}

	

}
