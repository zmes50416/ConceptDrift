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


public class Drift_exp1_3 {
	
	static String maindir = "reuters/";
	static double betweeness_threshold = 0.7;
	static int size = 2;
	static int topic_count = 3;
	
	static int times = 5;

	/**
	 * 多主題、複數文件與betweeness_threshold敏感度分析
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double thresholds[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		BufferedWriter bw2 = null;
		for(int i=0; i<thresholds.length; i++){
			betweeness_threshold = thresholds[i];
			
			double avg = 0;
			double sum = 0;
			
			try {
				new File("bc_exp/1-3/").mkdirs();
				bw2 = new BufferedWriter(new FileWriter("bc_exp/1-3/"+size+"_"+topic_count+"_"+betweeness_threshold+"_result.txt"));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			for(int t = 0; t<times; t++){

				BufferedWriter bw = null;
		
				Map<String, Double> ngds;
				Map<String, Double> allNgds = new HashMap<String, Double>();
				
				HashSet<String> vertices = new HashSet<String>();		
			
				File testdir = new File("bc_exp/1-3/"+size+"_"+topic_count+"_"+betweeness_threshold+"_"+t);
				testdir.mkdirs();
			
				Go_Training3.generateTrainSet(maindir,size,topic_count, testdir.getPath());
			
			
				//讀取測試資料夾每個文件的文件特徵
				for(File f :testdir.listFiles()){
					ngds = Go_Training3.featureExtract(f);
					for(Entry<String,Double> e: ngds.entrySet()){
						String pair = e.getKey();
						vertices.add(pair.split(",")[0]);
						vertices.add(pair.split(",")[1]);
					}
					
					allNgds.putAll(ngds);
				}//end of for
			
				try {
					ServerUtil.initialize();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
				for(String v1 : vertices){
					for(String v2:vertices){
						if(!v1.equals(v2) && !allNgds.containsKey(v1+","+v2) && !allNgds.containsKey(v2+","+v1) ){
						
							double x = ServerUtil.getHits("\""+v1+"\"");
							double y = ServerUtil.getHits("\""+v2+"\"");
						
							double m = ServerUtil.getHits("+\""+v1+"\" +\""+v2+"\"");
						
							double ngd = NGD_calculate.NGD_cal(x, y, m);
						
							allNgds.put(v1+","+v2, ngd);
						}
					}
				}
			
				//ServerUtil.update();
			
				List<Map.Entry<String, Double>> list_Data =
						new ArrayList<Map.Entry<String, Double>>(allNgds.entrySet());
			
				Collections.sort(list_Data, new Comparator<Map.Entry<String, Double>>(){
					public int compare(Map.Entry<String, Double> entry1,
	                               Map.Entry<String, Double> entry2){
						if(entry1.getValue() > entry2.getValue())
							return 1;
						else if (entry1.getValue() < entry2.getValue())
							return -1;
						else
							return 0;

					}
				});
			
				try {
					bw = new BufferedWriter(new FileWriter(testdir.getPath()+"/"+"concept.txt"));
					for(Entry<String, Double> e : list_Data){
						bw.write(e.getKey()+","+e.getValue());
						bw.newLine();
					
					}
					bw.flush();
					bw.close();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

				betweennessCentralityt bc = new betweennessCentralityt();
				bc.betweeness_threshold = betweeness_threshold;
				
				Map<String, Integer> map = 
					bc.betweenness_cal(testdir.getPath(), "concept.txt", "center.txt", "concpets.txt",true);
					
				double m = bc.computeModularity(bc.g, map);
				
				sum+=m;
				
				
				try {
					bw2.append(t+":"+m);
					bw2.newLine();
					bw2.flush();
						
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		

			}
			avg = sum/times;
			
			try {
				bw2.append("avg: "+avg);
				bw2.flush();
				bw2.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			
		}
	}
}
