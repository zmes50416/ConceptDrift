package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class BBCExperimentsCase {
	Experiment exp;
	Path rootPath;
	private IndexSearchable searcher;
	private int round;
	private double parama;
	private Path bbcData = Paths.get(SettingManager.getSetting("bbcDataSet"));
	private MemoryBasedUserProfile user;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public BBCExperimentsCase(Path path) {
		this.rootPath = path;
	}
	public static void main(String[] args) {
		//Environment setup
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		HttpIndexSearcher.url = "http://localhost/searchweb/";
		Path path = Paths.get(SettingManager.chooseProject());
		BBCExperimentsCase expController = new BBCExperimentsCase(path); 
		System.out.println("You Dir is:"+expController.rootPath);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.時間實驗");
		System.out.println("4.核心數目實驗");
		System.out.println("5.概念飄移實驗");
		System.out.println("6.長期興趣門檻");
		String i;
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
		expController.searcher.shutdown();
	}
	private void conceptDriftExperiment() {

		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.3);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(0.2);
				
		exp = new Experiment(rootPath.toString(),maper,user);
		exp.debugMode = true;
		exp.experimentDays = 14;
		
		BBCNewsPopulator populater = new BBCNewsPopulator(this.rootPath){

			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(5);
				this.trainTopics.clear();
				if(this.theDay<=7){
					this.addTrainingTopics("tech");
				}else{
					this.addTrainingTopics("politics");
				}
				
			}
			
		};
		populater.addTrainingTopics("business");//only to avoid warning
		populater.addTestingTopics("tech");
		populater.addTestingTopics("business");
		exp.newsPopulater = populater;
		execute();		
	}
	private void coreExperiment() {
		// TODO Auto-generated method stub
		
	}
	private void timeExperiment() {
		// TODO Auto-generated method stub
		
	}
	private void removeThresholdExperiment() {
		// TODO Auto-generated method stub
		
	}
	private void TopicRelatedScore() {
		for(int i = 0;i<round;i++){
			double topicSimliarityThreshold = parama + (i/10.0);

			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.1);

			Path tempDir = this.rootPath.resolve("turn_"+i);
			exp = new Experiment(tempDir.toString(),maper,user);
			exp.debugMode = true;
			this.exp.experimentDays = 10;

			BBCNewsPopulator populater = new BBCNewsPopulator(tempDir){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(2);	
					
				}
				
			};
			exp.newsPopulater = populater;
			for(String topic:BBCNewsPopulator.TOPICS){
				populater.addTestingTopics(topic);
			}
			populater.addTrainingTopics("tech");

			execute();
		}		
	}
	private void execute() {
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
