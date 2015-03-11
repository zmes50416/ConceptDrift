import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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


public class Drift_exp2_2 {
	static String cat[] = {"acq"};
	static double betweeness_threshold = 0.35; //去掉多少連線
	
	static double core_threshold = 0.75; //取多少當作核心
	
	static String trainDir;
	static String testDir;
	

	/**
	 * core_threshold的訂定
	 */
	public static void main(String[] args) {
		
		double core_thresholds[]= {0.75};
		//String cats[] = {"acq", "cocoa", "coffee", "crude","earn", "sugar", "trade"};
		String cats[] = {"trade"};
		
		for(int t=5; t<=5; t++)
			for(String c: cats){
				cat[0] = c;
				
				for(int i=0; i<core_thresholds.length; i++){
					core_threshold = core_thresholds[i];
					
					trainDir = "exp2-2/"+core_threshold+"_training/"+cat[0]+"_"+t;
					testDir = "exp2-2/"+core_threshold+"_testing/"+cat[0]+"_"+t;
					
					
					Map<String, Double> map_Data = new HashMap<String, Double>();
					HashSet<String> vertices = new HashSet<String>();
					Map<String, Double> ngds;
					
					Go_Training3.generateTrainSet(5, trainDir, cat[0]);
					Go_Training3.generateTestSet(10, testDir, cat);
					
					File dir = new File(trainDir);
					
					for(File f: dir.listFiles()){
						ngds = Go_Training3.featureExtract(f);
						for(Entry<String,Double> e: ngds.entrySet()){
							String pair = e.getKey();
							vertices.add(pair.split(",")[0]);
							vertices.add(pair.split(",")[1]);
						}
						map_Data.putAll(ngds);	
					}
					
					
					try {
						ServerUtil.initialize(); //初始化索引伺服器slor
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
					for(String v1 : vertices){
						for(String v2:vertices){
							if(!v1.equals(v2) && !map_Data.containsKey(v1+","+v2) && !map_Data.containsKey(v2+","+v1) ){
							
								double x = ServerUtil.getHits("\""+v1+"\""); //取得包含頂點1字詞的文件數量
								double y = ServerUtil.getHits("\""+v2+"\""); //取得包含頂點2字詞的文件數量
							
								double m = ServerUtil.getHits("+\""+v1+"\" +\""+v2+"\""); //取得包含頂點1和2字詞的文件數量
							
								double ngd = NGD_calculate.NGD_cal(x, y, m); //計算ngd分數
							
								map_Data.put(v1+","+v2, ngd);
							}
						}
					}
					
					List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(
							map_Data.entrySet());
					
					Collections.sort(list_Data,	new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
									if(o1.getValue()>o2.getValue())
										return 1;
									else if(o1.getValue()<o2.getValue())
										return -1;
									else
										return 0;
								}
							});
					
					File d = new File(trainDir+"/concept.txt");
					
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
					
					betweennessCentralityt bc = new betweennessCentralityt();
					bc.betweeness_threshold = betweeness_threshold;
					bc.core_threshold = core_threshold;
					
					bc.betweenness_cal(trainDir, "concept.txt", "center.txt", "concepts.txt", true);
					
					CompareRelateness cr = new CompareRelateness();
					
					try {
						//cr.caculRank(trainDir, "center.txt", "all.txt" ,testDir, cat, 10, true);
						cr.relateness_threshold = 0.525;
						cr.caculRetateness(trainDir, "center.txt", "all.txt", testDir, cat, true);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			
		}
		
		
		
	}

}
