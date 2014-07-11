package tw.edu.ncu.sia.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTextArea;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import tw.edu.ncu.sia.util.ServerUtil;


public class IndexStatus {
	public static void indexed(String textArea, String s) throws Exception{
		//System.out.println("process: " + textArea);
		long runstartTime = System.currentTimeMillis(); // 取出目前時間
		//ServerUtil.initialize();
		// initialize query
		SolrQuery query = new SolrQuery();
		//show info of all files
		query.setQuery(textArea);
		QueryResponse rsp = ServerUtil.execQuery(query);
		SolrDocumentList docs = rsp.getResults();
		//System.out.println(docs.getNumFound());
		
		BufferedWriter bw;

		try {
			bw = new BufferedWriter(new FileWriter("citeulike/citeulike_Search1/" + s + "_"
					+ "google_output1.txt", true));
			
			if (docs.getNumFound() != 0)
			{
				String test = "約有 " + docs.getNumFound() + " 項結果"; 
				bw.write(String.valueOf(test));
				bw.newLine();
				bw.flush(); // 清空緩衝區
				bw.close(); // 關閉BufferedWriter物件
				// 建立運用緩衝區輸出資料至data.txt檔的BufferedWriter物件
				// ，並由bw物件參考引用
				// 將字串寫入檔案
			} else if (docs.getNumFound() == 0)
			{
				bw.write("約有 0 項結果\r\n");
				bw.close(); // 關閉BufferedWriter物件
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("finish: " + textArea);
		//System.out.println("此單字花費時間" + (System.currentTimeMillis() - runstartTime));
		
		//textArea.append("\n\n=== Show indexed docs ===");
		//textArea.append("\n 1. Docs indexed:" + docs.getNumFound());
	}
}