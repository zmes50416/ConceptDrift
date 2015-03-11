import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tw.edu.ncu.sia.util.ServerUtil;


public class Drift_exp4_withDetect {
	static double betweeness_threshold = 0.35; //去掉多少連線
	static double core_threshold = 0.75; //取多少當作核心
	static double relateness_threshhold = 0.525;
	
	static int chunck =15;	
	static int changeChunck1 = 5;
	static int changeChunck2 = 10;
	
	static String learningCat[] = {"trade","crude"};
	
	static String trainDir_relate;
	static String trainDir_unrelate;
	static String testDir;
	static String mainDir;
	
	static LinkedHashSet<String> trainset;
	
	static double delta = -0.05;
	static double Lambda = 0.15;
	
	static double sumFmeasure = 0;
	static List<Double> mT;

	
	/**
	 * 一個主題存在下，學習一主題、接著忘記一主題
	 * 偵測到才調整的結果
	 */
	public static void main(String[] args) {
		
		try {
			ServerUtil.initialize();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("exp4_withDetet/result.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		trainset = new LinkedHashSet<String>();
		
		betweennessCentralityt bc = new betweennessCentralityt();
		bc.betweeness_threshold = betweeness_threshold;
		bc.core_threshold = core_threshold;
		
		CompareRelateness cr = null;
		
		mT = new LinkedList<Double>();
		double fmeasure = 0;
		
		for(int i=1; i<=chunck; i++){
			trainDir_relate = "exp4_withDetet/chunck"+i+"/training/related";
			trainDir_unrelate = "exp4_withDetet/chunck"+i+"/training/unrelated";
			testDir = "exp4_withDetet/chunck"+i+"/testing";
			mainDir = "exp4_withDetet/chunck"+i+"/";
			
			
			//初始化 底下註解是為了使用一樣的訓練、測試文件 使用一樣的資料務必修改CompareRelateness.java的getConcept()
			if(i==1){
				String objTopic[] = {learningCat[0]};
			//	HashSet<String> set = Go_Training3.generateTrainSet(5, trainDir_relate, learningCat[0]);
			//	trainset.addAll(set);
			//	conceptGenerating(new File(trainDir_relate));
				
			//	bc.betweenness_cal(trainDir_relate, "concept.txt", "center.txt", "concepts.txt", true);
				
			//	Go_Training3.generateTestSet(10, testDir, learningCat);
				cr = new CompareRelateness();
				try {
					fmeasure = cr.caculRetateness(trainDir_relate, "center.txt", "all.txt" ,testDir, objTopic, true);
					sumFmeasure += fmeasure;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double avg = sumFmeasure/i;
				double mt = fmeasure-avg-delta;
				
				mT.add(mt);
				
				double minMT=100;
				double sumMt=0;
				
				for(double m:mT){
					sumMt+=m;
					if(m<minMT)
						minMT=m;
				}
				
				double PHT = sumMt - minMT;
				
				try {
					bw.write("chunck"+i+": "+PHT);
					bw.newLine();
					bw.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			//階段一 	
			}else if (i<=changeChunck1){
				String objTopic[] = {learningCat[0]};
				
				//Go_Training3.set = trainset;
				//Go_Training3.generateTestSet(10, testDir, learningCat);
				
				try {
					fmeasure = cr.caculRetateness(mainDir, "all.txt" ,testDir, objTopic, false);
					sumFmeasure += fmeasure;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double avg = sumFmeasure/i;
				double mt = fmeasure-avg-delta;
				
				mT.add(mt);
				
				double minMT=100;
				double sumMt=0;
				
				for(double m:mT){
					sumMt+=m;
					if(m<minMT)
						minMT=m;
				}
				
				double PHT = sumMt - minMT;
				
				try {
					bw.write("chunck"+i+": "+PHT);
					bw.newLine();
					bw.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				//概念飄移
				if(PHT>Lambda){
						
					HashSet<String> set = Go_Training3.generateTrainSet(2, trainDir_relate, objTopic[0]);
					trainset.addAll(set);
					
					for(String f: new File(trainDir_relate).list()){
						cr.getConcept2(f, trainDir_relate);
						cr.updateRelated(trainDir_relate, "concept.txt");
					}
					
					bc.simMin = cr.simMin;
					
					bc.betweenness_cal(trainDir_relate, "concept.txt", "center.txt", "concepts.txt", false);
					cr.getCenter(trainDir_relate+"/centers/center.txt", false);
					
				}
				
			}
			//階段二 一主題存在下 學習另一主題
			else if(i>changeChunck1 && i<=changeChunck2){
				String objTopic[] = {learningCat[0], learningCat[1]};
				
			//	Go_Training3.set = trainset;
			//	Go_Training3.generateTestSet(10, testDir, learningCat);
				
				try {
					fmeasure = cr.caculRetateness(mainDir, "all.txt" ,testDir, objTopic, false);
					sumFmeasure += fmeasure;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double avg = sumFmeasure/i;
				double mt = fmeasure-avg-delta;
				
				mT.add(mt);
				
				double minMT=100;
				double sumMt=0;
				
				for(double m:mT){
					sumMt+=m;
					if(m<minMT)
						minMT=m;
				}
				
				double PHT = sumMt - minMT;
				
				try {
					bw.write("chunck"+i+": "+PHT);
					bw.newLine();
					bw.flush();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//概念飄移
				if(PHT>Lambda){
						
					for(String t: objTopic){
						HashSet<String> set = Go_Training3.generateTrainSet(1, trainDir_relate, t);
						trainset.addAll(set);
					}

					for(String f: new File(trainDir_relate).list()){
						cr.getConcept2(f, trainDir_relate);
						cr.updateRelated(trainDir_relate, "concept.txt");
					}
					
					bc.simMin = cr.simMin;
					
					bc.betweenness_cal(trainDir_relate, "concept.txt", "center.txt", "concepts.txt", false);
					cr.getCenter(trainDir_relate+"/centers/center.txt", false);
					
				}
				
			}
			//階段三 兩主題情況下 忘記一主題
			else if(i>changeChunck2 && i<=chunck){
				String objTopic[] = {learningCat[0]};
				
			//	Go_Training3.set = trainset;
			//	Go_Training3.generateTestSet(10, testDir, learningCat);
				
				try {
					fmeasure = cr.caculRetateness(mainDir, "all.txt" ,testDir, objTopic, false);
					sumFmeasure += fmeasure;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double avg = sumFmeasure/i;
				double mt = fmeasure-avg-delta;
				
				mT.add(mt);
				
				double minMT=100;
				double sumMt=0;
				
				for(double m:mT){
					sumMt+=m;
					if(m<minMT)
						minMT=m;
				}
				
				double PHT = sumMt - minMT;
				
				//概念飄移
				if(PHT>Lambda){
					
					HashSet<String> set = Go_Training3.generateTrainSet(1, trainDir_relate, learningCat[0]);
					trainset.addAll(set);
					
					HashSet<String> set2 = Go_Training3.generateTrainSet(1, trainDir_unrelate, learningCat[1]);
					trainset.addAll(set2);

					cr.getConcept2(new File(trainDir_unrelate).list()[0], trainDir_unrelate);
					cr.updateUnRelated(trainDir_unrelate, "concept.txt");
					
					cr.getConcept2(new File(trainDir_relate).list()[0], trainDir_relate);
					cr.updateRelated(trainDir_relate, "concept.txt");
					
					
					bc.simMin = cr.simMin;
					
					bc.betweenness_cal(trainDir_relate, "concept.txt", "center.txt", "concepts.txt", false);
					cr.getCenter(trainDir_relate+"/centers/center.txt", false);
					
				}
				
			}
			
		}
	}
	
	public static void conceptGenerating(File trainDir){
		
		Map<String, Double> map_Data = new HashMap<String, Double>();
		HashSet<String> vertices = new HashSet<String>();
		Map<String, Double> ngds;
		
		for(File f: trainDir.listFiles()){
			ngds = Go_Training3.featureExtract(f);
			for(Entry<String,Double> e: ngds.entrySet()){
				String pair = e.getKey();
				vertices.add(pair.split(",")[0]);
				vertices.add(pair.split(",")[1]);
			}
			map_Data.putAll(ngds);	
		}
		
	
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
		
		
	}
	
	

}
