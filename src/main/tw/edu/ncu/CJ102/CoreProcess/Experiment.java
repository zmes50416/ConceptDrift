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

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;

public class Experiment {
	private Path projectPath,userProfilePath;
	private AbstractUserProfile user;
	ExperimentFilePopulater newsPopulater;
	TopicMappingTool maper;
	protected int experimentDays;
	Boolean isInitialized = false;
	protected HashSet<String> traingLabel= new HashSet<>();
	protected HashMap<TopicTermGraph,PerformanceMonitor> monitors= new HashMap<>();
	protected PerformanceMonitor systemPerformance = new PerformanceMonitor();
	public boolean debugMode;
	public Experiment(String project) {		// 創造出實驗資料匣
		try{
			this.projectPath = Paths.get(project);
			this.userProfilePath = projectPath.resolve("user_profile");
		
			Files.createDirectories(projectPath);
			Files.createDirectories(userProfilePath); // 創造出實驗使用者模型資料匣
			
			Files.createFile(projectPath.resolve(".lock"));//give the flag that this project have been create but not finish yet.
		} catch(FileAlreadyExistsException e){
			throw new RuntimeException("The Project have been lock in others process, please clean the project dir first");
		}catch (IOException e) {
			throw new RuntimeException("IO have been Interrupted");
		}
		
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
		}
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
		Files.createDirectories(projectPath.resolve(ExperimentFilePopulater.TESTINGPATH));
		Files.createDirectories(projectPath.resolve(ExperimentFilePopulater.TRAININGPATH));
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
		if(debugMode == true){
			this.simplelog(today);
		}
		userManager.updateUserProfile(today, user);
		this.removeOutdatedMonitor();
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
		for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
			if(topicPair.getKey()!=topicPair.getValue()){//the same topic mean no likliy topic in user profile
				systemAnswer = true;
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
			if (realAnswer == true) {// two possible Type: TP,FN
				if (matchedTopics.contains(topic)) {// topic is matched -- TP
					PerformanceMonitor monitor = this.monitors.get(topic);
					monitor.set_EfficacyMeasure(PerformanceType.TRUEPOSTIVE);
				} else {// topic is not matched -- FN
					PerformanceMonitor monitor = this.monitors.get(topic);
					monitor.set_EfficacyMeasure(PerformanceType.FALSENEGATIVE);
				}
			} else { // two possible type: FP,TN
				if (matchedTopics.contains(topic)) {// topic is matched but not relative -- FP
					PerformanceMonitor monitor = this.monitors.get(topic);
					monitor.set_EfficacyMeasure(PerformanceType.FALSEPOSTIVE);

				} else { // topic is not matched, and it is not relative too -- TN
					PerformanceMonitor monitor = this.monitors.get(topic);
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
				writer.append("topic:"+topic.hashCode()+",is Long term:"+topic.isLongTermInterest()+",Decay Factor:"+user.getDecayRate(topic, theDay)+",number of terms:"+topic.getVertexCount()+" Core term:"+topic.getCoreTerm());
				writer.newLine();
				
			}
			writer.append("System Performance:"+this.systemPerformance);
			writer.newLine();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
