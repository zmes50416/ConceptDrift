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
import java.util.HashMap;

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
			
			Files.createFile(projectPath.resolve(".lock"));//give the flag that this project have been creat but not finish yet.
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
	public void run() {
		for(int day=1;day<=this.experimentDays;day++){
			trainFromSimpleText(day);
			test(day);
		}
		
	}
	/**
	 * 訓練使用者模型階段
	 * 應該進行讀取當天文件、主題映射、更新前一天的遺忘因子、加入新的文章主題進入使用者模型、記錄主題共現
	 * @param days
	 */
	private void trainFromSimpleText(int theDay){
		Path training = this.projectPath.resolve("training/day_"+theDay);
		UserProfileManager userManager = new UserProfileManager(this.maper);

		for(File doc:training.toFile().listFiles()){
			try(BufferedReader documentReader = new BufferedReader(new FileReader(doc));){
				double ngd = Double.valueOf(documentReader.readLine());
				System.out.print("取出文件NGD=" + ngd + "\n");
				ArrayList<TopicTermGraph> documentTopics = new ArrayList<>();

				double docTF = 0; // 單文件的TF值
				for(String line = documentReader.readLine();line!=null;line = documentReader.readLine()){
					String term = line.split(",")[0]; // 字詞
					int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
					double TFScore = Integer.valueOf(line.split(",")[1]); // 字詞分數
					docTF += TFScore;
					TopicTermGraph c = null;
					try{
						c = documentTopics.get(group);
					}catch(IndexOutOfBoundsException e){
						c = new TopicTermGraph(group,theDay);
						documentTopics.add(c);
					}finally{
						boolean isAdd = c.addVertex(new TermNode(term,TFScore));
						c.setUpdateDate(theDay);
						if(!isAdd){
							throw new RuntimeException("Term are duplicate in document! please check");
						}
					}
				}//end of For
				
				userManager.addTopic(documentTopics, user);
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}//end of for(Doc)
		userManager.updateUserProfile(theDay, user);
		
	}
	private void test(int days){
		
	}

}
