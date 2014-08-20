package tw.edu.ncu.CJ102;

import java.io.File;
import java.io.IOException;
import java.util.Set;
/*
 * Writer: 蘇鼎文
 * 實驗流程,每一個class應該都會依據正確順序運行Experiment Workflow, Run this for will Run every class correctly in order
 * 
 */
public class testRun {

	public static void main(String[] args) {
		SettingManager set = SettingManager.getSettingManager();
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
		new POS_Filter().filterDir(set.getSetting(SettingManager.KFCDIR));
		for(File file:new File(set.getSetting(SettingManager.POSFilterDIR)).listFiles()){
			try {
				Lucene_Search1.doit(file.getName(),set.getSetting(SettingManager.POSFilterDIR));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
