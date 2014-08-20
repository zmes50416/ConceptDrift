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
import java.text.ParseException;
import java.util.Enumeration;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class cache_choose extends HTMLEditorKit.ParserCallback
{

	public cache_choose() {
	}

	public void doit(int no) throws IOException
	{
		long runstartTime = System.currentTimeMillis();
		BufferedWriter bw;
		int flag = 0;
		String line, c1, c2, c3, c4, c5;
		double c1_nij = 0, c2_nij = 0, c3_nij = 0, c4_nij = 0, c5_nij = 0;
		double c1_wordcount = 0, c2_wordcount = 0, c3_wordcount = 0, c4_wordcount = 0, c5_wordcount = 0;
		double c1_tf = 0, c2_tf = 0, c3_tf = 0, c4_tf = 0, c5_tf = 0;
		double c1_tfidf = 0, c2_tfidf = 0, c3_tfidf = 0, c4_tfidf = 0, c5_tfidf = 0;
		double idf = 0;
		int D = 5; //總文件數
		int d = 0; //包含某字的文件數
		double highest = 0;
		int choose = 1;

		FileReader FileStream = new FileReader("POS_filter/" + no + "_"
				+ "filter_output1.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		
		FileReader cache_ai = new FileReader("Cache/ai.txt");
		BufferedReader Buffered_cache_ai = new BufferedReader(cache_ai);
		
		FileReader cache_art = new FileReader("Cache/art.txt");
		BufferedReader Buffered_cache_art = new BufferedReader(cache_art);
		
		FileReader cache_biology = new FileReader("Cache/biology.txt");
		BufferedReader Buffered_cache_biology = new BufferedReader(cache_biology);
		
		FileReader cache_computer_science = new FileReader("Cache/computer_science.txt");
		BufferedReader Buffered_cache_computer_science = new BufferedReader(cache_computer_science);
		
		FileReader cache_ir = new FileReader("Cache/ir.txt");
		BufferedReader Buffered_cache_ir = new BufferedReader(cache_ir);

		while ((line = BufferedStream.readLine()) != null)
		{
			//初始化
			d = 0;
			c1_nij = 0;
			c2_nij = 0;
			c3_nij = 0;
			c4_nij = 0;
			c5_nij = 0;
			c1_wordcount = 0;
			c2_wordcount = 0;
			c3_wordcount = 0;
			c4_wordcount = 0;
			c5_wordcount = 0;
			
			while((c1 = Buffered_cache_ai.readLine()) != null)
			{
				flag = 0; //表示沒對中
				c1_wordcount++;
				if(c1.split("=")[0].equals(line))
				{
					flag = 1; //表示對中
					d++;
					c1_nij = Integer.parseInt(c1.split("=")[3]);
				}
			}
			
			while((c2 = Buffered_cache_art.readLine()) != null)
			{
				flag = 0; //表示沒對中
				c2_wordcount++;
				if(c2.split("=")[0].equals(line))
				{
					flag = 1; //表示對中
					d++;
					c2_nij = Integer.parseInt(c2.split("=")[3]);
				}
			}
			
			while((c3 = Buffered_cache_biology.readLine()) != null)
			{
				flag = 0; //表示沒對中
				c3_wordcount++;
				if(c3.split("=")[0].equals(line))
				{
					flag = 1; //表示對中
					d++;
					c3_nij = Integer.parseInt(c3.split("=")[3]);
				}
			}
			
			while((c4 = Buffered_cache_computer_science.readLine()) != null)
			{
				flag = 0; //表示沒對中
				c4_wordcount++;
				if(c4.split("=")[0].equals(line))
				{
					flag = 1; //表示對中
					d++;
					c4_nij = Integer.parseInt(c4.split("=")[3]);
				}
			}
			
			while((c5 = Buffered_cache_ir.readLine()) != null)
			{
				flag = 0; //表示沒對中
				c5_wordcount++;
				if(c5.split("=")[0].equals(line))
				{
					flag = 1; //表示對中
					d++;
					c5_nij = Integer.parseInt(c5.split("=")[3]);
				}
			}
			
			c1_tf = c1_nij / c1_wordcount;
			c2_tf = c2_nij / c2_wordcount;
			c3_tf = c3_nij / c3_wordcount;
			c4_tf = c4_nij / c4_wordcount;
			c5_tf = c5_nij / c5_wordcount;
			
			idf = Math.log10(D / (1 + d));
			
			c1_tfidf = c1_tf * idf;
			c2_tfidf = c2_tf * idf;
			c3_tfidf = c3_tf * idf;
			c4_tfidf = c4_tf * idf;
			c5_tfidf = c5_tf * idf;
			
			if(highest < c1_tfidf)
			{
				highest = c1_tfidf;
				choose = 1;
			}
			else if(highest < c2_tfidf)
			{
				highest = c2_tfidf;
				choose = 2;
			}
			else if(highest < c3_tfidf)
			{
				highest = c3_tfidf;
				choose = 3;
			}
			else if(highest < c4_tfidf)
			{
				highest = c4_tfidf;
				choose = 4;
			}
			else if(highest < c5_tfidf)
			{
				highest = c5_tfidf;
				choose = 5;
			}
			
			System.out.println(line);
			System.out.println("c1_tfidf= " + c1_tfidf);
			System.out.println("c2_tfidf= " + c2_tfidf);
			System.out.println("c3_tfidf= " + c3_tfidf);
			System.out.println("c4_tfidf= " + c4_tfidf);
			System.out.println("c5_tfidf= " + c5_tfidf);
			System.out.println("最高的TF-IDF分數" + highest);
			System.out.println("選擇第" + choose + "個Cache");
			
			//重新讀取cache
			cache_ai = new FileReader("Cache/ai.txt");
			Buffered_cache_ai = new BufferedReader(cache_ai);
			cache_art = new FileReader("Cache/art.txt");
			Buffered_cache_art = new BufferedReader(cache_art);
			cache_biology = new FileReader("Cache/biology.txt");
			Buffered_cache_biology = new BufferedReader(cache_biology);
			cache_computer_science = new FileReader("Cache/computer_science.txt");
			Buffered_cache_computer_science = new BufferedReader(cache_computer_science);
			cache_ir = new FileReader("Cache/ir.txt");
			Buffered_cache_ir = new BufferedReader(cache_ir);
		}

		//File f = new File("Cache_choose/" + no + "_" + "cache_choose1.txt");
		//f.delete();

		bw = new BufferedWriter(new FileWriter("Cache_choose/" + no + "_" + "cache_choose1.txt"));
		//先把Cache1註掉
		//bw2 = new BufferedWriter(new FileWriter("Cache/cache1.txt", true));
		bw.write(String.valueOf(choose));
		bw.newLine();
		bw.flush(); // 清空緩衝區
		bw.close();
		
		System.out.println(System.currentTimeMillis() - runstartTime);
	}
	
	public static void main(int i) throws IOException, ParseException {

		new Google_Search1_cache().doit(i);

	}

	public static void main(String args[]) throws IOException, ParseException {

		long StartTime = System.currentTimeMillis(); // 取出目前時間
		main(1);
		System.out.println(System.currentTimeMillis() - StartTime);
	}
}