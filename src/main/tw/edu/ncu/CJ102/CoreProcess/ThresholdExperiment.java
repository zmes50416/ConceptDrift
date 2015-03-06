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
		System.out.println("You Dir is:"+exp.projectDir);
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
				System.out.println("Experimetn have been done!\n please choose next or press 0 to exit:");
			}while(i != '0');
		}catch(IOException e){
			i = '0';
			System.err.println("IO Have been Interrupted. System stopped.");
			e.printStackTrace();
		}
	}
	public void topicMappingExperiment(){
		for(int i=1;i<=1;i++){
			String turnProjectDir = projectDir+"\\"+i+"_turn\\";
			Tom_exp exp = new Tom_exp(turnProjectDir);
			exp.setExperimentDays(10);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(0.1);
			exp.setmUserProfile(mUserProfile);
			RouterNewsPopulator p = new RouterNewsPopulator(turnProjectDir){

				@Override
				public void setGenarationRule() {
					//this.setTrainSize(5);
					//this.setTestSize(5);					
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
			exp.setmUserProfile(new UserProfile(true));
			exp.getmUserProfile().setRemoveRate(i);
			RouterNewsPopulator longFreqInterest = new RouterNewsPopulator(turnProjectDir){
				@Override
				public void setGenarationRule(){
					this.setTrainSize(1);
					this.setTestSize(1);
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
			exp.setmUserProfile(new UserProfile(true));
			exp.getmUserProfile().setRemoveRate(i);
			RouterNewsPopulator longRareInterest = new RouterNewsPopulator(turnProjectDir){
				@Override
				public void setGenarationRule() {
						if(this.theDay==1||this.theDay==8){
							this.setTrainSize(3);
						}else{
							this.setTrainSize(0);;
						}
						this.setTestSize(1);
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
			exp.setmUserProfile(new UserProfile(true));
			exp.getmUserProfile().setRemoveRate(i);
			RouterNewsPopulator shortFreqInterest = new RouterNewsPopulator(turnProjectDir){
				
				@Override
				public void setGenarationRule() {
					if(this.theDay <= 7){
						this.setTrainSize(1);
					}else{
						this.setTrainSize(0);
					}
					this.setTestSize(1);
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
			exp.setmUserProfile(new UserProfile(true));
			exp.getmUserProfile().setRemoveRate(i);
			RouterNewsPopulator shortRareInterest = new RouterNewsPopulator(turnProjectDir){
				
				@Override
				public void setGenarationRule() {
					if(this.theDay == 1){
						this.setTrainSize(3);
					}else{
						this.setTrainSize(0);
					}
					this.setTestSize(1);

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
