package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class NewThresholdExperiment {
	Path projectDir;
	String topicPath = "Tom_reuters_0.4/single";
	Experiment exp ;
	AbstractUserProfile user;
	IndexSearchable searcher;
	private double topicSimliarityThreshold;
	private int experimentDays;
	private double removeRate;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public int round;
	public double parama;
	private boolean debugMode;
	public static void main(String[] args) {
		//Environment setup
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		HttpIndexSearcher.url = "http://localhost/searchweb/";
		
		Path path = Paths.get(SettingManager.chooseProject());
		NewThresholdExperiment expController = new NewThresholdExperiment(path); 
		System.out.println("You Dir is:"+path);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.時間實驗");
		System.out.println("4.核心數目實驗");
		System.out.println("5.概念飄移實驗");
		System.out.println("6.長期興趣門檻");
		String i;
		try{
			Scanner scanner = new Scanner(System.in);
				i = scanner.next();
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
				
				if(i.equals("1")){
					System.out.println("請填入主題相關應得門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					expController.TopicRelatedScore();
					
				}else if(i.equals("2")){
					System.out.println("請填入移除門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					expController.removeThresholdExperiment();
				}else if(i.equals("3")){
					expController.timeExperiment();
				}else if(i.equals("4")){
					expController.coreExperiment();
				}else if(i.equals("5")){
					expController.conceptDriftExperiment();
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
		debugMode = true;
	}
	
	public void TopicRelatedScore() throws IOException{
		experimentDays = 10;
		removeRate = 0.1;
		for(int i = 0;i<round;i++){
			topicSimliarityThreshold = parama + (i/10.0);

			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(removeRate);

			Path tempDir = this.projectDir.resolve("turn_"+i);
			exp = new Experiment(tempDir.toString(),maper,user);
			exp.debugMode = debugMode;
			this.exp.experimentDays = experimentDays;

			RouterNewsPopulator populater = new RouterNewsPopulator(tempDir.toString(),topicPath){
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

	public void removeThresholdExperiment() throws IOException{
		topicSimliarityThreshold = 0.4;
		experimentDays = 14;
		for (int i = 0; i < round; i++) {
			Path tempDir = this.projectDir.resolve("turn_" + i);
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(removeRate);

			this.exp = new Experiment(tempDir.toString(), maper, user);
			this.exp.setExperimentDays(experimentDays);
			exp.debugMode = debugMode;

			removeRate = parama + (i / 10.0);

			RouterNewsPopulator populater = new RouterNewsPopulator(
					tempDir.toString(), topicPath) {
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
			for (String topic : RouterNewsPopulator.test) {
				populater.addTestingTopics(topic);
			}

			execute();
		}
	}
	
	public void timeExperiment() throws IOException{
		//TODO unfinished Experiment
		topicSimliarityThreshold = 0.4;
		removeRate = 0.1;
		user.setRemoveRate(removeRate);
		exp.setUser(user);

		this.exp.experimentDays = 10;

		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString(),topicPath){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(4);
				this.setTestSize(4);	
				
			}
			
		};

		File copyData = new File("DEMODATA");
		populater = new ManualRouterNewsPopulater(this.projectDir.toString(), copyData.toPath());
		exp.newsPopulater = populater;
		populater.addTrainingTopics("acq");
		populater.addTestingTopics("earn");
		execute();
	}
	
	public void coreExperiment(){
		this.topicSimliarityThreshold = 0.4;
		this.removeRate = 0.1;
		for(int i=0;i<round;i++){
			Path tempProject = this.projectDir.resolve("round_"+i);
			TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(this.removeRate);
			TopicTermGraph.MAXCORESIZE = 5 + i*5;
			
			exp = new Experiment(tempProject.toString(),maper,user);
			exp.experimentDays = 14;
			
			RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
			populater.addTrainingTopics("acq");
			populater.addTestingTopics("acq");
			populater.addTestingTopics("earn");
			exp.newsPopulater = populater;
			execute();
			
		}
	}
	
	public void conceptDriftExperiment(){

		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.4);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(0.1);
				
		exp = new Experiment(this.projectDir.toString(),maper,user);
		exp.debugMode = debugMode;
		exp.experimentDays = 14;
		
		RouterNewsPopulator populater = new RouterNewsPopulator(this.projectDir.toString()){

			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(5);
				this.trainTopics.clear();
				if(this.theDay<=7){
					this.addTrainingTopics("acq");
				}else{
					this.addTrainingTopics("earn");
				}
				
			}
			
		};
		populater.addTrainingTopics("acq");//only to avoid warning
		populater.addTestingTopics("acq");
		populater.addTestingTopics("trade");
		exp.newsPopulater = populater;
		execute();
	}

	private void execute(){
		try {
			this.exp.initialize();

			Long sumTime = (long) 0;
			for (int dayN = 1; dayN <= this.exp.experimentDays; dayN++) {
				Long time = System.currentTimeMillis();
				this.exp.run(dayN);
				Long spendedTime = System.currentTimeMillis() - time;
				logger.info("Run a day {}, time: {}ms", dayN, spendedTime);
				sumTime += spendedTime;
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(
						exp.getUserProfilePath().resolve("userLog.txt")
								.toFile(), true))) {
					writer.append("Time spend:" + spendedTime);
					writer.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(exp
					.getProjectPath().resolve("setting.txt").toFile(), true));
			writer.append("Total time:" + sumTime);
			writer.newLine();
			writer.append("Performance:" + exp.systemPerformance);
			writer.append(exp.systemPerformance.get_all_result().toString());
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//this.exp.simplelog(experimentDays);

	}
}
