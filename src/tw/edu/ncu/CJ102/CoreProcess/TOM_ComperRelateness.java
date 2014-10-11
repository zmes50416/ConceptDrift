package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tw.edu.ncu.CJ102.NGD_calculate;
import tw.edu.ncu.CJ102.SolrSearcher;

public class TOM_ComperRelateness {

	/**
	 * Edit from TOM_ComperRelateness
	 * 這個類別是用來做甚麼的??
	 * @param args
	 */
	double relateness_threshold = 0.4; // 為0.525文羽學長實驗結果
	double TP = 0, TN = 0, FP = 0, FN = 0;
	int ConceptDrift_times = 0; // 概念飄移次數

	// update_doc、maybe_update_term、sure_update_term最後實驗後發現效果不好，因此相關步驟都被註解掉
	// HashMap<Integer,HashMap<String,Double>> update_doc = new
	// HashMap<Integer,HashMap<String,Double>> (); //等待被更新的文件特徵

	public static void main(String[] args) {
		HashMap<Integer, HashMap<String, Double>> User_profile_test = new HashMap<Integer, HashMap<String, Double>>();
		HashMap<Integer, HashMap<String, Double>> doc_test = new HashMap<Integer, HashMap<String, Double>>();
		HashMap<Integer, Integer> topic_test = new HashMap<Integer, Integer>();
		User_profile_test.put(0, null);
		topic_test.put(0, null);
		TOM_ComperRelateness TC = new TOM_ComperRelateness();
		TC.Comper_topic_profile_doc("Tom_exp/", User_profile_test, doc_test,
				0.2);
	}

	// 主題映射程序，參數為實驗資料匣名稱, 使用者模型, 文件模型, ngd門檻值, 操作類別("train"或"test")
	public HashMap<Integer, Integer> Comper_topic_profile_doc_only(
			String exp_dir, HashMap<Integer, HashMap<String, Double>> profile,
			HashMap<Integer, HashMap<String, Double>> doc, double doc_ngd,
			String operate) {
		int doc_topic_num = 0; // 某一文件主題的字詞數量
		int profile_topic_num = 0; // 某一模型主題的字詞數量
		double profile_topic_tf_sum = 0; // 某一模型主題的總TF值
		double threshold = 0; // ngd門檻值
		double link_num = 0; // 某一文件主題與模型主題的密切連線數量
		int how_many_topic = 0; // 模型的主題數
		HashMap<Integer, Integer> topic_mapping = new HashMap<Integer, Integer>(); // 主題映射結果
		// update_doc、maybe_update_term、sure_update_term最後實驗後發現效果不好，因此相關步驟都被註解掉
		/*
		 * HashMap<String,Double> maybe_update_term = new
		 * HashMap<String,Double>(); //文檢主題i有可能被保存下來的字詞 HashMap<String,Double>
		 * sure_update_term = new HashMap<String,Double>(); //文檢主題i會被保存下來的字詞
		 */
		// update_doc.clear();

		FileWriter FWrite;
		try {
			BufferedReader br = new BufferedReader(new FileReader(exp_dir
					+ "user_porfile/user_profile_TR.txt"));
			how_many_topic = Integer.valueOf(br.readLine()); // 模型的主題數
			br.close();

			FWrite = new FileWriter(exp_dir + "Comper_topic_profile_doc.txt",
					true); // true 時,為 Append模式
			BufferedWriter Comper_log = new BufferedWriter(FWrite);

			/*以下先冰封存，因為計算TFIDF太過麻煩，對於系統可以能造成相當大的運算負擔，於評估後再決定是否使用。(未寫完)
			//因為映射我們不是直接採取學長的字詞個數的連線數，而是各字詞在這個TFIDF分數，這樣更能利用主題特色字詞，所以要先更新各字詞的分數
			//更新模型時也要順變更新該字詞的遺忘因子，先讀取遺忘因子的紀錄文件 PS. TDF = Term Decay Factor
			try {
				BufferedReader br = new BufferedReader(new FileReader(exp_dir+"user_porfile/user_profile_TDF.txt"));
				String line="";
				//紀錄字詞的編號，等等便於提出權重與更新時間點
				HashMap<String,Integer> terms_info = new HashMap<String,Integer>(); //字詞
				int update_time = Integer.valueOf(br.readLine()); //目前為止的更新編號
				ArrayList<Double> term_decayfactor = new ArrayList<Double>(); //字詞遺忘因子陣列
				ArrayList<Integer> term_update_time = new ArrayList<Integer>(); //字詞更新時間點陣列
				ArrayList<Double> term_sum_score = new ArrayList<Double>(); //字詞在模型內的總分(不分主題統計)
				int term_src=0; //暫時的字詞編號，便於連結term_decayfactor與term_update_time該字詞的對映位置
				while((line=br.readLine())!=null){
					terms_info.put(line.split(",")[0], term_src);
					term_decayfactor.add(Double.valueOf(line.split(",")[1]));
					term_update_time.add(Integer.valueOf(line.split(",")[2]));
					term_src++;
				}
				//更新TDF裡所有字詞資訊與使用者模型字詞分數
				for(String s: terms_info.keySet()){
					double sum_decayfactor = 1; //累計的遺忘因子，後面讓分數直接成上這個遺忘因子即可
					for(int j=term_update_time.get(terms_info.get(s)); j < update_time; j++){
						sum_decayfactor = sum_decayfactor*(1-term_decayfactor.get(terms_info.get(s)));
						term_decayfactor = update_Term_Decay_Factor(terms_info,term_decayfactor,"minus",s);
					}
				}
				//對所有字詞更新分數，順便累計個字詞的
			}catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/

			// 文件的所有主題與使用者模型的所有主題進行NGD計算，找出互相對應的主題
			for (int i : doc.keySet()) {
				doc_topic_num = doc.get(i).size();
				// maybe_update_term.clear();
				// sure_update_term.clear();
				// 不論文件主題或是模型主題的最低編號都是1，因此初始映射設0，在最後回傳還是0的話代表示此文件主題為模型內沒有的新主題
				topic_mapping.put(i, 0);
				// 儲存文件主題i對映到的最大相關度
				double bigest_sim = 0;

				for (int j : profile.keySet()) {
					link_num = 0;
					profile_topic_num = profile.get(j).size();
					Comper_log
							.write("文件主題" + i + " 字詞數為" + doc_topic_num + "個");
					Comper_log.newLine();
					Comper_log.write("模型主題" + j + " 字詞數為" + profile_topic_num
							+ "個");
					Comper_log.newLine();
					// System.out.println("文件主題"+i+" 字詞數為"+doc_topic_num+"個");
					// System.out.println("模型主題"+j+" 字詞數為"+profile_topic_num+"個");
					profile_topic_tf_sum = 0;
					for (String profile_term : profile.get(j).keySet()) {
						double term_tf = profile.get(j).get(profile_term);
						profile_topic_tf_sum = profile_topic_tf_sum + term_tf;
					}
					for (String doc_term : doc.get(i).keySet()) {
						// Double TF = doc.get(i).get(doc_term);
						// boolean term_doc_term_inmaybe = false;
						for (String profile_term : profile.get(j).keySet()) {
							double term_tf = profile.get(j).get(profile_term);
							// System.out.println(doc_term+","+profile_term+" ngd計算");
							double a = SolrSearcher.getHits("\"" + doc_term
									+ "\"");
							double b = SolrSearcher.getHits("\"" + profile_term
									+ "\"");
							// System.err.println("測試文件與使用者模組概念比對 Query: +\""+doc_term+"\" +\""+profile_term+"\"");
							double mValue = SolrSearcher
									.getHits("+\"" + doc_term + "\" +\""
											+ profile_term + "\"");

							double NGD = NGD_calculate.NGD_cal(a, b, mValue);
							if (NGD <= doc_ngd) {
								// link_num = link_num + 1; //累積連線數方法
								link_num = link_num + term_tf; // TF方法
								// term_doc_term_inmaybe = true;
							}
						}
						/*
						 * if(term_doc_term_inmaybe){
						 * maybe_update_term.put(doc_term,TF); //唯有緊密的字詞有可能被保存下來
						 * }
						 */
					}

					// 方法2(累積連線數方法)判定可以映射的連線門檻值為比對的文件主題的字詞數*比對的模型主題字詞數*相關判定門檻值
					// threshold =
					// doc_topic_num*profile_topic_num*relateness_threshold;
					// 方法3(累積連線數方法)(相似度平分於連線版本)判定可以映射的連線門檻值為比對的文件主題的字詞數*比對的模型主題字詞數*相關判定門檻值
					// threshold =
					// (doc_topic_num*profile_topic_num*relateness_threshold)/(doc_topic_num*profile_topic_num);
					// link_num = (link_num /
					// (doc_topic_num*profile_topic_num));

					// 方法4(TF方法)判定可以映射的連線門檻值為比對的文件主題的字詞數*比對的模型主題字詞TF值總合*相關判定門檻值
					// threshold =
					// doc_topic_num*profile_topic_tf_sum*relateness_threshold;
					// 方法5(TF方法)(相似度平分於連線版本)判定可以映射的連線門檻值為比對的文件主題的字詞數*比對的模型主題字詞TF值總合*相關判定門檻值
					threshold = (doc_topic_num * profile_topic_tf_sum * relateness_threshold)
							/ (doc_topic_num * profile_topic_num);
					link_num = link_num / (doc_topic_num * profile_topic_num);

					// 方法1(學長實驗最佳值方法)判定可以映射的連線門檻值為0.525，此方法是利用
					// (連線賺得總值/(文件主題的字詞數*比對的模型主題字詞TF值總合)) 的比例來算，經學長實驗得到的門檻值
					// threshold = relateness_threshold;
					// link_num = (link_num /
					// (doc_topic_num*profile_topic_num));

					// 方法6(累積連線數方法)(延續方法2+額外突出比例版本)計算賺得連線數超出門檻值的數值對於門檻值的比例
					// 方法7(TF方法)(延續方法3+額外突出比例版本)計算賺得連線數超出門檻值的數值對於門檻值的比例
					// 方法8(TF方法)(延續方法4+額外突出比例版本)計算賺得連線數超出門檻值的數值對於門檻值的比例
					// 方法9(TF方法)(延續方法5+額外突出比例版本)計算賺得連線數超出門檻值的數值對於門檻值的比例
					// (如果選方法1~5請註解下面7行程式碼)--(連線賺得總值-門檻值)/(門檻值)，最大超出比例的才會成為映射主題
					// link_num = (link_num-threshold)/threshold;
					Comper_log.write("文件主題" + i + "與模型主題" + j + "的相關門檻值為 "
							+ threshold);
					Comper_log.newLine();
					Comper_log.write("文件主題" + i + "與模型主題" + j + "之緊密連線為 "
							+ link_num);
					Comper_log.newLine();
					// System.out.println("文件主題"+i+"與模型主題"+j+"的相關門檻值為 "+threshold);
					// System.out.println("文件主題"+i+"與模型主題"+j+"之緊密連線為 "+link_num);
					if (link_num > threshold) {
						// if(link_num>bigest_sim){
						if (((link_num - threshold) / threshold) > bigest_sim) {
							Comper_log.write("文件主題" + i + " 暫定 映射於模型主題 " + j);
							Comper_log.newLine();
							// sure_update_term.clear();
							// sure_update_term = new
							// HashMap(maybe_update_term);
							// //最終會被保留下來的字詞會跟隨相關性最大的那個主題的比對成果
							topic_mapping.put(i, j);
							// bigest_sim = link_num;
							bigest_sim = ((link_num - threshold) / threshold);
						}
					}

					// (如果選方法6~9請註解下面4行程式碼)如果密切連線數量大於門檻值就紀錄下來這對應的主題，我們會選最高的，記錄放是為mapping(文件主題編號,模型主題編號)
					/*
					 * if(link_num>threshold && link_num>bigest_sim){
					 * topic_mapping.put(i, j); bigest_sim = link_num; }
					 */
				}
				if (topic_mapping.get(i) == 0) { // 如果是0代表示新主題，我們要把新主題加進去
					System.out.println("發現新主題");
					if (operate == "test") {
						Comper_log.write("發現新主題，測試中斷");
						Comper_log.newLine();
						/*
						 * topic_mapping.put(i, how_many_topic+1);
						 * how_many_topic++;
						 */
						break; // 因為對於測試文件而言如果有新主題就是代表非使用者相關文章，因此不必再計算
					} else if (operate == "train") {
						Comper_log.write("發現新主題");
						Comper_log.newLine();
						topic_mapping.put(i, how_many_topic + 1);
						// update_doc.put(i, new HashMap(doc.get(i)));
						// //新主題將保留下所有字詞
						how_many_topic++;
					}
				} else {
					Comper_log.write("舊主題");
					Comper_log.newLine();
					System.out.println("舊主題");
					// update_doc.put(i, new HashMap(sure_update_term));
					// //舊主題將保留下與映設主題緊密的字詞
				}
			}
			Comper_log.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return topic_mapping;
	}

	/*
	 * public HashMap<Integer,HashMap<String,Double>> updata_fine_doc(){ return
	 * update_doc; }
	 */

	public HashMap<Integer, Integer> Comper_topic_profile_doc(String exp_dir,
			HashMap<Integer, HashMap<String, Double>> profile,
			HashMap<Integer, HashMap<String, Double>> doc, double doc_ngd) {
		/*
		 * try { BufferedWriter EfficacyMeasure_w = new BufferedWriter(new
		 * FileWriter(exp_dir+"EfficacyMeasure.txt"));
		 * EfficacyMeasure_w.write("aaa: xxxx"); EfficacyMeasure_w.newLine();
		 * EfficacyMeasure_w.flush(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		HashMap<Integer, Integer> topic_mapping = new HashMap<Integer, Integer>(); // 主題映射結果
		topic_mapping = Comper_topic_profile_doc_only(exp_dir, profile, doc,
				doc_ngd, "train");
		update_topic_relation(exp_dir, topic_mapping);
		return topic_mapping;
	}

	public void update_topic_relation(String exp_dir,
			HashMap<Integer, Integer> topic_mapping) {

		// 目前等多考慮到一個文件有兩個主題，因此可能文件包含了單個主題或兩的主題對映到同一個或不同的模型主題
		try {
			// 讀取主題關係文件 PS. TR = Topic Relation
			BufferedReader br = new BufferedReader(new FileReader(exp_dir
					+ "user_porfile/user_profile_TR.txt"));
			String line;
			int how_many_topic = Integer.valueOf(br.readLine()); // 得知目前主題數
			String topics;
			double topic_relation;
			HashMap<String, Double> TR = new HashMap<String, Double>(); // 讀取出來的主題關係
			while ((line = br.readLine()) != null) {
				topics = line.split(",")[0];
				topic_relation = Double.valueOf(line.split(",")[1]);
				TR.put(topics, topic_relation);
			}
			String this_update;
			if (topic_mapping.size() == 1) {
				// 文件只包含一個主題
				if (topic_mapping.get(1) > how_many_topic) {
					how_many_topic = topic_mapping.get(1);
				}
				this_update = topic_mapping.get(1) + "-" + topic_mapping.get(1);
				// 更新致原有主題關細資料中
				if (TR.get(this_update) == null) {
					ConceptDrift_times++;
					TR.put(this_update, 1.0);
				} else {
					TR.put(this_update, TR.get(this_update) + 1);
				}
			} else {
				// 文件包含多個主題，讓數字小的排前面
				int doc_topic_num = topic_mapping.size(); // 得知文件內主題數量
				// 兩兩配對的話執行次數為 (主題數量*(主題數量-1))/2
				// 不論文件主題或是模型主題的最低編號都是1，所以j從1開始一直到最後一個的前一個(因為要與z變數兩兩配對)
				for (int j = 1; j < doc_topic_num; j++) {
					// z變數為j的下一個開始一直到最後一個
					for (int z = j + 1; z <= doc_topic_num; z++) {
						if (topic_mapping.get(j) < topic_mapping.get(z)) {
							if (topic_mapping.get(z) > how_many_topic) {
								how_many_topic = topic_mapping.get(z);
							}
							this_update = topic_mapping.get(j) + "-"
									+ topic_mapping.get(z);
						} else {
							if (topic_mapping.get(j) > how_many_topic) {
								how_many_topic = topic_mapping.get(j);
							}
							this_update = topic_mapping.get(z) + "-"
									+ topic_mapping.get(j);
						}
						// 更新至原有主題關細資料中
						if (TR.get(this_update) == null) {
							ConceptDrift_times++;
							TR.put(this_update, 1.0);
						} else {
							TR.put(this_update, TR.get(this_update) + 1);
						}
					}
				}
			}

			// 輸出新的主題關係文件
			BufferedWriter bw = new BufferedWriter(new FileWriter(exp_dir
					+ "user_porfile/user_profile_TR.txt"));
			bw.write("" + Integer.valueOf(how_many_topic)); // 目前主題數
			bw.newLine();
			bw.flush();
			for (String s : TR.keySet()) {
				// 存放格式為 主題1-主題2,關係程度,此次更新編號
				bw.write(s + "," + TR.get(s));
				bw.newLine();
				bw.flush();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 接收的參數為 String實驗資料匣, hashmap文件與模型主題的映射, String文件的標籤主題,
	// ArrayList<String>系統目前保有的標籤主題
	public String Comper_relateness_profile_doc(String exp_dir,
			HashMap<Integer, Integer> topic_mapping, String doc_label,
			ArrayList<String> profile_label) {
		String label_result; // 儲存標籤答案
		String label_system = "0"; // 儲存系統判斷結果
		String doc_system_label = ""; // 系統幫文件轉換的標籤

		if (profile_label.contains(doc_label)) {
			label_result = "T"; // 文件標籤的主題存在於系統中
		} else {
			label_result = "F"; // 文件標籤的主題不存在於系統中
		}
		System.out.println("標簽認為是" + label_result);

		// 讀取主題關係文件 PS. TR = Topic Relation
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(exp_dir
					+ "user_porfile/user_profile_TR.txt"));
			String line;
			HashMap<String, Double> TR = new HashMap<String, Double>(); // 讀取出來的主題關係
			String topics;
			double topic_relation;
			line = br.readLine(); // 頭一個主題總數資訊在此將不會被用到，所以先讀掉
			while ((line = br.readLine()) != null) {
				topics = line.split(",")[0];
				topic_relation = Double.valueOf(line.split(",")[1]);
				TR.put(topics, topic_relation);
			}
			br.close();

			/*
			 * //學長的簡單判定方法(學長與本研究判定方法請選擇一方注解) int doc_topic_num =
			 * topic_mapping.size(); //得知文件內主題數量 label_system="F";
			 * System.out.println("文件主題數量topic_mapping.size()="+doc_topic_num);
			 * for(int i=1;i<=doc_topic_num;i++){ if(topic_mapping.get(i)!=0){
			 * label_system="T"; } }
			 */

			// 本研究判定方法(學長與本研究判定方法請選擇一方注解)
			// 如果主題映射發現測試文件中包含有新主題，就不可能是相關文件，如不包新主題在判斷關係是否存在於模型中
			int doc_topic_num = topic_mapping.size(); // 得知文件內主題數量
			System.out.println("文件主題數量topic_mapping.size()=" + doc_topic_num);
			if (doc_topic_num == 1) {
				if (topic_mapping.get(1) == 0) {
					System.out.println("文件含有新主題");
					label_system = "F";
				} else {
					// 文件只包含一個主題
					doc_system_label = topic_mapping.get(1) + "-"
							+ topic_mapping.get(1);
					if (TR.get(doc_system_label) != null) {
						label_system = "T";
					} else {
						System.out.println("模型中無" + doc_system_label + "主題關係");
						label_system = "F";
					}
				}
			} else {
				for (int i = 1; i < doc_topic_num; i++) {
					if (topic_mapping.get(i) == 0) {
						label_system = "F";
					}
				}
				if (!label_system.equals("F")) {
					// 兩兩配對的話執行次數為 (主題數量*(主題數量-1))/2，文件包含多個主題，讓數字小的排前面
					// 不論文件主題或是模型主題的最低編號都是1，所以j從1開始一直到最後一個的前一個(因為要與z變數兩兩配對)
					for (int j = 1; j < doc_topic_num; j++) {
						// z變數為j的下一個開始一直到最後一個
						for (int z = j + 1; z <= doc_topic_num; z++) {
							if (topic_mapping.get(j) < topic_mapping.get(z)) {
								doc_system_label = topic_mapping.get(j) + "-"
										+ topic_mapping.get(z);
							} else {
								doc_system_label = topic_mapping.get(z) + "-"
										+ topic_mapping.get(j);
							}
							if (TR.get(doc_system_label) == null) {
								System.out.println("模型中無" + doc_system_label
										+ "主題關係");
								label_system = "F";
								break; // 如發現有任何一個關係不存在於模型中就代表不相關，停止繼續判斷
							}
						}
						if (label_system.equals("F")) {
							break; // 如發現有任何一個關係不存在於模型中就代表不相關，停止繼續判斷
						}
					}
				}
				if (!label_system.equals("F")) {
					label_system = "T";
				}
			}
			System.out.println("系統認為是" + label_system);

			// 最終文件相關判定
			if (label_result == "T") {
				if (label_system == "T") {
					return "TP"; // 標籤結果與系統判定結果都是T
				} else {
					return "FN"; // 系統將T判斷成F
				}
			} else {
				if (label_system == "T") {
					return "FP"; // 系統將F判斷成T
				} else {
					return "TN"; // 標籤結果與系統判定結果都是N
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERR";
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERR";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERR";
		}
	}

	public int get_ConceptDrift_times() {
		return ConceptDrift_times;
	}

	// 效能衡量
	public void init_EfficacyMeasure() {
		TP = 0;
		TN = 0;
		FP = 0;
		FN = 0;
	}

	public void set_EfficacyMeasure(String result) {
		System.out.println("此文件被判定為" + result);
		if (result == "TP") {
			TP++;
		}
		if (result == "TN") {
			TN++;
		}
		if (result == "FP") {
			FP++;
		}
		if (result == "FN") {
			FN++;
		}
	}

	public void show_all_result() {
		System.out.println("TP=" + TP + ", TN=" + TN + ", FP=" + FP + ", FN="
				+ FN);
	}

	public double[] get_all_result() {
		double all_result[] = { TP, TN, FP, FN };
		return all_result;
	}

	public double get_precision() {
		return TP / (TP + FP);
	}

	public double get_recall() {
		return TP / (TP + FN);
	}

	public double get_f_measure() {
		return (2 * get_precision() * get_recall())
				/ (get_precision() + get_recall());
	}

	public double get_accuracy() {
		return (TP + TN) / (TP + TN + FP + FN);
	}

	public double get_error() {
		return (FP + FN) / (TP + TN + FP + FN);
	}
}
