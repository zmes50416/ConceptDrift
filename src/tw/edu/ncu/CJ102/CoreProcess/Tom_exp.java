package tw.edu.ncu.CJ102.CoreProcess;

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

	BufferedReader br, br2, br3, br4; // br用來讀目前的user_profile，br2用來讀文件，br3用來直接指定訓練文件的順序，br4用來直接指定測試文件的順序
	BufferedWriter bw2, bw3; // bw2用來紀錄讀取文件的順序，bw3用來紀錄遺忘因子文件與主題關係文件
	IOWriter efficacyMeasurer, performanceTimer; // EfficacyMeasure_w用來紀錄系統效能，performanceTimer用來紀錄系統執行時間
	Go_Training_Tom GTT = new Go_Training_Tom();
	String projectDir = "exp_acq_DecayFactor_fs_fix0.05/";
	String real_people; // 選擇citeulike資料流 讀者的成員編號
	String train_topics[] = { "acq" };
	String test_topics[] = { "acq", "earn", "crude", "coffee", "sugar",
			"trade", "cocoa" };
	int days; // Reuters-實驗天數
	long StartTime;
	ArrayList<String> profile_label; // 儲存用來訓練的標籤答案

	/**
	 * @param args
	 */
	Tom_exp() {

	}

	public static void main(String[] args) {
		new Tom_exp().start();
	}

	public void start() {
		double train_sum_time_read_doc = 0, test_sum_time_read_doc = 0, train_time_read_doc = 0, test_time_read_doc = 0; // 讀取文件的總時間
		double train_sum_time_read_profile = 0, test_sum_time_read_profile = 0, train_time_read_profile = 0, test_time_read_profile = 0; // 讀取模型的總時間
		double train_sum_time_topic_mapping = 0, test_sum_time_topic_mapping = 0, train_time_topic_mapping = 0, test_time_topic_mapping = 0; // 主題映射的總時間
		double train_sum_time_add_user_profile = 0, train_time_add_user_profile = 0; // 新增興趣到模型的總時間(只有訓練部份有)
		double test_sum_time_Comper_relateness = 0, test_time_Comper_relateness = 0; // 相關判定的總時間(只有測試部份有)
		double sum_time_update_OneDayTerm = 0, time_update_OneDayTerm = 0; // 使用遺忘因子更新模型的總時間(每天做一次)

		// 記錄user_profile各主題的主題字詞，格式<主題編號,<主題字詞,字詞分數>>
		HashMap<Integer, HashMap<String, Double>> User_profile_term = new HashMap<Integer, HashMap<String, Double>>();
		// 記錄文件各主題的主題字詞，格式<主題編號,<主題字詞,字詞分數>>
		HashMap<Integer, HashMap<String, Double>> doc_term = new HashMap<Integer, HashMap<String, Double>>();
		// 暫存字詞與分數
		HashMap<String, Double> topic_term = new HashMap<String, Double>();
		// 儲存文件與模型的主題映射
		HashMap<Integer, Integer> topic_mapping = new HashMap<Integer, Integer>();
		profile_label = new ArrayList<String>();
		TOM_ComperRelateness TC = new TOM_ComperRelateness();
		UserProfile userProfiler = new UserProfile(false);
		ConceptDrift_Forecasting CDF = new ConceptDrift_Forecasting();
		BufferedWriter Comper_log; // Comper_log用來紀錄主題映射的數值
		int preprocess_times = 0; // 某一天的第X篇文章
		int doc_term_count; // 單文件內的字詞數量
		double docTF; // 單文件的TF值
		double doc_ngd=0;
		String line = "";
		String topicname = ""; // 儲存標籤答案
		int train_times, test_times; // 設定每次訓練、測試互換的篇數
		TC.init_EfficacyMeasure();
		IOWriter performanceTimer = null;

		new File(projectDir).mkdirs(); // 創造出實驗資料匣
		new File(projectDir + "user_porfile").mkdirs(); // 創造出實驗使用者模型資料匣
		try {
			efficacyMeasurer = new IOWriter(projectDir + "EfficacyMeasure.txt");
			performanceTimer = new IOWriter(projectDir + "time.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 以下為實驗個資料匣與訓練、測試資料集創建程式片段，如要執行同實驗請註解掉
		StartTime = System.currentTimeMillis();
		performanceTimer.addRecorded("訓練與測試集產生中 :");
		performanceTimer.addRecorded("開始時間 :" + StartTime);

		new File(projectDir + "training").mkdirs(); // 創造出實驗訓練集資料匣
		new File(projectDir + "testing").mkdirs(); // 創造出實驗測試集資料匣

		this.readFileFromRouter();// read from Router or CiteUlike

		for (int day = 1; day <= days; day++) {
			efficacyMeasurer.addRecorded("第" + day + "天...");
			performanceTimer.addRecorded("第" + day + "天...");

			File train_d = new File(projectDir + "training/day_" + day);

			if (day > 7) { // 短期興趣只保留7天
				profile_label.clear();
			}

			for (File train_f : train_d.listFiles()) {
				System.out.println("訓練開始\n" + "第" + day + "天, 第"
						+ (preprocess_times + 1) + "篇文章");

				StartTime = System.currentTimeMillis();
				User_profile_term.clear();
				doc_term.clear();
				topic_term.clear();
				// 讀取目前的user_profile
				if (preprocess_times != 0) { // 非第一次就讀取上一次建好的user_profile
					try {
						br = new BufferedReader(new FileReader(projectDir
								+ "user_porfile/user_profile_"
								+ preprocess_times + ".txt"));

						System.out.print("讀取模型" + projectDir + "user_profile_"
								+ preprocess_times + ".txt\n");
						while ((line = br.readLine()) != null) {
							topic_term.clear();
							String term = line.split(",")[0]; // 字詞
							int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
							double TFScore = Double.valueOf(line.split(",")[1]); // 字詞分數
							// System.out.print("取出模型資訊=>"+term+","+TFScore+","+group+"\n");
							if (User_profile_term.get(group) == null) { // 新的主題直接把字裝進去
								topic_term.put(term, TFScore);
							} else { // 舊主題就先取出目前的資料，再更新
								topic_term = User_profile_term.get(group);
								topic_term.put(term, TFScore);
							}
							User_profile_term.put(group,
									new HashMap(topic_term));
						}
						br.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else { // 第一次的模型就等同是空的所以設一個空主題，並初始化文字權重文件
					System.out.println("無發現模型，將字詞temp分數0.0放入模型");
					// 如果User_profile_term都沒有值，後續步驟會出現錯誤訊息，所以這邊先塞一個，並且設分數為0，因此不影響相關判定
					topic_term.put("temp", 0.0);
					User_profile_term.put(1, new HashMap(topic_term));
					// 初始化遺忘因子文件
					try {
						bw3 = new BufferedWriter(new FileWriter(projectDir
								+ "user_porfile/user_profile_TDF.txt"));
						bw3.write("" + 1);
						bw3.newLine();
						// 存放格式為 字詞,字詞遺忘因子,此次更新編號,字詞總TF分數
						bw3.write("temp,0.079,1,0.0");
						bw3.newLine();
						bw3.flush();
						bw3.close();
						// 初始化主題關係文件
						bw3 = new BufferedWriter(new FileWriter(projectDir
								+ "user_porfile/user_profile_TR.txt"));
						bw3.write("" + 0);
						bw3.newLine();
						bw3.flush();
						bw3.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				long EndTime = System.currentTimeMillis();
				train_time_read_profile = train_time_read_profile
						+ ((EndTime - StartTime) / 1000);
				test_time_read_profile = train_time_read_profile; // 原因是因為測試時因為偷懶沒有重讀

				/*
				 * if(User_profile_term.get(3)!=null){ for(String doc_terms:
				 * User_profile_term.get(3).keySet()){
				 * System.out.println("test1, "+doc_terms); } }
				 */

				// 讀取文件
				StartTime = System.currentTimeMillis();
				preprocess_times++; // 紀錄目前是第幾份文件
				// 開始讀取文件
				for (int ii = 0; ii < train_f.getName().split("_").length; ii++) {
					if (ii == 0) {
						topicname = train_f.getName().split("_")[0];
					} else {
						char[] topicname_temp = train_f.getName().split("_")[ii]
								.toCharArray();
						if (!Character.isDigit(topicname_temp[0])) { // 如果第一個字元是數字代表到檔名結尾了
							topicname = topicname + "_"
									+ train_f.getName().split("_")[ii];
						} else {
							break;
						}
					}
				}

				if (!profile_label.contains(topicname)) {
					System.out.println("新增正解標籤" + topicname);
					profile_label.add(topicname);
				}

				System.out.print("讀取文件" + projectDir + "training/day_" + day
						+ "/" + train_f.getName() + "\n");
				try {
					br2 = new BufferedReader(new FileReader(train_f));
					doc_ngd = Double.valueOf(br2.readLine()); // 文件的NGD門檻
					System.out.print("取出文件NGD=" + doc_ngd + "\n");
					topic_term.clear();
					doc_term.clear();
					docTF = 0;
					doc_term_count = 0;
					while ((line = br2.readLine()) != null) {
						String term = line.split(",")[0]; // 字詞
						int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
						double TFScore = Integer.valueOf(line.split(",")[1]); // 字詞分數
						docTF += TFScore;
						doc_term_count++;
						topic_term.clear();
						// System.out.print("取出文件資訊=>"+term+","+TFScore+","+group+"\n");
						if (doc_term.get(group) == null) { // 新的主題直接把字裝進去
							topic_term.put(term, TFScore);
						} else { // 舊主題就先取出目前的資料，再更新
							topic_term = doc_term.get(group);
							topic_term.put(term, TFScore);
						}
						doc_term.put(group, new HashMap(topic_term));
					}
					userProfiler.sum_avg_docTF(docTF);
					userProfiler.sum_avg_termTF(docTF, doc_term_count);
					br2.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 如果文件完全沒有特徵字詞會使得程式出錯，因此對這種文件放入一個temp字詞，之後再想辦法解決
				if (doc_term.get(1) == null) {
					topic_term.clear();
					topic_term.put("temp", 0.0);
					doc_term.put(1, new HashMap(topic_term));
					System.out.println("該文件沒有任何特徵");
				}
				EndTime = System.currentTimeMillis();
				train_time_read_doc = train_time_read_doc
						+ ((EndTime - StartTime) / 1000);

				/*
				 * if(doc_term.get(1)!=null){ for(String doc_terms:
				 * doc_term.get(1).keySet()){
				 * System.out.println("test1, "+doc_terms); } }
				 */

				// 記錄讀取順序
				/*
				 * bw2.write(f.getName()+","+preprocess_times); bw2.newLine();
				 * bw2.flush();
				 */

				// 文件與模型的主題字詞資訊都萃取出來後就進行主題間的映射，以便於分辨文件主題是對應到模型的哪一個
				// 此步驟也會紀錄主題關係
				try{
				StartTime = System.currentTimeMillis();
				FileWriter FWrite = new FileWriter(projectDir
						+ "Comper_topic_profile_doc.txt", true);// true=append
																// mode
				Comper_log = new BufferedWriter(FWrite);
				Comper_log.write("訓練文件名稱:" + train_f.getName());
				Comper_log.newLine();
				Comper_log.write("對映使用者模型:" + projectDir + "user_profile_"
						+ preprocess_times + ".txt");
				Comper_log.newLine();
				Comper_log.close();
				topic_mapping = TC.Comper_topic_profile_doc(projectDir,
						User_profile_term, doc_term, doc_ngd);
				// updata_fine_doc()方法最後實驗後發現效果不好，因此下兩行被註解掉
				// doc_term.clear();
				// doc_term = new
				// HashMap<Integer,HashMap<String,Double>>(TC.updata_fine_doc());
				EndTime = System.currentTimeMillis();
				train_time_topic_mapping = train_time_topic_mapping
						+ ((EndTime - StartTime) / 1000);

				FWrite = new FileWriter(projectDir
						+ "Comper_topic_profile_doc.txt", true);
				Comper_log = new BufferedWriter(FWrite);
				Comper_log.write("主題映射結果如下");
				Comper_log.newLine();
				Comper_log.flush();
				// 觀看主題映射成果
				for (int i : topic_mapping.keySet()) {
					System.out.print("文件主題 " + i + " 映射於模型主題 "
							+ topic_mapping.get(i) + "\n");
					Comper_log.write("文件主題 " + i + " 映射於模型主題 "
							+ topic_mapping.get(i));
					Comper_log.newLine();
					Comper_log.flush();
				}
				Comper_log.write("");
				Comper_log.newLine();
				Comper_log.flush();
				Comper_log.close();
				}catch(Exception e){
					e.printStackTrace();
				}

				// 使用使用者模型主題字詞更新，來取得更新後的主題字詞
				// 此步驟也會用到遺忘因子
				StartTime = System.currentTimeMillis();
				User_profile_term = userProfiler.add_user_profile_term(
						User_profile_term, doc_term, topic_mapping);
				EndTime = System.currentTimeMillis();
				train_time_add_user_profile = train_time_add_user_profile
						+ ((EndTime - StartTime) / 1000);

				// 輸出使用者模型
				userProfiler.out_new_user_profile(projectDir, preprocess_times,
						User_profile_term);
				System.err.println("文件" + projectDir + "training/day_" + day
						+ "/" + train_f.getName() + "處理結束");
			}

			// ~~~以下為測試階段~~~//
			File test_d = new File(projectDir + "testing/day_" + day);
			for (File test_f : test_d.listFiles()) {
				try {
					System.out.println("測試開始");
					// File test_f = test_fs.get(test_fs_num);
					// test_fs_num++;
					// 讀取規定順序文件
					/*
					 * br4 = new BufferedReader(new
					 * FileReader(dir+"testing_order.txt")); String
					 * testing_order; test_times = 1; //設定每份訓練文件訓練完後要用幾份測試文件進行測試
					 * testing_order=br4.readLine(); while(testing_order!=null
					 * && test_times!=0){ test_times--; File test_f = new
					 * File(dir+"testing/"+testing_order);
					 */
					// 開始讀取文件
					br2 = new BufferedReader(new FileReader(test_f));
					System.out.print("讀取文件" + projectDir + "testing/day_" + day
							+ "/" + test_f.getName() + "\n");
					doc_ngd = Double.valueOf(br2.readLine()); // 文件的NGD門檻
					System.out.print("取出文件NGD=" + doc_ngd + "\n");
					// 讀取測試文件
					StartTime = System.currentTimeMillis();
					topic_term.clear();
					doc_term.clear();
					while ((line = br2.readLine()) != null) {
						// System.out.println("line_check: "+line);
						String term = line.split(",")[0]; // 字詞
						int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
						double TFScore = Integer.valueOf(line.split(",")[1]); // 字詞分數
						topic_term.clear();
						// System.out.print("取出文件資訊=>"+term+","+TFScore+","+group+"\n");
						if (doc_term.get(group) == null) { // 新的主題直接把字裝進去
							topic_term.put(term, TFScore);
						} else { // 舊主題就先取出目前的資料，再更新
							topic_term = doc_term.get(group);
							topic_term.put(term, TFScore);
						}
						doc_term.put(group, new HashMap(topic_term));
					}
					br2.close();
					// 如果文件完全沒有特徵字詞會使得程式出錯，因此對這種文件放入一個temp字詞，之後再想辦法解決
					if (doc_term.get(1) == null) {
						topic_term.clear();
						topic_term.put("temp", 0.0);
						doc_term.put(1, new HashMap(topic_term));
						System.out.println("該文件沒有任何特徵");
					}
					long EndTime = System.currentTimeMillis();
					test_time_read_doc = test_time_read_doc
							+ ((EndTime - StartTime) / 1000);

					// 文件與模型的主題字詞資訊都萃取出來後就進行主題間的映射，以便於分辨文件主題是對應到模型的哪一個
					StartTime = System.currentTimeMillis();
					FileWriter FWrite = new FileWriter(projectDir
							+ "Comper_topic_profile_doc.txt", true);
					Comper_log = new BufferedWriter(FWrite);
					Comper_log.write("測試文件名稱:" + test_f.getName());
					Comper_log.newLine();
					Comper_log.write("對映使用者模型:" + projectDir + "user_profile_"
							+ preprocess_times + ".txt");
					Comper_log.newLine();
					Comper_log.close();
					topic_mapping = TC.Comper_topic_profile_doc_only(
							projectDir, User_profile_term, doc_term, doc_ngd,
							"test");
					EndTime = System.currentTimeMillis();
					test_time_topic_mapping = test_time_topic_mapping
							+ ((EndTime - StartTime) / 1000);

					FWrite = new FileWriter(projectDir
							+ "Comper_topic_profile_doc.txt", true);
					Comper_log = new BufferedWriter(FWrite);
					Comper_log.write("主題映射結果如下");
					Comper_log.newLine();
					// 觀看主題映射成果
					for (int i : topic_mapping.keySet()) {
						System.out.print("文件主題 " + i + " 映射於模型主題 "
								+ topic_mapping.get(i) + "\n");
						Comper_log.write("文件主題 " + i + " 映射於模型主題 "
								+ topic_mapping.get(i));
						Comper_log.newLine();
						Comper_log.flush();
					}
					Comper_log.write("");
					Comper_log.newLine();
					Comper_log.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 取得測試文件標籤
				topicname = "";
				for (int ii = 0; ii < test_f.getName().split("_").length; ii++) {
					if (ii == 0) {
						topicname = test_f.getName().split("_")[0];
					} else {
						char[] topicname_temp = test_f.getName().split("_")[ii]
								.toCharArray();
						if (!Character.isDigit(topicname_temp[0])) { // 如果第一個字元是數字代表到檔名結尾了
							topicname = topicname + "_"
									+ test_f.getName().split("_")[ii];
						} else {
							break;
						}
					}
				}
				System.out.println("測試文件的標籤為" + topicname);
				StartTime = System.currentTimeMillis();
				String relateness_result = TC.Comper_relateness_profile_doc(
						projectDir, topic_mapping, topicname, profile_label);
				float EndTime = System.currentTimeMillis();
				test_time_Comper_relateness = test_time_Comper_relateness
						+ ((EndTime - StartTime) / 1000);

				efficacyMeasurer.addRecorded("測試文件: " + test_f.getName());
				efficacyMeasurer.addRecorded("判定為: " + relateness_result);
				TC.set_EfficacyMeasure(relateness_result);
				// br4.close();
			}
			System.out.println("使用者模型天更新處理...");
			StartTime = System.currentTimeMillis();
			// 每日需執行的遺忘因子的作用
			User_profile_term = userProfiler.update_OneDayTerm_Decay_Factor(
					projectDir, User_profile_term);
			// 每日需執行的字詞去除
			User_profile_term = userProfiler.interest_remove_term(projectDir,
					User_profile_term, day);
			// 每日需執行的興趣去除
			User_profile_term = userProfiler.interest_remove_doc(projectDir,
					User_profile_term, day);
			// 概念飄移預測
			CDF.forecasting_NGDorSIM(projectDir);

			// 將更新後的profile寫入
			userProfiler.out_new_user_profile(projectDir, preprocess_times,
					User_profile_term);
			long endTime = System.currentTimeMillis();
			sum_time_update_OneDayTerm = sum_time_update_OneDayTerm
					+ ((endTime - StartTime) / 1000);

			// bw2.close();
			// br3.close();
			TC.show_all_result();
			System.out.println("正確率為 " + TC.get_accuracy());
			System.out.println("錯誤率為 " + TC.get_error());
			System.out.println("精準率為 " + TC.get_precision());
			System.out.println("查全度為 " + TC.get_recall());
			System.out.println("F-measure為 " + TC.get_f_measure());
			System.out.println("概念飄移次數為: "
					+ (userProfiler.get_ConceptDrift_times() + TC
							.get_ConceptDrift_times()));
			System.out.println("預測而連接的主題關係邊數為: " + CDF.get_forecasting_times());
			double all_result[] = TC.get_all_result();
			efficacyMeasurer.addRecorded("TP=" + all_result[0] + ", TN="
					+ all_result[1] + ", FP=" + all_result[2] + ", FN="
					+ all_result[3]);
			efficacyMeasurer.addRecorded("正確率為: " + TC.get_accuracy());
			efficacyMeasurer.addRecorded("錯誤率為: " + TC.get_error());
			efficacyMeasurer.addRecorded("精準率為: " + TC.get_precision());
			efficacyMeasurer.addRecorded("查全度為: " + TC.get_recall());
			efficacyMeasurer.addRecorded("F-measure為: " + TC.get_f_measure());
			efficacyMeasurer.addRecorded("概念飄移次數為: "
					+ (userProfiler.get_ConceptDrift_times() + TC
							.get_ConceptDrift_times()));
			efficacyMeasurer.addRecorded("預測而連接的主題關係邊數為: "
					+ CDF.get_forecasting_times() + "\n\n");
			performanceTimer.addRecorded("訓練區讀取模型使用時間為: "
					+ train_time_read_profile);
			train_sum_time_read_profile += train_time_read_profile;
			train_time_read_profile = 0;
			performanceTimer
					.addRecorded("訓練區讀取文件使用時間為: " + train_time_read_doc);
			train_sum_time_read_doc += train_time_read_doc;
			train_time_read_doc = 0;
			performanceTimer.addRecorded("訓練區主題映射使用時間為: "
					+ train_time_topic_mapping);
			train_sum_time_topic_mapping += train_time_topic_mapping;
			train_time_topic_mapping = 0;
			performanceTimer.addRecorded("測試區讀取模型使用時間為: "
					+ test_time_read_profile);
			test_sum_time_read_profile += test_time_read_profile;
			test_time_read_profile = 0;
			performanceTimer.addRecorded("測試區讀取文件使用時間為: " + test_time_read_doc);
			test_sum_time_read_doc += test_time_read_doc;
			test_time_read_doc = 0;
			performanceTimer.addRecorded("測試區主題映射使用時間為: "
					+ test_time_topic_mapping);
			test_sum_time_topic_mapping += test_time_topic_mapping;
			test_time_topic_mapping = 0;
			performanceTimer.addRecorded("訓練區新增興趣使用時間為: "
					+ train_time_add_user_profile);
			train_sum_time_add_user_profile += train_time_add_user_profile;
			train_time_add_user_profile = 0;
			performanceTimer.addRecorded("測試區相關判定使用時間為: "
					+ test_time_Comper_relateness);
			test_sum_time_Comper_relateness += test_time_Comper_relateness;
			test_time_Comper_relateness = 0;
			performanceTimer.addRecorded("每日模型更新使用時間為: "
					+ time_update_OneDayTerm);
			sum_time_update_OneDayTerm += time_update_OneDayTerm;
			time_update_OneDayTerm = 0;
		}
		performanceTimer.addRecorded(days + "天累計...");
		performanceTimer.addRecorded("訓練區讀取模型使用總時間為: "
				+ train_sum_time_read_profile);
		performanceTimer.addRecorded("訓練區讀取文件使用總時間為: "
				+ train_sum_time_read_doc);
		performanceTimer.addRecorded("訓練區主題映射使用總時間為: "
				+ train_sum_time_topic_mapping);
		performanceTimer.addRecorded("測試區讀取模型使用總時間為: "
				+ test_sum_time_read_profile);
		performanceTimer
				.addRecorded("測試區讀取文件使用總時間為: " + test_sum_time_read_doc);
		performanceTimer.addRecorded("測試區主題映射使用總時間為: "
				+ test_sum_time_topic_mapping);
		performanceTimer.addRecorded("訓練區新增興趣使用總時間為: "
				+ train_sum_time_add_user_profile);
		performanceTimer.addRecorded("測試區相關判定使用總時間為: "
				+ test_sum_time_Comper_relateness);
		performanceTimer.addRecorded("每日模型更新使用總時間為: "
				+ sum_time_update_OneDayTerm);
		try {
			performanceTimer.close();
			efficacyMeasurer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 以下為Routers訓練、測試資料集創建程式碼
	private void readFileFromRouter() {
		days = 15;// Routers實驗天數
		for (int i = 1; i <= days; i++) {
			System.out.println("第" + i + "天");
			new File(projectDir + "training/" + "day_" + i).mkdirs(); // 創造出實驗訓練集第i天資料匣
			new File(projectDir + "testing/" + "day_" + i).mkdirs(); // 創造出實驗測試集第i天資料匣
			if (i == 1) {
				for (int j = 0; j < train_topics.length; j++) {
					GTT.point_topic_doc_generateSet("Tom_reuters_0.4/single",
							projectDir + "training/" + "day_" + i,
							train_topics[j], 3, i);
				}
			}
			// if(i==15){
			for (int j = 0; j < test_topics.length; j++) {
				GTT.point_topic_doc_generateSet("Tom_reuters_0.4/single",
						projectDir + "testing/" + "day_" + i, test_topics[j],
						1, days + i);
			}
			// }
		}
	}

	private void readFileFromCiteUlike() throws IOException {
		int train_days = 0, test_days = -1; // citeulike-實驗天數，0為全部，-1為不使用
		real_people = "626838af45efa5ca465683ab3b3f303e";

		System.out.println("Real Word資料流為: " + real_people);
		days = GTT.real_word_generateSet(
				"citeulike/citeulike_Tom_citeulike_0.4/", projectDir,
				real_people, train_days, test_days);
		real_people = GTT.get_real_people();
		// 以上為CiteULike訓練、測試資料集創建程式碼
		long EndTime = System.currentTimeMillis();
		IOWriter performanceTimer = new IOWriter(projectDir + "time.txt", true);
		performanceTimer.addRecorded("結束時間 :" + EndTime);
		performanceTimer.addRecorded("共使用時間(秒) :" + (EndTime - StartTime)
				/ 1000);

		performanceTimer.addRecorded("資料流名稱: " + real_people);
	}

	public void startTraining() {

	}

	class IOWriter {
		BufferedWriter timeWriter;
		long StartTime;

		IOWriter(String writePath) throws IOException {
			this(writePath, false);
		}

		IOWriter(String writePath, Boolean isAppending) throws IOException { // remember
																				// pass
																				// in
			this.timeWriter = new BufferedWriter(new FileWriter(writePath,
					isAppending));
			this.StartTime = System.currentTimeMillis();
		}

		public void addRecorded(String record) {
			try {
				timeWriter.write(record);
				timeWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void close() throws IOException {
			this.timeWriter.close();
		}

	}
}
