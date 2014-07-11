import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

//import Google_Search1.ParserGetter;

public class Google_Search2 extends HTMLEditorKit.ParserCallback {
	class ParserGetter extends HTMLEditorKit {
		// purely to make this methods public
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	// 記錄是否將資料印出
	private boolean inHeader = false;
	private static int _sn = -1;
	String i1;
	int l = 1;

	public Google_Search2() {
	}

	// 將 Parse HTML 後的資料印出
	public void handleText(char[] text, int position) {
		if (inHeader) {
			// 印出 xxxx => <A HREF = ....> xxxx </A>
			// xxxx => HTML Tag A 的文字 (text)
			// System.out.println("handleText: " + new String(text));

			BufferedWriter bw;
			try {
				// File out = new File(_sn + "_" + "google_output1.txt");
				// FileOutputStream outFile = new FileOutputStream(out, true);
				// OutputStreamWriter bw = new OutputStreamWriter(outFile,
				// "UTF-8");
				// System.out.println(String.valueOf(text).contains("約有 "));
				bw = new BufferedWriter(new FileWriter("Search2/" + _sn + "_"
						+ "google_output2.txt", true));
				if (String.valueOf(text).contains("約有 ")
						|| String.valueOf(text).contains(" 項結果")) {

					System.out.println("write==>" + l);
					bw.write(String.valueOf(text));
					bw.newLine();
					// bw.write("\r\n");

					l++;
					bw.flush(); // 清空緩衝區
					bw.close(); // 關閉BufferedWriter物件
					// 建立運用緩衝區輸出資料至data.txt檔的BufferedWriter物件
					// ，並由bw物件參考引用
					// 將字串寫入檔案

				} else if (String.valueOf(text).contains("找不到和您的查詢")) {

					bw.write("約有 0 項結果\n");
					bw.close(); // 關閉BufferedWriter物件
					l++;
				}

			} catch (IOException e) {
				e.printStackTrace();
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

	public void doit(int no) throws IOException {
		// try {

		FileReader FileStream = new FileReader("Pairs/" + no + "_"
				+ "pairs.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		String line;
		// ArrayList<String> stop_list = Stop_Loader.loadList("stop_list.txt");
		// int l=1;
		while ((line = BufferedStream.readLine()) != null) {
			// line = BufferedStream.readLine();
			// if (stop_list.contains(line.toLowerCase())) {
			// System.out.println("skip:"+line+"============");
			// } else {
			// String i1 = "\"" + line + "\"";
			i1 = line;
			_sn = no;
			line = URLEncoder.encode(line, "UTF-8");
			search_pair(line);
			// in.close();
			// r.close();
			// if(l%150==0)
			// {
			// String ip1=InetAddress.getLocalHost().toString().split("/")[1];
			// //URL test=new URL("http://www.google.com/");
			// //boolean flag;
			// System.out.println("ip1="+ip1);
			// InetAddress test=InetAddress.getByName("140.115.1.254");
			// while(ip1.equals(InetAddress.getLocalHost().toString().split("/")[1])||!test.isReachable(5000))
			// {
			// try {
			// Process p=Runtime.getRuntime().exec("IP_1.bat");
			// System.out.println(InetAddress.getLocalHost().toString().split("/")[1]);
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// Thread.sleep(5000);
			// }
			// System.out.println("final ip="+InetAddress.getLocalHost().toString().split("/")[1]);
			// }
			// //Thread.sleep(1000);// 隔1秒搜尋一次
			// System.out.println("finish: " + i1);
			// l++;
			// System.out.println(System.currentTimeMillis()-runstartTime);
			// }

		}

		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	private void search_pair(String i1) {
		try {
			ParserGetter kit = new ParserGetter();
			HTMLEditorKit.Parser parser = kit.getParser();
			HTMLEditorKit.ParserCallback callback = new Google_Search2();
			long runstartTime = System.currentTimeMillis(); // 取出目前時間

			System.out.println("process: " + i1 + "==>");
			// 輸入欲分析的網頁
			// URL u = new URL("http://www.yam.com");
			String j = "http://www.google.com.tw/search?aq=f&sourceid=chrome&ie=UTF-8&q=";
//			String j = "http://www.google.com.tw/search?aq=f&sourceid=chrome&ie=Big5&q=";
			// String f = "&btnG=搜尋&aq=f&aqi=&aql=&oq=&gs_rfai=";
			String http = j + i1;
			// System.out.println(http);

			URL u = new URL(http);
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
			// in.close();
			// r.close();
			Thread.sleep(0);// 隔1秒搜尋一次
			System.out.println("finish: " + i1);
			System.out.println(System.currentTimeMillis() - runstartTime);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IP被鎖，重新取得IP");

			IP_Operation.IP_change();// 換IP
			System.out.println("重新取得字詞(" + i1 + ")搜尋結果");

			search_pair(i1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(int i) throws IOException {

		File f = new File("Search2/" + i + "_" + "google_output2.txt");
		f.delete();
		new Google_Search2().doit(i);

	}

	public static void main(String args[]) throws IOException {

		// new Google_Search1().doit(i);
		long StartTime = System.currentTimeMillis(); // 取出目前時間
		main(1);
		System.out.println(System.currentTimeMillis() - StartTime);

	}
}
