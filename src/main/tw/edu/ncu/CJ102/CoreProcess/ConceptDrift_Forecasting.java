package tw.edu.ncu.CJ102.CoreProcess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Comparator;

import tw.edu.ncu.CJ102.algorithm.*;
import tw.edu.ncu.CJ102.Data.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import Algorithm.feature_algorithm.similarity;

/**
 * 
 * @author TingWen
 *
 */
public class ConceptDrift_Forecasting {

	double topic_close_threshold = 0.6; //NGD+LOG=0.802, NGD=0.088, 簡單重疊比例方法=0.6
	int forecastingTimes;
	
	UndirectedSparseGraph<TopicNode,CEdge> topicCRGraph = new UndirectedSparseGraph<>(); //topic co-occurence Relation Graph 共現矩陣圖形  First is Topic ID, Second is Cooccuren 
	int topicSize;
	HashMap<String,Double> TR = new HashMap<String,Double>(); //讀取出來的主題關係
	HashMap<String,Double> TR_NGD = new HashMap<String,Double>(); //NGD計算後的主題關係
	
	HashMap<String,Double> sum_topic_freq = new HashMap<String,Double>(); //各主題的出現
	HashMap<String, TopicNode> topics = new HashMap<>(); //topic(V)列表
	HashMap<String, CEdge> edges = new HashMap<>();
	TreeMap<CEdge, Pair<TopicNode>> PredictionRank = new TreeMap<>(new Comparator<CEdge>(){
		@Override
		public int compare(CEdge o1, CEdge o2) {
			return o1.getCoScore()>=o2.getCoScore()?1:-1;
		}
		
	});
	double sum_topics_relation = 0;
	String projectDir;
	Boolean isLoaded = false;
	
	
	public ConceptDrift_Forecasting(String projectDir){
		this.projectDir = projectDir;
	}
	
	public void readFromProject() throws IOException {
			BufferedReader br = new BufferedReader(new FileReader(projectDir
					+ "user_profile/user_profile_TR.txt"));
			this.setTopicSize(Integer.parseInt(br.readLine())); // 得知目前主題數

			for(String line = br.readLine();line != null;line = br.readLine()) {
				
				String topicPair = line.split(",")[0];
				String tName = topicPair.split("-")[0];
				String anotherTName = topicPair.split("-")[1];
				TopicNode t = new TopicNode(tName);
				TopicNode t2 = new TopicNode(anotherTName);
				
				// 將還沒加進topic列表的字詞加入
				if (!topics.containsKey(tName)) {
					topics.put(tName, t);
					this.topicCRGraph.addVertex(t);
				}
				if (!topics.containsKey(anotherTName)) {
					topics.put(anotherTName, t2);
					this.topicCRGraph.addVertex(t2);
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
			int count = 0;

			// 計算各主題關係的NGD值，
			for (String topicPair : TR.keySet()) {
				String topic = topicPair.split("-")[0];
				String anotherTopic = topicPair.split("-")[1];
				// 如果紀錄的是非自己主題的關係就需要計算兩相異主題間的NGD距離
				if (!topic.equals(anotherTopic)) {
					
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
					double x = sum_topic_freq.get(topic); // 第一個主題的出現次數的log10
					double y = sum_topic_freq.get(anotherTopic); // 第二個主題的出現次數的log10
					double xy = TR.get(topicPair); // 第一、二主題的共現次數的log10

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
					TR_NGD.put(topicPair, NGD);
					//I should put in the index instead of value, because no two edge can be the same
					TopicNode firstTopic = topics.get(topic);
					TopicNode secondTopic = topics.get(anotherTopic);
					Pair<TopicNode> pair = new Pair<>(firstTopic,secondTopic); 
					CEdge<TopicNode> c =  new CEdge<>(pair,NGD);
					this.edges.put(String.valueOf(count++), c);
					this.topicCRGraph.addEdge(c, pair);
				}
			}
			
			
			this.isLoaded = true;
	}
	private void setTopicSize(int number) {
		this.topicSize = number;
		
	}

	/**
	 * Newest Forecasting method
	 * @param algorithm : LinkPrediction algorithm instance
	 */
	public void forecastingBy(LinkPrediction<TopicNode,CEdge> algorithm) throws IOException{
		if(!this.isLoaded){
			System.err.println("Not read user profile yet! please call read method first");
			return;
		}

		this.PredictionRank.clear();
		this.forecastingTimes = 0;

		BufferedWriter bw2 = new BufferedWriter(new FileWriter(projectDir
				+ "user_profile/Forecasting_Recorder.txt", true));
		
		for(TopicNode node: this.topicCRGraph.getVertices()){
			for(TopicNode anotherNode:this.topicCRGraph.getVertices()){
				double index = algorithm.predict(node, anotherNode);
				if(!node.equals(anotherNode)&& index>0){
					this.forecastingTimes++;
					String newID = String.valueOf(this.edges.size()+this.PredictionRank.size()+1);
					Pair<TopicNode> nodeConnected = new Pair<TopicNode>(node, anotherNode);
					CEdge<TopicNode> newEdge = new CEdge<TopicNode>(nodeConnected,index);
					this.PredictionRank.put(newEdge, nodeConnected );
					//TODO Haven't deterime NGD Distance yet	
					//this.topicCRGraph.addEdge(new CEdge("sd",1.0), node, anotherNode);
					
					
				}
			}
			
		}
	}
	@SuppressWarnings("unchecked")
	@Deprecated
	public void forecastingByNGD() throws IOException {
		if(!this.isLoaded){//Need read project first
			System.err.println("Not read user profile yet! please call read method first");
			return;
		}
		this.forecastingTimes = 0;
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(projectDir
				+ "user_profile/Forecasting_Recorder.txt", true));
		
		// 預測步驟，計算兩兩邊之間的距離加總，如果總距離小於門檻值，相近的兩點即會產生連接的邊
		for (TopicNode n : this.topics.values()) {
			for (TopicNode neighborOfN : this.topicCRGraph.getNeighbors(n)) {
				for (TopicNode n2 : this.topicCRGraph.getNeighbors(neighborOfN)) {
					if ((this.topicCRGraph.findEdge(n2, n)) == null) {
						CEdge<TopicNode> edge = this.topicCRGraph.findEdge(n, neighborOfN);
						CEdge<TopicNode> anotherEdge = this.topicCRGraph.findEdge(
								neighborOfN, n2);
						if (edge.getCoScore() + anotherEdge.getCoScore() <= this.topic_close_threshold) {
							CEdge<TopicNode> newEdge;
							double maxOfFreq = Math.max(sum_topic_freq.get(n.getId()),
										sum_topic_freq.get(n2.getId()));
							double minOfFreq = Math.min(sum_topic_freq.get(n.getId()),sum_topic_freq.get(n2.getId()));
							
							double should = maxOfFreq - ((edge.getCoScore() + anotherEdge.getCoScore()) * (sum_topics_relation - minOfFreq));
							// SIM反推
							// double should =
							// (TR_NGD.get(edge1)+TR_NGD.get(edge2))*(sum_topic_freq.get(new_edge.split("-")[0])+sum_topic_freq.get(new_edge.split("-")[1]))/2;
							String newID = String.valueOf(this.edges.size()+1);
							Pair<TopicNode> pair = new Pair<>(n,n2);
							newEdge = new CEdge<TopicNode>(pair,should);
							edges.put(newID, newEdge);
							TR.put(newEdge.getId(), should);
							// 預測紀錄
							bw2.write(edge+ "與"+ anotherEdge
									+ "相加的NGD為"+ (TR_NGD.get(edge.getId()) + TR_NGD.get(anotherEdge.getId())));
							bw2.newLine();
							bw2.write("新建立邊" + newEdge + " NGD為" + should);
							bw2.newLine();
							forecastingTimes++;
						}

					}//if find no Edge
				}//for all n2 (nK^2)
			}//for all neighborOfN(nk)
		}//for all n 
		bw2.close();




		// 將跑完預測的TR文件重新寫入
		BufferedWriter bw = new BufferedWriter(new FileWriter(projectDir
				+ "user_profile/user_profile_TR.txt"));
		bw.write(String.valueOf(topicSize)); // 目前主題數
		bw.newLine();
		bw.flush();
		for (String two_topic : TR.keySet()) {
			// 存放格式為 主題1-主題2,關係程度,此次更新編號
			bw.write(two_topic + "," + TR.get(two_topic));
			bw.newLine();
			bw.flush();
		}
		bw.close();

	}
	@Deprecated
	private String makeEdge(String edge1_v1, String edge1_v2, String edge2_v1, String edge2_v2){
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
	
	@Deprecated
	public void forecasting_cosine(String exp_dir){
		try {
			
			HashMap<String,double[]> TR_vector = new HashMap<String,double[]>(); //各主題向量
			HashMap<String,Double> TR_cosine = new HashMap<String,Double>(); //各主題間相似度
			double vector[];
			int node_num = topics.size();
			vector = new double[node_num];
			
			//建立各主題的向量
			for(int i=0;i<node_num;i++){
				for(int z=0;z<vector.length;z++){
					vector[z]=0.0;
				}
				for(int j=0;j<node_num;j++){
					if(TR.get(topics.get(i)+"-"+topics.get(j))!=null){
						vector[j]=TR.get(topics.get(i)+"-"+topics.get(j));
						//System.out.println(topic_list.get(i)+" & "+topic_list.get(j)+" = "+TR.get(topic_list.get(i)+"-"+topic_list.get(j)));
					}else if(TR.get(topics.get(j)+"-"+topics.get(i))!=null){
						vector[j]=TR.get(topics.get(j)+"-"+topics.get(i));
						//System.out.println(topic_list.get(j)+" & "+topic_list.get(i)+" = "+TR.get(topic_list.get(j)+"-"+topic_list.get(i)));
					}else{
						vector[j]=0.0;
					}
				}
				TR_vector.put(topics.get(i).toString(), vector.clone());
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/user_profile_TR.txt"));
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
	
	public Graph<TopicNode, CEdge> getTopicCooccurGrahp(){
		return this.topicCRGraph;
	}
}
