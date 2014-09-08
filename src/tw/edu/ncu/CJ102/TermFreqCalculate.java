package tw.edu.ncu.CJ102;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TermFreqCalculate {

	static HashMap<String,Integer> TF_term_times = new HashMap<String,Integer>();
	static HashMap<String,Integer> TF_term_times_other = new HashMap<String,Integer>();
	String readPath;
	String writePath;
	/**
	 * @param args
	 * @throws Exception 
	 */
	
	//should read keyword Freq 
	TermFreqCalculate(String input, String output){
		readPath = input;
		writePath = output;
	}
	
	public void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//TF_main("sTF_process/org_data/", "sTF_score/");
		//TF_main("sTF_process/add_data/", "sTF_scoretest/");
		//TF_main("citeulike/citeulike_Stem/", "citeulike/citeulike_sTF_score/");
	}
	
	public void start() throws IOException{
		System.out.print("sTF開始\n");
		boolean onlyterm;
		//new File("sTFIDF_process").mkdirs(); //輸出資料夾
		File dir = new File(readPath); //來源資料夾
		//各類別字詞統計
		One_Type_Term(dir,writePath, "no", false);
		TF_term_times.clear();
		System.out.print("End\n");
		
	}
	
	private void One_Type_Term(File source_dir, String resultDir, String thistype) throws IOException{
		One_Type_Term(source_dir,resultDir,thistype,true);
	}
	
	private void One_Type_Term(File source_dir, String resultDir, String thistype, boolean onlyterm) throws IOException{
		BufferedWriter ts_w1;
		BufferedReader keywordFreqFile, m_r2;
		String line,filename="";
		int term_times,temp_use;
		Pattern p = Pattern.compile("[(),\"\\?!:;=]"); //過濾字詞的一些雜質
		File[] fileslist;
		
		System.out.print("來源檔案"+source_dir.getName()+"\n");
		if(source_dir.isDirectory()){
			fileslist = source_dir.listFiles(); //資料夾模式
		}else{ //單檔模式
			fileslist = new File[1];
			fileslist[0] = source_dir;
		}
		
		for (File file : fileslist) {
			TF_term_times.clear();
			if (file.isDirectory()) {
				One_Type_Term(file, resultDir, thistype, onlyterm);
			} else if (file.isFile()) {
				filename = "";
				filename = file.getName();

				System.out.print("讀取檔案" + file.getName() + "\n");
				keywordFreqFile = new BufferedReader(new FileReader(readPath
						+ filename));
				m_r2 = new BufferedReader(new FileReader(file));
				while ((line = m_r2.readLine()) != null) {
					String keyword = line.split(",")[0];
					TF_term_times.put(keyword, 0); // 紀錄檔案出現的名詞
				}
				while ((line = keywordFreqFile.readLine()) != null) {
					String keyword = line.split(",")[0];
					int i = 3;
					if (TF_term_times.containsKey(keyword)) {
						term_times = Integer.valueOf(line.split(", ")[1]);
						while (i < line.length() && term_times == 0) {
							term_times = Integer.valueOf(line.split(", ")[i]);
							i++;
						}
						Matcher m = p.matcher(keyword);
						keyword = m.replaceAll("");
						TF_term_times.put(keyword, term_times);
					}
				}
				keywordFreqFile.close();
				// 依分數高低排序
				List<Map.Entry<String, Integer>> list_Data = new ArrayList<Map.Entry<String, Integer>>(
						TF_term_times.entrySet());
				Collections.sort(list_Data,
						new Comparator<Map.Entry<String, Integer>>() {
							public int compare(
									Map.Entry<String, Integer> entry1,
									Map.Entry<String, Integer> entry2) {
								Integer r = entry2.getValue()
										- entry1.getValue();
								if (r == 0) {
									return 0;
								} else if (r > 0) {
									return 1;
								} else {
									return -1;
								}
							}
						});
				new File(resultDir).mkdir();
				if (onlyterm) {
					ts_w1 = new BufferedWriter(new FileWriter(writePath
							+ filename));
					// System.out.print("產出"+resultDir+filename+"_Term_TFcalculate(onlyterm).txt"+"輸出中\n");
				} else {
					ts_w1 = new BufferedWriter(new FileWriter(writePath
							+ filename));
					// System.out.print("產出"+resultDir+filename+"_Term_TFcalculate.txt"+"輸出中\n");
				}

				if (onlyterm) {
					for (Map.Entry<String, Integer> entry : list_Data) {
						if (TF_term_times.get(entry.getKey()) != 0) {
							ts_w1.write(entry.getKey());
							ts_w1.newLine();
							ts_w1.flush();
						}
					}
				} else {
					// ts_w1.write("字,出現頻率");
					// ts_w1.newLine();
					// ts_w1.flush();
					for (Map.Entry<String, Integer> entry : list_Data) {
						if (TF_term_times.get(entry.getKey()) != 0) {
							ts_w1.write(entry.getKey() + ","
									+ TF_term_times.get(entry.getKey()));
							ts_w1.newLine();
							ts_w1.flush();
						}
					}
				}
				ts_w1.close();
				System.out.print("檔案" + file.getName() + "處理結束\n");
			}
		}
		
	}
}
