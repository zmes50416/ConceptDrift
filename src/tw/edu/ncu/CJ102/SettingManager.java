package tw.edu.ncu.CJ102;
import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
/*
 * 需要調整寫法，目前調用設定需要呼叫太長了
 * TODO 增加一個檢查路徑方法並且應該要可以替換實驗集
 * TODO 
 */
enum projectPath{
	stemDir
}
class SettingManager {
	/** 
	 * @author 102鼎文
	 *
	 * Setting & reading Property xml from here
	 * including FilePath...
	 * will generate xml file automaticly
	 * use getString & final String to get setting 
	 * BeCare If you add new content, you should rebuild xml or it will not recognize
	 * 
	 * 
	 */
	
	static final String DOCDIR = "DocumetnDirPath";
	static final String KFCDIR = "KeyWordFreqCountDirPath";
	static final String POSFilterDIR = "POSDirPath";
	static String EXPDIR = "ExperimentDirPath";
	static String IndexDir = "IndexedDirPath";
	static String PairDir = "PairDirPath";
	static String TermRankDir = "TermRankDirPath";
	static String NumOfTermDir = "NumberOfTermDirPath";
	static String stemmedDir = "StemmedWordDirPath";
	private static String projectName;
	//static String 
	private static SettingManager instance = new SettingManager();
	Properties settingProps;
	private SettingManager(){
		settingProps = new Properties();
			try {
				settingProps.loadFromXML(new FileInputStream("setting.xml"));
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("System can't find setting.xml, rebuilding it from default...");
				loadDefaultSetting();
			}
		
	}
	public static SettingManager getSettingManager(){
		return instance;
	}
	
	public static String getSetting(String key){
		if(instance.settingProps.getProperty(key)==null){
			System.err.println("Can't Find Setting:"+key);
		}
		return instance.settingProps.getProperty(key);
	}
	private void loadDefaultSetting(){
		settingProps.put(DOCDIR, "usedData/");
		settingProps.put(EXPDIR, "usedData/");
		settingProps.put(KFCDIR, "Util/Keyword_output_freq/");
		settingProps.put(POSFilterDIR, "Util/POS_filter/");
		settingProps.put(IndexDir, "Util/WikiSearch/");
		settingProps.put(PairDir, "Util/Pair/");
		settingProps.put(TermRankDir, "Util/TermRank/");
		settingProps.put(NumOfTermDir, "Util/numberOfTerm/");
		settingProps.put(stemmedDir, "Util/Stem/");
		try {
			settingProps.storeToXML(new FileOutputStream("setting.xml"), "XMLSetting, You can change setting from here");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createProject(){
		
	}
	public void loadProject(){
		
	}
	private void checkDirExsit(){
		
	}
	
	

}
