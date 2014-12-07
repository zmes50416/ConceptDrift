package tw.edu.ncu.CJ102.CoreProcess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import tw.edu.ncu.CJ102.algorithm.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import Algorithm.feature_algorithm.similarity;

/**
 * 
 * @author TingWen
 *
 */
public class ConceptDrift_Forecasting {

	double topic_close_threshold = 0.6; //NGD+LOG=0.802, NGD=0.088, 簡單重疊比例方法=0.6
	int forecastingTimes = 0;
	
	UndirectedSparseGraph<String,Double> topicCooccurrenceGraph = new UndirectedSparseGraph<String,Double>(); 
	int topicSize;
	HashMap<String,Double> TR = new HashMap<String,Double>(); //讀取出來的主題關係
	HashMap<String,Double> TR_NGD = new HashMap<String,Double>(); //NGD計算後的主題關係
	HashMap<String,Double> sum_topic_freq = new HashMap<String,Double>(); //各主題的出現
	ArrayList<String> topic_list = new ArrayList<String>(); //topic列表
	double sum_topics_relation=0;
	String projectDir;
	Boolean isLoaded = false;
	
	
	public ConceptDrift_Forecasting(String projectDir){
		this.projectDir = projectDir;
		this.topicCooccurrenceGraph.addVertex("test");
		this.topicCooccurrenceGraph.addVertex("test");
	}

	public void writeBackto(String projectDir){
		
	}
	
	public void readFromProject() throws IOException {
			BufferedReader br = new BufferedReader(new FileReader(projectDir
					+ "user_porfile/user_profile_TR.txt"));
			this.setTopicSize(Integer.parseInt(br.readLine())); // 得知目前主題數

			for(String line = br.readLine();line != null;line = br.readLine()) {
				String topicPair = line.split(",")[0];
				// 將還沒加進topic列表的字詞加入
				if (!topic_list.contains(topicPair.split("-")[0])) {
					topic_list.add(topicPair.split("-")[0]);
					this.topicCooccurrenceGraph.addVertex(topicPair.split("-")[0]);
				}
				if (!topic_list.contains(topicPair.split("-")[1])) {
					topic_list.add(topicPair.split("-")[1]);
					this.topicCooccurrenceGraph.addVertex(topicPair.split("-")[1]);

				}
				// 累計主題的出現次數
				if (topicPair.split("-")[0].equals(topicPair.split("-")[1])) { // 自己對到自己就只存一次數值，避免重複累加
					if (sum_topic_freq.get(topicPair.split("-")[0]) != null) {
						sum_topic_freq.put(topicPair.split("-")[0],
								sum_topic_freq.get(topicPair.split("-")[0])
										+ Double.valueOf(line.split(",")[1]));
					} else {
						sum_topic_freq.put(topicPair.split("-")[0],
								Double.valueOf(line.split(",")[1]));
					}
				} else {
					if (sum_topic_freq.get(topicPair.split("-")[0]) != null) {
						sum_topic_freq.put(topicPair.split("-")[0],
								sum_topic_freq.get(topicPair.split("-")[0])
										+ Double.valueOf(line.split(",")[1]));
					} else {
						sum_topic_freq.put(topicPair.split("-")[0],
								Double.valueOf(line.split(",")[1]));
					}
					if (sum_topic_freq.get(topicPair.split("-")[1]) != null) {
						sum_topic_freq.put(topicPair.split("-")[1],
								sum_topic_freq.get(topicPair.split("-")[1])
										+ Double.valueOf(line.split(",")[1]));
					} else {
						sum_topic_freq.put(topicPair.split("-")[1],
								Double.valueOf(line.split(",")[1]));
					}
				}

				double topic_relation = Double.valueOf(line.split(",")[1]);
				sum_topics_relation += topic_relation;
				TR.put(topicPair, topic_relation);
			}
			br.close();
			
			// 計算各主題關係的NGD值，
			for (String two_topic : TR.keySet()) {
				// 如果紀錄的是非自己主題的關係就需要計算兩相異主題間的NGD距離
				if (!two_topic.split("-")[0].equals(two_topic.split("-")[1])) {
					
					/* NGD+LOG方法 double
					 * 
					 * logx=Math.log10(sum_topic_freq.get(two_topic.split("-")[0]));
					 * //第一個主題的出現次數的log10
					 * //System.out.println("log("+two_topic.split
					 * ("-")[0]+")="+logx); double
					 * logy=Math.log10(sum_topic_freq.get(two_topic.split("-")[1]));
					 * //第二個主題的出現次數的log10
					 * //System.out.println("log("+two_topic.split
					 * ("-")[1]+")="+logy); double
					 * logxy=Math.log10(TR.get(two_topic)); //第一、二主題的共現次數的log10
					 * //System.out.println("log("+two_topic+")="+logxy); double
					 * NGD=(Math.max(logx, logy) - logxy) /
					 * (Math.log10(sum_topics_relation) - Math.min(logx, logy));
					 */

					// NGD方法或SIM方法使用
					double x = sum_topic_freq.get(two_topic.split("-")[0]); // 第一個主題的出現次數的log10
					double y = sum_topic_freq.get(two_topic.split("-")[1]); // 第二個主題的出現次數的log10
					double xy = TR.get(two_topic); // 第一、二主題的共現次數的log10

					// NGD方法
					/*
					 * double NGD =(Math.max(x, y) - xy) / (sum_topics_relation -
					 * Math.min(x, y));
					 * //System.out.println("logsum_topics_relation="
					 * +Math.log10(sum_topics_relation));
					 * //System.out.println("NGD="+NGD); if (xy == 0){ //NGD =
					 * 1;//避免無限大 } if (NGD > 1){ //NGD = 1; } if (NGD < 0){ //NGD =
					 * 0; }
					 */

					// 簡單重疊比例方法(SIM)
					double NGD = (2 * xy) / (x + y);

					// System.out.println("邊"+two_topic+"的NGD值為"+NGD);
					TR_NGD.put(two_topic, NGD);
					//I should put in the index instead of value, because no two edge can be the same
					this.topicCooccurrenceGraph.addEdge(NGD,two_topic.split("-")[0] , two_topic.split("-")[1]);
				}
			}
			
			this.isLoaded = true;
	}
	private void setTopicSize(int number) {
		this.topicSize = number;
		
	}
	/**
	 * Not implement yet
	 */
	public void forecastingBySim(){
		
	}
	//Need read project first
	public void forecastingByNGD() {
		if(!this.isLoaded){
			System.err.println("Not read user profile yet! please call read method first");
			return;
		}


		// 預測步驟，計算兩兩邊之間的距離加總，如果總距離小於門檻值，相近的兩點即會產生連接的邊
		String edge1_v1, edge1_v2; // 第一個邊的第一個節點, 第一個邊的第二個節點
		String edge2_v1, edge2_v2; // 第二個邊的第一個節點, 第二個邊的第二個節點
		boolean door = false;
		try {
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(projectDir
					+ "user_porfile/Forecasting_Recorder.txt", true));

			for (String edge1 : TR_NGD.keySet()) {
				edge1_v1 = edge1.split("-")[0];
				edge1_v2 = edge1.split("-")[1];
				for (String edge2 : TR_NGD.keySet()) {
					if (edge1.equals(edge2)) {
						door = true; // 減少重複計算的可能
					}
					if (door && !edge1.equals(edge2)) { // 自己跟自己不用計算
						edge2_v1 = edge2.split("-")[0];
						edge2_v2 = edge2.split("-")[1];
						// 兩個邊中有其中一個節點是互相連接的，才計算連接的長度
						if (edge2_v1.equals(edge1_v1)
								|| edge2_v1.equals(edge1_v2)
								|| edge2_v2.equals(edge1_v1)
								|| edge2_v2.equals(edge1_v2)) {
							// 當兩邊距離加起來小於門檻值時就創立除兩邊連接節點外的兩點之間關係
							System.out.println(edge1 + "與" + edge2 + "相加的NGD為"
									+ (TR_NGD.get(edge1) + TR_NGD.get(edge2)));
							// NGD方法是NGD距離越低越好
							if ((TR_NGD.get(edge1) + TR_NGD.get(edge2) <= topic_close_threshold)) {
								// SIM方法是相似度數值越高越好
								// if((TR_NGD.get(edge1)+TR_NGD.get(edge2)>=topic_close_threshold)){
								String new_edge = edge_make(edge1_v1, edge1_v2,
										edge2_v1, edge2_v2);
								if (TR.get(new_edge) == null) {
									System.out.println("新建立邊" + new_edge);
									// NGD反推
									double should = Math.max(sum_topic_freq
											.get(new_edge.split("-")[0]),
											sum_topic_freq.get(new_edge
													.split("-")[1]))
											- ((TR_NGD.get(edge1) + TR_NGD
													.get(edge2)) * ((sum_topics_relation - Math
													.min(sum_topic_freq.get(new_edge
															.split("-")[0]),
															sum_topic_freq
																	.get(new_edge
																			.split("-")[1])))));
									// SIM反推
									// double should =
									// (TR_NGD.get(edge1)+TR_NGD.get(edge2))*(sum_topic_freq.get(new_edge.split("-")[0])+sum_topic_freq.get(new_edge.split("-")[1]))/2;
									TR.put(new_edge, should);
									// 預測紀錄
									bw2.write(edge1
											+ "與"
											+ edge2
											+ "相加的NGD為"
											+ (TR_NGD.get(edge1) + TR_NGD
													.get(edge2)));
									bw2.newLine();
									bw2.write("新建立邊" + new_edge + " NGD為"
											+ should);
									bw2.newLine();
									bw2.flush();
									forecastingTimes++;
								}
							}
						}
					}
				}
				door = false;
			}

			// 將跑完預測的TR文件重新寫入
			BufferedWriter bw = new BufferedWriter(new FileWriter(projectDir
					+ "user_porfile/user_profile_TR.txt"));
			bw.write("" + topicSize); // 目前主題數
			bw.newLine();
			bw.flush();
			for (String two_topic : TR.keySet()) {
				// 存放格式為 主題1-主題2,關係程度,此次更新編號
				bw.write(two_topic + "," + TR.get(two_topic));
				bw.newLine();
				bw.flush();
			}
			bw.close();

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
	
	public String edge_make(String edge1_v1, String edge1_v2, String edge2_v1, String edge2_v2){
		String new_edge="";
		int int_edge1_v1 = Integer.valueOf(edge1_v1);
		int int_edge1_v2 = Integer.valueOf(edge1_v2);
		int int_edge2_v1 = Integer.valueOf(edge2_v1);
		int int_edge2_v2 = Integer.valueOf(edge2_v2);
		if(int_edge1_v1 == int_edge2_v1){
			if(int_edge1_v2 < int_edge2_v2){
				new_edge = int_edge1_v2+"-"+int_edge2_v2;
			}else{
				new_edge = int_edge2_v2+"-"+int_edge1_v2;
			}
		}
		if(int_edge1_v1 == int_edge2_v2){
			if(int_edge1_v2 < int_edge2_v1){
				new_edge = int_edge1_v2+"-"+int_edge2_v1;
			}else{
				new_edge = int_edge2_v1+"-"+int_edge1_v2;
			}
		}
		if(int_edge1_v2 == int_edge2_v1){
			if(int_edge1_v1 < int_edge2_v2){
				new_edge = int_edge1_v1+"-"+int_edge2_v2;
			}else{
				new_edge = int_edge2_v2+"-"+int_edge1_v1;
			}
		}
		if(int_edge1_v2 == int_edge2_v2){
			if(int_edge1_v1 < int_edge2_v1){
				new_edge = int_edge1_v1+"-"+int_edge2_v1;
			}else{
				new_edge = int_edge2_v1+"-"+int_edge1_v1;
			}
		}
		return new_edge;
	}
	
	
	public void forecasting_cosine(String exp_dir){
		try {
			
			HashMap<String,double[]> TR_vector = new HashMap<String,double[]>(); //各主題向量
			HashMap<String,Double> TR_cosine = new HashMap<String,Double>(); //各主題間相似度
			double vector[];
			int node_num = topic_list.size();
			vector = new double[node_num];
			
			//建立各主題的向量
			for(int i=0;i<node_num;i++){
				for(int z=0;z<vector.length;z++){
					vector[z]=0.0;
				}
				for(int j=0;j<node_num;j++){
					if(TR.get(topic_list.get(i)+"-"+topic_list.get(j))!=null){
						vector[j]=TR.get(topic_list.get(i)+"-"+topic_list.get(j));
						//System.out.println(topic_list.get(i)+" & "+topic_list.get(j)+" = "+TR.get(topic_list.get(i)+"-"+topic_list.get(j)));
					}else if(TR.get(topic_list.get(j)+"-"+topic_list.get(i))!=null){
						vector[j]=TR.get(topic_list.get(j)+"-"+topic_list.get(i));
						//System.out.println(topic_list.get(j)+" & "+topic_list.get(i)+" = "+TR.get(topic_list.get(j)+"-"+topic_list.get(i)));
					}else{
						vector[j]=0.0;
					}
				}
				TR_vector.put(topic_list.get(i), vector.clone());
			}
			
			/*for(String node: TR_vector.keySet()){
				System.out.print(node+" ={");
				for(int i=0;i<node_num;i++){
					System.out.print(TR_vector.get(node)[i]+",");
				}
				System.out.println("}");
			}*/
			
			//計算各主題間相似度
			for(String two_topic: TR.keySet()){
				//如果紀錄的是非自己主題的關係就需要計算兩相異主題間的NGD距離
				if(!two_topic.split("-")[0].equals(two_topic.split("-")[1])){
					System.out.println(two_topic+" = "+similarityCalculator(TR_vector.get(two_topic.split("-")[0]),TR_vector.get(two_topic.split("-")[1]),"cosine"));
					//TR_cosine.put(two_topic,similarityCalculator(TR_vector.get(two_topic.split("-")[0]),TR_vector.get(two_topic.split("-")[1]),"cosine"));
				}
			}
			
			//將跑完預測的TR文件重新寫入
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_porfile/user_profile_TR.txt"));
			bw.write(""+topicSize); //目前主題數
			bw.newLine();
			bw.flush();
			for(String two_topic: TR.keySet()){
				//存放格式為 主題1-主題2,關係程度,此次更新編號
				bw.write(two_topic+","+TR.get(two_topic));
				bw.newLine();
				bw.flush();
			}
			bw.close();
			
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
	
	public double similarityCalculator(double[] point_one, double[] point_two, String method){
		
		double similarity = 0;
		
		if(method.equals("cosine")){
			double sum = 0;
			double o_norm = 0;
			double t_norm = 0;
			
			if(point_one.length == point_two.length){
				for(int i = 0; i < point_one.length; i++){
					sum = sum + (point_one[i] * point_two[i]);
					o_norm = o_norm + Math.pow(point_one[i], 2);
					t_norm = t_norm + Math.pow(point_two[i], 2);
				}
				o_norm = Math.sqrt(o_norm);
				t_norm = Math.sqrt(t_norm);
				similarity = sum / (o_norm * t_norm);
			}
		}
		else if(method.equals("jaccard")){
			double sum = 0;
			double o_norm = 0;
			double t_norm = 0;
			
			if(point_one.length == point_two.length){
				for(int i = 0; i < point_one.length; i++){
					sum = sum + (point_one[i] * point_two[i]);
					o_norm = o_norm + Math.pow(point_one[i], 2);
					t_norm = t_norm + Math.pow(point_two[i], 2);
				}
				o_norm = Math.pow(Math.sqrt(o_norm), 2);
				t_norm = Math.pow(Math.sqrt(t_norm), 2);
				similarity = sum / ((o_norm + t_norm) - sum);
			}
		}
		
		return similarity;
	}
	
	public int getForecastingTimes(){
		return this.forecastingTimes;
	}
	
	public Graph<String,Double> getTopicCooccurGrahp(){
		return this.topicCooccurrenceGraph;
	}
}
