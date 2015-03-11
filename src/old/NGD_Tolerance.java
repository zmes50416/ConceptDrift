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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class NGD_Tolerance {

	static double tolerance_rate=0.4;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Tolerance_main("citeulike/citeulike_sTF_score/", "citeulike/citeulike_NGD_Tolerance_0.4/");
		//Tolerance_main("sTF_scoretest/", "source_dir/");
	}
	
	public static void Tolerance_main(String source_dir){
		String resultDir=source_dir;
		Tolerance_main(source_dir, resultDir);
	}
	
	public static void Tolerance_main(String source_dir, String resultDir){
		System.out.print("Tol_NGD開始\n");
		File dir = new File(source_dir); //來源資料夾
		Tolerance_work(dir,resultDir);
		System.out.print("End\n");
	}
	
	public static void Tolerance_work(File source_dir, String resultDir){
		Map<String, Double> ngds = null;
		File[] fileslist;
		String filename="";
		System.out.print("來源資料匣"+source_dir.getName()+"\n");
		if(source_dir.isDirectory()){
			fileslist = source_dir.listFiles(); //資料夾模式
		}else{ //單檔模式
			fileslist = new File[1];
			fileslist[0] = source_dir;
		}
		
		for (File files : fileslist){
			if(files.isDirectory()){
				Tolerance_work(files,resultDir);
			}else if(files.isFile()){
				try {
					filename="";
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
					
					File brfile = new File("citeulike/citeulike_Rank/"+filename+"_Rank.txt");
					File br2file = new File(source_dir+"/"+filename+"_Term_TFcalculate.txt");
					BufferedWriter bw;
					BufferedReader br = new BufferedReader(new FileReader(brfile));
					BufferedReader br2 = new BufferedReader(new FileReader(br2file));
					List<Tolerance_object> tolerance_list = new LinkedList<Tolerance_object>();
					List<Map.Entry<String, Double>> list_data = new ArrayList<Map.Entry<String, Double>>();
					HashMap<String,Integer> TF_term = new HashMap<String,Integer>();
					ngds = new HashMap<String, Double>();
					String line, v1, v2, bigTF, smallTF, word="";
					double temp_ngd;
					int small_than_tolerance_rate_num; //看小於ngd門檻值的資料有幾條
					
					System.out.print("讀取檔案"+br2file.getName()+"\n");
					//取出此檔案的所有字詞與權重
					while((line=br2.readLine())!=null){
						v1 = line.split(",")[0];
						TF_term.put(v1,Integer.valueOf(line.split(",")[1]));
					}
					
					System.out.print("讀取檔案"+brfile.getName()+"\n");
					
					small_than_tolerance_rate_num=0;
					//取出所有_Rank的資料
					while((line=br.readLine())!=null){
						temp_ngd = Double.parseDouble(line.split(",")[2]);
						if(temp_ngd<=tolerance_rate){
							small_than_tolerance_rate_num++;
						}
						//去除NDG分數大於1的結果
						if(temp_ngd<1){
							v1 = line.split(",")[0];
							v2 = line.split(",")[1];
							word = v1+","+v2;
							Map.Entry<String, Double> temp_entry = new AbstractMap.SimpleEntry<String, Double>(word, temp_ngd);
							list_data.add(temp_entry);
						}
					}

					//開始NGD容差步驟
					for(int i=1;i<=small_than_tolerance_rate_num;i++){
						Map.Entry<String, Double> entry = list_data.get(i-1);	
						temp_ngd = entry.getValue();
						word = entry.getKey();
						//System.out.print("讀取"+ word + "," + temp_ngd + "\n");
						v1 = word.split(",")[0];
						v2 = word.split(",")[1];
						//字詞必須是在TF文件中有紀錄的字詞
						if(TF_term.get(v1)!=null && TF_term.get(v2)!=null){
							//容差過後前small_than_tolerance_rate_num行內兩兩節點應該相同，若不相同則進行容差
							if(!v1.equals(v2)){
								//取TF值較高的字詞取代較低的
								if(TF_term.get(v1)>=TF_term.get(v2)){
									//System.out.print("新增取代規則"+v1+"將取代"+v2+"\n");
									bigTF = v1;
									smallTF = v2;
								}else{
									//System.out.print("新增取代規則"+v2+"將取代"+v1+"\n");
									bigTF = v2;
									smallTF = v1;
								}
								
								//System.out.println("bigTF="+bigTF+" ,smallTF="+smallTF);
								
								//bigTF開始對所有smallTF節點進行取代
								int temp_index=0;
								for(Map.Entry<String, Double> entry2 : list_data){
									if(entry2.getKey().split(",")[0].equals(smallTF)){
										Map.Entry<String, Double> temp_entry = new AbstractMap.SimpleEntry<String, Double>(bigTF+","+entry2.getKey().split(",")[1],entry2.getValue());
										list_data.set(temp_index, temp_entry);
										//System.out.println("編號"+temp_index+" 取代前 = "+ entry2.getKey() +","+ entry2.getValue());
										//System.out.println("編號"+temp_index+" 取代後 = "+ list_data.get(temp_index).getKey() +","+ list_data.get(temp_index).getValue());
										entry2=list_data.get(temp_index);
									}
									if(entry2.getKey().split(",")[1].equals(smallTF)){
										Map.Entry<String, Double> temp_entry = new AbstractMap.SimpleEntry<String, Double>(entry2.getKey().split(",")[0]+","+bigTF,entry2.getValue());
										list_data.set(temp_index, temp_entry);
										//System.out.println("編號"+temp_index+" 取代前 = "+ entry2.getKey() +","+ entry2.getValue());
										//System.out.println("編號"+temp_index+" 取代後 = "+ list_data.get(temp_index).getKey() +","+ list_data.get(temp_index).getValue());
									}
									temp_index++;
								}
							}
						}
					}
					br.close();
					
					bw = new BufferedWriter(new FileWriter(resultDir+filename+"_TolNGD.txt"));
					for(Map.Entry<String, Double> entry : list_data){
						bw.write(entry.getKey()+","+entry.getValue());
						bw.newLine();
						bw.flush();
					}
					bw.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

/*class StrLenComparator implements Comparator<String>{
    public int compare(String s1, String s2){
        if(s1.length() > s2.length())
            return 1;
        if(s1.length() < s2.length())
            return -1;
        return s1.compareTo(s2);
    }
}*/
