package tw.edu.ncu.CJ102.CoreProcess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.BaseLineUserProfile;
import tw.edu.ncu.CJ102.Data.FreqBasedUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

import com.google.common.collect.Lists;

public class RouterExperimentCase extends AbstractExperimentCase {
	final String[][] TEST_TOPICS = {{"acq","earn"},{"crude","trade"},{"earn","coffee"},{"cocoa","sugar"},{"acq","crude"}};
	public RouterExperimentCase(Path _projectDir) {
		super(_projectDir);
		topicPath = "stanford/";
	}
	@Override
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
	}
	@Override
	public void removeThresholdExperiment(int turn){
		topicSimliarityThreshold = 0.7;
		experimentDays = 14;
		Path tempDir = this.rootDir.resolve("turn_" + turn);
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			removeRate = parama + (turn / 10.0);
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
	}
	@Override
	public void coreExperiment(int turn){
		String methodName = null;
		if(TopicTermGraph.METHODTYPE==0){
			methodName = "Degree";
		}else if(TopicTermGraph.METHODTYPE==1){
			methodName = "LP";
		}else if(TopicTermGraph.METHODTYPE==2){
			methodName = "Betweenness";
		}
		this.topicSimliarityThreshold = 0.6;
		this.removeRate = 0.5;
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
	}
	//core less will compare with core
	@Override
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
	}
	@Override
	public void oldConceptDriftExperiment(int turn, int j) {
		this.topicSimliarityThreshold = 0.8;
		this.removeRate = 0.7;	
		this.experimentDays = 14;
		Path project = this.rootDir.resolve("turn_" + turn).resolve(
				"seed_" + j);
		TopicMappingTool maper = new TopicMappingTool(
				new NgdReverseTfTopicSimilarity(),
				this.topicSimliarityThreshold);
		user = new BaseLineUserProfile(this.removeRate);
		user.longTermThreshold = (int) (25 * turn + parama);
		experiment = new Experiment(project.toString(), maper, user);
		experiment.debugMode = debugMode;
		experiment.setExperimentDays(experimentDays);


		RouterNewsPopulator populater = new RouterNewsPopulator(
				project.toString()) {

			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(10);
				this.trainTopics.clear();
				if (this.theDay <= 7) {
					this.addTrainingTopics(TEST_TOPICS[j][0]);

				} else {
					this.addTrainingTopics(TEST_TOPICS[j][1]);
				}

			}

		};
		populater.addTestingTopics(TEST_TOPICS[j][0]);
		experiment.newsPopulater = populater;
	}
	@Override
	public void conceptDriftExperiment(int turn, int seed) {
		this.topicSimliarityThreshold = 0.8;
		this.removeRate = 0.7;
		this.experimentDays = 14;
		Path project = this.rootDir.resolve("turn_" + turn).resolve(
				"seed_" + seed);
		TopicMappingTool maper = new TopicMappingTool(
				new NgdReverseTfTopicSimilarity(),
				this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		user.longTermThreshold = (int) (25 * turn + parama);
		experiment = new Experiment(project.toString(), maper, user);
		experiment.debugMode = debugMode;
		experiment.setExperimentDays(experimentDays);
		
		RouterNewsPopulator populater = new RouterNewsPopulator(
				project.toString()) {

			@Override
			public void setGenarationRule() {
				this.setTrainSize(5);
				this.setTestSize(10);
				this.trainTopics.clear();

				if (this.theDay <= 7) {
					this.addTrainingTopics(TEST_TOPICS[seed][0]);
				} else {
					this.addTrainingTopics(TEST_TOPICS[seed][1]);
				}

			}

		};
		populater.addTestingTopics(TEST_TOPICS[seed-1][0]);

//		populater.addTestingTopics(TEST_TOPICS[seed][1]);
		experiment.newsPopulater = populater;
	}
	@Override
	public void performanceExperiment(int turn){
		String[][] trainTopic={{"acq","earn"},{"crude","coffee"},{"sugar","trade"},{"acq","cocoa"},{"crude","trade"}};
		TopicTermGraph.METHODTYPE = 1; //LP method
		Path tempProject = this.rootDir.resolve("round_"+turn);
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
			populater.addTrainingTopics(trainTopic[turn][0]);
			populater.addTrainingTopics(trainTopic[turn][1]);
			for(String topic:RouterNewsPopulator.test){
				populater.addTestingTopics(topic);
			}
			experiment.newsPopulater = populater;
	}
	
	
}
