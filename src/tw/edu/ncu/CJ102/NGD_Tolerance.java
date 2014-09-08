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

	double toleranceRate=0.4;//Default 0.4?
	String readNGDPath;
	String readTFPath;
	String writeTolPath;
	/**
	 * @param args
	 * @throws IOException 
	 */
	//Should read TFDir and write ToleranceDir
	public NGD_Tolerance(String NGDDirPath, String TFDirPath,String TolPath,double toleranceRate){
		readNGDPath = NGDDirPath;
		readTFPath = TFDirPath;
		writeTolPath = TolPath;
		this.toleranceRate = toleranceRate;
	}
	
	public void start(){
		System.out.print("Tol_NGD開始\n");
		File dir = new File(readNGDPath); //來源資料夾
		Tolerance_work(dir,writeTolPath);
		System.out.print("End\n");
	}
	
	public void Tolerance_work(File source_dir, String resultDir){
		Map<String, Double> ngds = null;
		File[] fileslist;
		String fileName="";
		//System.out.print("來源資料匣"+source_dir.getName()+"\n");
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
					fileName = files.getName();
					
					BufferedWriter bw;
					BufferedReader br = new BufferedReader(new FileReader(new File(readNGDPath+fileName)));
					BufferedReader br2 = new BufferedReader(new FileReader(new File(readTFPath+fileName)));
					List<Map.Entry<String, Double>> list_data = new ArrayList<Map.Entry<String, Double>>();
					HashMap<String,Integer> termFreqList = new HashMap<String,Integer>();
					ngds = new HashMap<String, Double>();
					String line,bigTF, smallTF;
					double temp_ngd;
					int belowTolreanceRate=0; //小於NGD門檻值的資料
					
					//System.out.print("讀取檔案"+fileName+"\n");
					//取出此檔案的所有字詞與權重
					while((line=br2.readLine())!=null){
						String word = line.split(",")[0];
						termFreqList.put(word,Integer.valueOf(line.split(",")[1]));
					}
					
					//System.out.print("讀取檔案"+fileName+"\n");
					//取出所有_Rank的資料
					while((line=br.readLine())!=null){
						temp_ngd = Double.parseDouble(line.split(",")[2]);
						if(temp_ngd<=toleranceRate){
							belowTolreanceRate++;
						}
						//去除NDG分數大於1的結果
						if(temp_ngd<1){
							String word = line.split(",")[0];
							String anotherWord = line.split(",")[1];
							String words = word+","+anotherWord;
							list_data.add(new AbstractMap.SimpleEntry<String, Double>(words, temp_ngd));
						}
					}

					//開始NGD容差步驟
					for(int i=1;i<=belowTolreanceRate;i++){//因為NGD已經排序了所以取前i個就一定會取到對的,雖然邏輯可以通但寫法應可改為以參照的物件去比對更符合邏輯 
						Map.Entry<String, Double> entry = list_data.get(i-1);	
						temp_ngd = entry.getValue();
						String words = entry.getKey();
						//System.out.print("讀取"+ word + "," + temp_ngd + "\n");
						String word = words.split(",")[0];
						String anotherWord = words.split(",")[1];
						//字詞必須是在TF文件中有紀錄的字詞
						if(termFreqList.get(word)!=null && termFreqList.get(anotherWord)!=null){
							//容差過後前belowtoleranceRate行內兩兩節點應該相同，若不相同則進行容差
							if(!word.equals(anotherWord)){
								//取TF值較高的字詞取代較低的
								if(termFreqList.get(word)>=termFreqList.get(anotherWord)){
									//System.out.print("新增取代規則"+v1+"將取代"+v2+"\n");
									bigTF = word;
									smallTF = anotherWord;
								}else{
									//System.out.print("新增取代規則"+v2+"將取代"+v1+"\n");
									bigTF = anotherWord;
									smallTF = word;
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
					
					bw = new BufferedWriter(new FileWriter(writeTolPath+fileName));
					for(Map.Entry<String, Double> entry : list_data){
						bw.write(entry.getKey()+","+entry.getValue());
						bw.newLine();
						bw.flush();
					}
					bw.close();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//Not used?
	class Tolerance_object {

			/**
			 * @param args
			 */
			
			String v1=""; //取代者
			String v2=""; //被取代者
			double ngd=0;

			
			public void addv1(String addv1){
				v1=addv1;
			}
			
			public void addv2(String addv2){
				v2=addv2;
			}
			
			public void addngd(double addngd){
				ngd=addngd;
			}
			
			public void add(String addv1, String addv2, double addngd){
				addv1(addv1);
				addv2(addv2);
				addngd(addngd);
			}
			
			public String getv1(){
				return v1;
			}
			
			public String getv2(){
				return v2;
			}
			
			public double getngd(){
				return ngd;
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
