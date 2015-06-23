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
	public BBCExperimentsCase(Path path) {
		super(path);
		this.topicPath = SettingManager.getSetting("bbcDataSet");
	}

	@Override
	public void conceptDriftExperiment(int turn,int j) {
		Path project = this.rootDir.resolve("turn_"+turn);
		this.topicSimliarityThreshold = 0.7;
		this.removeRate = 0.7;
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		MemoryBasedUserProfile.longTermThreshold = (int) (this.parama + 25*turn);
		experiment = new Experiment(project.toString(),maper,user);
		experiment.debugMode = true;
		experiment.experimentDays = 14;
		
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
		experiment.newsPopulater = populater;
	}
	@Override
	public void coreExperiment(int turn) {
		this.topicSimliarityThreshold = 0.6;
		this.removeRate = 0.5;
		String methodName = null;
		if (TopicTermGraph.METHODTYPE == 0) {
			methodName = "Degree";
		} else if (TopicTermGraph.METHODTYPE == 1) {
			methodName = "LP";
		} else if (TopicTermGraph.METHODTYPE == 2) {
			methodName = "Betweenness";
		}
		Path tempProject = this.rootDir.resolve("Methode_" + methodName).resolve("round_" + turn);
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(), topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(removeRate);
		TopicTermGraph.MAXCORESIZE = 5 + turn * 5;

		experiment = new Experiment(tempProject.toString(), maper, user);
		experiment.experimentDays = 14;
		experiment.debugMode = true;

		BBCNewsPopulator populater = new BBCNewsPopulator(tempProject) {
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
		experiment.newsPopulater = populater;
		
	}
	@Override
	public void  corelessExperiment(int turn){
		this.topicSimliarityThreshold = 0.5;
		this.removeRate = 0.5;
		this.experimentDays = 14;
		Path tempProject = this.rootDir.resolve("round_"+turn+"coreless");
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		TopicTermGraph.MAXCORESIZE = 1000;
		
		experiment = new Experiment(tempProject.toString(),maper,user);
		experiment.setExperimentDays(this.experimentDays);
		experiment.debugMode = true;
		
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
		experiment.newsPopulater = populater;
	}
	@Override
	public void performanceExperiment(int turn) {
		this.topicSimliarityThreshold = 0.7;
		this.removeRate = 0.7;
		String[][] trainTopic = { { "business", "sport" },
				{ "entertainment", "politics" }, { "sport", "tech" },
				{ "business", "politics" }, { "entertainment", "tech" } };
		TopicTermGraph.METHODTYPE = 1; // LP method
		Path tempProject = this.rootDir.resolve("round_" + turn);
		TopicMappingTool maper = new TopicMappingTool(
				new NgdReverseTfTopicSimilarity(), this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);

		experiment = new Experiment(tempProject.toString(), maper, user);
		experiment.debugMode = true;
		experiment.experimentDays = 10;

		BBCNewsPopulator populater = new BBCNewsPopulator(tempProject) {
			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(5);

			}

		};
		populater.addTrainingTopics(trainTopic[turn][0]);
		populater.addTrainingTopics(trainTopic[turn][1]);
		for (String topic : BBCNewsPopulator.TOPICS) {
			populater.addTestingTopics(topic);
		}
		experiment.newsPopulater = populater;
	}
	@Override
	public void removeThresholdExperiment(int turn) {
		int experimentDays = 14;
		this.topicSimliarityThreshold = 0.7;
		this.removeRate = parama + (turn / 10.0);

		Path tempDir = this.rootDir.resolve("turn_" + turn);
		TopicMappingTool maper = new TopicMappingTool(
				new NgdReverseTfTopicSimilarity(), this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(removeRate);

		this.experiment = new Experiment(tempDir.toString(), maper, user);
		this.experiment.setExperimentDays(experimentDays);
		experiment.debugMode = true;
		BBCNewsPopulator populater = new BBCNewsPopulator(tempDir) {
			@Override
			public void setGenarationRule() {
				this.setTrainSize(3);
				this.setTestSize(3);

			}

		};
		experiment.newsPopulater = populater;
		populater.addTrainingTopics("business");
		populater.addTrainingTopics("entertainment");
		populater.addTrainingTopics("politics");
		for (String topic : BBCNewsPopulator.TOPICS) {
			populater.addTestingTopics(topic);
		}

	}
	@Override
	public void TopicRelatedScore(int turn) {
		this.experimentDays = 10;
		this.topicSimliarityThreshold = parama + (turn / 10.0);
		this.removeRate = 0.1;
		TopicMappingTool maper = new TopicMappingTool(
				new NgdReverseTfTopicSimilarity(),
				this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		Path tempDir = this.rootDir.resolve("turn_" + turn);
		
		experiment = new Experiment(tempDir.toString(), maper, user);
		experiment.debugMode = true;
		this.experiment.setExperimentDays(this.experimentDays);

		BBCNewsPopulator populater = new BBCNewsPopulator(tempDir) {
			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(5);

			}

		};
		experiment.newsPopulater = populater;
		for (String topic : BBCNewsPopulator.TOPICS) {
			populater.addTestingTopics(topic);
		}
		populater.addTrainingTopics("tech");

	}
}
