package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class BBCExperimentsCase extends AbstractExperimentCase {
	Experiment exp;
	Path rootPath;
	private int round;
	private double parama;
	private File excelSummary;
	long sumTime;

	public BBCExperimentsCase(Path path) {
		super(path);
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
		System.out.println("3.效能實驗");
		System.out.println("4.核心數目實驗");
		System.out.println("5.概念飄移實驗");
		System.out.println("6.長期興趣門檻");
		String i;
		try (Scanner scanner = new Scanner(System.in)) {
			i = scanner.next();
			System.out.println("使用HTTP(1)或是嵌入式SOLR(2)?");
			if (scanner.nextInt() == 1) {
				expController.searcher = new HttpIndexSearcher();
			} else {
				expController.searcher = new EmbeddedIndexSearcher();
			}
			System.out.println("請填入核心數目");
			TopicTermGraph.MAXCORESIZE = scanner.nextInt();
			System.out.println("請填入遞迴回數:");
			expController.round = scanner.nextInt();
			if (i.equals("1")) {
				System.out.println("請填入主題相關應得門檻起始值:");
				expController.parama = scanner.nextDouble();
				if (expController.parama > 1) {
					throw new RuntimeException("輸入門檻值不得大於1");
				}
				expController.TopicRelatedScore();

			} else if (i.equals("2")) {
				System.out.println("請填入移除門檻起始值:");
				expController.parama = scanner.nextDouble();
				if (expController.parama > 1) {
					throw new RuntimeException("輸入門檻值不得大於1");
				}
				expController.removeThresholdExperiment();
			} else if (i.equals("3")) {
				expController.performanceExperiment();
			} else if (i.equals("4")) {
				for (int turn = 0; turn < expController.round; turn++) {
					expController.coreExperiment(turn);
					expController.corelessExperiment(turn);
				}
			} else if (i.equals("5")) {
				System.out.println("請填入長期門檻起始值:");
				expController.parama = scanner.nextDouble();
				for(int turn =0;turn <expController.round;turn++){
					expController.conceptDriftExperiment(turn);
				}
			}
			System.exit(0);
		}
	}
	private void conceptDriftExperiment(int turn) {
		Path project = this.rootPath.resolve("turn_"+turn);
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.7);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(0.7);
		MemoryBasedUserProfile.longTermThreshold = (int) (this.parama + 25*turn);
		exp = new Experiment(project.toString(),maper,user);
		exp.debugMode = true;
		exp.experimentDays = 14;
		
		BBCNewsPopulator populater = new BBCNewsPopulator(project){

			@Override
			public void setGenarationRule() {
				this.setTrainSize(10);
				this.setTestSize(5);
				this.trainTopics.clear();
				if(this.theDay<=7){
					this.addTrainingTopics("business");
				}else{
					this.addTrainingTopics("politics");
				}
				
			}
			
		};
		populater.addTrainingTopics("business");//only to avoid warning
		populater.addTestingTopics("politics");
		populater.addTestingTopics("business");
		ArrayList<String> topics = Lists.newArrayList(BBCNewsPopulator.TOPICS);
		topics.remove("business");
		topics.remove("politics");
		populater.addTestingTopics(topics.get(new Random(0).nextInt(topics.size())));
		exp.newsPopulater = populater;
		execute();	
		this.recordThisRound(turn);
	}
	private void coreExperiment(int turn) {
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
				Path tempProject = this.rootPath.resolve("Methode_"+methodName).resolve("round_"+turn);
				TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),topicSimliarityThreshold);
				user = new MemoryBasedUserProfile();
				user.setRemoveRate(removeRate);
				TopicTermGraph.MAXCORESIZE = 5 + turn*5;
				
				exp = new Experiment(tempProject.toString(),maper,user);
				exp.experimentDays = 14;
				exp.debugMode = true;
				
				BBCNewsPopulator populater = new BBCNewsPopulator(tempProject){
					@Override
					public void setGenarationRule() {
						this.setTrainSize(10);
						this.setTestSize(5);	
						
					}
					
				};
				populater.addTrainingTopics("business");
				populater.addTestingTopics("business");
				ArrayList<String> topics = Lists.newArrayList(BBCNewsPopulator.TOPICS);
				topics.remove("business");
				String randomTopic = topics.get(new Random(1).nextInt(topics.size()));
				populater.addTestingTopics(randomTopic);
				exp.newsPopulater = populater;
				execute();
				this.recordThisRound(turn*4+j);
		}
		
	}
	
	public void  corelessExperiment(int turn){
		Path tempProject = this.rootPath.resolve("round_"+turn+"coreless");
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.5);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(0.5);
		TopicTermGraph.MAXCORESIZE = 1000;
		
		exp = new Experiment(tempProject.toString(),maper,user);
		exp.experimentDays = 14;
		exp.debugMode = true;
		
		BBCNewsPopulator populater = new BBCNewsPopulator(tempProject){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(10);
				this.setTestSize(5);	
				
			}
			
		};
		populater.addTrainingTopics("business");
		populater.addTestingTopics("business");
		populater.addTestingTopics("sport");
		ArrayList<String> topics = Lists.newArrayList(BBCNewsPopulator.TOPICS);
		topics.remove("business");
		topics.remove("sport");
		String randomTopic = topics.get(new Random(1).nextInt(topics.size()));
		populater.addTestingTopics(randomTopic);
		exp.newsPopulater = populater;
		execute();
		this.recordThisRound(turn*4+3);
	}
	
	private void performanceExperiment() {
		String[][] trainTopic = {{"business","sport"},{"entertainment","politics"},{"sport","tech"},{"business","politics"},{"entertainment","tech"}};
		TopicTermGraph.METHODTYPE = 1; //LP method
		for(int i = 0;i<round;i++){
			Path tempProject = this.rootPath.resolve("round_"+i);
			TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.7);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.7);
			
			exp = new Experiment(tempProject.toString(),maper,user);
			exp.debugMode = true;
			exp.experimentDays = 10;
			
			BBCNewsPopulator populater = new BBCNewsPopulator(tempProject){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
			populater.addTrainingTopics(trainTopic[i][0]);
			populater.addTrainingTopics(trainTopic[i][1]);
			for(String topic:BBCNewsPopulator.TOPICS){
				populater.addTestingTopics(topic);
			}
			exp.newsPopulater = populater;
			execute();
		}
	}
	private void removeThresholdExperiment() {
		int experimentDays = 14;
		for (int i = 0; i < round; i++) {
			Path tempDir = this.rootPath.resolve("turn_" + i);
			double removeRate = parama + (i/10.0);
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),0.4);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(removeRate);

			this.exp = new Experiment(tempDir.toString(), maper, user);
			this.exp.setExperimentDays(experimentDays);
			exp.debugMode = true;

			removeRate = parama + (i / 10.0);

			BBCNewsPopulator populater = new BBCNewsPopulator(tempDir) {
				@Override
				public void setGenarationRule() {
					this.setTrainSize(3);
					this.setTestSize(3);

				}

			};
			exp.newsPopulater = populater;
			populater.addTrainingTopics("business");
			populater.addTrainingTopics("entertainment");
			populater.addTrainingTopics("politics");
			for (String topic : BBCNewsPopulator.TOPICS) {
				populater.addTestingTopics(topic);
			}

			execute();
			this.recordThisRound(i);
		}		
	}
	private void TopicRelatedScore() {
		for(int i = 0;i<round;i++){
			this.topicSimliarityThreshold = parama + (i/10.0);

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
					this.setTestSize(5);	
					
				}
				
			};
			exp.newsPopulater = populater;
			for(String topic:BBCNewsPopulator.TOPICS){
				populater.addTestingTopics(topic);
			}
			populater.addTrainingTopics("tech");

			execute();
			this.recordThisRound(i);
		}		
	}
}
