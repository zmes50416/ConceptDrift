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
				Qtag.tagging(f.getName().split("\\.")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		 
		Term_Freq_and_POS_filter.counting(new File(Qtag.writeFilePath));
	
		for(File file:new File(set.getSetting(SettingManager.POSFilterDIR)).listFiles()){
			try {
				new Lucene_Search1().doit(file.getName(),set.getSetting(SettingManager.POSFilterDIR));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for(File file:new File(set.getSetting(SettingManager.POSFilterDIR)).listFiles()){
		try {
			GoogleFilter.search_filter(file.getName());
			Stem.stemming(file.getName());
			NGD_calculate.NGD(file.getName());
			NGDResult_Rank.ranking(file.getName());
		} catch (IOException e) {
			e.printStackTrace();
			}
		}
		try {
			new TermFreqCalculate(SettingManager.getSetting(SettingManager.KFCDIR),SettingManager.getSetting(SettingManager.TFDir)).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new NGD_Tolerance(SettingManager.getSetting(SettingManager.NGDRankDir),SettingManager.getSetting(SettingManager.TFDir), SettingManager.getSetting(SettingManager.NGDToleranceDir),0.4).start();
		new BCCalculator(SettingManager.getSetting(SettingManager.NGDRankDir),SettingManager.getSetting(SettingManager.TFDir),SettingManager.getSetting(SettingManager.conceptDir)).start();
		
		
		
	}

}
