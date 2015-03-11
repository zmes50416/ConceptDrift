package tw.edu.ncu.CJ102;
import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JFileChooser;
/*
 * 需要調整寫法，目前調用設定需要呼叫太長了
 * TODO 增加一個檢查路徑方法並且應該要可以替換實驗集
 * TODO 
 */
public class SettingManager {
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
	static String IndexMultiTermDir = "MultiTermIndexResultDirPath";
	static String PairDir = "PairDirPath";
	static String TermRankDir = "TermRankDirPath";
	static String NumOfTermDir = "NumberOfTermDirPath";
	static String stemmedDir = "StemmedWordDirPath";
	static String NGDCalcDir = "NGDCalculatedDirPath";
	static String NGDRankDir = "NGDRankDirPath";
	static String NGDToleranceDir = "NGDToleranceDirPath";
	static String TFDir = "TermFreqDirPath";
	static String conceptDir = "ConceptDirPath";
	static String ServerURL = "SolrURL";
	private String projectName; 
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
			System.err.println("Can't Find Setting:"+key+"\n System ShoutDown");
			System.exit(1);
		}
		return instance.settingProps.getProperty(key);
	}
	private void loadDefaultSetting(){
		settingProps.put(DOCDIR, "usedData/");
		settingProps.put(EXPDIR, "usedData/");
		settingProps.put(KFCDIR, "Util/Keyword_output_freq/");
		settingProps.put(POSFilterDIR, "Util/POS_filter/");
		settingProps.put(IndexDir, "Util/WikiSearch/");
		settingProps.put(IndexMultiTermDir, "Util/WikiSearchMultiTerm/");
		settingProps.put(PairDir, "Util/Pair/");
		settingProps.put(TermRankDir, "Util/TermRank/");
		settingProps.put(NumOfTermDir, "Util/numberOfTerm/");
		settingProps.put(stemmedDir, "Util/Stem/");
		settingProps.put(NGDCalcDir, "Util/NGDCalc/");
		settingProps.put(NGDRankDir, "Util/NGDRank/");
		settingProps.put(TFDir, "Util/TF/");
		settingProps.put(NGDToleranceDir, "Util/NGDTol/");
		settingProps.put(conceptDir,"Util/concept/");
		settingProps.put(ServerURL, "http://140.115.82.105/searchweb");
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
	public void loadProject(String projectName){
		this.projectName = projectName;
	}
	public void prepare(){
		
	}
	
	public static String chooseProject(){
		JFileChooser projectChooser = new JFileChooser(new File("."));
		projectChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(!(projectChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)){
			throw new Error();
		}else{
			return projectChooser.getSelectedFile().getAbsolutePath();
		}
		
	}

}
