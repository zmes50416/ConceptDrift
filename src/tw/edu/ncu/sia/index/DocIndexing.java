package tw.edu.ncu.sia.index;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import javax.swing.JTextArea;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;

import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.common.SolrInputDocument;

import tw.edu.ncu.sia.util.Config;
import tw.edu.ncu.sia.util.ServerUtil;

/** Index all text files under a directory. */
public class DocIndexing {
	public static JTextArea textArea = null;
	private static int fileCount = 0;
	
	/** Index all text files under a directory. */
	public static void preProcess(String docName, JTextArea ta){
		try {
			ServerUtil.initialize();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		fileCount = 0;
		textArea = ta;
		File file = null;
		if(docName!=null){
			file = new File(Config.docfolder + "/" + docName);
		}else{
			return;
		}
		
		if (!file.exists() || file == null || !file.canRead()) {
			textArea.append("\nFile '"
							+ file.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			return;
		}

		Date start = new Date();
		try {
			textArea.append("\nIndexing a doc:\n  " + file.getName());
			indexDocs(file);

			Date end = new Date();
			textArea.append("\n" + (end.getTime() - start.getTime())
					+ " total milliseconds");
		} catch (IOException e) {
			textArea.append("\n caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}
	
	static void indexDocs(File file) throws IOException {
	
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(new File(file, files[i]));
					}
				}
				
			} else if (file.getName().endsWith(".txt")) {
				fileCount++;   //count of files
				textArea.append("\n[" + fileCount + "] " + file.getName());
				try {
					processTXT(file);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			//processing .pdf file
			} else if (file.getName().endsWith(".pdf")) {
				fileCount++;   //count of files
				textArea.append("\n[" + fileCount + "] " + file.getName());
				
				/*
				try {
					processPDF(file);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}
		}
	}
	static void processTXT(File file)throws IOException, SolrServerException{
		// makes a solr document
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
		System.err.println("*** id:" + fileID);
				
		solrdoc.addField("text", getFileTxt(file));
		
		// commit to solr server
		try {
			ServerUtil.addDocument(solrdoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	static  StringBuffer getFileTxt(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuffer sourceStr = new StringBuffer();
			String str = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((str = br.readLine()) != null)
				sourceStr.append("\n").append(str);
			reader.close();
			return sourceStr;
		} catch (Exception e) {
			return null;
		}

	}
	
	static void processPDF(File file) throws IOException, SolrServerException{
		  SolrServer server = new CommonsHttpSolrServer(Config.hosturl);

		  ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
		  up.addFile(file);
		  String id = file.getName().substring(file.getName().lastIndexOf('/')+1);
		  System.out.println(id);

		  up.setParam("literal.id", id);
		  up.setParam("uprefix", "attr_");
		  up.setParam("fmap.content", "attr_content");
		  up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		  server.request(up);
	}

}
