package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;

import javax.swing.JFileChooser;

public class Experiment1 {

	public static void main(String[] args) {
		String projectDir = new Experiment1().chooseProject();
		System.out.println(projectDir);
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
	
	public String chooseProject(){
		JFileChooser projectChooser = new JFileChooser(new File("."));
		projectChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(!(projectChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)){
			throw new Error();
		}else{
			return projectChooser.getSelectedFile().getAbsolutePath();
		}
		
	}

}
