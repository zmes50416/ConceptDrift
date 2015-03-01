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
			mUserProfile.setRemoveRate(0.1);
			exp.mUserProfile= mUserProfile;
			RouterNewsPopulator p = new RouterNewsPopulator(turnProjectDir){

				@Override
				public void setGenarationRule() {
					this.trainSize = 5;
					this.testSize = 5;					
				}
				
			};
			
			for(String topic:RouterNewsPopulator.test){
				p.addTestingTopics(topic);
			}
			p.addTrainingTopics("acq");
			exp.setExperementSource(p);
			exp.start();
		}
	}
	
	public void topicRemovingExperiment(){
		for(double i=0.1;i<=0.9;i=i+0.1){
			String turnProjectDir = projectDir+"\\LongFreq_"+i+"_removeRate\\";
					Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(15);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(i);
			RouterNewsPopulator longFreqInterest = new RouterNewsPopulator(turnProjectDir){
				@Override
				public void setGenarationRule(){
					this.trainSize = 1;
					this.testSize = 1;
				}
			};
			for(String topic:RouterNewsPopulator.test){
				longFreqInterest.addTestingTopics(topic);
			}
			longFreqInterest.addTrainingTopics("acq");
			exp.setExperementSource(longFreqInterest);
			exp.start();
		}
		for(double i=0.1;i<=0.9;i=i+0.1){
			String turnProjectDir = projectDir+"\\LongRare_"+i+"_removeRate\\";
					Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(15);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(i);
			RouterNewsPopulator longRareInterest = new RouterNewsPopulator(turnProjectDir){
				@Override
				public void setGenarationRule() {
						if(this.theDay==1||this.theDay==8){
							this.trainSize = 3;
							this.testSize = 3;
						}else{
							this.trainSize = 0;
							this.testSize = 0;
						}					
				}
			};
			for(String topic:RouterNewsPopulator.test){
				longRareInterest.addTestingTopics(topic);
			}
			longRareInterest.addTrainingTopics("acq");
			exp.setExperementSource(longRareInterest);
			exp.start();
		}
		for(double i=0.1;i<=0.9;i=i+0.1){
			String turnProjectDir = projectDir+"\\shortFreq_"+i+"_removeRate\\";
					Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(15);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(i);
			RouterNewsPopulator shortFreqInterest = new RouterNewsPopulator(turnProjectDir){
				
				@Override
				public void setGenarationRule() {
					if(this.theDay <= 7){
						this.trainSize = 1;
						this.testSize = 1;
					}else{
						this.trainSize = 0;
						this.testSize = 0;
					}
					
				}
			};
			for(String topic:RouterNewsPopulator.test){
				shortFreqInterest.addTestingTopics(topic);
			}
			shortFreqInterest.addTrainingTopics("acq");
			exp.setExperementSource(shortFreqInterest);
			exp.start();
		}
		for(double i=0.1;i<=0.9;i=i+0.1){
			String turnProjectDir = projectDir+"\\ShortRare_"+i+"_removeRate\\";
			Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(15);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(i);
			RouterNewsPopulator shortRareInterest = new RouterNewsPopulator(turnProjectDir){
				
				@Override
				public void setGenarationRule() {
					if(this.theDay == 1){
						this.setTrainSize(3);
						this.setTestSize(3);
					}else{
						this.trainSize = 0;
						this.testSize = 0;
					}
					
				}
			};
			for(String topic:RouterNewsPopulator.test){
				shortRareInterest.addTestingTopics(topic);
			}
			shortRareInterest.addTrainingTopics("acq");
			exp.setExperementSource(shortRareInterest);
			exp.start();
		}
		
	}
	
	
	public void topicClosenessExperiment(){
		
	}
	
	

}
