/*
 * 詞性標記
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qtag.Tagger;

public class Qtag {
	static String readFilePath = "usedData/acq/";
	static String writeFilePath = "usedData/acq_qtag/";
	public static String[] tokenise(String line) {

		StringTokenizer st = new StringTokenizer(line);
		int numberOfTokens = st.countTokens();
		String result[] = new String[numberOfTokens];
		for (int i = 0; st.hasMoreTokens(); i++)
			result[i] = st.nextToken();

		return result;
	}

	public static void main(String args[]) throws IOException {
		File F = new File(readFilePath);
		for(File f : F.listFiles()){
			System.out.println(f.getName().split("\\.")[0]);
			tagging(f.getName().split("\\.")[0]);
		}
		
	}

	public static void tagging(String f) throws IOException {
		
		// Open the input file for reading
		FileReader FileStream = new FileReader(readFilePath+f+".txt");

		BufferedReader in = new BufferedReader(FileStream);
		// Create a Tagger
		Tagger tagger = new Tagger("qtag-eng");
		String line = in.readLine();
		int tokenId = 1;
		//ArrayList<String> stop_list = Stop_Loader.loadList("stop_list.txt");
		// Process line by line
		if(!new File(writeFilePath).exists()){
			boolean mkdirSuccess = new File(writeFilePath).mkdirs();
			if (!mkdirSuccess) {
				System.out.println("Directory creation failed");
			}
		}
		BufferedWriter bw= new BufferedWriter(new FileWriter(writeFilePath+f + "_"
				+ "qtag.txt", false));
		while (line != null) {
			String line2=line.toString();
			line=line.replace("]", "");
			line=line.replace("[", "");
			line=line.replace("<", "");
			line=line.replace(">", "");
	
			Pattern p= Pattern.compile("[(),.\"\\?!:;]");

	         Matcher m=p.matcher(line);

	         line=m.replaceAll("");
	         line2=m.replaceAll(",");
			String tokens[] = Qtag.tokenise(line);
			String tokens2[]=Qtag.tokenise(line2);
			String tags[] = tagger.tag(tokens);
			for (int i = 0; i < tokens.length; i++, tokenId++) {
				System.out.println(tokens2[i] + "," + tags[i]);
				try {
						System.out.println("write");
						
						bw.write(tokens2[i] + ", " + tags[i]);//employees, NNS
						bw.newLine();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			line = in.readLine();
		}
		bw.flush(); // 清空緩衝區
		bw.close(); // 關閉BufferedWriter物件

	}
}