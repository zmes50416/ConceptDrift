package tw.edu.ncu.CJ102;

import java.io.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import tw.edu.ncu.sia.util.ServerUtil;

/**
 * 
 * @author 102蘇鼎文
 * 改名學長的IndexStatus，改為更符合的名稱
 *
 */

public class SolrSearcher {
	public void searchIndexed(String queryString, String fileName) throws Exception{
		SolrQuery query = new SolrQuery();
		query.setQuery(queryString);
		//execQuery can return null, will break the program!
		QueryResponse rsp = ServerUtil.execQuery(query);
		SolrDocumentList docs = rsp.getResults();
		
		BufferedWriter bw;

		try {
			bw = new BufferedWriter(new FileWriter(SettingManager.getSetting(SettingManager.IndexDir) + fileName, true));
			
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
	}
}