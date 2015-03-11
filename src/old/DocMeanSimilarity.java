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

//import ConceptCompare.ParserGetter;

public class DocMeanSimilarity extends HTMLEditorKit.ParserCallback {
	Map<String, Double> center = new HashMap<String, Double>();
	Map<String, Double> concept = new HashMap<String, Double>();
	Map<String, Integer> degree = new HashMap<String, Integer>();
	Map<String, Integer> core = new HashMap<String, Integer>();
	ArrayList<String> newAdd = new ArrayList<String>();
static String dirPath="Topic3/";
	class ParserGetter extends HTMLEditorKit {
		// purely to make this methods public
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	// 記錄是否將資料印出
	private boolean inHeader = false;
	private static int _sn = -1;
	// static private ArrayList<Double> num=new ArrayList<Double>();
	static double mValue = 0;

	public DocMeanSimilarity() {
	}

	// 將 Parse HTML 後的資料印出
	public void handleText(char[] text, int position) {
		if (inHeader) {
			// 印出 xxxx => <A HREF = ....> xxxx </A>
			// xxxx => HTML Tag A 的文字 (text)
			// System.out.println("handleText: " + new String(text));

			BufferedWriter bw;

			// File out = new File(_sn + "_" + "google_output1.txt");
			// FileOutputStream outFile = new FileOutputStream(out, true);
			// OutputStreamWriter bw = new OutputStreamWriter(outFile,
			// "UTF-8");
			if (String.valueOf(text).contains("約有 ")
					|| String.valueOf(text).contains(" 項結果")) {
				String value = String.valueOf(text).replaceAll(",", ""); // 約有
				// 173,000
				// 項結果

				double value1 = Double.parseDouble(value.split(" ")[value
						.split(" ").length - 2]);

				// num.add(value1);
				// System.out.println("update1" + value1);
				mValue = value1;

				// set.add(value1);
				// String key = ((String) datas[count]);
				// set.add(key + "," + value1);
				// System.out.println(key+"==>"+value1);
				// count++;
			} else if (String.valueOf(text).contains("找不到和您的查詢")) {
				double value1 = Double.parseDouble("0");
				// System.out.println("update2");
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

	void pairTest(String s) {
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		HTMLEditorKit.ParserCallback callback = new DocMeanSimilarity();
		// for(String s:data){
		// line = BufferedStream.readLine();
		// if (stop_list.contains(line.toLowerCase())) {
		// System.out.println("skip:"+line+"============");
		// } else {
		// String i1 = "\"" + line + "\"";
		String i1 = s;
		// long runstartTime = System.currentTimeMillis(); // 取出目前時間
		// System.out.println("process: " + i1);
		// 輸入欲分析的網頁
		// URL u = new URL("http://www.yam.com");
		String j = "http://www.google.com.tw/search?aq=f&sourceid=chrome&ie=UTF-8&q=";
		// String f = "&btnG=搜尋&aq=f&aqi=&aql=&oq=&gs_rfai=";
		String http = j + i1;
		// System.out.println(http);

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
			// InputStream in = u.openStream();
			BufferedInputStream in = new BufferedInputStream(urlConnection
					.getInputStream());
			InputStreamReader r = new InputStreamReader(in, "UTF-8");
			// System.out.println(r);

			// 呼叫 parse method 開始進行 Parse HTML
			// _sn = no;
			parser.parse(r, callback, true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				IP_change();
				pairTest(s);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// in.close();
		// r.close();
		// Thread.sleep(1000);// 隔1秒搜尋一次
		// System.out.println("finish: " + i1);
		// System.out.println(System.currentTimeMillis()-runstartTime);
		// System.out.println(num);
		// }

		// }
		// System.out.println(mValue);

	}

	double NGDandGCD(double x, double y, double m) {
		// System.out.println(num);
		// if(num.size()==3){
		// double x = num.get(0);
		// double y = num.get(1);
		double a = Math.log10(x) / Math.PI; // 圓一半徑
		double b = Math.log10(y) / Math.PI; // 圓二半徑

		double r1 = 1.78 - Math.sqrt(a);
		double r2 = 1.78 - Math.sqrt(b);
		double d = Math.pow(r1, 2) + Math.pow(r2, 2);
		double GCD = Math.sqrt(d);
		// System.out.println("GCD="+GCD);

		double logX = Math.log10(x);
		double logY = Math.log10(y);
		double logM = Math.log10(m);
		double logN = 9.906;

		double NGD = (Math.max(logX, logY) - logM)
				/ (logN - Math.min(logX, logY));
		// System.out.println("NGD="+NGD);
		// }
		return NGD;
	}

	void getCenter(String path) {

		BufferedReader br;

		// br2 = new BufferedReader(new FileReader(no + "_" + "stem.txt"));
		String line = "";
		// try {
		center.clear();
		double sum = 0;
		try {
			br = new BufferedReader(new FileReader(dirPath +"Set/" + path));
			while ((line = br.readLine()) != null) {
				// c1.put();
				// sum += Double.parseDouble(line.split(",")[2]);
				center.put(line.split(",")[0], Double.parseDouble(line
						.split(",")[1]));
				// }
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void getConcept(String s) {

		BufferedReader br;

		// br2 = new BufferedReader(new FileReader(no + "_" + "stem.txt"));
		String line = "";
		// try {
		concept.clear();
		double sum = 0;
		try {
			br = new BufferedReader(new FileReader(dirPath+"Set/" + s));
			while ((line = br.readLine()) != null) {
				// c1.put();
				// sum += Double.parseDouble(line.split(",")[2]);
				concept.put(line.split(",")[0], Double.parseDouble(line
						.split(",")[1]));
				// }
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
		long StartTime = System.currentTimeMillis(); // 取出目前時間
		DocMeanSimilarity test = new DocMeanSimilarity();
		String[] s = new String[3];
		Scanner input = new Scanner(System.in);

		// test.getCenter("MixTopic/center.txt");
		File dir = new File(dirPath+"Set/" );
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				dirPath+"threshold.txt", true));
		 double total = 0;
		String[] fs=dir.list();
		for (int i=0;i<fs.length;i++) {
			
			test.getCenter(fs[i]);
			for (int j=i+1;j<fs.length;j++) {
//				if (fs.equals(f2)) {
//				} else {

					test.getConcept(fs[j]);
					// test.mValue = 100;
					Iterator<String> i1 = test.center.keySet().iterator();
					double sum = 0;

					while (i1.hasNext()) {
						s[0] = i1.next();
						Iterator<String> i2 = test.concept.keySet().iterator();
						while (i2.hasNext()) {
							s[1] = i2.next();

							s[2] = "\"" + s[0] + "\"+\"" + s[1] + "\"";
							test.pairTest(s[2]);
							// if (rel_loader.Rel_exist(s[2]) != -1) {
							double NGD = test.NGDandGCD(test.center.get(s[0]),
									test.concept.get(s[1]), mValue);
							// System.out.println(mValue);
							System.out.println(s[0] + "," + s[1] + ":" + NGD);
							if (NGD > 1)
								NGD = 1;
							if (NGD < 0)
								NGD = 0;

							sum = sum + NGD;

						}

					}
					double avg1 = sum
							/ (test.center.keySet().size() * test.concept
									.keySet().size());
					total = total + avg1;
					bw.write(fs[i] + "<=>" + fs[j] + ":" + avg1);
					bw.newLine();
					bw.flush(); // 清空緩衝區
//				}
			}
//			System.out.println(total / (dir.list().length - 1));
			
//			bw.newLine();
//			bw.flush(); // 清空緩衝區
		}
		bw.write("avg="+total / ((fs.length*(fs.length-1))/2));
		bw.newLine();
		bw.flush(); // 清空緩衝區
		// System.out.println("AVG2=" + avg2);
		// System.out.println("AVG3=" + avg3);
		// System.out.println(test.newAdd);

		// s[0]="\""+input.next()+"\"";
		// s[1]="\""+input.next()+"\"";

		// s[2]=s[0]+"+"+s[1];
		// long ProcessTime = System.currentTimeMillis() - StartTime; // 計算處理時間
		// AverageTime += ProcessTime; // 累積計算時間
		// System.out.println(ProcessTime);
	}

	private static void IP_change() throws IOException, InterruptedException {
		String ip1 = InetAddress.getLocalHost().toString().split("/")[1];
		// URL test=new URL("http://www.google.com/");
		// boolean flag;
		System.out.println("ip1=" + ip1);
		InetAddress test = InetAddress.getByName("140.115.1.254");
		while (ip1.equals(InetAddress.getLocalHost().toString().split("/")[1])
				|| !test.isReachable(5000)) {
			try {
				Process p = Runtime.getRuntime().exec("IP_1.bat");
				System.out.println(InetAddress.getLocalHost().toString().split(
						"/")[1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(5000);
		}
		System.out.println("final ip="
				+ InetAddress.getLocalHost().toString().split("/")[1]);
	}
}
