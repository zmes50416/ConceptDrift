package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class NewThresholdExperiment {
	Path projectDir;
	String topicPath = "Tom_reuters_0.4/single";
	Experiment exp ;
	AbstractUserProfile user;
	EmbeddedIndexSearcher searcher = new EmbeddedIndexSearcher();
	private double topicSimliarityThreshold;
	private int experimentDays;
	private double removeRate;
	public static void main(String[] args) {
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrLocalPath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCollection");
		Path path = Paths.get(SettingManager.chooseProject());
		TopicTermGraph.MAXCORESIZE = 15;
		NewThresholdExperiment expController = new NewThresholdExperiment(path); 
		System.out.println("You Dir is:"+path);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.相似度容差實驗");
		char i;
		try{
				i = (char)System.in.read();
				
				if(i == '1'){
					expController.TopicRelatedScore();
					
				}else if(i == '2'){
					expController.anotherExperiment();
				}else if(i == '3'){
					expController.timeExperiment();
				}
				System.out.println("Experimetn have been done!\n");
				System.exit(0);
		}catch(IOException e){
			System.err.println("IO Have been Interrupted. System stopped.");
			e.printStackTrace();
		}
		expController.searcher.shutdown();
		
	}
	
	public NewThresholdExperiment(Path _projectDir) {
		this.projectDir = _projectDir;
		exp = new Experiment(this.projectDir.toString());
		exp.debugMode = true;
	}
	
	public void TopicRelatedScore() throws IOException{
		 //set up a test case as topicMapping thresholdExperiment
		for(double i = 0.1;i<=1;i = i+0.1){
			exp = new Experiment(this.projectDir.resolve("turn_"+i).toString());
			exp.debugMode = true;
			user = new MemoryBasedUserProfile();
			exp.setUser(user);
			topicSimliarityThreshold = i;
			experimentDays = 10;
			removeRate = 0.1;
			exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(searcher), topicSimliarityThreshold);
			this.exp.experimentDays = experimentDays;
			user.setRemove_rate(removeRate);

			RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.resolve("turn_"+i).toString(),topicPath){
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
		
	}

	public void anotherExperiment() throws IOException{
		topicSimliarityThreshold = 0.4;
		experimentDays = 14;
		removeRate = 0.1;

		exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), topicSimliarityThreshold);
		this.exp.setExperimentDays(experimentDays);
		user.setRemove_rate(removeRate);
		exp.setUser(user);


		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString(),topicPath){
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

		execute();
	}
	
	public void timeExperiment() throws IOException{
		topicSimliarityThreshold = 0.4;
		removeRate = 0.1;
		user.setRemove_rate(removeRate);
		exp.setUser(user);

		this.exp.experimentDays = 10;

		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString(),topicPath){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(4);
				this.setTestSize(4);	
				
			}
			
		};
		exp.maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), topicSimliarityThreshold);

		File copyData = new File("DEMODATA");
		populater = new ManualRouterNewsPopulater(this.projectDir.toString(), copyData.toPath());
		exp.newsPopulater = populater;
		populater.addTrainingTopics("acq");
		populater.addTestingTopics("earn");
		execute();
	}
	private void execute() throws IOException{
		this.exp.initialize();

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
			writer.append("Performance:"+exp.systemPerformance);
			writer.append(exp.systemPerformance.get_all_result().toString());
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		//this.exp.simplelog(experimentDays);

	}
}
