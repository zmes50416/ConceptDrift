package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;

import javax.swing.JFileChooser;

public class Experiment1 {

	public static void main(String[] args) {
		String projectDir = new Experiment1().chooseProject();
		System.out.println(projectDir);
		for(int i=1;i<=1;i++){
			Tom_exp exp = new Tom_exp(projectDir+"\\"+i+"_turn\\");
			exp.setExperimentDays(10);
			UserProfile mUserProfile = new UserProfile(true);
			mUserProfile.setRemoveRate(0.1);
			exp.mUserProfile= mUserProfile;
			exp.size = 5;
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
