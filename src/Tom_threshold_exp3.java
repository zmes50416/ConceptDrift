import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Tom_threshold_exp3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Tom_threshold_exp3 TTE3 = new Tom_threshold_exp3();
		//TTE3.forecasting_NGDorSIM("Tom實驗/主題緊密門檻值實驗/exp_6_TCR_close/");
		TTE3.forecasting_cosine("Tom實驗/主題緊密門檻值實驗/exp_6_TCR_close/");
	}
	
	public void forecasting_NGDorSIM(String exp_dir){
		try {
			//儲存分析發現兩邊之間只有兩點有相連的結果
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(exp_dir+"TR_2con_result_SIM.txt"));
			//儲存分析發現兩邊之間三點都有相連的結果
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(exp_dir+"TR_3con_result_SIM.txt"));
			//讀取TR檔
			BufferedReader br = new BufferedReader(new FileReader(exp_dir+"user_porfile/user_profile_TR.txt"));
			int con2_num=0, con3_num=0;
			double sum_con2=0, sum_con3=0;
			int how_many_topic = Integer.valueOf(br.readLine()); //得知目前主題數
			String topics;
			String line;
			double topic_relation;
			double sum_topics_relation=0;
			HashMap<String,Double> TR = new HashMap<String,Double>(); //讀取出來的主題關係
			HashMap<String,Double> TR_NGD = new HashMap<String,Double>(); //NGD計算後的主題關係
			HashMap<String,Double> sum_topic_freq = new HashMap<String,Double>(); //各主題的出現
			ArrayList<String> topic_list = new ArrayList<String>(); //topic列表
			
			while((line=br.readLine())!=null){
				topics = line.split(",")[0];
				//System.out.println("line="+line);
				//將還沒加進topic列表的字詞加入
				if(!topic_list.contains(topics.split("-")[0])){
					topic_list.add(topics.split("-")[0]);
				}
				if(!topic_list.contains(topics.split("-")[1])){
					topic_list.add(topics.split("-")[1]);
				}
				//累計主題的出現次數
				if(topics.split("-")[0].equals(topics.split("-")[1])){ //自己對到自己就只存一次數值，避免重複累加
					//System.out.println(topics.split("-")[0]+"++");
					if(sum_topic_freq.get(topics.split("-")[0])!=null){
						sum_topic_freq.put(topics.split("-")[0], sum_topic_freq.get(topics.split("-")[0])+Double.valueOf(line.split(",")[1]));
					}else{
						sum_topic_freq.put(topics.split("-")[0], Double.valueOf(line.split(",")[1]));
					}
				}else{
					//System.out.println(topics.split("-")[0]+"++");
					//System.out.println(topics.split("-")[1]+"++");
					if(sum_topic_freq.get(topics.split("-")[0])!=null){
						sum_topic_freq.put(topics.split("-")[0], sum_topic_freq.get(topics.split("-")[0])+Double.valueOf(line.split(",")[1]));
					}else{
						sum_topic_freq.put(topics.split("-")[0], Double.valueOf(line.split(",")[1]));
					}
					if(sum_topic_freq.get(topics.split("-")[1])!=null){
						sum_topic_freq.put(topics.split("-")[1], sum_topic_freq.get(topics.split("-")[1])+Double.valueOf(line.split(",")[1]));
					}else{
						sum_topic_freq.put(topics.split("-")[1], Double.valueOf(line.split(",")[1]));
					}
				}
				
				topic_relation = Double.valueOf(line.split(",")[1]);
				sum_topics_relation+=topic_relation;
				TR.put(topics, topic_relation);
			}
			br.close();
			
			System.out.println("所有主題的總出現次數為"+sum_topics_relation);
			for(String s: sum_topic_freq.keySet()){
				System.out.println(s+"的出現次數為"+sum_topic_freq.get(s));
			}
			
			//計算各主題關係的NGD值，
			for(String two_topic: TR.keySet()){
				//如果紀錄的是非自己主題的關係就需要計算兩相異主題間的NGD距離
				if(!two_topic.split("-")[0].equals(two_topic.split("-")[1])){
					double x=sum_topic_freq.get(two_topic.split("-")[0]); //第一個主題的出現次數
					System.out.println(two_topic.split("-")[0]+"="+x);
					double y=sum_topic_freq.get(two_topic.split("-")[1]); //第二個主題的出現次數
					System.out.println(two_topic.split("-")[1]+"="+y);
					double xy=TR.get(two_topic); //第一、二主題的共現次數
					System.out.println(two_topic+"="+xy);
					
					//NGD方法
					/*double NGD=(Math.max(x, y) - xy) / (sum_topics_relation - Math.min(x, y));
					System.out.println("sum_topics_relation="+sum_topics_relation);
					System.out.println("NGD="+NGD);
					if (xy == 0){
						//NGD = 1;//避免無限大
					}
					if (NGD > 1){
						//NGD = 1;
					}
					if (NGD < 0){
						//NGD = 0;
					}*/
										
					//簡單重疊比例方法(SIM)
					double NGD=(2*xy)/(x+y);
					
					TR_NGD.put(two_topic, NGD);
					System.out.println("邊"+two_topic+"的分佈相似度值為"+NGD);
				}
			}
			
			//預測步驟，計算兩兩邊之間的距離加總，如果總距離小於門檻值，相近的兩點即會產生連接的邊
			String edge1_v1, edge1_v2; //第一個邊的第一個節點, 第一個邊的第二個節點
			String edge2_v1, edge2_v2; //第二個邊的第一個節點, 第二個邊的第二個節點
			String maybe_edge3; //可能的第三個邊
			boolean door = false;
			boolean con = false;
			for(String edge1: TR_NGD.keySet()){
				edge1_v1 = edge1.split("-")[0];
				edge1_v2 = edge1.split("-")[1];
				for(String edge2: TR_NGD.keySet()){
					con=false;
					maybe_edge3="";
					if(edge1.equals(edge2)){
						door = true; //減少重複計算的可能
					}
					if(door && !edge1.equals(edge2)){ //自己跟自己不用計算
						edge2_v1 = edge2.split("-")[0];
						edge2_v2 = edge2.split("-")[1];
						//兩個邊中有其中一個節點是互相連接的，才查看另外兩點是否有相連
						if(edge2_v1.equals(edge1_v1)){
							//組成第三個邊
							maybe_edge3 = make_edge3(edge2_v2,edge1_v2);
							con=true;
						}else if(edge2_v1.equals(edge1_v2)){
							maybe_edge3 = make_edge3(edge2_v2,edge1_v1);
							con=true;
						}else if(edge2_v2.equals(edge1_v1)){
							maybe_edge3 = make_edge3(edge2_v1,edge1_v2);
							con=true;
						}else if(edge2_v2.equals(edge1_v2)){
							maybe_edge3 = make_edge3(edge2_v1,edge1_v1);
							con=true;
						}
						if(con){
							if(TR_NGD.containsKey(maybe_edge3)){ //第三邊是否早已存在
								//紀錄三邊兩兩相加長度	
								bw3.write("edge1+edge2 = "+edge1+"+"+edge2+","+String.valueOf(TR_NGD.get(edge1)+TR_NGD.get(edge2)));
								bw3.newLine();
								sum_con3 = sum_con3 + TR_NGD.get(edge1)+TR_NGD.get(edge2);
								bw3.write("edge1+maybe_edge3 = "+edge1+"+"+maybe_edge3+","+String.valueOf(TR_NGD.get(edge1)+TR_NGD.get(maybe_edge3)));
								bw3.newLine();
								sum_con3 = sum_con3 + TR_NGD.get(edge1)+TR_NGD.get(maybe_edge3);
								bw3.write("edge2+maybe_edge3 = "+edge2+"+"+maybe_edge3+","+String.valueOf(TR_NGD.get(edge2)+TR_NGD.get(maybe_edge3)));
								bw3.newLine();
								bw3.flush();
								sum_con3 = sum_con3 + TR_NGD.get(edge2)+TR_NGD.get(maybe_edge3);
								con3_num = con3_num + 3;
							}else{
								//紀錄三邊兩兩相加長度	
								bw2.write("edge1+edge2 = "+edge1+"+"+edge2+","+String.valueOf(TR_NGD.get(edge1)+TR_NGD.get(edge2)));
								bw2.newLine();
								bw2.flush();
								sum_con2 = sum_con2 + TR_NGD.get(edge1)+TR_NGD.get(edge2);
								con2_num = con2_num + 1;
							}
						}
					}
				}
				door=false;
			}
			bw2.write("符合兩邊之間只有兩點有相連的結果的邊總長為 "+sum_con2);
			bw2.newLine();
			bw2.write("邊數 "+con2_num);
			bw2.newLine();
			bw2.write("平均邊長 "+(sum_con2/con2_num));
			bw2.newLine();
			bw2.flush();
			bw3.write("符合兩邊之間三點都有相連的結果的邊總長為 "+sum_con3);
			bw3.newLine();
			bw3.write("邊數 "+con3_num);
			bw3.newLine();
			bw3.write("平均邊長 "+(sum_con3/con3_num));
			bw3.newLine();
			bw3.flush();
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
	
	public String make_edge3(String v1, String v2){
		String new_edge="";
		int int_edge1_v1 = Integer.valueOf(v1);
		int int_edge1_v2 = Integer.valueOf(v2);
		if(int_edge1_v1 < int_edge1_v2){
			new_edge = int_edge1_v1 + "-" + int_edge1_v2;
		}else{
			new_edge = int_edge1_v2 + "-" + int_edge1_v1;
		}
		return new_edge;
	}
	
	public void forecasting_cosine(String exp_dir){
		try {
			//儲存分析發現兩邊之間只有兩點有相連的結果
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(exp_dir+"TR_2con_result_CONSINE.txt"));
			//儲存分析發現兩邊之間三點都有相連的結果
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(exp_dir+"TR_3con_result_CONSINE.txt"));
			BufferedReader br = new BufferedReader(new FileReader(exp_dir+"user_porfile/user_profile_TR.txt"));
			int how_many_topic = Integer.valueOf(br.readLine()); //得知目前主題數
			String topics;
			String line;
			double topic_relation;
			HashMap<String,Double> TR = new HashMap<String,Double>(); //讀取出來的主題關係
			HashMap<String,double[]> TR_vector = new HashMap<String,double[]>(); //各主題向量
			HashMap<String,Double> TR_cosine = new HashMap<String,Double>(); //各主題間相似度
			ArrayList<String> topic_list = new ArrayList<String>(); //topic列表
			double vector[];
			int con2_num=0, con3_num=0;
			double sum_con2=0, sum_con3=0;
			
			while((line=br.readLine())!=null){
				topics = line.split(",")[0];
				//System.out.println("line="+line);
				//將還沒加進topic列表的字詞加入
				if(!topic_list.contains(topics.split("-")[0])){
					topic_list.add(topics.split("-")[0]);
				}
				if(!topic_list.contains(topics.split("-")[1])){
					topic_list.add(topics.split("-")[1]);
				}
				topic_relation = Double.valueOf(line.split(",")[1]);
				TR.put(topics, topic_relation);
			}
			br.close();
			
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
			ConceptDrift_Forecasting CDF = new ConceptDrift_Forecasting();
			for(String two_topic: TR.keySet()){
				//如果紀錄的是非自己主題的關係就需要計算兩相異主題間的NGD距離
				if(!two_topic.split("-")[0].equals(two_topic.split("-")[1])){
					System.out.println(two_topic+" = "+CDF.similarityCalculator(TR_vector.get(two_topic.split("-")[0]),TR_vector.get(two_topic.split("-")[1]),"cosine"));
					TR_cosine.put(two_topic,CDF.similarityCalculator(TR_vector.get(two_topic.split("-")[0]),TR_vector.get(two_topic.split("-")[1]),"cosine"));
				}
			}
			
			//預測步驟，計算兩兩邊之間的距離加總，如果總距離小於門檻值，相近的兩點即會產生連接的邊
			String edge1_v1, edge1_v2; //第一個邊的第一個節點, 第一個邊的第二個節點
			String edge2_v1, edge2_v2; //第二個邊的第一個節點, 第二個邊的第二個節點
			String maybe_edge3; //可能的第三個邊
			boolean door = false;
			boolean con = false;
			for(String edge1: TR_cosine.keySet()){
				edge1_v1 = edge1.split("-")[0];
				edge1_v2 = edge1.split("-")[1];
				for(String edge2: TR_cosine.keySet()){
					con=false;
					maybe_edge3="";
					if(edge1.equals(edge2)){
						door = true; //減少重複計算的可能
					}
					if(door && !edge1.equals(edge2)){ //自己跟自己不用計算
						edge2_v1 = edge2.split("-")[0];
						edge2_v2 = edge2.split("-")[1];
						//兩個邊中有其中一個節點是互相連接的，才查看另外兩點是否有相連
						if(edge2_v1.equals(edge1_v1)){
							//組成第三個邊
							maybe_edge3 = make_edge3(edge2_v2,edge1_v2);
							con=true;
						}else if(edge2_v1.equals(edge1_v2)){
							maybe_edge3 = make_edge3(edge2_v2,edge1_v1);
							con=true;
						}else if(edge2_v2.equals(edge1_v1)){
							maybe_edge3 = make_edge3(edge2_v1,edge1_v2);
							con=true;
						}else if(edge2_v2.equals(edge1_v2)){
							maybe_edge3 = make_edge3(edge2_v1,edge1_v1);
							con=true;
						}
						if(con){
							if(TR_cosine.containsKey(maybe_edge3)){ //第三邊是否早已存在
								//紀錄三邊兩兩相加長度	
								bw3.write("edge1+edge2 = "+edge1+"+"+edge2+","+String.valueOf(TR_cosine.get(edge1)+TR_cosine.get(edge2)));
								bw3.newLine();
								sum_con3 = sum_con3 + TR_cosine.get(edge1)+TR_cosine.get(edge2);
								bw3.write("edge1+maybe_edge3 = "+edge1+"+"+maybe_edge3+","+String.valueOf(TR_cosine.get(edge1)+TR_cosine.get(maybe_edge3)));
								bw3.newLine();
								sum_con3 = sum_con3 + TR_cosine.get(edge1)+TR_cosine.get(maybe_edge3);
								bw3.write("edge2+maybe_edge3 = "+edge2+"+"+maybe_edge3+","+String.valueOf(TR_cosine.get(edge2)+TR_cosine.get(maybe_edge3)));
								bw3.newLine();
								bw3.flush();
								sum_con3 = sum_con3 + TR_cosine.get(edge2)+TR_cosine.get(maybe_edge3);
								con3_num = con3_num + 3;
							}else{
								//紀錄三邊兩兩相加長度	
								bw2.write("edge1+edge2 = "+edge1+"+"+edge2+","+String.valueOf(TR_cosine.get(edge1)+TR_cosine.get(edge2)));
								bw2.newLine();
								bw2.flush();
								sum_con2 = sum_con2 + TR_cosine.get(edge1)+TR_cosine.get(edge2);
								con2_num = con2_num + 1;
							}
						}
					}
				}
				door=false;
			}
			bw2.write("符合兩邊之間只有兩點有相連的結果的邊總長為 "+sum_con2);
			bw2.newLine();
			bw2.write("邊數 "+con2_num);
			bw2.newLine();
			bw2.write("平均邊長 "+(sum_con2/con2_num));
			bw2.newLine();
			bw2.flush();
			bw3.write("符合兩邊之間三點都有相連的結果的邊總長為 "+sum_con3);
			bw3.newLine();
			bw3.write("邊數 "+con3_num);
			bw3.newLine();
			bw3.write("平均邊長 "+(sum_con3/con3_num));
			bw3.newLine();
			bw3.flush();
			
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
}
