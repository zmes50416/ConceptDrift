package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Experiment {
	private Path projectPath;
	private AbstractUserProfile user;
	ExperimentFilePopulater newsPopulater;
	TopicMappingTool maper;
	protected int experimentDays;
	Boolean isInitialized = false;
	
	public Experiment(String project) {		// 創造出實驗資料匣
		try{
			this.projectPath = Paths.get(project);
			Path userProfile = projectPath.resolve("user_profile");
		
			Files.createDirectories(projectPath);
			Files.createDirectories(userProfile); // 創造出實驗使用者模型資料匣
			
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
	private void train(int theDay){
		Path training = this.projectPath.resolve("training/day_"+theDay);
		UserProfileManager userManager = new UserProfileManager(this.maper);
		
		userManager.updateUserProfile(theDay, user);
		for(File doc:training.toFile().listFiles()){
			List<TopicTermGraph> documentTopics = this.readFromSimpleText(theDay,doc);
			String topicLabel = this.newsPopulater.getTopics(doc);
			userManager.addTopic(documentTopics, user);
		}
		
		userManager.updateUserProfile(theDay, user);
		
	}
	
	private void test(int theDay){
		//TODO implement testing phase
		Path testingPath = this.projectPath.resolve("testing/day_" + theDay);
		for(File doc:testingPath.toFile().listFiles()){
			
		}
	}
	/**
	 * Read the Simple txt file from preprocess to get document topic
	 * @param theDay
	 * @param doc 
	 * @return List of Document topic or Null if Exception happened
	 */
	private List<TopicTermGraph> readFromSimpleText(int theDay, File doc){
		try(BufferedReader documentReader = new BufferedReader(new FileReader(doc));){
			double ngd = Double.valueOf(documentReader.readLine());
			System.out.print("取出文件NGD=" + ngd + "\n");
			TopicTermGraph[] documentTopics = new TopicTermGraph[5];
			
			double docTF = 0; // 單文件的TF值
			for(String line = documentReader.readLine();line!=null;line = documentReader.readLine()){
				String term = line.split(",")[0]; // 字詞
				int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
				double TFScore = Integer.valueOf(line.split(",")[1]); // 字詞分數
				docTF += TFScore;
				TopicTermGraph c = null;
				try{
					c = documentTopics[group];
				}catch(IndexOutOfBoundsException e){
					c = new TopicTermGraph(theDay);
					documentTopics[group] = c;
				}finally{
					c.addVertex(new TermNode(term,TFScore));
					c.setUpdateDate(theDay);
				}
			}
			return Arrays.asList(documentTopics);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		
	}

}
