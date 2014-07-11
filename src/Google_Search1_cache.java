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
import java.util.Enumeration;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.util.*;
import java.text.*;

public class Google_Search1_cache extends HTMLEditorKit.ParserCallback {
	class ParserGetter extends HTMLEditorKit {
		// purely to make this methods public
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	// 記錄是否將資料印出
	private boolean inHeader = false;
	private static int _sn = -1;
	static int choose = 1;
	static java.util.Date d1 = new java.util.Date();
    static SimpleDateFormat sdfmt = new SimpleDateFormat("yyyy-MM-dd");
	
	public Google_Search1_cache() {
	}

	// 將 Parse HTML 後的資料印出
	public void handleText(char[] text, int position) {
		if (inHeader) {

			BufferedWriter bw, bw2 = null;
			try {
				bw = new BufferedWriter(new FileWriter("Search1/" + _sn + "_"
						+ "google_output1.txt", true));
				//先把Cache1註掉
				//bw2 = new BufferedWriter(new FileWriter("Cache/cache1.txt", true));
				if(Google_Search1_cache.choose == 1)
					bw2 = new BufferedWriter(new FileWriter("Cache/ai.txt", true));
				else if(Google_Search1_cache.choose == 2)
					bw2 = new BufferedWriter(new FileWriter("Cache/art.txt", true));
				else if(Google_Search1_cache.choose == 3)
					bw2 = new BufferedWriter(new FileWriter("Cache/biology.txt", true));
				else if(Google_Search1_cache.choose == 4)
					bw2 = new BufferedWriter(new FileWriter("Cache/computer_science.txt", true));
				else if(Google_Search1_cache.choose == 5)
					bw2 = new BufferedWriter(new FileWriter("Cache/ir.txt", true));

				if (String.valueOf(text).contains("約有 ")
						|| String.valueOf(text).contains(" 項結果")) {

					bw.write(String.valueOf(text));
					bw.newLine();
					bw.flush(); // 清空緩衝區
					bw.close(); // 關閉BufferedWriter物件
					// 建立運用緩衝區輸出資料至data.txt檔的BufferedWriter物件
					// ，並由bw物件參考引用
					// 將字串寫入檔案

					bw2.write(String.valueOf(text)+ "=" + sdfmt.format(d1) +"=1");
					bw2.newLine();
					bw2.flush();
					bw2.close();

				} else if (String.valueOf(text).contains("找不到和您的查詢")) {

					bw.write("約有 0 項結果\n");
					bw.close(); // 關閉BufferedWriter物件
					bw2.write("約有0項結果\n");
					bw2.close();
					// l++;
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

	public void doit(int no) throws IOException, ParseException {
		BufferedWriter bw = null;
		int flag = 0;
		String line;
		String line2;
		int l = 0, limit = 0;
		FileReader FileStream2 = null;
		
		File f = new File("Search1/" + no + "_" + "google_output1.txt");
		f.delete();
		// try {
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		HTMLEditorKit.ParserCallback callback = new Google_Search1_cache();
		FileReader FileStream = new FileReader("POS_filter/" + no + "_"
				+ "filter_output1.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		
		//FileReader FileStream2 = new FileReader("Cache/cache1.txt");
		//BufferedReader BufferedStream2 = new BufferedReader(FileStream2);
		
		//取出先前選擇的Cache number
		FileReader choosestream = new FileReader("Cache_choose/" + no + "_" + "cache_choose1.txt");
		BufferedReader chooseBufferedStream2 = new BufferedReader(choosestream);
		Google_Search1_cache.choose = Integer.parseInt(chooseBufferedStream2.readLine());
		
		if(Google_Search1_cache.choose == 1)
			FileStream2 = new FileReader("Cache/ai.txt");
		else if(Google_Search1_cache.choose == 2)
			FileStream2 = new FileReader("Cache/art.txt");
		else if(Google_Search1_cache.choose == 3)
			FileStream2 = new FileReader("Cache/biology.txt");
		else if(Google_Search1_cache.choose == 4)
			FileStream2 = new FileReader("Cache/computer_science.txt");
		else if(Google_Search1_cache.choose == 5)
			FileStream2 = new FileReader("Cache/ir.txt");
		
		BufferedReader BufferedStream2 = new BufferedReader(FileStream2);
		
		// ArrayList<String> stop_list = Stop_Loader.loadList("stop_list.txt");
		/*while ((line = BufferedStream.readLine()) != null)
		{
			_sn = no;
			search_term(line);
		}*/
		
		while ((line = BufferedStream.readLine()) != null)
		{
			while((line2 = BufferedStream2.readLine()) != null)
			{
				//if(limit >= 200)
				//{
					//limit = 0;
					//break;
				//}
				//else
					//limit++;
				
				//Date beginDate= sdfmt.parse("2007-12-24");
				String endDateString = line2.split("=")[2];
				Date endDate= sdfmt.parse(endDateString); //字串轉日期
				long day=(d1.getTime() - endDate.getTime())/(24*60*60*1000);
				//System.out.println("相隔的天數=" + day);
				
				if(day <= 30) //如果在一個月內才使用來比對
				{
					long runstartTime = System.currentTimeMillis();
				
					l = 1;
					flag = 0;
					if(line2.split("=")[0].equals(line))
					{
						System.out.println("!!與Cache比中!!");
						System.out.println("line1" + line);
						System.out.println("line2" + line2);
						flag = 1;
						bw = new BufferedWriter(new FileWriter("Search1/" + no + "_"
								+ "google_output1.txt", true));
						if (String.valueOf(line2.split("=")[1]).contains("約有 ") || String.valueOf(line2.split("=")[1]).contains(" 項結果"))
						{
							bw.write(String.valueOf(line2.split("=")[1]));
							bw.newLine();
							bw.flush(); // 清空緩衝區
							bw.close(); // 關閉BufferedWriter物件
							//建立運用緩衝區輸出資料至data.txt檔的BufferedWriter物件
							//，並由bw物件參考引用
							//將字串寫入檔案
						}
						else if (String.valueOf(line2.split("=")[1]).contains("找不到和您的查詢"))
						{
							bw.write("約有 0 項結果\n");
							bw.close(); // 關閉BufferedWriter物件
						}
						System.out.println(System.currentTimeMillis() - runstartTime);
						break;
					}
				}
			}

			//確定沒有在cache找到
			if(flag == 0 && l == 1)
			{
				if(Google_Search1_cache.choose == 1)
					bw = new BufferedWriter(new FileWriter("Cache/ai.txt", true));
				else if(Google_Search1_cache.choose == 2)
					bw = new BufferedWriter(new FileWriter("Cache/art.txt", true));
				else if(Google_Search1_cache.choose == 3)
					bw = new BufferedWriter(new FileWriter("Cache/biology.txt", true));
				else if(Google_Search1_cache.choose == 4)
					bw = new BufferedWriter(new FileWriter("Cache/computer_science.txt", true));
				else if(Google_Search1_cache.choose == 5)
					bw = new BufferedWriter(new FileWriter("Cache/ir.txt", true));
				//bw = new BufferedWriter(new FileWriter("Cache/cache1.txt", true));
				bw.write(line + "=");
				bw.flush();
				bw.close();
				_sn = no;
				search_term(line);
			}
			
			//當一開始cache.txt為空檔時，要查第一次
			if(l == 0)
			{
				if(Google_Search1_cache.choose == 1)
					bw = new BufferedWriter(new FileWriter("Cache/ai.txt", true));
				else if(Google_Search1_cache.choose == 2)
					bw = new BufferedWriter(new FileWriter("Cache/art.txt", true));
				else if(Google_Search1_cache.choose == 3)
					bw = new BufferedWriter(new FileWriter("Cache/biology.txt", true));
				else if(Google_Search1_cache.choose == 4)
					bw = new BufferedWriter(new FileWriter("Cache/computer_science.txt", true));
				else if(Google_Search1_cache.choose == 5)
					bw = new BufferedWriter(new FileWriter("Cache/ir.txt", true));
				bw.write(line + "=");
				bw.flush();
				bw.close();
				_sn = no;
				search_term(line);
			}
			
			//確保每次line2都從一開始被比對
			if(Google_Search1_cache.choose == 1)
				FileStream2 = new FileReader("Cache/ai.txt");
			else if(Google_Search1_cache.choose == 2)
				FileStream2 = new FileReader("Cache/art.txt");
			else if(Google_Search1_cache.choose == 3)
				FileStream2 = new FileReader("Cache/biology.txt");
			else if(Google_Search1_cache.choose == 4)
				FileStream2 = new FileReader("Cache/computer_science.txt");
			else if(Google_Search1_cache.choose == 5)
				FileStream2 = new FileReader("Cache/ir.txt");
			BufferedStream2 = new BufferedReader(FileStream2);
		}
	}

	private void search_term(String i1) {
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		HTMLEditorKit.ParserCallback callback = new Google_Search1_cache();
		long runstartTime = System.currentTimeMillis(); // 取出目前時間
		System.out.println("process: " + i1);
		// 輸入欲分析的網頁
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
			BufferedInputStream in = new BufferedInputStream(urlConnection
					.getInputStream());
			InputStreamReader r = new InputStreamReader(in, "UTF-8");
			parser.parse(r, callback, true);
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
			System.out.println("IP被鎖，重新取得IP");
			
				IP_Operation.IP_change();//換IP
				System.out.println("重新取得字詞("+i1+")搜尋結果");
			search_term(i1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public static void main(int i) throws IOException, ParseException {
	    //取得當日日期
		Calendar cal = Calendar.getInstance();
	    d1 = cal.getTime();
	    //System.out.println("Now Date = " + sdfmt.format(d1));
		new Google_Search1_cache().doit(i);
	}

	public static void main(String args[]) throws IOException, ParseException {

		long StartTime = System.currentTimeMillis(); // 取出目前時間
		main(1);
		System.out.println(System.currentTimeMillis() - StartTime);
	}
}