package tw.edu.ncu.CJ102;
import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
/* Setting & reading Property xml from here
 * including FilePath...
 * will generate xml file automaticly
 * BeCare If you add new content, you should rebuild xml or it will not recognize
 */
class SettingManager {
	static final String DOCDIR = "DocumetnDirPath";
	static final String QTAGDIR = "QTagDirPath";
	static final String POSDIR = "POSDirPath";
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
	
	public String getSetting(String key){
		return settingProps.getProperty(key);
	}
	private void loadDefaultSetting(){
		settingProps.put(DOCDIR, "usedData/");
		settingProps.put(QTAGDIR, "Keyword_output_freq/");
		settingProps.put(POSDIR, "POS_filter/");
		
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

}
