package tw.edu.ncu.CJ102;

import java.io.*;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * 
 * @author 102蘇鼎文
 * 改名學長的IndexStatus，改為更符合的名稱
 * 使用此類別來取代ServerUtil
 *
 */

public class SolrSearcher {
	private static CommonsHttpSolrServer server=null; // Singleton Design pattern only access it by getServer() to ensure connection
	private static Boolean initialize(){
		String url = SettingManager.getSetting("solrURL");
		try {
			server = new CommonsHttpSolrServer(url); // last two parameter will determined by Computer Power. Higher mean more speedy Index
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
		server.setSoTimeout(3000); // socket read timeout
		server.setConnectionTimeout(3000);
		server.setDefaultMaxConnectionsPerHost(100);
		server.setMaxTotalConnections(100);
		server.setFollowRedirects(false); // defaults to false
		server.setAllowCompression(false);
		server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
		return true;
	}

	public static CommonsHttpSolrServer getServer(){
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
		//execQuery can return null, will break the program!
		QueryResponse rsp = execQuery(query);
		SolrDocumentList docs = rsp.getResults();
		return docs.getNumFound();
	}
	public void search(){
		
	}
	public void getHits(String key){
		//TODO reimplement the method from UtilServer
	}
}