//package dataset;

import java.io.*;

public class ReadFile {
	static public String Load(String filePath, String fileName)throws IOException{
		//String filepath = "./HTML files/" + Cluster +"/";
		//String filePath = "C:\\Users\\NeoLS\\workspace\\TEST\\dataset\\collection_cut\\";
		InputStream fileIn = new FileInputStream(filePath + fileName);
		Reader fileReader=new InputStreamReader(fileIn);
		int i=-1;
		StringBuffer txtContent=new StringBuffer();
		while((i=fileReader.read())!=-1){
			txtContent.append((char)i);
		}
		fileReader.close();
		fileIn.close();
		return txtContent.toString();
	}
	
	static public void processAll(String filePath)throws IOException{
		//String filepath = "./HTML files/" + Cluster +"/";
		//String filePath = "C:\\Users\\NeoLS\\workspace\\TEST\\dataset\\collection_cut\\";
		File dic=new File(filePath);
		String[] files=dic.list();
//		String[] output=new String[files.length];
		int j=0;
		BufferedWriter bw;
		
		for(String fileName:files){
			
		InputStream fileIn = new FileInputStream(filePath + fileName);
		Reader fileReader=new InputStreamReader(fileIn);
		int i=-1;
		StringBuffer txtContent=new StringBuffer();
		while((i=fileReader.read())!=-1){
			txtContent.append((char)i);
		}
		fileReader.close();
		fileIn.close();
		
//		bw = new BufferedWriter(new FileWriter(String.valueOf(j)+"_"+fileName.replace(".txt", "")+"_"+Rel+".txt", false));
//		
//		bw.write(output);
//		bw.flush();
//		bw.close();
		
//		output[j]=txtContent.toString();
		j++;
		
		}
		System.out.println("Finish!");
//		return output;
	}
}
