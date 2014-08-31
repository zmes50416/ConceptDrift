package tw.edu.ncu.CJ102;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class testRun {
	/**
	 * @author 102鼎文
	 * 重現學長實驗流程,每一個class應該都會依據正確順序運行
	 */
	public static void main(String[] args) {
		SettingManager set = SettingManager.getSettingManager();
		try {
			File F = new File(Qtag.readFilePath);
			for(File f : F.listFiles()){
//				System.out.println(f.getName().split("\\.")[0]);
				Qtag.tagging(f.getName().split("\\.")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//new POS_Filter().filterDir(set.getSetting(SettingManager.KFCDIR));
		 
		 
		Term_Freq_and_POS_filter.counting(new File(Qtag.writeFilePath));
//		for(File file:new File(set.getSetting(SettingManager.POSFilterDIR)).listFiles()){
//			try {
//				Lucene_Search1.doit(file.getName(),set.getSetting(SettingManager.POSFilterDIR));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		for(File file:new File(set.getSetting(SettingManager.POSFilterDIR)).listFiles()){
		try {
			GoogleFilter.search_filter(file.getName());
			Stem.stemming(file.getName());
			NGD_calculate.NGD(file.getName());
		} catch (IOException e) {
			e.printStackTrace();
			}
		}
		
		
		
	}

}
