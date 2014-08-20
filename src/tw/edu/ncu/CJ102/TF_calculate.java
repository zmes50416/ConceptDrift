package tw.edu.ncu.CJ102;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TF_calculate {

	static HashMap<String,Integer> TF_term_times = new HashMap<String,Integer>();
	static HashMap<String,Integer> TF_term_times_other = new HashMap<String,Integer>();
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//TF_main("sTF_process/org_data/", "sTF_score/");
		//TF_main("sTF_process/add_data/", "sTF_scoretest/");
		TF_main("citeulike/citeulike_Stem/", "citeulike/citeulike_sTF_score/");
	}
	
	public static void TF_main(String source_dir) throws IOException{
		String resultDir=source_dir;
		TF_main(source_dir, resultDir);
	}
	
	public static void TF_main(String source_dir, String resultDir) throws IOException{
		System.out.print("sTF開始\n");
		boolean onlyterm;
		//new File("sTFIDF_process").mkdirs(); //輸出資料夾
		File dir = new File(source_dir); //來源資料夾
		//各類別字詞統計
		One_Type_Term(dir,resultDir, "no", false);
		TF_term_times.clear();
		TF_term_times_other.clear();
		System.out.print("End\n");
		
	}
	
	public static void One_Type_Term(File source_dir, String resultDir, String thistype) throws IOException{
		One_Type_Term(source_dir,resultDir,thistype,true);
	}
	
	public static void One_Type_Term(File source_dir, String resultDir, String thistype, boolean onlyterm) throws IOException{
		BufferedWriter ts_w1;
		BufferedReader m_r1, m_r2;
		String line,v1,filename="";
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
		
		for (File files : fileslist){
			TF_term_times.clear();
			if(files.isDirectory()){
				One_Type_Term(files,resultDir,thistype,onlyterm);
			}else if(files.isFile()){
				filename="";
				if(files.getName().split("_")[0].equals(thistype)||thistype.equals("no")){
					/*//reuters資料集的檔名萃取方法
					for(int i=0; i<files.getName().split("_").length;i++){
						//System.out.println("filename = "+ filename);
						if(i==0){
							filename=files.getName().split("_")[0];
						}else{
							char[] filename_temp = files.getName().split("_")[i].toCharArray();
							if(!Character.isDigit(filename_temp[0])){ //如果第一個字元是數字代表到檔名結尾了
								filename=filename+"_"+files.getName().split("_")[i];
							}else{
								filename=filename+"_"+files.getName().split("_")[i];
								break;
							}
						}
					}*/
					
					//citeulike資料集的檔名萃取方法
					filename = files.getName().split("_")[0];
					
					System.out.print("讀取檔案"+files.getName()+"\n");
					m_r1 = new BufferedReader(new FileReader("citeulike/citeulike_Keyword_output_freq/"+filename+"_keyword_output_freq.txt"));
					m_r2 = new BufferedReader(new FileReader(files));
					while((line=m_r2.readLine())!=null){
						v1 = line.split(",")[0];
						TF_term_times.put(v1,0);
						//System.out.print("放入字詞"+v1+"\n");
					}
					while((line=m_r1.readLine())!=null){
						v1 = line.split(",")[0];
						//System.out.print("尋找"+v1+"出現次數\n");
						int i = 3;
						if(TF_term_times.containsKey(v1)){
							term_times = Integer.valueOf(line.split(", ")[1]);
							//System.out.print(v1+"的值="+term_times+"\n");
							while(i<line.length() && term_times==0){
								term_times = Integer.valueOf(line.split(", ")[i]);
								i++;
							}
							Matcher m = p.matcher(v1);
							v1 = m.replaceAll("");
							TF_term_times.put(v1, term_times);
						}
					}
					m_r1.close();
					//依分數高低排序
					List<Map.Entry<String, Integer>> list_Data = 
							new ArrayList<Map.Entry<String, Integer>>(TF_term_times.entrySet());
					Collections.sort(list_Data, new Comparator<Map.Entry<String, Integer>>(){
						public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2){
							Integer r = entry2.getValue()-entry1.getValue();
							if(r==0){
								return 0;
							}else if(r>0){
								return 1;
							}else{
								return -1;
							}
					    }
					});
					new File(resultDir).mkdir();
					if(onlyterm){
						ts_w1 = new BufferedWriter(new FileWriter(resultDir+filename+"_Term_TFcalculate(onlyterm).txt"));
						System.out.print("產出"+resultDir+filename+"_Term_TFcalculate(onlyterm).txt"+"輸出中\n");
					}else{
						ts_w1 = new BufferedWriter(new FileWriter(resultDir+filename+"_Term_TFcalculate.txt"));
						System.out.print("產出"+resultDir+filename+"_Term_TFcalculate.txt"+"輸出中\n");
					}
					
					if(onlyterm){
						for (Map.Entry<String, Integer> entry : list_Data){
							if(TF_term_times.get(entry.getKey())!=0){
								ts_w1.write(entry.getKey());
								ts_w1.newLine();
								ts_w1.flush();
							}
						}
					}else{
						//ts_w1.write("字,出現頻率"); 
						//ts_w1.newLine();
						//ts_w1.flush();
						for (Map.Entry<String, Integer> entry : list_Data){
							if(TF_term_times.get(entry.getKey())!=0){
								ts_w1.write(entry.getKey()+","+TF_term_times.get(entry.getKey())); 
								ts_w1.newLine();
								ts_w1.flush();
							}
						}
					}
					ts_w1.close();
					System.out.print("檔案"+files.getName()+"處理結束\n");
				}
			}
		}
		
	}
}
