package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.Graph;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.CEdge;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.im.Preprocess.RouterNewsPreprocessor;
import tw.edu.ncu.im.Preprocess.Decorator.*;
import tw.edu.ncu.im.Util.*;

public class Experiment {
	private Path projectPath,userProfilePath;
	private AbstractUserProfile user;
	ExperimentFilePopulater newsPopulater;
	protected UserProfileManager userManager;
	protected int experimentDays;
	Boolean isInitialized = false;
	protected Set<String> traingLabel;//系統正確主題指標
	protected PerformanceMonitor systemDailyPerformance;
	public boolean debugMode;
	double betweenessThreshold = 0.35;
	private Logger logger = LoggerFactory.getLogger(Experiment.class);
	public Experiment(String project,TopicMappingTool maper,AbstractUserProfile user) {		// 創造出實驗資料匣
		this.projectPath = Paths.get(project);
		this.userProfilePath = projectPath.resolve("user_profile");
		this.systemDailyPerformance = new PerformanceMonitor();
		this.userManager= new UserProfileManager(maper);
		this.user = user;
		File lock = projectPath.resolve(".lock").toFile();
		if(lock.isFile()){
			throw new IllegalStateException("The Project have been lock in others process, please clean the project dir first");
		}
		
		System.getProperties().setProperty("project.dir", project);
		PropertyConfigurator.configure(System.getProperty("user.dir")+"\\src\\main\\resources\\log4j.properties");
	}
	
	public Path getProjectPath() {
		return projectPath;
	}

	/**
	 * @return the userProfilePath
	 */
	public Path getUserProfilePath() {
		return userProfilePath;
	}

	public AbstractUserProfile getUser() {
		return user;
	}
	public void setUser(AbstractUserProfile user) {
		this.user = user;
	}
	public int getExperimentDays() {
		return experimentDays;
	}

	public void setExperimentDays(int experimentDays) {
		this.experimentDays = experimentDays;
	}

	/**
	 * prepare the experiment traininig & testing data
	 * insure every data is ready to use
	 * @throws IOException 
	 */
	
	public void initialize() throws IOException{
		if(this.experimentDays==0){
			throw new RuntimeException("Haven't set the experiment days yet");
		}else if(this.user == null){
			throw new RuntimeException("Haven't set the user yet");
		}else if(this.newsPopulater == null){
			throw new RuntimeException("Haven't set the news populater");
		}
		
		Files.createDirectories(projectPath);
		Files.createDirectories(userProfilePath); // 創造出實驗使用者模型資料匣
		Files.createDirectories(projectPath.resolve(ExperimentFilePopulater.TESTINGPATH));
		Files.createDirectories(projectPath.resolve(ExperimentFilePopulater.TRAININGPATH));
		Files.createFile(projectPath.resolve(".lock"));//give the flag that this project have been create but not finish yet.
		if(this.debugMode ==true){
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.projectPath.resolve("setting.txt").toFile(),true))){
				writer.write("主題相關門檻值:"+userManager.mapper.relateness_threshold);
				writer.newLine();
				writer.write("實驗天數:"+this.experimentDays);
				writer.newLine();
				writer.write("去除比例:"+this.user.getRemoveRate());
				writer.newLine();
				writer.write("Core Size:"+TopicTermGraph.MAXCORESIZE);
				writer.newLine();
				writer.write("Long Term Threshold:"+user.getLongTermThreshold());
				writer.newLine();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		this.newsPopulater.populateExperiment(experimentDays);
	}
	/**
	 * 執行第n天的
	 * @param dayN
	 */
	public void run(int dayN) {
		if(dayN<=this.experimentDays){
			this.systemDailyPerformance.saveRecord();
			if(this.systemDailyPerformance.phTest()){
				logger.warn("Concept Drift Happened in day {}",dayN-1);
			}
			userManager.checkTopicType(user);
			userManager.updateUserProfile(dayN, user);
			userManager.removeForgottenTopics(user);
			
			if(debugMode == true){
				this.simplelog(dayN);
				try {
					Path drawDir = this.userProfilePath.resolve("Day_"+dayN);
					Files.createDirectories(drawDir);
					userManager.draw(user,drawDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			train(dayN);
			test(dayN);
			

			
			
		}else{
			throw new IllegalArgumentException(dayN+" are not valid date");
		}
		
	}
	/**
	 * 訓練使用者模型階段
	 * 應該進行讀取當天文件、主題映射、更新前一天的遺忘因子、加入新的文章主題進入使用者模型、記錄主題共現
	 * @param days
	 */
	protected void train(int today){
		Path training = this.projectPath.resolve("training/day_"+today);
		int countOfNewTopic = 0;
		traingLabel = new HashSet<String>();//training label只會有當天的目標
		for(File doc:training.toFile().listFiles()){
			List<TopicTermGraph> documentTopics = this.readFromSimpleText(today,doc);
			String docTopic = this.newsPopulater.identifyTopic(doc);
			this.traingLabel.add(docTopic);
			Map<TopicTermGraph, TopicTermGraph> topicMap = userManager.mapTopics(documentTopics, user);
			
			for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
				if(topicPair.getKey()==topicPair.getValue()){//new Topic, add a monitor
					countOfNewTopic++;
				}
			}
			user.addDocument(topicMap,today);
		}
		
		logger.info("Day {}, {} New Topic is generated",today,countOfNewTopic);
		
	}
	
	protected void test(int today){
		Path testingPath = this.projectPath.resolve("testing/day_" + today);
		
		for(File doc:testingPath.toFile().listFiles()){
			String docTopic = this.newsPopulater.identifyTopic(doc);
			List<TopicTermGraph> documentTopics = this.readFromSimpleText(today, doc);
			Map<TopicTermGraph, TopicTermGraph> topicMap = userManager.mapTopics(documentTopics, user);
			this.performanceTest(topicMap, docTopic);
		}
		
	}
	/**
	 * Read the Simple txt file from preprocess process to get document topic
	 * @param theDay
	 * @param doc 
	 * @return List of Document topic or Null if Exception happened
	 */
	public List<TopicTermGraph> readFromSimpleText(int theDay, File doc){
		try(BufferedReader documentReader = new BufferedReader(new FileReader(doc));){
			documentReader.readLine(); //Skip first NGD line
			TopicTermGraph[] documentTopics = new TopicTermGraph[1];//因為無法得知大小，因此先給1個空間，當空間不夠時再產生新的陣列
			//不使用List，因為無法保證插入順序 if 1 > 3 > 2  then it will be 1 2[3] 3[2]
			for(String line = documentReader.readLine();line!=null;line = documentReader.readLine()){
				String term = line.split(",")[0]; // 字詞
				int group = Integer.valueOf(line.split(",")[2])-1; // 字詞所屬群別
				double TFScore = Double.valueOf(line.split(",")[1]); // 字詞分數

				TopicTermGraph c = null;
				try{
					c = documentTopics[group];
				}catch(ArrayIndexOutOfBoundsException e){ 
					TopicTermGraph[] temp = documentTopics;
					documentTopics = new TopicTermGraph[group+1];
					System.arraycopy(temp, 0, documentTopics, 0, temp.length);
				}
				
				if(c==null){
					c = new TopicTermGraph(theDay);
					documentTopics[group] = c;
				}
					TermNode addTerm = new TermNode(term,TFScore);
					c.addVertex(addTerm);
					for(TermNode otherVertex:c.getVertices()){
						if(otherVertex!= addTerm){
							c.addEdge(new CEdge(), addTerm, otherVertex);
						}
					}
			}
			return Arrays.asList(documentTopics);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		
	}
	/**
	 * preprocess document on the fly
	 * @param theDay
	 * @param doc raw txt file
	 * @return a set of document topics
	 */
	public List<TopicTermGraph> readFromDTG(int theDay,File doc){
		Transformer<CEdge, Double> edgeTransformer = new Transformer<CEdge,Double>(){

			@Override
			public Double transform(CEdge input) {
				double weight = input.getCoScore();
				return weight;
			}
			
		};
		
		RouterNewsPreprocessor<TermNode,CEdge> c = new RouterNewsPreprocessor<TermNode,CEdge>(new Factory<TermNode>(){

			@Override
			public TermNode create() {
				return new TermNode();
			}
			
		},new Factory<CEdge>(){

			@Override
			public CEdge create() {
				return new CEdge();
			}
			
		});
		c.execute(doc);

		PartOfSpeechFilter<TermNode,CEdge> posComp = new PartOfSpeechFilter<TermNode,CEdge>(c, c.getStringOfVertex());
		TermToLowerCaseDecorator<TermNode,CEdge> lowerComp = new TermToLowerCaseDecorator<TermNode,CEdge>(posComp, posComp.getVertexResultsTerms());
		FilteredTermLengthDecorator<TermNode,CEdge> termLengthComp = new FilteredTermLengthDecorator<TermNode,CEdge>(lowerComp, posComp.getVertexResultsTerms(), 3);
		
		StemmingDecorator<TermNode,CEdge> stemmedC = new StemmingDecorator<TermNode,CEdge>(termLengthComp, posComp.getVertexResultsTerms());
		TermFreqDecorator<TermNode,CEdge> tfComp = new TermFreqDecorator<TermNode,CEdge>(stemmedC, posComp.getVertexResultsTerms());
		SearchResultFilter<TermNode,CEdge> filitedTermComp = new SearchResultFilter<TermNode,CEdge>(tfComp,  posComp.getVertexResultsTerms(), 10, 1000, new EmbeddedIndexSearcher());
		NGDistanceDecorator<TermNode,CEdge> ngdComp = new NGDistanceDecorator<TermNode,CEdge>(filitedTermComp,posComp.getVertexResultsTerms(),new EmbeddedIndexSearcher());
		NgdEdgeFilter<TermNode,CEdge> ngdflitedComp = new NgdEdgeFilter<TermNode,CEdge>(ngdComp, ngdComp.getEdgeDistance(), 0.5);
		Graph<TermNode,CEdge> docGraph = ngdflitedComp.execute(doc);
		
		HashSet<TermNode> termsToRemove = new HashSet<>();
		for(TermNode term:docGraph.getVertices()){
			if(docGraph.getNeighborCount(term)==0){
				termsToRemove.add(term);
			}else{
				term.termFreq = tfComp.getTermFreqMap().get(term);
				term.setTerm(posComp.getVertexResultsTerms().get(term));
			}
		}
		for(CEdge edge:docGraph.getEdges()){
			edge.setCoScore(ngdComp.getEdgeDistance().get(edge));
		}
		for(TermNode term:termsToRemove){
			docGraph.removeVertex(term);
			tfComp.getTermFreqMap().remove(term);
			posComp.getVertexResultsTerms().remove(term);
		}
		
		System.out.println("Size of vertex:"+docGraph.getVertexCount());
		System.out.println("Size of Edges:"+docGraph.getEdgeCount());
		
		int numOfEdgeToRemove = (int) (docGraph.getEdgeCount()*betweenessThreshold);
		EdgeBetweennessClusterer<TermNode,CEdge> bc = new EdgeBetweennessClusterer<>(numOfEdgeToRemove);
//		EdgeBetweennessClusterer<TermNode,CEdge> bc = new WeightedBetweennessCluster<>(numOfEdgeToRemove,edgeTransformer);

		Set<Set<TermNode>> clusters = bc.transform(docGraph);
		List<TopicTermGraph> topics = new ArrayList<TopicTermGraph>();
		
		for(Set<TermNode> cluster:clusters){
			TopicTermGraph topic = new TopicTermGraph(theDay);
			for(TermNode term:cluster){
				topic.addVertex(term);
			}
			topics.add(topic);
		}
		return topics;
		
	}
	/**
	 * 測試此文件是否為相關文件
	 * @param topicMap
	 * @param documentTopicLabel
	 */
	protected void performanceTest(Map<TopicTermGraph, TopicTermGraph> topicMap,String documentTopicLabel){
		boolean realAnswer = false;
		if(this.traingLabel.contains(documentTopicLabel)){
			realAnswer = true;
		}
		boolean systemAnswer = false;
		int hitCount =0;//at least half of topic should be in cover
		for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
			if(topicPair.getKey()!=topicPair.getValue()){//the same topic mean no likliy topic in user profile
				hitCount ++;
				if(hitCount>=((topicMap.size()/3)*2)){
					systemAnswer = true;
					break;
				}
			}
		}
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.userProfilePath.resolve("testResult.txt").toFile(),true))){
			if (realAnswer == true) {// two possible Type: TP,FN
				if(systemAnswer==true){
					writer.write(documentTopicLabel+":TP");
					this.systemDailyPerformance.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
				}else{
					writer.write(documentTopicLabel+":FN");
					this.systemDailyPerformance.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
				}
			} else { // two possible type: FP,TN
				if(systemAnswer==true){
					writer.write(documentTopicLabel+":FP");
					this.systemDailyPerformance.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
				}else{
					writer.write(documentTopicLabel+":TN");
					this.systemDailyPerformance.set_EfficacyMeasure(PerformanceType.TRUENEGATIVE);
				}
			}//end of RealAnswer if
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	protected void simplelog(int theDay){
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.userProfilePath.resolve("userLog.txt").toFile(),true))){
			writer.append("==Day"+theDay+"==");
			writer.newLine();
			Collection<TopicTermGraph> topics = user.getUserTopics();
			writer.append("Size of Topics:"+topics.size());
			writer.newLine();
			writer.append("Topic remove threshold:"+user.getTopicRemoveThreshold());
			writer.newLine();
			writer.append("Term remove threshold:"+user.getTermRemoveThreshold());
			writer.newLine();
			writer.append("LongTermThreshold:"+user.getLongTermThreshold());
			writer.newLine();
			for(TopicTermGraph topic:topics){
				writer.append("topic:"+topic.toString()+"value:"+(int)topic.getValue()+",is Long term:"+topic.isLongTermInterest()+",Decay Factor:"+topic.getDecayRate()+",number of terms:"+topic.getVertexCount()+" Core term:"+topic.getCoreTerm());
				writer.newLine();
			}
			writer.append("System Performance:"+this.systemDailyPerformance.getResult());
			writer.newLine();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
