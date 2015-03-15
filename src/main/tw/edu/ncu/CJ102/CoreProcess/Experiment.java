package tw.edu.ncu.CJ102.CoreProcess;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Experiment {
	private Path projectPath;
	private UserProfile user;
	ExperimentFilePopulater newsPopulater;
	TopicMaper maper;
	int experimentDays;
	Boolean isInitialized = false;
	public Experiment(String project) {
		this.projectPath = Paths.get(project);
		Path userProfile = projectPath.resolve("user_profile");
		// 創造出實驗資料匣
		try{
			Files.createDirectories(projectPath);
			Files.createDirectories(userProfile); // 創造出實驗使用者模型資料匣
			
			Files.createDirectories(projectPath.resolve("training"));
			Files.createDirectories(projectPath.resolve("testing"));
			
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

	public UserProfile getUser() {
		return user;
	}
	public void setUser(UserProfile user) {
		this.user = user;
	}
	
	public void initialize(){
		
	}
	public void run() throws Exception{
		if(this.experimentDays==0){
			throw new Exception("Haven't set the experiment days yet");
		}
		
	}
	private void train(){
		
	}
	private void test(){
		
	}

}
