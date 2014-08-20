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

import tw.edu.ncu.sia.index.IndexStatus2;
import tw.edu.ncu.sia.util.ServerUtil;

//import Google_Search1.ParserGetter;
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
	String i1;
	int l = 1;

	public Lucene_Search2() {
	}

	public void doit(String no) throws IOException {
		// try {
		System.out.println("處理檔案"+no+"中...");
		File f = new File("citeulike/citeulike_Search2/" + no + "_" + "google_output2.txt");
		f.delete();
		
		FileReader FileStream = new FileReader("citeulike/citeulike_Pairs/" + no + "_"
				+ "pairs.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("time/pairs.txt", true));
		
		String line;
		
		long t1 =0 , t2 = 0;
		long sum = 0;
		int l = 0;
		
		try {
			ServerUtil.initialize();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while ((line = BufferedStream.readLine()) != null) {
			_sn = no;
			t1 = System.currentTimeMillis();
			
			IndexStatus2 qq = new IndexStatus2();
			try {
				qq.indexed("+"+line, _sn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			t2 = System.currentTimeMillis();
			l++;
			sum += (t2-t1);
			
		}
		bw.write(l+":"+sum);
		bw.newLine();
		bw.close();
		BufferedStream.close();
		System.out.println("處理檔案"+no+"處理完畢");
	}

	public static void main(String i) throws IOException {
		new Lucene_Search2().doit(i);

	}

	public static void main(String args[]) throws IOException {

		long StartTime = System.currentTimeMillis(); // 取出目前時間
		/*File F = new File("citeulike/citeulike_Pairs/");
		for(File f : F.listFiles()){
			//System.out.println(f.getName().split("_")[0]);
			main(f.getName().split("_")[0]);
		}*/
		
		//找出缺少的文件來處理
		File F = new File("citeulike/citeulike_Pairs/");
		for(File f : F.listFiles()){
			File file = new File("citeulike/citeulike_Search2/"+f.getName().split("_")[0]+"_google_output2.txt");
			if(!file.exists()){
				System.out.println(file.getName().split("_")[0]);
				main(file.getName().split("_")[0]);
			}
			
		}
		
		//System.out.println(System.currentTimeMillis() - StartTime);

	}
}