package tw.edu.ncu.CJ102.CoreProcess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import tw.edu.ncu.CJ102.Data.TopicTermGraph;


public class UserProfile {
	/**
	 * old user profile, trying to redesign it
	 * User profile data and decay
	 * @param args
	 */
	boolean isDynamicDecayMode;
	double DecayFactor_top;	//遺忘因子上限
	double DecayFactor_botton;//遺忘因子下限
	double DecayFactor_plus;//遺忘因子加速值
	double DecayFactor_minus; //遺忘因子減緩值
	double DecayFactor_init; //遺忘因子初始值*/
	
	double sum_avg_docTF = 0; //累計平均單文件總TF值
	double sum_avg_termTF = 0; //累計平均單字詞TF值
	double remove_rate = 0.6; //興趣去除比例
	double interest_remove_rate; //主題去除的累計平均單文件總TF值比例
	double term_remove_rate = remove_rate; //字詞去除的累計平均單字詞TF值比例
	static ArrayList<String> term_had_changed = new ArrayList<String>();
	int ConceptDrift_times = 0; //概念飄移次數
	
	HashSet<TopicTermGraph> topics = new HashSet<>();
	HashMap<String,Double> terms = new HashMap<>();//遺忘因子紀錄
	public final static String DEFUALT_USER_PROFILE = "user_profile";
	public final static String TDF_FILENAME = "user_profile_TDF";
	public final static String TR_FILENAME = "user_profile_TR";

	public UserProfile(boolean isDynamicDecayMode){
		this.isDynamicDecayMode = isDynamicDecayMode;
		if(this.isDynamicDecayMode){
			 DecayFactor_top = 0.55;
			 DecayFactor_botton = 0.02; 
			 DecayFactor_plus = 0.079; 
			 DecayFactor_minus = 0.075; 
			 DecayFactor_init = DecayFactor_plus*2;
		}else{
			 DecayFactor_top = 0.05;
			 DecayFactor_botton = 0.05;
			 DecayFactor_plus = 0; 
			 DecayFactor_minus = 0; 
			 DecayFactor_init = 0.05; 
		}
		this.setRemoveRate(0.6);//Default remove Rate
	}

	
	public HashSet<TopicTermGraph> getTopics() {
		return topics;
	}
	public void setTopics(HashSet<TopicTermGraph> topics) {
		this.topics = topics;
	}
	
	public HashMap<Integer,HashMap<String,Double>> add_user_profile_term(){
		
		return null;
		
	}
	/**
	 * 
	 * @param User_profile_term 原使用者模型(字詞)
	 * @param doc_term 文件內容資訊
	 * @param topic_mapping 主題的映射
	 * @return 更新後的使用者模型(字詞)
	 */
	public HashMap<Integer,HashMap<String,Double>> add_user_profile_term(HashMap<Integer,HashMap<String,Double>> User_profile_term, HashMap<Integer,HashMap<String,Double>> doc_term, HashMap<Integer,Integer> topic_mapping){
		//將個文件主題的字詞TF分數依據得到的主題映射關係存入使用者模型中
		for(int i: doc_term.keySet()){
			//先取出兩個相對映的主題
			HashMap<String, Double> doc_topic = new HashMap<String, Double>(doc_term.get(i));
			HashMap<String, Double> User_profile_topic; //使用者Topic
			if(User_profile_term.get(topic_mapping.get(i))==null){
				User_profile_topic = new HashMap<String, Double>();
				User_profile_topic.put("temp", 0.0); //新主題的初始化
				term_had_changed.add("temp");
			}else{
				User_profile_topic = new HashMap<String, Double>( User_profile_term.get(topic_mapping.get(i)));
			}
			
			//將主題內的字詞分數存入使用者模型 (更新)
			for(String s: doc_topic.keySet()){
				if(!term_had_changed.contains(s)){
					term_had_changed.add(s); //將字詞存入，以便每天字詞分數與遺忘因子總整理時提出
				}
				if(User_profile_topic.get(s)!=null){ //主題的舊字詞就以累加的方式存入
					User_profile_topic.put(s,User_profile_topic.get(s)+doc_topic.get(s)); //更新此字詞分數
				}else{ //主題的新字詞就直接存入
					User_profile_topic.put(s,doc_topic.get(s));
				}
			}
			System.out.print("更新使用者模型主題 "+topic_mapping.get(i)+",字詞數量為"+User_profile_topic.size()+"\n");
			//將更新後的模型放回去
			User_profile_term.put(topic_mapping.get(i), new HashMap(User_profile_topic));
		}
		return User_profile_term;
	}
	
	//每日需執行的使用者模型更新，包含遺忘因子的作用與主題、字詞的去除
	//參數為實驗資料匣位置名稱, 使用者模型
	public HashMap<Integer,HashMap<String,Double>> update_OneDayTerm_Decay_Factor(String exp_dir, HashMap<Integer,HashMap<String,Double>> User_profile_term){
		//更新模型時也要順變更新該字詞的遺忘因子，先讀取遺忘因子的紀錄文件 PS. TDF = Term Decay Factor
		String line="";
		//double sum_decayfactor; //紀錄某字詞應該乘上的遺忘因子
		int this_term_src; //紀錄該次處理的字詞編號
		//int this_term_update_time; //紀錄該次處理的字詞的更新時間
		double this_term_tf = 0; //紀錄該次處理的某主題的某字詞的TF分數
		double new_this_term_tf = 0; //更新後的的某主題的某字詞的TF分數
		try{
			BufferedReader br = new BufferedReader(new FileReader(exp_dir+"user_profile/user_profile_TDF.txt"));
			//紀錄字詞的編號，等等便於提出權重與更新時間點
			HashMap<String,Integer> terms_info = new HashMap<String,Integer>(); //字詞
			int update_time = Integer.valueOf(br.readLine()); //目前為止的更新編號
			update_time++; //這次的更新編號，就是上一次的再加1
			ArrayList<Double> term_decayfactor = new ArrayList<Double>(); //字詞遺忘因子陣列
			ArrayList<Integer> term_update_time = new ArrayList<Integer>(); //字詞更新時間點陣列
			ArrayList<Double> term_sum_score = new ArrayList<Double>(); //字詞在模型內的總分(不分主題統計)
			int term_src=0; //暫時的字詞編號，便於連結term_decayfactor與term_update_time該字詞的對映位置
			while((line=br.readLine())!=null){
				terms_info.put(line.split(",")[0], term_src);
				term_decayfactor.add(Double.valueOf(line.split(",")[1]));
				term_update_time.add(Integer.valueOf(line.split(",")[2]));
				term_sum_score.add(Double.valueOf(line.split(",")[3]));
				term_src++;
			}
			
			//先將新增的(原user_profile_TDF.txt沒有的)字詞加入
			for(int i=0; i<term_had_changed.size(); i++){
				if(terms_info.get(term_had_changed.get(i))==null){
					//System.out.println("新增字詞"+term_had_changed.get(i)+"進入TDF");
					terms_info.put(term_had_changed.get(i), term_src);
					term_decayfactor.add(DecayFactor_init);
					term_update_time.add(update_time);
					term_sum_score.add(0.0);
					term_src++;
				}
			}
			
			//更新使用者模型所有字詞的分數與遺忘因子
			for(int i: User_profile_term.keySet()){
				for(String s: User_profile_term.get(i).keySet()){
					
					this_term_src = terms_info.get(s);
					term_update_time.set(this_term_src, update_time); //更新字詞更新時間至目前時間
					//更新字詞TF分數
					this_term_tf = User_profile_term.get(i).get(s);
					new_this_term_tf = this_term_tf * (1-term_decayfactor.get(this_term_src));
					System.out.println("更新使用者模型主題"+i+"字詞"+s+"遺忘因子"+term_decayfactor.get(this_term_src)+"更新後分數為"+new_this_term_tf);
					//如果是原有字詞就更新字詞的總分數為目前分數-舊分數+新分數，新字詞的總分數則直接存入new_this_term_tf
					if(term_sum_score.get(this_term_src)==0){
						term_sum_score.set(this_term_src,new_this_term_tf);
					}else{
						term_sum_score.set(this_term_src, term_sum_score.get(this_term_src)-this_term_tf+new_this_term_tf);
					}
					User_profile_term.get(i).put(s, new_this_term_tf);
					//更新遺忘因子，如果有發生改變得就增加，沒有就減少
					if(term_had_changed.contains(s)){
						term_decayfactor = update_Term_Decay_Factor(terms_info,term_decayfactor,"plus",s);
					}else{
						term_decayfactor = update_Term_Decay_Factor(terms_info,term_decayfactor,"minus",s);
					}
				}
			}
			
			/*//更新當天有變動的字詞的資訊
			for(int i=0; i<term_had_changed.size(); i++){
				//更新某字詞的遺忘因子，順便累計更新分數所需要乘上的數字
				sum_decayfactor = 1;
				if(terms_info.get(term_had_changed.get(i))!=null){
					this_term_src = terms_info.get(term_had_changed.get(i));
					this_term_update_time = term_update_time.get(this_term_src);
					for(int p = this_term_update_time; this_term_update_time<(update_time-1); this_term_update_time++){
						sum_decayfactor = sum_decayfactor*term_decayfactor.get(this_term_src);
						term_decayfactor = update_Term_Decay_Factor(terms_info,term_decayfactor,"minus",term_had_changed.get(i));
					}
					term_decayfactor = update_Term_Decay_Factor(terms_info,term_decayfactor,"plus",term_had_changed.get(i));
					term_update_time.set(this_term_src, update_time); //更新字詞更新時間至目前時間
					
					for(int j: User_profile_term.keySet()){
						//如果某個字詞存在於某個模型主題中的話，我們就更新他的分數
						if(User_profile_term.get(j).get(term_had_changed.get(i))!=null){
							this_term_tf = User_profile_term.get(j).get(term_had_changed.get(i));
							new_this_term_tf = this_term_tf * sum_decayfactor;
							//更新的分數為目前分數-舊分數+新分數
							term_sum_score.set(this_term_src, term_sum_score.get(this_term_src)-this_term_tf+new_this_term_tf);
							User_profile_term.get(j).put(term_had_changed.get(i), new_this_term_tf);
						}
					}
				}else{
					terms_info.put(term_had_changed.get(i), term_src);
					term_decayfactor.add(DecayFactor_plus);
					term_update_time.add(update_time);
					for(int j: User_profile_term.keySet()){
						if(User_profile_term.get(j).get(term_had_changed.get(i))!=null){
							new_this_term_tf = new_this_term_tf + User_profile_term.get(j).get(term_had_changed.get(i));
							User_profile_term.get(j).put(term_had_changed.get(i), new_this_term_tf);
						}
					}
					term_sum_score.add(new_this_term_tf);
				}
			}*/
			
			//輸出新的遺忘因子紀錄文件
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/user_profile_TDF.txt"));
			bw.write(""+Integer.valueOf(update_time));
			bw.newLine();
			bw.flush();
			for(String s: terms_info.keySet()){
				//System.out.println("....."+s+".....");
				//存放格式為 字詞,字詞遺忘因子,此次更新編號,字詞總TF分數
				bw.write(s+","+term_decayfactor.get(terms_info.get(s))+","+term_update_time.get(terms_info.get(s))+","+term_sum_score.get(terms_info.get(s)));
				bw.newLine();
				bw.flush();
			}
			bw.close();
			term_had_changed.clear();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return User_profile_term;
	}
	
	//代轉無operate參數的呼叫，通常使用是因為使用的是新字詞，operate輸入的是什麼不重要
	public ArrayList<Double> update_Term_Decay_Factor(HashMap<String,Integer> terms_info, ArrayList<Double> term_decayfactor, String update_term){
		return update_Term_Decay_Factor(terms_info,term_decayfactor,"plus",update_term);
	}
	//更新遺忘因子，輸入參數為遺忘字詞與編號對映資訊, 目前遺忘因子, 欲加速或減速, 目標字詞編號, 準備更新的字詞編號
	public ArrayList<Double> update_Term_Decay_Factor(HashMap<String,Integer> terms_info, ArrayList<Double> term_decayfactor, String operate, String update_term){
		if(terms_info.get(update_term)!=null){ //模型舊有字詞
			int this_time_term_src = terms_info.get(update_term); //這是更新的字詞編號
			//System.out.println(update_term+"為模型舊有字詞，目前遺忘因子為"+term_decayfactor.get(this_time_term_src));
			if(operate=="minus"){
				//減少遺忘因子
				//如果減少遺忘因子會低於下限就設為下限，否則就直接遞減
				if((term_decayfactor.get(this_time_term_src)-DecayFactor_minus)<DecayFactor_botton){
					term_decayfactor.set(this_time_term_src, DecayFactor_botton);
				}else{
					term_decayfactor.set(this_time_term_src, term_decayfactor.get(this_time_term_src)-DecayFactor_minus);
				}
			}else if(operate=="plus"){
				//因為本次此字詞又增加了分數所以遺忘因子跟著提高，如果提高後因子會高於上限就設為上限，否則就直接增加
				if((term_decayfactor.get(this_time_term_src)+DecayFactor_plus)>DecayFactor_top){
					term_decayfactor.set(this_time_term_src, DecayFactor_top);
				}else{
					term_decayfactor.set(this_time_term_src, term_decayfactor.get(this_time_term_src)+DecayFactor_plus);
				}
			}
		}else{
			//System.out.println(update_term+"為模型新字詞");
			term_decayfactor.add(DecayFactor_plus);
		}
		return term_decayfactor;
	}
	
	//初始化累計平均單文件總TF值
	public void init_docTF(){
		sum_avg_docTF = 0;
	}
	//興趣去除的累計平均單文件總TF值，輸入參數為單份文件的總TF值
	public void sum_avg_docTF(double docTF){
		sum_avg_docTF = (sum_avg_docTF+docTF)/2;
	}
	//興趣去除程序，輸入參數為實驗資料匣名稱, 使用者模型, 更新日
	public HashMap<Integer,HashMap<String,Double>> interest_remove_doc(String exp_dir, HashMap<Integer,HashMap<String,Double>> User_profile_term, int day){
		double removal_threshold = sum_avg_docTF*interest_remove_rate;
		double topic_sum_score; //模型內主題的總分
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/interst_Remove_Recorder.txt",true));
			System.out.println("目前主題移除門檻值為"+removal_threshold);
			for (Iterator iterator = User_profile_term.keySet().iterator(); iterator.hasNext();){
				int j = (Integer)iterator.next();
				topic_sum_score = 0;
				for(String s: User_profile_term.get(j).keySet()){
					topic_sum_score+=User_profile_term.get(j).get(s);
				}
				if(topic_sum_score<removal_threshold){
					bw.write("第"+day+"日，主題"+j+"分數為 "+topic_sum_score+" 因小於門檻值 "+removal_threshold+" 被移除");
					bw.newLine();
					bw.flush();
					System.out.println("主題"+j+"分數為"+topic_sum_score+"因小於門檻值被移除");
					iterator.remove();
					User_profile_term.remove(j); //小於門檻值就移除掉
					remove_topic(exp_dir, String.valueOf(j));
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return User_profile_term;
	}
	
	//初始化累計平均單字詞TF值
	public void init_termTF(){
		sum_avg_termTF = 0;
	}
	//字詞去除的累計平均單字詞TF值，輸入參數為單字詞的TF值
	public void sum_avg_termTF(double docTF, int term_count){
		sum_avg_termTF = (sum_avg_termTF+(docTF/term_count))/2;
	}
	//字詞去除程序，輸入參數為實驗資料匣名稱, 使用者模型, 更新日
	public HashMap<Integer,HashMap<String,Double>> interest_remove_term(String exp_dir, HashMap<Integer,HashMap<String,Double>> User_profile_term, int day){
		double removal_threshold = sum_avg_termTF*term_remove_rate;
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/interst_Remove_Recorder.txt",true));
			System.out.println("目前字詞移除門檻值為"+removal_threshold);
			for(int j: User_profile_term.keySet()){
				for (Iterator iterator = User_profile_term.get(j).keySet().iterator(); iterator.hasNext();){
					String s = (String)iterator.next();
					if(User_profile_term.get(j).get(s)<removal_threshold){
						bw.write("第"+day+"日，主題"+j+"的字詞"+s+"分數為 "+User_profile_term.get(j).get(s)+" 因小於門檻值 "+removal_threshold+" 被移除");
						bw.newLine();
						bw.flush();
						System.out.println("主題"+j+"的字詞"+s+"分數為"+User_profile_term.get(j).get(s)+"因小於門檻值被移除");
						iterator.remove();
						User_profile_term.get(j).remove(s); //小於門檻值就移除掉
					}
				}
			}
			bw.close();
			//移除掉使用者模型內已不存在的字詞
			remove_term(exp_dir, User_profile_term);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return User_profile_term;
	}
	
	//輸出每次的使用者模型結果，輸入參數為實驗資料匣名稱, 使用者模型序列編號, 使用者模型
	public void out_new_user_profile(String exp_dir, int preprocess_times, HashMap<Integer,HashMap<String,Double>> User_profile_term){
		BufferedWriter bw; //bw用來寫user_profile
		//暫存字詞與分數
		HashMap<String,Double> topic_term = new HashMap<String,Double>();
		try {
			bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/user_profile_"+preprocess_times+".txt"));
			for(int i: User_profile_term.keySet()){
				topic_term.clear();
				topic_term = new HashMap(User_profile_term.get(i));
				//System.out.print("使用者模型主題 "+i+",字詞數量為"+topic_term.size()+"\n");
				for(String s: topic_term.keySet()){
					//儲存格式為 字詞,字詞分數,字詞主題編號
					//System.out.print("建立使用者模型主題 "+i+",字詞"+s+",分數"+topic_term.get(s)+"\n");
					bw.write(s+","+topic_term.get(s)+","+i);
					bw.newLine();
					bw.flush();
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//從user_profile_TDF.txt中移除使用者模型內已不存在的字詞
	public void remove_term(String exp_dir, HashMap<Integer,HashMap<String,Double>> User_profile_term){
		try {
			String line="", v;
			ArrayList<String> TDF = new ArrayList<String>();
			BufferedReader br;
			br = new BufferedReader(new FileReader(exp_dir+"user_profile/user_profile_TDF.txt"));
			String un = br.readLine(); //第一行不重要
			boolean exis = false;
			while((line=br.readLine())!=null){
				v=line.split(",")[0];
				for(int i: User_profile_term.keySet()){
					if(User_profile_term.get(i).containsKey(v)){
						exis = true;
					}
				}
				if(exis){
					TDF.add(line);
				}
				exis=false;
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/user_profile_TDF.txt"));
			bw.write(un);
			bw.newLine();
			bw.flush();
			for(int i=0;i<TDF.size();i++){
				bw.write(TDF.get(i));
				bw.newLine();
				bw.flush();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//從user_profile_TR.txt中移除被刪除的主題
	public void remove_topic(String exp_dir, String old_topic){
		try {
			String line="", v1, v2;
			ArrayList<String> TR = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(exp_dir+"user_profile/user_profile_TR.txt"));
			String max_topic_index = br.readLine(); //第一行為最大主題編號
			while((line=br.readLine())!=null){
				TR.add(line);
			}
			br.close();
			for(int i=0;i<TR.size();i++){
				v1 = TR.get(i).split(",")[0].split("-")[0];
				v2 = TR.get(i).split(",")[0].split("-")[1];
				if(v1.equals(old_topic) || v2.equals(old_topic)){
					ConceptDrift_times++;
					TR.remove(i);
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir+"user_profile/user_profile_TR.txt"));
			bw.write(max_topic_index);
			bw.newLine();
			bw.flush();
			for(int i=0;i<TR.size();i++){
				bw.write(TR.get(i));
				bw.newLine();
				bw.flush();
			}
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int get_ConceptDrift_times(){
		return ConceptDrift_times;
	}
	
	public void setRemoveRate(double value){
		this.remove_rate = value;
		this.interest_remove_rate = this.remove_rate;
		this.term_remove_rate = this.remove_rate;
	}
	
	public void store(String savePlace){
		try (ObjectOutputStream tDRWriter = new ObjectOutputStream(
				new FileOutputStream(Paths.get(savePlace,DEFUALT_USER_PROFILE,TDF_FILENAME).toString()));
				ObjectOutputStream tRWriter = new ObjectOutputStream(
						new FileOutputStream(Paths.get(savePlace,DEFUALT_USER_PROFILE, TR_FILENAME).toString()));) {
			//tDR:紀錄遺忘因子文件, TR:主題關係文件
			// 存放格式為 字詞,字詞遺忘因子,此次更新編號,字詞總TF分數
			tDRWriter.writeObject(terms);

			// 初始化主題關係文件
			tRWriter.writeObject(this.topics);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
