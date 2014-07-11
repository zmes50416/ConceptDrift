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


public class TFIDF_calculate_backup20131222 {

	static HashMap<String,Integer> TF_term_times = new HashMap<String,Integer>();
	static HashMap<String,Integer> TF_term_times_other = new HashMap<String,Integer>();
	static HashMap<String,Integer> TF_term_docs = new HashMap<String,Integer>();
	static HashMap<String,Double> TF_term_score = new HashMap<String,Double>();
	static HashMap<String,Double> TF_term_score_other = new HashMap<String,Double>();
	static HashMap<String,Double> TF_term_score_fin = new HashMap<String,Double>();
	static int type_doc_cum, this_doc_term;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.print("sTFIDF開始\n");
		String filename="";
		type_doc_cum=0;
		this_doc_term=0;
		boolean firsttime;
		boolean onlyterm;
		//===整群處理區塊===
		//new File("sTFIDF_process").mkdirs(); //輸出資料夾
		File dir = new File("sTFIDF_process/org_data/"); //來源資料夾
		String types[] = {"acq","cocoa","coffee","crude","earn","sugar","trade"};
		//String types[] = {"trade"};
		//各類別字詞統計
		for(String s : types){
			System.out.print(s+"統計\n");
			One_Type_Term(dir,"sTFIDF_score/", s, false);
			TF_term_times.clear();
			TF_term_docs.clear();
			TF_term_times_other.clear();
			TF_term_score.clear();
			type_doc_cum=0;
			this_doc_term=0;
		}
		//個類別sTFIDF分數計算
		/*firsttime = true;
		onlyterm = true;
		for(String s : types){
			System.out.print(s+"-sTFIDF計算\n");
			Many_Type_TFIDF("sTFIDF_process/Term_calculate/","sTFIDF_process/sTFIDF_calculate/",s,types,firsttime,onlyterm);
			onlyterm = false;
			Many_Type_TFIDF("sTFIDF_process/Term_calculate/","sTFIDF_process/sTFIDF_calculate/",s,types,firsttime,onlyterm);
			onlyterm = true;
			firsttime = false;
			TF_term_score.clear();
			TF_term_score_fin.clear();
		}*/
		
		//filename = "trade_TFIDF_calculate";
		//限制字詞數
		//File f = new File("sTFIDF_process/sTFIDF_calculate/"+filename+".txt");
		//Limit_term_number(f,"sTFIDF_process/Term_Limit/",0.1,"score");
		//filename = filename+"_limitscore0.1";
		toRank("sTFIDF_process/Term_Limit/",filename);
		//===整群處理區塊===
		//===測試文件處理區塊===
		//File dir2;
		//onlyterm = true;
		//for(String s : types){
			//dir2 = new File(dir+"/"+s);
		//File files = new File("Tom_test/");
		//for (File files : dir2.listFiles()){
			//filename = files.getName().split("_")[0]+"_"+files.getName().split("_")[1];
			//One_Type_Term(files,"Tom_test_temp/","no",onlyterm);
			/*TF_term_times.clear();
			TF_term_docs.clear();
			TF_term_times_other.clear();
			TF_term_score.clear();
			filename="sugar_0002437";
			filename=filename+"_Term_calculate";*/
		//filename = "a_Term_calculate(onlyterm)";
			//toRank("t/",filename);
		//}
		//}
		//===測試文件處理區塊===
		//===限制測試文件最大概念字詞數處理區塊===
		/*File dir3 = new File("Tom_test_temp");
		for (File files : dir3.listFiles()){
			filename = files.getName().split("_")[0]+"_"+files.getName().split("_")[1];
			copyfile(new File("Rank/"+filename+"_Term_calculate_Rank.txt"),new File("exp3_o_30test_r3_sTFIDFbc/0.75_testing/trade_1/"+filename+"_Rank.txt"));
		}*/
		//===限制測試文件最大概念字詞數處理區塊===
		System.out.print("End\n");
	}
	
	public static void One_Type_Term(File source_dir, String resultDir, String thistype) throws IOException{
		One_Type_Term(source_dir,resultDir,thistype,true);
	}
	
	public static void One_Type_Term(File source_dir, String resultDir, String thistype, boolean onlyterm) throws IOException{
		BufferedWriter ts_w1;
		BufferedReader m_r1;
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
			filename=source_dir.getName().split("_")[0]+"_"+source_dir.getName().split("_")[1];
		}
		
		for (File files : fileslist){
			TF_term_times_other.clear();
			if(files.isDirectory()){
				One_Type_Term(files,resultDir,thistype,onlyterm);
			}else if(files.isFile()){
				if(files.getName().split("_")[0].equals(thistype)||thistype.equals("no")){
					System.out.print("讀取檔案"+files.getName()+"\n");
					type_doc_cum++;
					m_r1 = new BufferedReader(new FileReader(files));
					while((line=m_r1.readLine())!=null){
						this_doc_term++;
						v1 = line.split(", ")[0];
						term_times = Integer.valueOf(line.split(", ")[2]);
				        Matcher m = p.matcher(v1);
				        v1 = m.replaceAll("");
				        TF_term_times_other.put(v1, term_times);
					}
					for(String s : TF_term_times_other.keySet()){
						if(TF_term_times.get(s)==null){
							TF_term_times.put(s, TF_term_times_other.get(s));
						}else{
							TF_term_times.put(s, TF_term_times.get(s)+TF_term_times_other.get(s));
						}
						if(TF_term_docs.get(s)==null){
							TF_term_docs.put(s, 1);
						}else{
							TF_term_docs.put(s, TF_term_docs.get(s)+1);
						}
					}
					m_r1.close();
				}
			}
			for(String s : TF_term_times.keySet()){
				if(TF_term_score.get(s)!=null){
					//TF_term_score.put(s,TF_term_score.get(s)+Double.valueOf(TF_term_times.get(s))/type_doc_cum);
					TF_term_score.put(s,TF_term_score.get(s)+Double.valueOf(TF_term_times.get(s))/this_doc_term);
				}else{
					//類別中平均一個文件可以得到幾個字(類別分數，平衡類別文件多寡)
					//TF_term_score.put(s,Double.valueOf(TF_term_times.get(s))/type_doc_cum);
					//此文件中平均一個字可以得到幾個字(文件自身分數，平衡文件長度)
					TF_term_score.put(s,Double.valueOf(TF_term_times.get(s))/this_doc_term);
				}
			}
		}
		//依分數高低排序
		List<Map.Entry<String, Double>> list_Data = 
				new ArrayList<Map.Entry<String, Double>>(TF_term_score.entrySet());
		Collections.sort(list_Data, new Comparator<Map.Entry<String, Double>>(){
			public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2){
				Double r = entry2.getValue()-entry1.getValue();
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
		if(thistype.equals("no")){
			if(onlyterm){
				ts_w1 = new BufferedWriter(new FileWriter(resultDir+filename+"_Term_calculate(onlyterm).txt"));
			}else{
				ts_w1 = new BufferedWriter(new FileWriter(resultDir+filename+"_Term_calculate(withscore).txt"));
			}
			System.out.print("單檔模式");
		}else{
			if(onlyterm){
				ts_w1 = new BufferedWriter(new FileWriter(resultDir+thistype+"_Term_calculate(onlyterm).txt"));
			}else{
				ts_w1 = new BufferedWriter(new FileWriter(resultDir+thistype+"_Term_calculate(withscore).txt"));
			}
			System.out.print("資料夾模式");
		}
		System.out.print("輸出中\n");
		if(onlyterm){
			for (Map.Entry<String, Double> entry : list_Data){
				ts_w1.write(entry.getKey());
				ts_w1.newLine();
				ts_w1.flush();
			}
		}else{
			ts_w1.write(thistype+"類別總數為,"+type_doc_cum); 
			ts_w1.newLine();
			ts_w1.write("字,出現頻率,出現平均分攤,此類別中共同特徵分數"); 
			ts_w1.newLine();
			ts_w1.flush();
			for (Map.Entry<String, Double> entry : list_Data){
				ts_w1.write(entry.getKey()+","+TF_term_times.get(entry.getKey())+","+TF_term_docs.get(entry.getKey())+","+TF_term_score.get(entry.getKey())); 
				ts_w1.newLine();
				ts_w1.flush();
			}
		}
		ts_w1.close();
	}
	
	public static void Many_Type_TFIDF(String source_dir, String resultDir, String thistype, String[] types, boolean firsttime, boolean onlyterm) throws IOException{
		BufferedWriter ts_w1;
		BufferedReader m_r1;
		String line,v1;
		int this_type_doc_cum,other_type_doc_cum;
		double term_score,Local_score_sum=0;
		HashMap<String,Double> Local_score = new HashMap<String,Double>();
		
		m_r1 = new BufferedReader(new FileReader(source_dir+thistype+"_Term_calculate.txt"));
		line=m_r1.readLine();
		this_type_doc_cum = Integer.valueOf(line.split(",")[1]);
		line=m_r1.readLine();
		while((line=m_r1.readLine())!=null){
			//除上類別文件數是為了平衡類別文件數量的差異
			TF_term_score.put(line.split(",")[0], Double.valueOf(line.split(",")[3])/this_type_doc_cum);
			Local_score.put(line.split(",")[0], Double.valueOf(line.split(",")[3]));
			Local_score_sum = Local_score_sum + Double.valueOf(line.split(",")[3]);
		}
		m_r1.close();
		if(firsttime){ //只做一次統計(第一次)
			for(String one_type : types){
				other_type_doc_cum=0;
				m_r1 = new BufferedReader(new FileReader(source_dir+one_type+"_Term_calculate.txt"));
				line=m_r1.readLine();
				other_type_doc_cum = Integer.valueOf(line.split(",")[1]);
				line=m_r1.readLine();
				while((line=m_r1.readLine())!=null){
					v1 = line.split(",")[0];
					term_score = Double.valueOf(line.split(",")[3]);
					if(TF_term_score_other.get(v1)==null){
						//除上類別文件數是為了平衡類別文件數量的差異
						TF_term_score_other.put(v1, term_score/other_type_doc_cum);
					}else{
						TF_term_score_other.put(v1, TF_term_score_other.get(v1)+(term_score/other_type_doc_cum));
					}
				}
				m_r1.close();
			}
		}
		for(String s : TF_term_score.keySet()){
			/*if(TF_term_score_other.get(s)==null){
				TF_term_score_other.put(s, TF_term_score.get(s));
			}*/
			//50%全球性分數+50%區域性分數
			TF_term_score_fin.put(s, 0.5*(TF_term_score.get(s)/TF_term_score_other.get(s))+0.5*(Local_score.get(s)/Local_score_sum));
		}
		//依分數高低排序
		List<Map.Entry<String, Double>> list_Data = 
				new ArrayList<Map.Entry<String, Double>>(TF_term_score_fin.entrySet());
		Collections.sort(list_Data, new Comparator<Map.Entry<String, Double>>(){
			public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2){
				Double r = entry2.getValue()-entry1.getValue();
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
		
		if(!onlyterm){
			ts_w1 = new BufferedWriter(new FileWriter(resultDir+thistype+"_TFIDF_calculate.txt"));
			ts_w1.write("字,該類別平均分數,其餘類別累積分數,該類別佔分比例"); 
			ts_w1.newLine();
			ts_w1.flush();
		}else{
			ts_w1 = new BufferedWriter(new FileWriter(resultDir+thistype+"_TFIDF_calculate(only term).txt"));
		}
		for (Map.Entry<String, Double> entry : list_Data){
			if(!onlyterm){
				ts_w1.write(entry.getKey()+","+TF_term_score.get(entry.getKey())+","+TF_term_score_other.get(entry.getKey())+","+TF_term_score_fin.get(entry.getKey()));
			}else{
				ts_w1.write(entry.getKey());
			}
			ts_w1.newLine();
			ts_w1.flush();
		}
		ts_w1.close();
	}
	
	public static void Limit_term_number(File source_dir, String resultDir, double limit, String mothed) throws IOException{
		BufferedWriter ts_w1;
		BufferedReader m_r1;
		String line,v1;
		new File(resultDir).mkdirs(); //輸出資料夾
		m_r1 = new BufferedReader(new FileReader(source_dir));
		ts_w1 = new BufferedWriter(new FileWriter(resultDir+source_dir.getName().split("\\.")[0]+"_limit"+mothed+limit+".txt"));
		line=m_r1.readLine();
		if(mothed.equals("number")){
			for(int i=0;i<limit;i++){
				line=m_r1.readLine();
				ts_w1.write(line);
				ts_w1.newLine();
				ts_w1.flush();
			}
		}else if(mothed.equals("score")){
			while((line=m_r1.readLine())!=null){
				System.out.print(line+"\n");
				if(Double.valueOf(line.split(",")[3])>=limit){
					ts_w1.write(line.split(",")[0]);
					ts_w1.newLine();
					ts_w1.flush();
				}else{
					break;
				}
			}
		}
		ts_w1.close();
		m_r1.close();
	}
	
	public static void tofilter(String src, String filename) throws IOException{
		//詞性標記，記得把onlyterm模式的sTFIDF分數檔案複製到preprocess資料夾
		System.out.print(filename+"詞性標記開始"+"\n");
		copyfile(new File(src+filename+".txt") , new File("preprocess"+"/"+filename+".txt"));
		Qtag.tagging(filename);
		//詞性過濾、搜尋結果數過濾過程，記得把onlyterm模式的Qtag檔案複製到keyword_output_freq資料夾，並且將程式改成只有單字
		copyfile(new File("Qtag/"+filename+"_qtag.txt") , new File("keyword_output_freq"+"/"+filename+"_keyword_output_freq.txt"));
		System.out.print(filename+"詞性過濾"+"\n");
		POS_filter.filter(filename);
		System.out.print(filename+"搜尋結果數開始"+"\n");
		new Lucene_Search1().doit(filename);
		System.out.print(filename+"搜尋結果數過濾開始"+"\n");
		google_filter1.search_filter(filename);
		new Lucene_Search2().doit(filename);
		google_filter2.search_filter(filename);
		Stem.stemming(filename);
	}
	
	public static void toRank(String src, String filename) throws IOException{
		tofilter(src,filename);
		System.out.print(filename+"NGD運算開始"+"\n");
		NGD_calculate.NGD(filename);
		Result_Rank.ranking(filename);
	}
	
	public static void copyfile(File srFile, File dtFile) {
		try {
			File f1 = srFile;
			File f2 = dtFile;
			InputStream in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void copyfile2(File srFile, File dtFile) {
		try {
			File f1 = srFile;
			File f2 = dtFile;
			BufferedWriter ts_w1;
			BufferedReader m_r1;
			m_r1 = new BufferedReader(new FileReader(f1));
			ts_w1 = new BufferedWriter(new FileWriter(f2));
			int times=0;
			String line="";
			while((line=m_r1.readLine())!=null&&times<15){
				times++;
				ts_w1.write(line+",1");
				ts_w1.newLine();
				ts_w1.flush();
			}
			ts_w1.close();
			m_r1.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
