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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tw.edu.ncu.sia.util.ServerUtil;


public class Drift_exp3 {
	static String cat[] = {"acq"};
	static double betweeness_threshold = 0.5; //去掉多少連線
	
	static double core_threshold =1; //取多少當作核心
	
	static String baseDir;
	static String trainDir;
	static String testDir;
	

	/**
	 * 找出潛在相關文件能力評估
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long StartTime;
		long EndTime;
		double core_thresholds[]= {0.75};
		String cats[] = {"trade"};
				
		for(int t=1; t<=1; t++)
			for(String c: cats){
				cat[0] = c;
				
				for(int i=0; i<core_thresholds.length; i++){
					core_threshold = core_thresholds[i];
					
					//baseDirxx = "exp3_o_30test_2train";
					//baseDir = "exp3_o_30test_r3_sTFIDFbc";
					baseDir = "exp3_m0.25_30test_r3_fixo";
					//baseDir = "exp3_m0.40_30test_sTFIDFbc";
					//baseDir = "exp3_m0.40_30test_alsTFIDF_top300t_fixo_rt0.15";
					
					trainDir = baseDir + "/" + core_threshold+"_training/"+cat[0]+"_"+t;
					testDir = baseDir + "/" + core_threshold+"_testing/"+cat[0]+"_"+t;
					
					new File(baseDir).mkdirs();
					Map<String, Double> map_Data = new HashMap<String, Double>();
					HashSet<String> vertices = new HashSet<String>();
					HashMap<String, Integer> term_vertice_times = new HashMap<String, Integer>();
					Map<String, Double> ngds;
					
					File timetxt = new File(baseDir+"/time.txt");
					BufferedWriter timew = new BufferedWriter(new FileWriter(timetxt));
					BufferedWriter bw4_Tom = new BufferedWriter(new FileWriter(baseDir+"/weight.txt"));
					StartTime = System.currentTimeMillis();
					System.out.println("訓練、測試文件中 :");
					timew.write("訓練、測試文件中 :");
					timew.newLine();
					timew.flush();
					timew.write("開始時間 :" + StartTime);
					timew.newLine();
					timew.flush();
					//Go_Training3.generateTrainSet(5, trainDir, cat[0]);
					//Go_Training3.generateTestSet(30, testDir, cat);
					//Go_Training3.generateTestSet_multi(5, testDir, cat[0]);
					EndTime = System.currentTimeMillis();
					timew.write("結束時間 :" + EndTime);
					timew.newLine();
					timew.flush();
					timew.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
					timew.newLine();
					timew.flush();
							
					StartTime = System.currentTimeMillis();
					System.out.println("特徵萃取中 :");
					timew.write("特徵萃取中 :");
					timew.newLine();
					timew.flush();
					timew.write("開始時間 :" + StartTime);
					timew.newLine();
					timew.flush();
					File dir = new File(trainDir);
					for(File f: dir.listFiles()){
						ngds = Go_Training3.featureExtract(f);
						//term_vertice_times = Go_Training3.get_term_appear_times();
						for(Entry<String,Double> e: ngds.entrySet()){
							String pair = e.getKey();
							vertices.add(pair.split(",")[0]);
							vertices.add(pair.split(",")[1]);
						}
						//map_Data.putAll(ngds);	
					}
					EndTime = System.currentTimeMillis();
					timew.write("結束時間 :" + EndTime);
					timew.newLine();
					timew.flush();
					timew.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
					timew.newLine();
					timew.flush();
					
					try {
						ServerUtil.initialize();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					StartTime = System.currentTimeMillis();
					System.out.println("slor搜尋與NGD計算中 :");
					timew.write("slor搜尋與NGD計算中 :");
					timew.newLine();
					timew.flush();
					timew.write("開始時間 :" + StartTime);
					timew.newLine();
					timew.flush();
					for(String v1 : vertices){
						for(String v2:vertices){
							if(!v1.equals(v2) && !map_Data.containsKey(v1+","+v2) && !map_Data.containsKey(v2+","+v1) ){
							
								double x = ServerUtil.getHits("\""+v1+"\"");
								double y = ServerUtil.getHits("\""+v2+"\"");
							
								double m = ServerUtil.getHits("+\""+v1+"\" +\""+v2+"\"");
							
								double ngd = NGD_calculate.NGD_cal(x, y, m);
							
								map_Data.put(v1+","+v2, ngd);
							}
						}
					}
					EndTime = System.currentTimeMillis();
					timew.write("結束時間 :" + EndTime);
					timew.newLine();
					timew.flush();
					timew.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
					timew.newLine();
					timew.flush();
					
					List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(
							map_Data.entrySet());
					
					Collections.sort(list_Data,
							new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> o1,
										Map.Entry<String, Double> o2) {
									if(o1.getValue()>o2.getValue())
										return 1;
									else if(o1.getValue()<o2.getValue())
										return -1;
									else
										return 0;
								}
							});
					
					File d = new File(baseDir+"/concept.txt");
					
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(d));
						for(Entry<String, Double> e : list_Data){	
							bw.write(e.getKey()+","+e.getValue());
							bw.newLine();
						}
						bw.flush();
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					StartTime = System.currentTimeMillis();
					System.out.println("Betweennes計算中 :");
					timew.write("Betweennes計算中 :");
					timew.newLine();
					timew.flush();
					timew.write("開始時間 :" + StartTime);
					timew.newLine();
					timew.flush();
					TOM_betweennessCentrality bc = new TOM_betweennessCentrality();
					bc.betweeness_threshold = betweeness_threshold;
					bc.core_threshold = core_threshold;
					
					//bc.betweenness_cal(baseDir, "concept.txt", "center.txt", "concepts.txt", true);
					EndTime = System.currentTimeMillis();
					timew.write("結束時間 :" + EndTime);
					timew.newLine();
					timew.flush();
					timew.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
					timew.newLine();
					timew.flush();
					
					CompareRelateness cr = new CompareRelateness();
					
					try {
						StartTime = System.currentTimeMillis();
						System.out.println("文件與使用者模型相關性計算中 :");
						timew.write("文件與使用者模型相關性計算中 :");
						timew.newLine();
						timew.flush();
						timew.write("開始時間 :" + StartTime);
						timew.newLine();
						//cr.caculRank(trainDir, "center.txt", "all.txt" ,testDir, cat, 10, true);
						//String dog[] = {"trade","crude"};
						String dog[] = {"trade"};
						/*String line;
						BufferedReader br_center = new BufferedReader(new FileReader(baseDir+"/centers/center.txt"));
						line = br_center.readLine();
						while((line = br_center.readLine()) != null){
							term_vertice_times.put(line.split(",")[0], Integer.valueOf(line.split(",")[1]));
						}
						for(String v : term_vertice_times.keySet()){
							bw4_Tom.write(v+","+term_vertice_times.get(v)); 
							bw4_Tom.newLine();
							bw4_Tom.flush();
						}
						bw4_Tom.close();*/
						//cr.set_term_vertice_times(term_vertice_times); //如果要用權重模式記得打開
						cr.caculRetateness(baseDir, trainDir, "center.txt", "all.txt" ,testDir, dog, true);
						EndTime = System.currentTimeMillis();
						timew.write("結束時間 :" + EndTime);
						timew.newLine();
						timew.flush();
						timew.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
						timew.newLine();
						timew.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timew.close();
				}
			
			
		}
		
		
		
	}

}
