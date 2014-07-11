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


public class Drift_exp1_2 {
	
	static String maindir = "reuters/";
	static double betweeness_threshold ;
	static int size = 2;

	/**
	 * 挑同一主題 N篇結合特徵與betweeness_threshold敏感度分析
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double thresholds[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		for(int i = 0; i<thresholds.length; i++){
			betweeness_threshold = thresholds[i];
			
			BufferedWriter bw2 = null;
			try {
				File resultdir = new File("bc_exp/1-2x/");
				resultdir.mkdirs();
				
				bw2 = new BufferedWriter(new FileWriter("bc_exp/1-2x/"+size+"_"+betweeness_threshold+"_result.txt", true));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		
			BufferedWriter bw = null;
			File dir = new File(maindir);

			Map<String, Double> ngds = null;
			Map<String, Double> allNgds = null;
			
			HashSet<String> vertices;
			
			double avg = 0;
			double sum = 0;
		
			for(File d : dir.listFiles()){
				File testdir = new File("bc_exp/1-2x/"+d.getName()+"_"+size+"_"+betweeness_threshold+"_"+i);
				//testdir.mkdirs();
			
			//File resultdir = new File("bc_exp/"+d.getName()+"_result_"+size+"_"+betweeness_threshold);
			//resultdir.mkdirs();
			
				//Go_Training3.generateTrainSet(maindir+d.getName(), size, testdir.getPath());
			
				vertices = new HashSet<String>();
				allNgds = new HashMap<String, Double>();
			
				
			
			//讀入測試資料夾中的rank
				for(File f :testdir.listFiles()){
					System.out.print("目前文件為 = "+f.getName()+"\n");
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
					bc.betweenness_cal(testdir.getPath(), "concept.txt", "center.txt", "concpets.txt", true);
					
				double m = bc.computeModularity(bc.g, map);
				
				sum+=m;
				
				
				try {
					bw2.append(d.getName()+":"+m);
					bw2.newLine();
					bw2.flush();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			avg = sum/7;
			
			try {
				bw2.append("avg:"+avg);
				bw2.flush();
				bw2.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}
	}
}
