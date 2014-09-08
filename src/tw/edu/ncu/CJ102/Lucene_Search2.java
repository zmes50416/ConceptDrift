package tw.edu.ncu.CJ102;
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
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

//import Google_Search1.ParserGetter;
/**
 * Search MultiTerm should merge into Lucenne_Search1
 * @author TingWen
 *
 */
public class Lucene_Search2 extends HTMLEditorKit.ParserCallback {
	class ParserGetter extends HTMLEditorKit {
		// purely to make this methods public
		public HTMLEditorKit.Parser getParser() {
			return super.getParser();
		}
	}

	// 記錄是否將資料印出
	private boolean inHeader = false;
	private static String _sn = "";
	String i1;//Unknown Field
	int l = 1;

	public Lucene_Search2() {
	}

	public void doit(String fileName) throws IOException {
		String readPath = SettingManager.getSetting(SettingManager.PairDir);
		String writePath = SettingManager.getSetting(SettingManager.IndexMultiTermDir);
//		System.out.println("處理檔案"+fileName+"中...");
		//TODO　Clean Old Dir
		
		FileReader FileStream = new FileReader(readPath + fileName);
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		
		BufferedWriter bwTimer = new BufferedWriter(new FileWriter("Util/time/pairs.txt", true));
		
		String line;
		
		long t1 = 0 , t2 = 0;
		long sum = 0;
		int l = 0;

		while ((line = BufferedStream.readLine()) != null) {
			_sn = fileName;
			t1 = System.currentTimeMillis();
			
			SolrSearcher solr = new SolrSearcher();
			long num = solr.searchIndexed("+"+line, _sn);
			try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(writePath + fileName, true));
			
			String test = line + ", " + num; 
			bw.write(String.valueOf(test));
			bw.newLine();
			bw.flush(); // 清空緩衝區
			bw.close(); // 關閉BufferedWriter物件

		} catch (IOException e) {
			e.printStackTrace();
		}
			
			t2 = System.currentTimeMillis();
			l++;
			sum += (t2-t1);
			
		}
		bwTimer.write(l+":"+sum);
		bwTimer.newLine();
		bwTimer.close();
		BufferedStream.close();
//		System.out.println("處理檔案"+fileName+"處理完畢");
	}


	public static void main(String[] args) {

		System.out.println("開始測試方法");
		for(File f: new File(SettingManager.getSetting(SettingManager.PairDir)).listFiles()){
			try {
				new Lucene_Search2().doit(f.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("測試完畢");

	}
}