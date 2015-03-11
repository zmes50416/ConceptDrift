import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Tom_exp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader br,br2,br3,br4; //br用來讀目前的user_profile，br2用來讀文件，br3用來直接指定訓練文件的順序，br4用來直接指定測試文件的順序
		BufferedWriter bw2,bw3; //bw2用來紀錄讀取文件的順序，bw3用來紀錄遺忘因子文件與主題關係文件
		BufferedWriter EfficacyMeasure_w, Time_w; //EfficacyMeasure_w用來紀錄系統效能，Time_w用來紀錄系統執行時間
		double train_sum_time_read_doc = 0, test_sum_time_read_doc = 0, train_time_read_doc = 0, test_time_read_doc = 0; //讀取文件的總時間
		double train_sum_time_read_profile = 0, test_sum_time_read_profile = 0, train_time_read_profile = 0, test_time_read_profile = 0; //讀取模型的總時間
		double train_sum_time_topic_mapping = 0, test_sum_time_topic_mapping = 0, train_time_topic_mapping = 0, test_time_topic_mapping = 0; //主題映射的總時間
		double train_sum_time_add_user_profile = 0, train_time_add_user_profile = 0; //新增興趣到模型的總時間(只有訓練部份有)
		double test_sum_time_Comper_relateness = 0, test_time_Comper_relateness = 0; //相關判定的總時間(只有測試部份有)
		double sum_time_update_OneDayTerm = 0, time_update_OneDayTerm = 0; //使用遺忘因子更新模型的總時間(每天做一次)
		long StartTime;
		long EndTime;
		//記錄user_profile各主題的主題字詞，格式<主題編號,<主題字詞,字詞分數>>
		HashMap<Integer,HashMap<String,Double>> User_profile_term = new HashMap<Integer,HashMap<String,Double>>();
		//記錄文件各主題的主題字詞，格式<主題編號,<主題字詞,字詞分數>>
		HashMap<Integer,HashMap<String,Double>> doc_term = new HashMap<Integer,HashMap<String,Double>>();
		//暫存字詞與分數
		HashMap<String,Double> topic_term = new HashMap<String,Double>();
		//儲存文件與模型的主題映射
		HashMap<Integer,Integer> topic_mapping = new HashMap<Integer,Integer>();
		ArrayList<String> profile_label = new ArrayList<String>(); //儲存用來訓練的標籤答案
		int TP, TN, FP, FN;
		TOM_ComperRelateness TC = new TOM_ComperRelateness();
		Go_Training_Tom GTT = new Go_Training_Tom();
		Go_User_profile GU = new Go_User_profile();
		ConceptDrift_Forecasting CDF = new ConceptDrift_Forecasting();
		String dir = "exp_acq_DecayFactor_fs_fix0.05/";
		int days = 15; //Reuters-實驗天數
		int train_days = 0, test_days = -1; //citeulike-實驗天數，0為全部，-1為不使用
		String real_people = "626838af45efa5ca465683ab3b3f303e"; //選擇citeulike資料流
		FileWriter FWrite;
		BufferedWriter Comper_log; //Comper_log用來紀錄主題映射的數值
		int preprocess_times = 0; //某一天的第X篇文章
		int doc_term_count; //單文件內的字詞數量
		double docTF; //單文件的TF值
		double doc_ngd;
		String line = "";
		String topicname=""; //儲存標籤答案
		int train_times,test_times; //設定每次訓練、測試互換的篇數
		TC.init_EfficacyMeasure();
		
		String train_topics[] = {"acq"};
		String test_topics[] = {"acq","earn","crude","coffee","sugar","trade","cocoa"};
		//ArrayList<File> test_fs = new ArrayList<File>();
		//int test_fs_num;
		
		try {
			new File(dir).mkdirs(); //創造出實驗資料匣
			new File(dir+"user_porfile").mkdirs(); //創造出實驗使用者模型資料匣
			Time_w = new BufferedWriter(new FileWriter(dir+"time.txt"));
			
			/*//以下為實驗個資料匣與訓練、測試資料集創建程式片段，如要執行同實驗請註解掉
			StartTime = System.currentTimeMillis();
			Time_w.write("訓練與測試集產生中 :");
			Time_w.newLine();
			Time_w.flush();
			Time_w.write("開始時間 :" + StartTime);
			Time_w.newLine();
			Time_w.flush();
			new File(dir+"training").mkdirs(); //創造出實驗訓練集資料匣
			new File(dir+"testing").mkdirs(); //創造出實驗測試集資料匣
			//以下為Reuters訓練、測試資料集創建程式碼
			for(int i=1; i<=days; i++){
				System.out.println("第"+i+"天");
				new File(dir+"training/"+"day_"+i).mkdirs(); //創造出實驗訓練集第i天資料匣
				new File(dir+"testing/"+"day_"+i).mkdirs(); //創造出實驗測試集第i天資料匣
				if(i==1){
					for(int j=0; j<train_topics.length; j++){
						GTT.point_topic_doc_generateSet("Tom_reuters_0.4/single",dir+"training/"+"day_"+i,train_topics[j],3,i);
					}
				}
				//if(i==15){
					for(int j=0; j<test_topics.length; j++){
						GTT.point_topic_doc_generateSet("Tom_reuters_0.4/single",dir+"testing/"+"day_"+i,test_topics[j],1,days+i);
					}
				//}
			}
			//以上為Reuters訓練、測試資料集創建程式碼
			//以下為CiteULike訓練、測試資料集創建程式碼
			System.out.println("Real Word資料流為: "+real_people);
			days = GTT.real_word_generateSet("citeulike/citeulike_Tom_citeulike_0.4/",dir,real_people,train_days,test_days);
			real_people = GTT.get_real_people();
			//以上為CiteULike訓練、測試資料集創建程式碼
			EndTime = System.currentTimeMillis();
			Time_w.write("結束時間 :" + EndTime);
			Time_w.newLine();
			Time_w.flush();
			Time_w.write("共使用時間(秒) :" + (EndTime-StartTime)/1000);
			Time_w.newLine();
			Time_w.flush();
			Time_w.write("資料流名稱: "+real_people);
			Time_w.newLine();
			Time_w.flush();
			//如要執行同實驗請註解至此*/

			EfficacyMeasure_w = new BufferedWriter(new FileWriter(dir+"EfficacyMeasure.txt"));
			for(int day=1; day<=days; day++){
				EfficacyMeasure_w.write("第"+day+"天...");
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.flush();
				Time_w.write("第"+day+"天...");
				Time_w.newLine();
				Time_w.flush();
				//系統資料匣自行讀取測試部分
				/*File test_d = new File(dir+"testing/day_"+day);
				test_fs.clear();
				for(File test_f : test_d.listFiles()){
					test_fs.add(test_f);
				}
				test_fs_num = 0;*/
				
				//~~~以下為訓練階段~~~//
				//系統資料匣自行讀取
				//bw2 = new BufferedWriter(new FileWriter(dir+"processesting_order.txt"));
				File train_d = new File(dir+"training/day_"+day);
				
				if(day>7){ //短期興趣只保留7天
					profile_label.clear();
				}
				
				for(File train_f : train_d.listFiles()){
					System.out.println("訓練開始");
					System.out.println("第"+day+"天, 第"+(preprocess_times+1)+"篇文章");
					StartTime = System.currentTimeMillis();
					
				//讀取規定順序文件
				/*br3 = new BufferedReader(new FileReader(dir+"training_order.txt"));
				String training_order;
	
				while((training_order=br3.readLine())!=null){
					File f = new File(dir+"training/"+training_order);*/
					User_profile_term.clear();
					doc_term.clear();
					topic_term.clear();
					//讀取目前的user_profile
					if(preprocess_times!=0){ //非第一次就讀取上一次建好的user_profile
						br = new BufferedReader(new FileReader(dir+"user_porfile/user_profile_"+preprocess_times+".txt"));
						System.out.print("讀取模型"+dir+"user_profile_"+preprocess_times+".txt\n");
						while((line=br.readLine())!=null){
							topic_term.clear();
							String term = line.split(",")[0]; //字詞
							int group = Integer.valueOf(line.split(",")[2]); //字詞所屬群別
							double TFScore = Double.valueOf(line.split(",")[1]); //字詞分數
							//System.out.print("取出模型資訊=>"+term+","+TFScore+","+group+"\n");
							if(User_profile_term.get(group)==null){ //新的主題直接把字裝進去
								topic_term.put(term, TFScore);
							}else{ //舊主題就先取出目前的資料，再更新
								topic_term = User_profile_term.get(group);
								topic_term.put(term, TFScore);
							}
							User_profile_term.put(group,new HashMap(topic_term));
						}
						br.close();
					}else{ //第一次的模型就等同是空的所以設一個空主題，並初始化文字權重文件
						System.out.println("無發現模型，將字詞temp分數0.0放入模型");
						//如果User_profile_term都沒有值，後續步驟會出現錯誤訊息，所以這邊先塞一個，並且設分數為0，因此不影響相關判定
						topic_term.put("temp", 0.0);
						User_profile_term.put(1, new HashMap(topic_term));
						//初始化遺忘因子文件
						bw3 = new BufferedWriter(new FileWriter(dir+"user_porfile/user_profile_TDF.txt"));
						bw3.write(""+1);
						bw3.newLine();
						//存放格式為 字詞,字詞遺忘因子,此次更新編號,字詞總TF分數
						bw3.write("temp,0.079,1,0.0");
						bw3.newLine();
						bw3.flush();
						bw3.close();
						//初始化主題關係文件
						bw3 = new BufferedWriter(new FileWriter(dir+"user_porfile/user_profile_TR.txt"));
						bw3.write(""+0);
						bw3.newLine();
						bw3.flush();
						bw3.close();
					}
					EndTime = System.currentTimeMillis();
					train_time_read_profile = train_time_read_profile + ((EndTime-StartTime)/1000);
					test_time_read_profile = train_time_read_profile; //原因是因為測試時因為偷懶沒有重讀
					
					/*if(User_profile_term.get(3)!=null){
						for(String doc_terms: User_profile_term.get(3).keySet()){
							System.out.println("test1, "+doc_terms);
						}
					}*/
					
					//讀取文件
					StartTime = System.currentTimeMillis();
					preprocess_times++; //紀錄目前是第幾份文件
					//開始讀取文件
					br2 = new BufferedReader(new FileReader(train_f));
					for(int ii=0; ii<train_f.getName().split("_").length;ii++){
						if(ii==0){
							topicname=train_f.getName().split("_")[0];
						}else{
							char[] topicname_temp = train_f.getName().split("_")[ii].toCharArray();
							if(!Character.isDigit(topicname_temp[0])){ //如果第一個字元是數字代表到檔名結尾了
								topicname=topicname+"_"+train_f.getName().split("_")[ii];
							}else{
								break;
							}
						}
					}
					
					if(!profile_label.contains(topicname)){
						System.out.println("新增正解標籤"+topicname);
						profile_label.add(topicname);
					}
										
					System.out.print("讀取文件"+dir+"training/day_"+day+"/"+train_f.getName()+"\n");
					doc_ngd = Double.valueOf(br2.readLine()); //文件的NGD門檻
					System.out.print("取出文件NGD="+doc_ngd+"\n");
					topic_term.clear();
					doc_term.clear();
					docTF=0;
					doc_term_count=0;
					while((line=br2.readLine())!=null){
						String term = line.split(",")[0]; //字詞
						int group = Integer.valueOf(line.split(",")[2]); //字詞所屬群別
						double TFScore = Integer.valueOf(line.split(",")[1]); //字詞分數
						docTF+=TFScore;
						doc_term_count++;
						topic_term.clear();
						//System.out.print("取出文件資訊=>"+term+","+TFScore+","+group+"\n");
						if(doc_term.get(group)==null){ //新的主題直接把字裝進去
							topic_term.put(term, TFScore);
						}else{ //舊主題就先取出目前的資料，再更新
							topic_term = doc_term.get(group);
							topic_term.put(term, TFScore);
						}
						doc_term.put(group,new HashMap(topic_term));
					}
					GU.sum_avg_docTF(docTF);
					GU.sum_avg_termTF(docTF,doc_term_count);
					br2.close();
					//如果文件完全沒有特徵字詞會使得程式出錯，因此對這種文件放入一個temp字詞，之後再想辦法解決
					if(doc_term.get(1)==null){
						topic_term.clear();
						topic_term.put("temp", 0.0);
						doc_term.put(1, new HashMap(topic_term));
						System.out.println("該文件沒有任何特徵");
					}
					EndTime = System.currentTimeMillis();
					train_time_read_doc = train_time_read_doc + ((EndTime-StartTime)/1000);
					
					/*if(doc_term.get(1)!=null){
						for(String doc_terms: doc_term.get(1).keySet()){
							System.out.println("test1, "+doc_terms);
						}
					}*/
					
					//記錄讀取順序
					/*bw2.write(f.getName()+","+preprocess_times); 
					bw2.newLine();
					bw2.flush();*/
					
					//文件與模型的主題字詞資訊都萃取出來後就進行主題間的映射，以便於分辨文件主題是對應到模型的哪一個
					//此步驟也會紀錄主題關係
					StartTime = System.currentTimeMillis();
					//true 時,為 Append模式
					FWrite = new FileWriter(dir+"Comper_topic_profile_doc.txt",true);
					Comper_log = new BufferedWriter(FWrite);
					Comper_log.write("訓練文件名稱:"+train_f.getName());
					Comper_log.newLine();
					Comper_log.write("對映使用者模型:"+dir+"user_profile_"+preprocess_times+".txt");
					Comper_log.newLine();
					Comper_log.close();
					topic_mapping = TC.Comper_topic_profile_doc(dir,User_profile_term, doc_term, doc_ngd);
					//updata_fine_doc()方法最後實驗後發現效果不好，因此下兩行被註解掉
					//doc_term.clear();
					//doc_term = new HashMap<Integer,HashMap<String,Double>>(TC.updata_fine_doc());
					EndTime = System.currentTimeMillis();
					train_time_topic_mapping = train_time_topic_mapping + ((EndTime-StartTime)/1000);
					
					FWrite = new FileWriter(dir+"Comper_topic_profile_doc.txt",true);
					Comper_log = new BufferedWriter(FWrite);
					Comper_log.write("主題映射結果如下");
					Comper_log.newLine();
					Comper_log.flush();
					//觀看主題映射成果
					for(int i: topic_mapping.keySet()){
						System.out.print("文件主題 "+i+" 映射於模型主題 "+topic_mapping.get(i)+"\n");
						Comper_log.write("文件主題 "+i+" 映射於模型主題 "+topic_mapping.get(i));
						Comper_log.newLine();
						Comper_log.flush();
					}
					Comper_log.write("");
					Comper_log.newLine();
					Comper_log.flush();
					Comper_log.close();
					
					//使用使用者模型主題字詞更新，來取得更新後的主題字詞
					//此步驟也會用到遺忘因子
					StartTime = System.currentTimeMillis();
					User_profile_term = GU.add_user_profile_term(dir,User_profile_term, doc_term, topic_mapping);
					EndTime = System.currentTimeMillis();
					train_time_add_user_profile = train_time_add_user_profile + ((EndTime-StartTime)/1000);
					
					//輸出使用者模型
					GU.out_new_user_profile(dir,preprocess_times,User_profile_term);
					System.err.println("文件"+dir+"training/day_"+day+"/"+train_f.getName()+"處理結束");
				}
				
				//~~~以下為測試階段~~~//
				File test_d = new File(dir+"testing/day_"+day);
				for(File test_f : test_d.listFiles()){
					System.out.println("測試開始");
					//File test_f = test_fs.get(test_fs_num);
					//test_fs_num++;
					//讀取規定順序文件
					/*br4 = new BufferedReader(new FileReader(dir+"testing_order.txt"));
					String testing_order;
					test_times = 1; //設定每份訓練文件訓練完後要用幾份測試文件進行測試
					testing_order=br4.readLine();
					while(testing_order!=null && test_times!=0){
						test_times--;
						File test_f = new File(dir+"testing/"+testing_order);*/
						//開始讀取文件
						br2 = new BufferedReader(new FileReader(test_f));
						System.out.print("讀取文件"+dir+"testing/day_"+day+"/"+test_f.getName()+"\n");
						doc_ngd = Double.valueOf(br2.readLine()); //文件的NGD門檻
						System.out.print("取出文件NGD="+doc_ngd+"\n");
						//讀取測試文件
						StartTime = System.currentTimeMillis();
						topic_term.clear();
						doc_term.clear();
						while((line=br2.readLine())!=null){
							//System.out.println("line_check: "+line);
							String term = line.split(",")[0]; //字詞
							int group = Integer.valueOf(line.split(",")[2]); //字詞所屬群別
							double TFScore = Integer.valueOf(line.split(",")[1]); //字詞分數
							topic_term.clear();
							//System.out.print("取出文件資訊=>"+term+","+TFScore+","+group+"\n");
							if(doc_term.get(group)==null){ //新的主題直接把字裝進去
								topic_term.put(term, TFScore);
							}else{ //舊主題就先取出目前的資料，再更新
								topic_term = doc_term.get(group);
								topic_term.put(term, TFScore);
							}
							doc_term.put(group,new HashMap(topic_term));
						}
						br2.close();
						//如果文件完全沒有特徵字詞會使得程式出錯，因此對這種文件放入一個temp字詞，之後再想辦法解決
						if(doc_term.get(1)==null){
							topic_term.clear();
							topic_term.put("temp", 0.0);
							doc_term.put(1, new HashMap(topic_term));
							System.out.println("該文件沒有任何特徵");
						}
						EndTime = System.currentTimeMillis();
						test_time_read_doc = test_time_read_doc + ((EndTime-StartTime)/1000);
						
						//文件與模型的主題字詞資訊都萃取出來後就進行主題間的映射，以便於分辨文件主題是對應到模型的哪一個
						StartTime = System.currentTimeMillis();
						FWrite = new FileWriter(dir+"Comper_topic_profile_doc.txt",true);
						Comper_log = new BufferedWriter(FWrite);
						Comper_log.write("測試文件名稱:"+test_f.getName());
						Comper_log.newLine();
						Comper_log.write("對映使用者模型:"+dir+"user_profile_"+preprocess_times+".txt");
						Comper_log.newLine();
						Comper_log.close();
						topic_mapping = TC.Comper_topic_profile_doc_only(dir, User_profile_term, doc_term, doc_ngd, "test");
						EndTime = System.currentTimeMillis();
						test_time_topic_mapping = test_time_topic_mapping + ((EndTime-StartTime)/1000);
						
						FWrite = new FileWriter(dir+"Comper_topic_profile_doc.txt",true);
						Comper_log = new BufferedWriter(FWrite);
						Comper_log.write("主題映射結果如下");
						Comper_log.newLine();
						Comper_log.flush();
						//觀看主題映射成果
						for(int i: topic_mapping.keySet()){
							System.out.print("文件主題 "+i+" 映射於模型主題 "+topic_mapping.get(i)+"\n");
							Comper_log.write("文件主題 "+i+" 映射於模型主題 "+topic_mapping.get(i));
							Comper_log.newLine();
							Comper_log.flush();
						}
						Comper_log.write("");
						Comper_log.newLine();
						Comper_log.flush();
						Comper_log.close();
						
						//取得測試文件標籤
						topicname="";
						for(int ii=0; ii<test_f.getName().split("_").length;ii++){
							if(ii==0){
								topicname=test_f.getName().split("_")[0];
							}else{
								char[] topicname_temp = test_f.getName().split("_")[ii].toCharArray();
								if(!Character.isDigit(topicname_temp[0])){ //如果第一個字元是數字代表到檔名結尾了
									topicname=topicname+"_"+test_f.getName().split("_")[ii];
								}else{
									break;
								}
							}
						}
						System.out.println("測試文件的標籤為"+topicname);
						StartTime = System.currentTimeMillis();
						String relateness_result = TC.Comper_relateness_profile_doc(dir,topic_mapping,topicname,profile_label);
						EndTime = System.currentTimeMillis();
						test_time_Comper_relateness = test_time_Comper_relateness + ((EndTime-StartTime)/1000);
						
						EfficacyMeasure_w.write("測試文件: "+test_f.getName());
						EfficacyMeasure_w.newLine();
						EfficacyMeasure_w.write("判定為: "+relateness_result);
						EfficacyMeasure_w.newLine();
						EfficacyMeasure_w.flush();
						TC.set_EfficacyMeasure(relateness_result);
					//br4.close();
				}
				System.out.println("使用者模型天更新處理...");
				StartTime = System.currentTimeMillis();
				//每日需執行的遺忘因子的作用
				User_profile_term = GU.update_OneDayTerm_Decay_Factor(dir, User_profile_term);
				//每日需執行的字詞去除
				User_profile_term = GU.interest_remove_term(dir, User_profile_term, day);
				//每日需執行的興趣去除
				User_profile_term = GU.interest_remove_doc(dir, User_profile_term, day);
				//概念飄移預測
				CDF.forecasting_NGDorSIM(dir);
				
				//將更新後的profile寫入
				GU.out_new_user_profile(dir, preprocess_times, User_profile_term);
				EndTime = System.currentTimeMillis();
				sum_time_update_OneDayTerm = sum_time_update_OneDayTerm + ((EndTime-StartTime)/1000);
				
				//bw2.close();
				//br3.close();
				TC.show_all_result();
				System.out.println("正確率為 " + TC.get_accuracy());
				System.out.println("錯誤率為 " + TC.get_error());
				System.out.println("精準率為 " + TC.get_precision());
				System.out.println("查全度為 " + TC.get_recall());
				System.out.println("F-measure為 " + TC.get_f_measure());
				System.out.println("概念飄移次數為: " + (GU.get_ConceptDrift_times()+TC.get_ConceptDrift_times()));
				System.out.println("預測而連接的主題關係邊數為: " + CDF.get_forecasting_times());
				double all_result[] = TC.get_all_result();
				EfficacyMeasure_w.write("TP="+all_result[0]+", TN="+all_result[1]+", FP="+all_result[2]+", FN="+all_result[3]);
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("正確率為: " + TC.get_accuracy());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("錯誤率為: " + TC.get_error());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("精準率為: " + TC.get_precision());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("查全度為: " + TC.get_recall());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("F-measure為: " + TC.get_f_measure());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("概念飄移次數為: " + (GU.get_ConceptDrift_times()+TC.get_ConceptDrift_times()));
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.write("預測而連接的主題關係邊數為: " + CDF.get_forecasting_times());
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.newLine();
				EfficacyMeasure_w.flush();
				Time_w.write("訓練區讀取模型使用時間為: "+train_time_read_profile);
				Time_w.newLine();
				train_sum_time_read_profile+=train_time_read_profile;
				train_time_read_profile=0;
				Time_w.write("訓練區讀取文件使用時間為: "+train_time_read_doc);
				Time_w.newLine();
				train_sum_time_read_doc+=train_time_read_doc;
				train_time_read_doc=0;
				Time_w.write("訓練區主題映射使用時間為: "+train_time_topic_mapping);
				Time_w.newLine();
				train_sum_time_topic_mapping+=train_time_topic_mapping;
				train_time_topic_mapping=0;
				Time_w.write("測試區讀取模型使用時間為: "+test_time_read_profile);
				Time_w.newLine();
				test_sum_time_read_profile+=test_time_read_profile;
				test_time_read_profile=0;
				Time_w.write("測試區讀取文件使用時間為: "+test_time_read_doc);
				Time_w.newLine();
				test_sum_time_read_doc+=test_time_read_doc;
				test_time_read_doc=0;
				Time_w.write("測試區主題映射使用時間為: "+test_time_topic_mapping);
				Time_w.newLine();
				test_sum_time_topic_mapping+=test_time_topic_mapping;
				test_time_topic_mapping=0;
				Time_w.write("訓練區新增興趣使用時間為: "+train_time_add_user_profile);
				Time_w.newLine();
				train_sum_time_add_user_profile+=train_time_add_user_profile;
				train_time_add_user_profile=0;
				Time_w.write("測試區相關判定使用時間為: "+test_time_Comper_relateness);
				Time_w.newLine();
				test_sum_time_Comper_relateness+=test_time_Comper_relateness;
				test_time_Comper_relateness=0;
				Time_w.write("每日模型更新使用時間為: "+time_update_OneDayTerm);
				Time_w.newLine();
				sum_time_update_OneDayTerm+=time_update_OneDayTerm;
				time_update_OneDayTerm=0;
				Time_w.write("");
				Time_w.newLine();
				Time_w.flush();
			}
			Time_w.write(days+"天累計...");
			Time_w.newLine();
			Time_w.write("訓練區讀取模型使用總時間為: "+train_sum_time_read_profile);
			Time_w.newLine();
			Time_w.write("訓練區讀取文件使用總時間為: "+train_sum_time_read_doc);
			Time_w.newLine();
			Time_w.write("訓練區主題映射使用總時間為: "+train_sum_time_topic_mapping);
			Time_w.newLine();
			Time_w.write("測試區讀取模型使用總時間為: "+test_sum_time_read_profile);
			Time_w.newLine();
			Time_w.write("測試區讀取文件使用總時間為: "+test_sum_time_read_doc);
			Time_w.newLine();
			Time_w.write("測試區主題映射使用總時間為: "+test_sum_time_topic_mapping);
			Time_w.newLine();
			Time_w.write("訓練區新增興趣使用總時間為: "+train_sum_time_add_user_profile);
			Time_w.newLine();
			Time_w.write("測試區相關判定使用總時間為: "+test_sum_time_Comper_relateness);
			Time_w.newLine();
			Time_w.write("每日模型更新使用總時間為: "+sum_time_update_OneDayTerm);
			Time_w.newLine();
			Time_w.flush();
			Time_w.close();
			EfficacyMeasure_w.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
