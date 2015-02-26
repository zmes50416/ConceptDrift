package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.io.IOException;

import tw.edu.ncu.CJ102.*;

import javax.swing.JFileChooser;

public class ThresholdExperiment {
	String projectDir;
	public static void main(String[] args) {
		ThresholdExperiment exp = new ThresholdExperiment(); 
		exp.projectDir = SettingManager.chooseProject();
		System.out.println(exp.projectDir);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.主題緊密門檻值");
		char i;
		try{
			do{
				i = (char)System.in.read();
				if(i == '1'){
					exp.topicMappingExperiment();
				}else if(i == '2'){
					exp.topicRemovingExperiment();
				}else if(i == '3'){
					exp.topicClosenessExperiment();
				}
			}while(i != '0');
		}catch(IOException e){
			i = '0';
			System.err.println("IO Have been Interrupted. System stopped.");
		}
	}
	
	public void topicMappingExperiment(){
		for(int i=1;i<=1;i++){
			String turnProjectDir = projectDir+"\\"+i+"_turn\\";
			Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(10);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(0.9);
			exp.mUserProfile= mUserProfile;
			exp.trainSize = 5;
			exp.testSize = 5;
			RouterNewsPopulator p = new RouterNewsPopulator(turnProjectDir);
			String test[] = { "acq", "earn", "crude", "coffee", "sugar",
					"trade", "cocoa" };
			for(String topic:test){
				p.addTestingTopics(topic);

			}
			p.addTrainingTopics("acq");
			exp.setExperementSource(p);
			exp.start();
		}
	}
	
	public void topicRemovingExperiment(){
		
	}
	
	public void topicClosenessExperiment(){
		
	}
	
	

}
