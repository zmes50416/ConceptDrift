import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import tw.edu.ncu.sia.util.ServerUtil;

//import ConceptCompare.ParserGetter;

public class CompareAveNGD extends HTMLEditorKit.ParserCallback {
	private Map<String, Double> center = new HashMap<String, Double>();
	private Map<String, Double> concept = new HashMap<String, Double>();
	static String topicDir = "Topic_Cs/";
	static String writeFile = "all.txt";
	static String centerFile = "center.txt";
	static String mainDir = "Main_word/";
	static int centerSize = 4;

	class ParserGetter extends HTMLEditorKit {
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	// 記錄是否將資料印出
	private boolean inHeader = false;
	static double mValue = 0;

	public CompareAveNGD() {
	}

	// 將 Parse HTML 後的資料印出
	public void handleText(char[] text, int position) {
		if (inHeader) {

			if (String.valueOf(text).contains("約有 ")
					|| String.valueOf(text).contains(" 項結果")) {
				String value = String.valueOf(text).replaceAll(",", ""); // 約有
				// 173,000
				// 項結果

				double value1 = Double.parseDouble(value.split(" ")[value
						.split(" ").length - 2]);
				mValue = value1;
			} else if (String.valueOf(text).contains("找不到和您的查詢")) {
				mValue = 0;
			}

		}
	}

	// Parse HTML Start Tag
	public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes,
			int position) {

		// 分析 Tag 的重點在這行
		if (tag == HTML.Tag.DIV) {
			Enumeration e = attributes.getAttributeNames();
			while (e.hasMoreElements()) {
				Object name = e.nextElement();
				String value = (String) attributes.getAttribute(name);

				// 符合 <A HREF = "xxxx"> 屬性的字串，xxxx 會被印出
				if (name == HTML.Attribute.ID && value.equals("resultStats")) {
					this.inHeader = true;
				}
			}
		} else if (tag == HTML.Tag.SPAN) {
			Enumeration e = attributes.getAttributeNames();
			while (e.hasMoreElements()) {
				Object name = e.nextElement();
				String value = (String) attributes.getAttribute(name);

				// 符合 <A HREF = "xxxx"> 屬性的字串，xxxx 會被印出
				if (name == HTML.Attribute.ID) {
					this.inHeader = true;
				}
			}
		}
	}

	void search_term(String s) {
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		HTMLEditorKit.ParserCallback callback = new CompareAveNGD();
		String i1 = s;
		String j = "http://www.google.com.tw/search?aq=f&sourceid=chrome&ie=UTF-8&q=";
		String http = j + i1;

		URL u;
		try {
			u = new URL(http);
			HttpURLConnection urlConnection = (HttpURLConnection) u
					.openConnection();
			urlConnection
					.addRequestProperty(
							"user-agent",
							"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-TW; rv:1.8.1.14) Gecko/20080404 Firefox/2.0.0.14"
									+ "SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");

			// 讀入網頁
			BufferedInputStream in = new BufferedInputStream(urlConnection
					.getInputStream());
			InputStreamReader r = new InputStreamReader(in, "UTF-8");
			parser.parse(r, callback, true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IP被鎖，重新取得IP");

			IP_Operation.IP_change();// 換IP
			System.out.println("重新取得字詞(" + s + ")搜尋結果");
			search_term(s);

		}

	}

	private void getCenter(String path, int n) {

		BufferedReader br;
		String line = "";
		center.clear();
		try {
			br = new BufferedReader(new FileReader(path));
			while ((line = br.readLine()) != null && center.size() < n) {
				center.put(line.split(",")[0], Double.parseDouble(line
						.split(",")[2]));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	private void getConcept(String s, String mainDir) {

		BufferedReader br;
		String line = "";
		concept.clear();
		try {
			br = new BufferedReader(new FileReader(mainDir + s));
			while ((line = br.readLine()) != null) {
				concept.put(line.split(",")[0], Double.parseDouble(line
						.split(",")[1]));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException,
			InterruptedException {
		CompareAveNGD cp = new CompareAveNGD();
		cp.caculBatchDistance(topicDir, centerFile, writeFile, mainDir, centerSize);

	}

	public void caculBatchDistance(String topicDir, String centerFile,
			String writeFile, String batch, int centerSize)
			throws IOException {
		long StartTime = System.currentTimeMillis(); // 取出目前時間

		String[] s = new String[3];
		getCenter(topicDir + centerFile, centerSize);
		File dir = new File(batch);
		BufferedWriter bw = new BufferedWriter(new FileWriter(topicDir
				+ writeFile, true));
		double total = 0;
		//改用WIKI
		try {
			ServerUtil.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String f : dir.list()) {
			getConcept(f, batch);

			Iterator<String> i1 = center.keySet().iterator();
			double sum = 0;
			
			while (i1.hasNext()) {
				s[0] = i1.next();
				Iterator<String> i2 = concept.keySet().iterator();
				while (i2.hasNext()) {
					s[1] = i2.next();
					System.out.println(s[0] + "," + s[1]);
					s[2] = "\"" + s[0] + "\",\"" + s[1] + "\"";
					//search_term(s[2].replace(",", "+"));
					
					SolrQuery query = new SolrQuery();
					query.setQuery("+\""+s[0]+"\" +\""+s[1]+"\"");
					System.err.println("Query: +\""+s[0]+"\" +\""+s[1]+"\"");
					QueryResponse rsp = ServerUtil.execQuery(query);
					SolrDocumentList docs = rsp.getResults();
					mValue = docs.getNumFound();
					
					
					double NGD = NGD_calculate.NGD_cal(center.get(s[0]),
							concept.get(s[1]), mValue);
					System.out.println(mValue);
					if (NGD > 1)
						NGD = 1;
					if (NGD < 0)
						NGD = 0;
					sum = sum + NGD;
				}

			}
			double avg1 = sum
					/ (center.keySet().size() * concept.keySet().size());
			System.out.println(f + ":" + avg1);
			bw.write(f + ":" + avg1);
			bw.newLine();
			bw.flush(); // 清空緩衝區
			total = total + avg1;
		}
		bw.close();
		System.out.println(System.currentTimeMillis()-StartTime);
	}

	public double caculBatchDistance(String topicDir, String centerFile,
			String writeFile, String chunk, target_topic t, int cenSize)
			throws IOException {
		long StartTime = System.currentTimeMillis(); // 取出目前時間
		String[] s = new String[3];
		int data_index = 0;
		double tp = 0;
		double fp = 0;
		double tn = 0;
		double fn = 0;
		double precision = 0;
		double recall = 0;
		double fmeasure = 0;
		getCenter(topicDir + centerFile, cenSize);
		File dir = new File(chunk);
		BufferedWriter bw = new BufferedWriter(new FileWriter(topicDir
				+ writeFile, true));
		double total = 0;
		
		//改用WIKI
		try {
			ServerUtil.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String f : dir.list()) {
			getConcept(f, chunk);
			Iterator<String> i1 = center.keySet().iterator();
			double sum = 0;
			while (i1.hasNext()) {
				s[0] = i1.next();
				Iterator<String> i2 = concept.keySet().iterator();

				while (i2.hasNext()) {
					s[1] = i2.next();
					s[2] = "\"" + s[0] + "\",\"" + s[1] + "\"";
					//search_term(s[2].replace(",", "+"));
					
					//改用WIKI
					SolrQuery query = new SolrQuery();
					query.setQuery("+\""+s[0]+"\" +\""+s[1]+"\"");
					System.err.println("Query: +\""+s[0]+"\" +\""+s[1]+"\"");
					QueryResponse rsp = ServerUtil.execQuery(query);
					SolrDocumentList docs = rsp.getResults();
					mValue = docs.getNumFound();
					
					double NGD = NGD_calculate.NGD_cal(center.get(s[0]),
							concept.get(s[1]), mValue);
					if (NGD > 1)
						NGD = 1;
					if (NGD < 0)
						NGD = 0;
					sum = sum + NGD;

				}

			}

			double avg1 = sum
					/ (center.keySet().size() * concept.keySet().size());
			data_index = Integer.parseInt(f.split("_")[0]);
			if (data_index >= t.lower && data_index <= t.upper) {
				if (avg1 <= 0.9) {
					tp++;
				} else {
					fn++;
				}
			} else {
				if (avg1 <= 0.9) {
					fp++;
				} else {
					tn++;
				}
			}
			System.out.println(f + ":" + avg1 + " tp:" + tp + " fp:" + fp
					+ " tn:" + tn + " fn:" + fn);
			bw.write(f + ":" + avg1);
			bw.newLine();
			bw.flush(); // 清空緩衝區
			total = total + avg1;
		}
		precision = tp / (tp + fp);
		recall = tp / (tp + fn);
		fmeasure = (2 * precision * recall) / (precision + recall);

		bw.write("P:" + precision);
		bw.newLine();
		bw.write("R:" + recall);
		bw.newLine();
		bw.write("A:" + (tp + tn) / dir.list().length);
		bw.newLine();
		bw.write("F:" + fmeasure);
		bw.newLine();
		bw.close();
		System.out.println(System.currentTimeMillis()-StartTime);
		return fmeasure;
	}

}
