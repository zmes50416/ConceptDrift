import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import tw.edu.ncu.sia.util.ServerUtil;

//import ConceptCompare.ParserGetter;

public class TermToArt extends HTMLEditorKit.ParserCallback {
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
	static double Value[] = new double[2];

	static int n = 0;

	public TermToArt() {
	}

	// 將 Parse HTML 後的資料印出
	public void handleText(char[] text, int position) {
		if (inHeader) {

			BufferedWriter bw;
			if (String.valueOf(text).contains("約有 ")
					|| String.valueOf(text).contains(" 項結果")) {
				String value = String.valueOf(text).replaceAll(",", ""); // 約有
				// 173,000
				// 項結果

				double value1 = Double.parseDouble(value.split(" ")[value
						.split(" ").length - 2]);
				Value[n] = value1;
			} else if (String.valueOf(text).contains("找不到和您的查詢")) {
				double value1 = Double.parseDouble("0");
				System.out.println("update2");
				Value[n] = 0;
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

	void search_pair(String s) {
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		HTMLEditorKit.ParserCallback callback = new TermToArt();
		String i1 = s;
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
	parser.parse(r, callback, true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IP被鎖，重新取得IP");
			
			IP_Operation.IP_change();//換IP
			System.out.println("重新取得字詞("+s+")搜尋結果");
			search_pair(s);
			
		}
		

	}
	

	void getArt(String file,Map<String, Double> concept) {

		BufferedReader br;
		String line = "";
		concept.clear();
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
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
	//語意中心字詞計分
	public  double Scoring(String term,double volume,String path,int colSize) throws IOException {

		String[] s = new String[3];
		Map<String, Double> concept = new HashMap<String, Double>();
		int d = 0;
		double Dw = 0;
		double m = 0;
		File dir=new File(path);
		String files[]=dir.list();
		for (String f:files) {
			getArt(path+f,concept);
			Iterator<String> i2 = concept.keySet().iterator();
			try {
				ServerUtil.initialize();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (i2.hasNext()) {
				s[1] = i2.next();
				s[2] = "\"" + term + "\"+\"" + s[1] + "\"";

				long start=System.currentTimeMillis();
					n = 1;
					//search_pair(s[2]);
					
					try {
						SolrQuery query = new SolrQuery();
						query.setQuery("+\""+term+"\" +\""+s[1]+"\"");
						System.err.println("Query: +\""+term+"\" +\""+s[1]+"\"");
						
						QueryResponse rsp = ServerUtil.execQuery(query);
						SolrDocumentList docs = rsp.getResults();
						m = docs.getNumFound();
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(volume == 1) //避免分母為零
						volume++;
					
					double NGD = NGD_calculate.NGD_cal(volume, concept
							.get(s[1]), m);
					if (NGD < 0)
						NGD = 0;
					System.out.println(s[2] + "==>" + (start-System.currentTimeMillis()));
					if (NGD < 0.6) {
						d++;
						if (d >= 3) {
							System.out.println(Dw+"Add");
							Dw++;
							d=0;
							break;
						}
					}
			}
			d=0;
		}
		System.out.println("("+Dw+"/"+colSize+")");
		double score=((Dw)/(colSize))*(10/Math.log10(volume));
		System.out.println(term+":"+score);
		return score;
	}
}
