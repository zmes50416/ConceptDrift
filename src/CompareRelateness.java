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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tw.edu.ncu.sia.util.ServerUtil;

/**
 * 
 */

/**
 * @author yoshi
 *
 */
public class CompareRelateness {

	public List<String> coreConcepts;
	private List<String> concept;
	
	double simMin;
	
	double relateness_threshold = 0.525;
	HashMap<String,Integer> term_vertice_times = new HashMap<String,Integer>();
	/**
	 * @param 
	 */
	public void getCenter(String file, boolean changeSimMin) {
		
		coreConcepts = new LinkedList<String>();

		BufferedReader br;
		String line = "";
		
		int index = 0, lastindex = 0;
		String word = "";
		String origin = "";
	
		
		try {
			br = new BufferedReader(new FileReader(file));
			int i=0;
			while ((line = br.readLine()) != null ) {				
				if(!line.equals("")){					
					if(i==0 ){
						if(changeSimMin)
							simMin = Double.parseDouble(line);
						
						//break;
					}
					else if (i==1){
						word = line.split(",")[0];
						index = Integer.parseInt(line.split(",")[2]) - 1;
						
						coreConcepts.add(word);
						lastindex = index;
					}
					else{
						word = line.split(",")[0];
						index = Integer.parseInt(line.split(",")[2]) - 1;
						if(lastindex == index){
							origin = coreConcepts.get(index);
							coreConcepts.set(index, origin+ ","+word);
						}
						else
							coreConcepts.add(word);
						lastindex = index;
					}
					i++;
				}
				
			}
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}//end of getCenter
	
	public void getConcept(String f, String dir){
		BufferedReader br;
		String line;
		int index = 0, lastindex = 0;
		boolean first = true;
		
		//- 跑exp4要使用相同訓練、測試文件，務必註解這範圍內的程式
		///*
		concept = new LinkedList<String>();
		
		TOM_betweennessCentrality bc = new TOM_betweennessCentrality();
		bc.betweeness_threshold = 0.4; 
		
		//bc.betweenness_cal(dir, f, "center.txt", f+"_concepts.txt", true);
		//*/
				
		try {
			br = new BufferedReader(new FileReader(dir +"/concepts/"+f+"_concepts.txt"));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					String w1 = line.split(",")[0]; //字
					
					if(first){
						concept.add(w1);
						first = false;
						index = Integer.parseInt(line.split(",")[2]) - 1;
						lastindex = index;
					}else{
						index = Integer.parseInt(line.split(",")[2]) - 1;
						if(lastindex == index){
							String origin = concept.get(index);
							concept.set(index, origin+ ","+w1);
						}
						else
							concept.add(w1);
						lastindex = index;	
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void getConcept2(String f, String dir){
		BufferedReader br;
		String line;
		int index = 0, lastindex = 0;
		boolean first = true;
		
		concept = new LinkedList<String>();
		
		TOM_betweennessCentrality bc = new TOM_betweennessCentrality();
		bc.betweeness_threshold = 0.4; 
		
		//bc.betweenness_cal(dir, f, "center.txt", f+"_concepts.txt", true);
		
				
		try {
			br = new BufferedReader(new FileReader(dir +"/concepts/"+f+"_concepts.txt"));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					String w1 = line.split(",")[0]; //字
					
					if(first){
						concept.add(w1);
						first = false;
						index = Integer.parseInt(line.split(",")[2]) - 1;
						lastindex = index;
					}else{
						index = Integer.parseInt(line.split(",")[2]) - 1;
						if(lastindex == index){
							String origin = concept.get(index);
							concept.set(index, origin+ ","+w1);
						}
						else
							concept.add(w1);
						lastindex = index;	
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//輸出依照最大連線數、總連線數、滿足門檻比例的排名，並依此計算p@n
	public void caculRank(String topicDir, String centerFile,
			String writeFile, String testDir, String cat[], int nCount,boolean changeSimMin ) throws IOException{
		HashMap<String, Double> allThreshold_max = new HashMap<String, Double>();
		HashMap<String, Double> normalized_allThreshold_max = new HashMap<String, Double>();
		
		HashMap<String, Integer> sum_map = new HashMap<String, Integer>();
		HashMap<String, Integer> max_map = new HashMap<String, Integer>();
		
		
		getCenter(topicDir+"/centers/"+centerFile, changeSimMin);
		BufferedWriter bw = new BufferedWriter(new FileWriter(topicDir
				+"/"+ writeFile, true));
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(topicDir
				+"/sumRank.txt", true));
		
		BufferedWriter bw3 = new BufferedWriter(new FileWriter(topicDir
				+"/maxRank.txt", true));
		
		BufferedWriter bw4 = new BufferedWriter(new FileWriter(topicDir
				+"/maxThresholds.txt", true));
		
		BufferedWriter bw5 = new BufferedWriter(new FileWriter(topicDir
				+"/normalized_maxThresholds.txt", true));
		
		
		
		
		File dir = new File(testDir);
			
		try {
			ServerUtil.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bw.write("使用者模型概念數:"+coreConcepts.size());
		bw.newLine();
		
		for (String f : dir.list()){
			
			List<Integer> link_count = new LinkedList<Integer>();
			List<Integer> thresholds = new LinkedList<Integer>();
			List<Double> ex_thresholds_normalized = new LinkedList<Double>();
			List<Double> ex_thresholds = new LinkedList<Double>();
			int threshold=0;
			
			
			int links = 0;
			getConcept(f, testDir);
			
			bw.write(f+"概念數:"+concept.size());
			bw.newLine();
			
			int uc=0, dc=0;
			
			//對於使用者模型中的每個概念群
			for(String core : coreConcepts){
				String cores[] = core.split(",");
				dc=0;
				uc++;
	
				//對於文件的每個概念群
				for(String c : concept){
					dc++;
					
					String concepts[] = c.split(",");
					
					threshold = (int) (cores.length * concepts.length);
					links = 0;
					
					//使用者模型的其中的core與文件的其中的concept兩兩計算NGD、得到滿足門檻的連線數
					for(String s: cores){
						for(String w: concepts){
							System.out.println(s+","+w);
							
							double a = ServerUtil.getHits("\""+s+"\"");
							double b = ServerUtil.getHits("\""+w+"\"");
							
							double mValue = ServerUtil.getHits("+\""+s+"\" +\""+w+"\"");
							//System.err.println("Query: +\""+s+"\" +\""+w+"\"");
												
							double NGD = NGD_calculate.NGD_cal(a,b,mValue);
							
							if(NGD<=simMin){
								links++;
							}
						}
					}
					
					link_count.add(links);
					thresholds.add(threshold);
					ex_thresholds.add((double)links/threshold);
					
					bw.write(uc+"/"+dc+":" +links+"/"+threshold);
					bw.newLine();
					
					
				}
			}
			int max_c=0, sum_c=0, max=0, min=1000;
			double max_t=0, max_n=0;
			
			for(int t: thresholds){
				if(t>max)
					max=t;
				if(t<min)
					min=t;
			}
			
			for(int i =0; i<thresholds.size(); i++){
				double normalized;
				if(thresholds.size() == 1)
					normalized = 1;
				else
					normalized = (double)(thresholds.get(i)-min)/(max-min);
				ex_thresholds_normalized.add(ex_thresholds.get(i)*normalized);
			}
			
			for(double t: ex_thresholds_normalized){
				if(t>max_n)
					max_n=t;
			}
			normalized_allThreshold_max.put(f, max_n);


			//找出最大值與總和連結數
			for(int lc: link_count){
				if(lc>max_c)
					max_c = lc;
				sum_c+=lc;
			}
			
			sum_map.put(f, sum_c);
			max_map.put(f, max_c);
			
			for(double t: ex_thresholds){
				if(t>max_t)
					max_t = t;
				
			}
			allThreshold_max.put(f, max_t);
			
		}
		bw.close();
		
		List<Map.Entry<String, Double>> normailized_sorted_maxThrsd = 
				map_sort_d(normalized_allThreshold_max);

		List<Map.Entry<String, Double>> sorted_maxThrsd =
				map_sort_d(allThreshold_max);
		
		List<Map.Entry<String, Integer>> sorted_sum = map_sort(sum_map);
		List<Map.Entry<String, Integer>> sorted_max = map_sort(max_map);
		
		List <String> result = new LinkedList<String>(); 
		//寫出依照sum的排序結果以及p@N
		int i = 0, n=nCount, j=0;
		for(Map.Entry<String, Integer> e: sorted_sum){
			j++;
			bw2.write(e.getKey()+":"+e.getValue());
			bw2.newLine();
			for(String c: cat)
			if((e.getKey().split("_")[0].equals(c) || e.getKey().split("_")[1].equals(c)) ){
				result.add(""+j+":"+e.getValue());
				if(n>0)
					i++;
			}
			n--;
		}
		for(String r :result){
			bw2.write(r);
			bw2.newLine();
			bw2.flush();
		}
	
		double pAtN = (double)i/nCount;
		bw2.write("P at "+nCount+": "+pAtN);
		bw2.flush();
		bw2.close();
		
		//寫出依照max的排序結果以及p@N
		i = 0;
		j=0;
		n = nCount;
		result.clear();
		for(Map.Entry<String, Integer> e: sorted_max){
			j++;
			bw3.write(e.getKey()+":"+e.getValue());
			bw3.newLine();
			for(String c: cat)
			if((e.getKey().split("_")[0].equals(c) || e.getKey().split("_")[1].equals(c))){
				result.add(""+j+":"+e.getValue());
				if(n>0)
					i++;
			}
			n--;
		}
		
		for(String r :result){
			bw3.write(r);
			bw3.newLine();
			bw3.flush();
		}
		
		pAtN = (double)i/nCount;
		bw3.write("P at "+nCount+": "+pAtN);
		bw3.flush();
		bw3.close();
		
		//寫出依照最大滿足門檻比例的排序結果以及p@N
		i = 0;
		n = nCount;
		j=0;
		result.clear();
		double thrsd_sum = 0;
		for(Map.Entry<String, Double> e: sorted_maxThrsd){
			j++;
			bw4.write(e.getKey()+":"+e.getValue());
			bw4.newLine();
			for(String c: cat)
			if(e.getKey().split("_")[0].equals(c) || e.getKey().split("_")[1].equals(c)){
				result.add(""+j+":"+e.getValue());
				if(n>0)
					i++;
			}
			n--;
			thrsd_sum += e.getValue();
		}
		
		for(String r :result){
			bw4.write(r);
			bw4.newLine();
			bw4.flush();
		}
		
		pAtN = (double)i/nCount;
		bw4.write("P at "+nCount+": "+pAtN);
		bw4.newLine();
		bw4.write("avg"+ (double)thrsd_sum/sorted_maxThrsd.size());
		bw4.flush();
		bw4.close();
		
		//寫出依照正規化後最大滿足門檻比例的排序結果以及p@N
		i = 0;
		j=0;
		n = nCount;
		thrsd_sum = 0;
		result.clear();
		for(Map.Entry<String, Double> e: normailized_sorted_maxThrsd){
			j++;
			bw5.write(e.getKey()+":"+e.getValue());
			bw5.newLine();
			for(String c: cat)
			if(e.getKey().split("_")[0].equals(c) || e.getKey().split("_")[1].equals(c)){
				result.add(""+j+":"+e.getValue());
				if(n>0)
					i++;
			}
			n--;
			thrsd_sum += e.getValue();
		}
		
		for(String r :result){
			bw5.write(r);
			bw5.newLine();
			bw5.flush();
		}
		
		pAtN = (double)i/nCount;
		bw5.write("P at "+nCount+": "+pAtN);
		bw5.newLine();
		bw5.write("avg"+ (double)thrsd_sum/normailized_sorted_maxThrsd.size());
		bw5.flush();
		bw5.close();
	}
	
	public List<Map.Entry<String, Integer>> map_sort(HashMap<String, Integer> map){
		List<Map.Entry<String, Integer>> list_Data = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		
		Collections.sort(list_Data, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Entry<String, Integer> e1,
						Entry<String, Integer> e2) {				
				return e2.getValue()-e1.getValue();
			}
		});
		
		return list_Data;
	}
	
	public List<Map.Entry<String, Double>> map_sort_d(HashMap<String, Double> map){
		List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		Collections.sort(list_Data, new Comparator<Map.Entry<String, Double>>(){
			public int compare(Entry<String, Double> e1,
						Entry<String, Double> e2) {				
				if(e1.getValue()<e2.getValue())
					return 1;
				else if (e1.getValue()>e2.getValue())
					return -1;
				else
					return 0;
			}
		});
		
		
		return list_Data;
	}
	
	public void set_term_vertice_times(HashMap<String,Integer> get_term_vertice_times){
		term_vertice_times = get_term_vertice_times;
	}
	
	public double caculRetateness(String topicDir, String centerFile,
			String writeFile, String testDir, String cat[], boolean changeSimMin) throws IOException{
		return caculRetateness(topicDir,topicDir,centerFile,writeFile,testDir,cat,changeSimMin);
	}
	
	public double caculRetateness(String resultDir, String topicDir, String centerFile,
			String writeFile, String testDir, String cat[], boolean changeSimMin) throws IOException{
		
		int tp=0, fp=0, tn=0, fn=0, threshold=0, coreslength=0;
		double precision = 0;
		double recall = 0;
		double fmeasure = 0;
		boolean relateness = false; //系統判定是屬於該類
		
		getCenter(resultDir+"/centers/"+centerFile, changeSimMin);
		BufferedWriter bw = new BufferedWriter(new FileWriter(resultDir
				+"/"+ writeFile, true));
		//==rrr==
		BufferedWriter Tom_bw = new BufferedWriter(new FileWriter(resultDir
				+"/"+ "see.txt", true));
		//==rrr==
		
		//紀錄時間用
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("time/caculRetateness.txt", true));
		
		File dir = new File(testDir);
			
		try {
			ServerUtil.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bw.write("使用者模型概念數:"+coreConcepts.size());
		bw.newLine();
		
		//使用於recall值計算公式為 (double) tp / all_p時;
		/*int all_p = 0; //紀錄證向的數量文件，以便計算recall
		int recall_threshold = 1;
		for (String f : dir.list()){
			for(String c:cat){
				if(f.split("_")[0].equals(c)){
					all_p++;
				}
			}
		}
		bw.write("正向概念總數:"+all_p);
		bw.newLine();*/
		
		for (File files : dir.listFiles()){
			if(files.isFile()){
				String f = files.getName();
				System.out.print("文件 "+ f + " 比對中\n");
			//==rrr==
			Tom_bw.write("本文名稱為 : "+f);
			Tom_bw.newLine();
			Tom_bw.write("本文件類別為"+f.split("_")[0]);
			Tom_bw.newLine();
			Tom_bw.flush();
			//==rrr==
			relateness = false;
			int links = 0;
			getConcept(f, testDir);
			
			bw.write(f+"概念數:"+concept.size());
			bw.newLine();
			
			bw.write("本文件類別為"+f.split("_")[0]);
			bw.newLine();
			bw.flush();
			
			int uc=0, dc=0;
			int coreSum=0, conceptSum=0;
			
			for(String core : coreConcepts){
				String cores[] = core.split(",");
				coreSum +=cores.length;
			}
			
			for(String c : concept){
				String concepts[] = c.split(",");
				conceptSum+=concepts.length;
			}
			
			long t1 = System.currentTimeMillis();
			
			//對於使用者模型中的每個概念群
			for(String core : coreConcepts){
				String cores[] = core.split(",");
				dc=0;
				uc++;
				coreslength=0;
				for(String s: cores){
					if(term_vertice_times.get(s)!=null){
						coreslength=coreslength+term_vertice_times.get(s);
					}
				}
				if(coreslength==0){ //term_vertice_times沒設定時
					coreslength = cores.length;
				}
							
				if(relateness)
					break;
				
				//對於文件的每個概念群
				for(String c : concept){
					dc++;					
					String concepts[] = c.split(",");
										
					threshold = (int) (coreslength * concepts.length*relateness_threshold);
					links = 0;
					//==rrr==
					Tom_bw.write("本文第"+dc+"個概念與模型第"+uc+"個概念的門檻值為-->"+threshold);
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
					//使用者模型的其中的core與文件的其中的concept兩兩計算NGD、得到滿足門檻的連線數
					for(String s: cores){
						for(String w: concepts){
							System.out.println(s+","+w);
							
							double a = ServerUtil.getHits("\""+s+"\"");
							double b = ServerUtil.getHits("\""+w+"\"");
							
							double mValue = ServerUtil.getHits("+\""+s+"\" +\""+w+"\"");
							//System.err.println("測試文件與使用者模組概念比對 Query: +\""+s+"\" +\""+w+"\"");
												
							double NGD = NGD_calculate.NGD_cal(a,b,mValue);
							
							if(NGD<=simMin){ //term_vertice_times沒設定時
								if(coreslength==cores.length){
									links++;
									Tom_bw.write(s+","+w+",增加累積連線數:"+"1");
									Tom_bw.newLine();
									Tom_bw.write("NGD = "+NGD);
								}else{
									links = links + term_vertice_times.get(s);
									//==rrr==
									Tom_bw.write(s+","+w+",增加累積連線數:"+term_vertice_times.get(s));
									//==rrr==
								}
								Tom_bw.newLine();
								Tom_bw.write("目前links為:"+links);
								Tom_bw.newLine();
								Tom_bw.flush();
							}
						}
					}
					
					bw.write(uc+"/"+dc+":" +links+"/"+threshold);
					bw.newLine();
					
					if(links >= threshold){
						relateness = true;
						//==rrr==
						Tom_bw.write("本文第"+dc+"個概念"+threshold+"與模型第"+uc+"個概念"+links+"判定為相關");
						Tom_bw.newLine();
						Tom_bw.flush();
						//==rrr==
						break;
					}
						
					else
						relateness = false;
					//==rrr==
					Tom_bw.write("本文第"+dc+"個概念"+threshold+"與模型第"+uc+"個概念"+links+"判定為非相關");
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
				}
			}
			
			long t2 = System.currentTimeMillis();
			
			bw2.write(coreSum+","+conceptSum+":"+(t2-t1));
			bw2.newLine();
			bw2.flush();
			
			//ServerUtil.update();
			bw.write(f+ " : "+relateness);
			bw.newLine();
			bw.flush();
			
			boolean act_relateness = false; //實際上是否該類別
			
			for(String c:cat){
				if(f.split("_")[0].equals(c)){
					act_relateness = true;
				}
			}
				
			if(act_relateness){
				bw.write("實際上為相關");
				bw.newLine();
				bw.flush();
				//==rrr==
				Tom_bw.write("實際上為相關");
				Tom_bw.newLine();
				Tom_bw.flush();
				//==rrr==
				if(relateness){
					tp++;
					bw.write("判定為相關");
					bw.newLine();
					bw.flush();
					//==rrr==
					Tom_bw.write("判定為相關");
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
				}else{
					fn++;
					bw.write("判定為非相關");
					bw.newLine();
					bw.flush();
					//==rrr==
					Tom_bw.write("判定為非相關");
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
				}
			}else{
				bw.write("實際上為非相關");
				bw.newLine();
				bw.flush();
				//==rrr==
				Tom_bw.write("實際上為非相關");
				Tom_bw.newLine();
				Tom_bw.flush();
				//==rrr==
				if(relateness){
					fp++;
					bw.write("判定為相關");
					bw.newLine();
					bw.flush();
					//==rrr==
					Tom_bw.write("判定為相關");
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
				}else{
					tn++;
					bw.write("判定為非相關");
					bw.newLine();
					bw.flush();
					//==rrr==
					Tom_bw.write("判定為非相關");
					Tom_bw.newLine();
					Tom_bw.flush();
					//==rrr==
				}
			}
			if(tp!=0 || fp!=0){
				precision = (double)tp / (tp + fp);
			}
			recall = (double) tp / (tp + fn);
			if(precision!=0 || recall!=0){
				fmeasure = (double)(2 * precision * recall) / (precision + recall);
			}
				bw.write("P:" + precision);
				bw.newLine();
				bw.write("R:" + recall);
				bw.newLine();
				bw.write("A:" + (double)(tp + tn) / dir.list().length);
				bw.newLine();
				bw.write("F:" + fmeasure);
				bw.newLine();
				bw.write("tp:" + tp+" fp:" + fp+" tn:" + tn+" fn:" + fn);
				bw.newLine();
			}
		}
		
		bw2.close();
		
		precision = (double)tp / (tp + fp);
		recall =(double) tp / (tp + fn);
		fmeasure = (double)(2 * precision * recall) / (precision + recall);
		
		bw.write("Used NGD:"+ simMin);
		bw.newLine();
		bw.write("final_P:" + precision);
		bw.newLine();
		bw.write("final_R:" + recall);
		bw.newLine();
		bw.write("final_A:" + (double)(tp + tn) / dir.list().length);
		bw.newLine();
		bw.write("final_F:" + fmeasure);
		bw.newLine();
		bw.write("final_tp:" + tp+" final_fp:" + fp+" final_tn:" + tn+" final_fn:" + fn);
		bw.newLine();
		
		bw.close();

		return fmeasure;
	}
	
	//不需要centerfile
	public double caculRetateness(String topicDir,
			String writeFile, String testDir, String cat[], boolean changeSimMin) throws IOException{
		
		int tp=0, fp=0, tn=0, fn=0, threshold=0;
		double precision = 0;
		double recall = 0;
		double fmeasure = 0;
		boolean relateness = false; //系統判定是屬於該類
		
		new File(topicDir).mkdirs();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(topicDir
				+"/"+ writeFile, true));
		
		File dir = new File(testDir);
			
		try {
			ServerUtil.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bw.write("使用者模型概念數:"+coreConcepts.size());
		bw.newLine();
		
		for (String f : dir.list()){
			
			relateness = false;
			int links = 0;
			getConcept(f, testDir);
			
			bw.write(f+"概念數:"+concept.size());
			bw.newLine();
			
			int uc=0, dc=0;
			
			//對於使用者模型中的每個概念群
			for(String core : coreConcepts){
				String cores[] = core.split(",");
				dc=0;
				uc++;
							
				if(relateness)
					break;
				
				//對於文件的每個概念群
				for(String c : concept){
					dc++;
					
					String concepts[] = c.split(",");
					
					threshold = (int) (cores.length * concepts.length*relateness_threshold);
					links = 0;
					
					//使用者模型的其中的core與文件的其中的concept兩兩計算NGD、得到滿足門檻的連線數
					for(String s: cores){
						for(String w: concepts){
							System.out.println(s+","+w);
							
							double a = ServerUtil.getHits("\""+s+"\"");
							double b = ServerUtil.getHits("\""+w+"\"");
							
							double mValue = ServerUtil.getHits("+\""+s+"\" +\""+w+"\"");
							//System.err.println("Query: +\""+s+"\" +\""+w+"\"");
												
							double NGD = NGD_calculate.NGD_cal(a,b,mValue);
							
							if(NGD<=simMin){
								links++;
							}
						}
					}
					
					bw.write(uc+"/"+dc+":" +links+"/"+threshold);
					bw.newLine();
					
					if(links >= threshold){
						relateness = true;
						break;
					}
						
					else
						relateness = false;
				}
			}
			
			//ServerUtil.update();
			bw.write(f+ " : "+relateness);
			bw.newLine();
			bw.flush();
			
			boolean act_relateness = false; //實際上是否該類別
			
			for(String c:cat)
				if(f.split("_")[0].equals(c))
					act_relateness = true;
				
			if(act_relateness){
				if(relateness)
					tp++;
				else
					fn++;
			}
			else{
				if(relateness)
					fp++;
				else
					tn++;
			}
			
		}
		
		precision = (double)tp / (tp + fp);
		recall =(double) tp / (tp + fn);
		fmeasure = (double)(2 * precision * recall) / (precision + recall);
		
		bw.write("Used NGD:"+ simMin);
		bw.newLine();
		bw.write("P:" + precision);
		bw.newLine();
		bw.write("R:" + recall);
		bw.newLine();
		bw.write("A:" + (double)(tp + tn) / dir.list().length);
		bw.newLine();
		bw.write("F:" + fmeasure);
		bw.newLine();
		bw.write("tp:" + tp+" fp:" + fp+" tn:" + tn+" fn:" + fn);
		bw.newLine();
		
		bw.close();

		return fmeasure;
	}
	
	public HashMap<String, Double> getNGDs(){
		List<String> wordlist = new LinkedList<String>();
		HashSet<String> words = new HashSet<String>();

		HashMap<String,Double> NGDs = new HashMap<String,Double>();
		
		for(String c : concept){
			for(String t: c.split(","))
				words.add(t);
		}
		
		for(String s: coreConcepts){
			for(String term : s.split(",")){
				words.add(term);
			}
		}
		
		for(String w : words){
			wordlist.add(w);
		}
		words.clear();
		
		try {
			ServerUtil.initialize();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int i = 0; i<wordlist.size(); i++){
			for(int j = i+1; j<wordlist.size(); j++){
				double a = ServerUtil.getHits("\""+wordlist.get(i)+"\"");
				double b = ServerUtil.getHits("\""+wordlist.get(j)+"\"");
				double mValue = ServerUtil.getHits("+\""+wordlist.get(i)+"\" +\""+wordlist.get(j)+"\"");
				
				double NGD = NGD_calculate.NGD_cal(a, b, mValue);
				
				//改成只存<1
				if(NGD < 1)
					NGDs.put(wordlist.get(i)+","+wordlist.get(j), NGD);
				
			}
		}
		//ServerUtil.update();
		
		return NGDs;
	}
	
	
	
	
	public void updateRelated(String path, String conceptFile){
		Map<String, Double> NGDs = getNGDs();
		
		List<Map.Entry<String, Double>> list_Data =
	            new ArrayList<Map.Entry<String, Double>>(NGDs.entrySet());
		
		
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
        
        //紀錄結果於
        try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path+"/"+conceptFile));
			for (int i = 0; i < list_Data.size(); i++) {
				Entry<String, Double> entry = list_Data.get(i);
	            bw.write(entry.getKey()+","+entry.getValue());
	            bw.newLine();
	            bw.flush();
	        }
			bw.close();
			
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public void updateUnRelated(String path, String conceptFile){
		Map<String, Double> NGDs = getNGDs();
		
		List<Map.Entry<String, Double>> list_Data =
	            new ArrayList<Map.Entry<String, Double>>(NGDs.entrySet());
		
		
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
		
		List<Map.Entry<String, Double>> toRemove_Data = new ArrayList<Map.Entry<String, Double>>();
        
        for(Entry<String, Double> e: list_Data){
        	String w1 = e.getKey().split(",")[0];
        	String w2 = e.getKey().split(",")[1];
        	
        	for(String c :concept)
        		for(String w: c.split(","))
        			if(w.equals(w1) || w.equals(w2)){
        				toRemove_Data.add(e);
        	}
        }
        
        list_Data.removeAll(toRemove_Data);
        
      //紀錄結果
        try {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(path+"/"+conceptFile));
			for (int i = 0; i < list_Data.size(); i++) {
				Entry<String, Double> entry = list_Data.get(i);
	            bw.write(entry.getKey()+","+entry.getValue());
	            bw.newLine();
	            bw.flush();
	        }
			
			bw.close();
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
