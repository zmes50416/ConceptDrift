package tw.edu.ncu.CJ102.CoreProcess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;

import com.google.common.collect.Lists;

public class NewThresholdExperiment extends AbstractExperimentCase {
	String topicPath = "stanford/";
	public int round; // for how many round experiment should run
	public double parama;//different meaning in different experiment, the control variable
	public static void main(String[] args) {
		//Environment setup
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		HttpIndexSearcher.url = "http://localhost/searchweb/";
		
		Path path = Paths.get(SettingManager.chooseProject());
		NewThresholdExperiment expController = new NewThresholdExperiment(path); 
		System.out.println("You Dir is:"+path);		
		System.out.println("Which Experiment you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.效能實驗");
		System.out.println("4.核心數目實驗");
		System.out.println("5.概念飄移實驗");
		System.out.println("6.長期興趣門檻");
		String input;
		try(Scanner scanner = new Scanner(System.in)){
				input = scanner.next();
				System.out.println("使用HTTP(1)或是嵌入式SOLR(2)?");
				if(scanner.nextInt()==1){
					expController.searcher = new HttpIndexSearcher();
				}else{
					expController.searcher = new EmbeddedIndexSearcher();
				}
				System.out.println("請填入核心數目");
				TopicTermGraph.MAXCORESIZE = scanner.nextInt();
				System.out.println("請填入遞迴回數:");
				expController.round = scanner.nextInt();
				if(input.equals("1")){
					System.out.println("請填入主題相關應得門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					for(int turn = 0;turn<expController.round;turn++){
						expController.TopicRelatedScore(turn);
					}
					
				}else if(input.equals("2")){
					System.out.println("請填入移除門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					expController.removeThresholdExperiment();
				}else if(input.equals("3")){
					expController.performanceExperiment();
				}else if(input.equals("4")){
					for(int turn=0;turn<expController.round;turn++){
						expController.coreExperiment(turn);
						expController.corelessExperiment(turn);
						expController.recordThisRound(turn);
					}
				}else if(input.equals("5")){
					System.out.println("請填入長期門檻值參數:");
					expController.parama = scanner.nextDouble();
					for(int turn =0;turn<expController.round;turn++){
						expController.conceptDriftExperiment(turn);

					}
				}else if(input.equals("6")){
					System.out.println("請填入長期門檻的初始值:");
					expController.parama = scanner.nextDouble();
					for(int turn =0;turn<expController.round;turn++){
						expController.longTermThresholdExperiment(turn);
					}
				}
				System.out.println("Experimetn have been done!\n");
				System.exit(0);
		}catch(IOException e){
			System.err.println("IO Have been Interrupted. System stopped.");
			e.printStackTrace();
		}
	}
	
	public NewThresholdExperiment(Path _projectDir) {
		super(_projectDir);
	}
	
	public void TopicRelatedScore(int turn) throws IOException{
		experimentDays = 10;
		removeRate = 0.1;
		topicSimliarityThreshold = parama + (turn/10.0);
		TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(removeRate);
		Path experimentDir = this.rootDir.resolve("turn_"+turn);
		experiment = new Experiment(experimentDir.toString(),maper,user);
		experiment.debugMode = debugMode;
		this.experiment.experimentDays = experimentDays;

		RouterNewsPopulator populater = new RouterNewsPopulator(experimentDir.toString(),topicPath){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
		experiment.newsPopulater = populater;
		for(String topic:RouterNewsPopulator.test){
			populater.addTestingTopics(topic);
		}
		populater.addTrainingTopics("acq");
		execute();
		this.recordThisRound(turn);
	}

	public void removeThresholdExperiment() throws IOException{
		topicSimliarityThreshold = 0.7;
		experimentDays = 14;
		for (int i = 0; i < round; i++) {
			Path tempDir = this.rootDir.resolve("turn_" + i);
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			removeRate = parama + (i / 10.0);
			user.setRemoveRate(removeRate);

			this.experiment = new Experiment(tempDir.toString(), maper, user);
			this.experiment.setExperimentDays(experimentDays);
			experiment.debugMode = debugMode;


			RouterNewsPopulator populater = new RouterNewsPopulator(
					tempDir.toString(), topicPath) {
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);

				}

			};
			experiment.newsPopulater = populater;
			populater.addTrainingTopics("acq");
			for (String topic : RouterNewsPopulator.test) {
				populater.addTestingTopics(topic);
			}
			execute();
			this.recordThisRound(i);
		}
	}
	
	public void coreExperiment(int turn){
		this.topicSimliarityThreshold = 0.6;
		this.removeRate = 0.5;
		for(int j= 0;j<=2;j++){//examine 3 different method
				TopicTermGraph.METHODTYPE = j;
				String methodName = null;
				if(j==0){
					methodName = "Degree";
				}else if(j==1){
					methodName = "LP";
				}else if(j==2){
					methodName = "Betweenness";
				}
				Path tempProject = this.rootDir.resolve("Methode_"+methodName).resolve("round_"+turn);
				TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);
				user = new MemoryBasedUserProfile();
				user.setRemoveRate(this.removeRate);
				TopicTermGraph.MAXCORESIZE = 5 + turn*5;
				
				experiment = new Experiment(tempProject.toString(),maper,user);
				experiment.experimentDays = 14;
				experiment.debugMode = this.debugMode;
				
				RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
					@Override
					public void setGenarationRule() {
						this.setTrainSize(10);
						this.setTestSize(5);	
						
					}
					
				};
				populater.addTrainingTopics("acq");
				populater.addTestingTopics("acq");
				ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
				String randomTopic = topics.get(new Random(1).nextInt(topics.size()));
				populater.addTestingTopics(randomTopic);
				experiment.newsPopulater = populater;
				execute();
				
				this.recordThisRound(turn*4+j);
		}
	}
	//core less will compare with core
	public void corelessExperiment(int turn){
		Path tempProject = this.rootDir.resolve("Method_coreless").resolve("round_"+turn);
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);//Will be the same of Core experiment
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		TopicTermGraph.MAXCORESIZE = 1000;
		
		experiment = new Experiment(tempProject.toString(),maper,user);
		experiment.experimentDays = 14;
		experiment.debugMode = this.debugMode;
		
		RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(10);
				this.setTestSize(5);	
				
			}
			
		};
		populater.addTrainingTopics("acq");
		populater.addTestingTopics("acq");
		ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
		topics.remove("acq");
		String randomTopic = topics.get(new Random(1).nextInt(topics.size()));
		populater.addTestingTopics(randomTopic);
		experiment.newsPopulater = populater;
		execute();
		this.recordThisRound(turn*4+4);
	}
	
	public void longTermThresholdExperiment(int turn){
		
		for(int j=0;j<5;j++){
			Path project = this.rootDir.resolve("turn_"+turn).resolve("random_"+j);
			TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.8);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.7);
			MemoryBasedUserProfile.longTermThreshold = (int) (parama + turn*25);
			
			experiment = new Experiment(project.toString(),maper,user);
			experiment.debugMode = debugMode;
			experiment.experimentDays = 10;
			
			RouterNewsPopulator populater = new RouterNewsPopulator(project.toString()){

				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);
					
				}
				
			};
			populater.addTrainingTopics("acq");//only to avoid warning
			populater.addTestingTopics("acq");
			populater.addTestingTopics("trade");
			ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
			topics.remove("acq");
			topics.remove("trade");
			String randomTopic = topics.get(new Random(j).nextInt(topics.size()));
			populater.addTestingTopics(randomTopic);
			experiment.newsPopulater = populater;
			execute();
			this.recordThisRound(turn*5+j);
		}
		
		
	}
	
	public void conceptDriftExperiment(int turn){
		for(int j = 0;j<=5;j++){
			Path project = this.rootDir.resolve("turn_" + turn).resolve(j+"_seed");
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(), 0.5);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.7);
			MemoryBasedUserProfile.longTermThreshold = (int) (25*turn+parama);
			experiment = new Experiment(project.toString(), maper, user);
			experiment.debugMode = debugMode;
			experiment.experimentDays = 14;

			RouterNewsPopulator populater = new RouterNewsPopulator(
					project.toString()) {

				@Override
				public void setGenarationRule() {
					this.setTrainSize(10);
					this.setTestSize(5);
					this.trainTopics.clear();
					if (this.theDay <= 7) {
						this.addTrainingTopics("acq");
					} else {
						this.addTrainingTopics("earn");
					}

				}

			};
			populater.addTrainingTopics("acq");// only to avoid warning
			populater.addTestingTopics("acq");
			populater.addTestingTopics("trade");
			ArrayList<String> topics = Lists
					.newArrayList(RouterNewsPopulator.test);
			topics.remove("acq");
			topics.remove("trade");
			String randomTopic = topics.get(new Random(j).nextInt(topics
					.size()));
			populater.addTestingTopics(randomTopic);
			experiment.newsPopulater = populater;
			execute();
		}
	}
	
	public void performanceExperiment(){
		String[][] trainTopic={{"acq","earn"},{"crude","coffee"},{"sugar","trade"},{"acq","cocoa"},{"crude","trade"}};
		TopicTermGraph.METHODTYPE = 1; //LP method
		for(int i = 0;i<round;i++){
			Path tempProject = this.rootDir.resolve("round_"+i);
			TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.8);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.7);
			
			experiment = new Experiment(tempProject.toString(),maper,user);
			experiment.debugMode = debugMode;
			experiment.experimentDays = 10;
			
			RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
			populater.addTrainingTopics(trainTopic[i][0]);
			populater.addTrainingTopics(trainTopic[i][1]);
			for(String topic:RouterNewsPopulator.test){
				populater.addTestingTopics(topic);
			}
			experiment.newsPopulater = populater;
			execute();
			this.recordThisRound(i);
		}
	}
}
