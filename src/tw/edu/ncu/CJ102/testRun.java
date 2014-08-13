package tw.edu.ncu.CJ102;

import java.io.File;
import java.io.IOException;
/*
 * Writer: Su Ting Wen
 * Experiment Workflow, Run this for will Run every class correctly in order
 * 
 */
public class testRun {

	public static void main(String[] args) {
		SettingManager.getSettingManager();
		try {
			File F = new File(Qtag.readFilePath);
			for(File f : F.listFiles()){
				System.out.println(f.getName().split("\\.")[0]);
				Qtag.tagging(f.getName().split("\\.")[0]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TermFreqCount.counting(Qtag.writeFilePath);
	}

}
