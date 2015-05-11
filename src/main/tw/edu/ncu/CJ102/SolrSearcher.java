package tw.edu.ncu.CJ102;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

/**
 * 
 * @author 102蘇鼎文
 * 改名學長的IndexStatus，改為更符合的名稱
 * 使用此類別來取代ServerUtil
 *
 */

public class SolrSearcher {
	private static HttpSolrServer server=null; // Singleton Design pattern only access it by getServer() to ensure connection
	public static HashMap<String,Double> hitmap = null;// cache map; value = google distance
	public static boolean temp = false;// Whether Cache is turn on or ont
	private static Boolean initialize(){
		
		if(temp){
			hitmap = new HashMap<String,Double>();
			File hitsFile = new File("temp/tempfile.txt"); //暫存搜尋結果數
			if(hitsFile.exists()){
				String line;
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(hitsFile));
				
				while((line = br.readLine()) != null){
					String q = line.split(",")[0];
					double hits = Double.parseDouble(line.split(",")[1]);
				
					hitmap.put(q, hits);				
				}
				br.close();
				}catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			else{
				hitsFile.getParentFile().mkdir();
				try {
					hitsFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		String url = SettingManager.getSetting(SettingManager.ServerURL);

		server = new HttpSolrServer(url); // last two parameter will determined by Computer Power. Higher mean more speedy Index
		server.setSoTimeout(5000); // socket read timeout
		server.setConnectionTimeout(5000);
		server.setDefaultMaxConnectionsPerHost(1000);
		server.setMaxTotalConnections(1000);
		server.setFollowRedirects(false); // defaults to false
		server.setAllowCompression(true);
		server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
		
		return true;
	}

	public static SolrServer getServer(){
		if(server==null){
				initialize();
		}
			return server;		
	}
	
	public static SolrDocumentList query(String queryStr) throws Exception {
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		SolrDocumentList docs = null;
		try {
			QueryResponse rsp = getServer().query(query);
			docs = rsp.getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return docs;
	}
	
	public static QueryResponse execQuery(SolrQuery s){
		try {
			return getServer().query(s);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public long searchIndexed(String queryString, String fileName){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryString);
		//execQuery() can return null, will break the program! Will happen often if your network is unstable!
		QueryResponse rsp = execQuery(query);
		SolrDocumentList docs = rsp.getResults();
		return docs.getNumFound();
	}
	
	public void search(String queryStr){
		// TODO should be re-check from old version it have not been used
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		//query.setSortField("file", ORDER.asc);
		query.setHighlight(true).setHighlightSnippets(3);
		query.setParam("hl.fl", "*"); 
		//hl.fragsize
		//query.setParam("hl.fragsize", "300");
		//only the filed was assigned in a query, the highlight snippet can be showed.
		query.setParam("hl.requireFieldMatch", "true"); 
		
		try {
			QueryResponse rsp = server.query(query);
			SolrDocumentList docs = rsp.getResults();
			System.out.println("Count:" + docs.getNumFound());
			System.out.println("Time:" + rsp.getQTime());
			for (SolrDocument doc : docs) {
				String id = (String) doc.getFieldValue("id");
				String file = (String) doc.getFieldValue("file");
				System.out.println("\n" + file);
				
				//highlight
				System.out.println("****highlight begin***");
				if (rsp.getHighlighting().get(id) != null) {
					List<String> highlightSnippets =
					rsp.getHighlighting().get(id).get("methodbody");
					List<String> highlightSnippets1 =
						rsp.getHighlighting().get(id).get("field");
					
					if(highlightSnippets!=null){
						for(String s : highlightSnippets){
							System.out.println("[methodbody] " + s);
						}
					}
					
					if(highlightSnippets1!=null){
						for(String s : highlightSnippets1){
							System.out.println("[field] " + s);
						}
					}
					
				}
				System.out.println("****highlight end***");
			
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Return the number of document found by the key term
	 * @param key
	 * @return 
	 */
	public static double getHits(String key) {
		// TODO reimplement the method from UtilServer
		double hits;
		SolrQuery query = new SolrQuery();
		query.setQuery(key);
		QueryResponse rsp = execQuery(query);
		SolrDocumentList docs = rsp.getResults();
		hits = docs.getNumFound();

		return hits;
	}
}