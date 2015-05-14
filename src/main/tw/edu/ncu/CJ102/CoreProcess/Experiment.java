package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.Graph;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.CEdge;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.im.Preprocess.PreprocessComponent;
import tw.edu.ncu.im.Preprocess.RouterNewsPreprocessor;
import tw.edu.ncu.im.Preprocess.Decorator.*;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.WeightedBetweennessCluster;

public class Experiment {
	private Path projectPath,userProfilePath;
	private AbstractUserProfile user;
	ExperimentFilePopulater newsPopulater;
	TopicMappingTool maper;
	protected int experimentDays;
	Boolean isInitialized = false;
	protected Set<String> traingLabel;
	protected Map<TopicTermGraph,PerformanceMonitor> monitors;
	protected PerformanceMonitor systemPerformance;
	public boolean debugMode;
	double betweenessThreshold = 0.35;
	public Experiment(String project) {		// 創造出實驗資料匣
		this.projectPath = Paths.get(project);
		this.userProfilePath = projectPath.resolve("user_profile");
		this.monitors= new HashMap<>();
		this.traingLabel= new HashSet<>();
		this.systemPerformance = new PerformanceMonitor();
		File lock = projectPath.resolve(".lock").toFile();
		if(lock.isFile()){
			throw new IllegalStateException("The Project have been lock in others process, please clean the project dir first");
		}
		
		System.getProperties().setProperty("project.dir", project);

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
				writer.write("主題相關門檻值:"+this.maper.relateness_threshold);
				writer.newLine();
				writer.write("實驗天數:"+this.experimentDays);
				writer.newLine();
				writer.write("去除比例:"+this.user.getRemove_rate());
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
			train(dayN);
			test(dayN);
		}
		
	}
	/**
	 * 訓練使用者模型階段
	 * 應該進行讀取當天文件、主題映射、更新前一天的遺忘因子、加入新的文章主題進入使用者模型、記錄主題共現
	 * @param days
	 */
	protected void train(int today){
		Path training = this.projectPath.resolve("training/day_"+today);
		UserProfileManager userManager = new UserProfileManager(this.maper);
		
		for(File doc:training.toFile().listFiles()){
			List<TopicTermGraph> documentTopics = this.readFromSimpleText(today,doc);
			String docTopic = this.newsPopulater.getTopics(doc);
			this.traingLabel.add(docTopic);
			Map<TopicTermGraph, TopicTermGraph> topicMap = userManager.mapTopics(documentTopics, user);
			
			for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
				if(topicPair.getKey()==topicPair.getValue()){//new Topic, add a monitor
					this.monitors.put(topicPair.getKey(), new PerformanceMonitor());
				}
			}
			user.addDocument(topicMap,today);
		}
		
		userManager.updateUserProfile(today, user);
		this.removeOutdatedMonitor();
		if(debugMode == true){
			this.simplelog(today);
		}
	}
	
	protected void test(int theDay){
		Path testingPath = this.projectPath.resolve("testing/day_" + theDay);
		UserProfileManager userManager = new UserProfileManager(this.maper);
		
		for(File doc:testingPath.toFile().listFiles()){
			String docTopic = this.newsPopulater.getTopics(doc);
			List<TopicTermGraph> documentTopics = this.readFromSimpleText(theDay, doc);
			Map<TopicTermGraph, TopicTermGraph> topicMap = userManager.mapTopics(documentTopics, user);
			this.performanceTest(topicMap, docTopic);
		}
		
		this.checkLongTermMemory();
		
	}
	/**
	 * Read the Simple txt file from preprocess to get document topic
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
				double TFScore = Integer.valueOf(line.split(",")[1]); // 字詞分數

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
					c.addVertex(new TermNode(term,TFScore));
					c.setUpdateDate(theDay);
				
			}
			return Arrays.asList(documentTopics);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		
	}
	public List<TopicTermGraph> readFromDTG(int theDay,File doc){
		Transformer<CEdge<Double>, Double> edgeTransformer = new Transformer<CEdge<Double>,Double>(){

			@Override
			public Double transform(CEdge<Double> input) {
				double weight = input.getCoScore();
				return weight;
			}
			
		};
		
		RouterNewsPreprocessor<TermNode,CEdge<Double>> c = new RouterNewsPreprocessor<TermNode,CEdge<Double>>(new Factory<TermNode>(){

			@Override
			public TermNode create() {
				return new TermNode();
			}
			
		},new Factory<CEdge<Double>>(){

			@Override
			public CEdge<Double> create() {
				return new CEdge<Double>();
			}
			
		});
		c.execute(doc);

		PartOfSpeechFilter<TermNode,CEdge<Double>> posComp = new PartOfSpeechFilter<TermNode,CEdge<Double>>(c, c.getStringOfVertex());
		TermToLowerCaseDecorator<TermNode,CEdge<Double>> lowerComp = new TermToLowerCaseDecorator<TermNode,CEdge<Double>>(posComp, posComp.getVertexResultsTerms());
		FilteredTermLengthDecorator<TermNode,CEdge<Double>> termLengthComp = new FilteredTermLengthDecorator<TermNode,CEdge<Double>>(lowerComp, posComp.getVertexResultsTerms(), 3);
		
		StemmingDecorator<TermNode,CEdge<Double>> stemmedC = new StemmingDecorator<TermNode,CEdge<Double>>(termLengthComp, posComp.getVertexResultsTerms());
		TermFreqDecorator<TermNode,CEdge<Double>> tfComp = new TermFreqDecorator<TermNode,CEdge<Double>>(stemmedC, posComp.getVertexResultsTerms());
		SearchResultFilter<TermNode,CEdge<Double>> filitedTermComp = new SearchResultFilter<TermNode,CEdge<Double>>(tfComp,  posComp.getVertexResultsTerms(), 10, 1000, new EmbeddedIndexSearcher());
		NGDistanceDecorator<TermNode,CEdge<Double>> ngdComp = new NGDistanceDecorator<TermNode,CEdge<Double>>(filitedTermComp,posComp.getVertexResultsTerms(),new EmbeddedIndexSearcher());
		NgdEdgeFilter<TermNode,CEdge<Double>> ngdflitedComp = new NgdEdgeFilter<TermNode,CEdge<Double>>(ngdComp, ngdComp.getEdgeDistance(), 0.5);
		Graph<TermNode,CEdge<Double>> docGraph = ngdflitedComp.execute(doc);
		
		HashSet<TermNode> termsToRemove = new HashSet<>();
		for(TermNode term:docGraph.getVertices()){
			if(docGraph.getNeighborCount(term)==0){
				termsToRemove.add(term);
			}else{
				term.termFreq = tfComp.getTermFreqMap().get(term);
				term.setTerm(posComp.getVertexResultsTerms().get(term));
			}
		}
		for(CEdge<Double> edge:docGraph.getEdges()){
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
		EdgeBetweennessClusterer<TermNode,CEdge<Double>> bc = new EdgeBetweennessClusterer<>(numOfEdgeToRemove);
//		EdgeBetweennessClusterer<TermNode,CEdge<Double>> bc = new WeightedBetweennessCluster<>(numOfEdgeToRemove,edgeTransformer);

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
//		boolean systemAnswer = false;
		boolean systemAnswer = true;
		for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
			if(topicPair.getKey()!=topicPair.getValue()){//the same topic mean no likliy topic in user profile
//			if(topicPair.getKey()==topicPair.getValue()){
				systemAnswer = false;
				break;
			}
		}
		Collection<TopicTermGraph> matchedTopics = topicMap.values();
		if (realAnswer == true) {// two possible Type: TP,FN
			if(systemAnswer==true){
				this.systemPerformance.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
			}else{
				this.systemPerformance.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
			}
		} else { // two possible type: FP,TN
			if(systemAnswer==true){
				this.systemPerformance.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
			}else{
				this.systemPerformance.set_EfficacyMeasure(PerformanceType.TRUENEGATIVE);
			}
		}//end of RealAnswer if
		
		
		for (TopicTermGraph topic : user.getUserTopics()) {
			PerformanceMonitor monitor = this.monitors.get(topic);
			if (realAnswer == true) {// two possible Type: TP,FN
				if (matchedTopics.contains(topic)) {// topic is matched -- TP
					monitor.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
				} else {// topic is not matched -- FN
					monitor.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
				}
			} else { // two possible type: FP,TN
				if (matchedTopics.contains(topic)) {// topic is matched but not relative -- FP
					monitor.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);
				} else { // topic is not matched, and it is not relative too -- TN
					monitor.set_EfficacyMeasure(PerformanceType.TRUENEGATIVE);
				}
			}// end of RealAnswer if
		}
		
		
	}
	
	protected void removeOutdatedMonitor(){
		Collection<TopicTermGraph> userTopics = this.user.getUserTopics();
		Iterator<Entry<TopicTermGraph, PerformanceMonitor>> iterator = this.monitors.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<TopicTermGraph, PerformanceMonitor> entry = iterator.next();
			if(!userTopics.contains(entry.getKey())){
				iterator.remove();
			}
		}
	}
	protected void checkLongTermMemory(){
		for(Entry<TopicTermGraph, PerformanceMonitor> monitorPair:this.monitors.entrySet()){
			PerformanceMonitor monitor = monitorPair.getValue();
			TopicTermGraph topic = monitorPair.getKey();
			if(topic.isLongTermInterest()&&monitor.phTest()){
				System.out.println("Concept Drift occur in topic:"+monitorPair.getKey());
			}
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
			int i = 1;
			for(TopicTermGraph topic:topics){
				writer.append("topic:"+topic.toString()+",is Long term:"+topic.isLongTermInterest()+",Decay Factor:"+user.getDecayRate(topic, theDay)+",number of terms:"+topic.getVertexCount()+" Core term:"+topic.getCoreTerm());
				writer.newLine();
				
			}
			writer.append("System Performance:"+this.systemPerformance);
			writer.newLine();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
