package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;

public class NewThresholdExperiment {
	Path projectDir;
	Experiment exp ;
	AbstractUserProfile user;
	private double topicSimliarityThreshold;
	private int experimentDays;
	private double removeRate;
	public static void main(String[] args) {
		Path path = Paths.get(SettingManager.chooseProject());
		TopicTermGraph.MAXCORESIZE = 15;
		NewThresholdExperiment expController = new NewThresholdExperiment(path); 
		System.out.println("You Dir is:"+path);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		char i;
		try{
				i = (char)System.in.read();
				
				if(i == '1'){
					expController.TopicRelatedScore();
					
				}else if(i == '2'){
					expController.anotherExperiment();
				}else if(i == '3'){
				}
				System.out.println("Experimetn have been done!\n");
				System.exit(0);
		}catch(IOException e){
			System.err.println("IO Have been Interrupted. System stopped.");
			e.printStackTrace();
		}
		
		
	}
	
	public NewThresholdExperiment(Path _projectDir) {
		this.projectDir = _projectDir;
		exp = new Experiment(this.projectDir.toString());
		exp.debugMode = true;
		user = new MemoryBasedUserProfile();
		exp.setUser(user);
	}
	
	public void TopicRelatedScore(){
		 //set up a test case as topicMapping thresholdExperiment
		topicSimliarityThreshold = 0.1;
		experimentDays = 10;
		removeRate = 0.1;
		exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), topicSimliarityThreshold);
		this.exp.experimentDays = experimentDays;
		user.setRemove_rate(removeRate);
		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString()){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(5);	
				
			}
			
		};
		exp.newsPopulater = populater;
		for(String topic:RouterNewsPopulator.test){
			populater.addTestingTopics(topic);
		}
		populater.addTrainingTopics("acq");
		
		execute();
	}

	public void anotherExperiment(){
		topicSimliarityThreshold = 0.4;
		experimentDays = 14;
		removeRate = 0.1;
		exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), topicSimliarityThreshold);
		this.exp.setExperimentDays(experimentDays);
		user.setRemove_rate(removeRate);
		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString()){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(3);
				this.setTestSize(3);	
				
			}
			
		};
		exp.newsPopulater = populater;
		populater.addTrainingTopics("acq");
		populater.addTrainingTopics("sugar");
		populater.addTrainingTopics("earn");
		for(String topic:RouterNewsPopulator.test){
			populater.addTestingTopics(topic);
		}
		
		try {
			this.exp.initialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		execute();
	}
	
	private void execute(){
		try {
			this.exp.initialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Long sumTime = (long) 0;
		for(int dayN= 1;dayN<=this.exp.experimentDays;dayN++){
			Long time = System.currentTimeMillis();
			this.exp.run(dayN);
			Long spendedTime = System.currentTimeMillis() - time;
			System.out.println("Run a day:"+dayN+", Speend total time of "+spendedTime+"ms");
			sumTime += spendedTime;
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(exp.getUserProfilePath().resolve("userLog.txt").toFile(),true))){
				writer.append("Time spend:"+spendedTime);
				writer.newLine();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(exp.getProjectPath().resolve("setting.txt").toFile(),true))){
			writer.append("Total time:"+sumTime);
			writer.newLine();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		//this.exp.simplelog(experimentDays);

	}
}
