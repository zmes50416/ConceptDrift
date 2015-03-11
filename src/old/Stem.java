import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stem {

	/**
	 * 
	 */
	public Stem() {
		// TODO Auto-generated constructor stub
	}

	public static void stemming(String no) throws IOException {
		System.out.println("處理檔案"+no+"中...");
		FileReader FileStream = new FileReader("citeulike/citeulike_Number_of_term/"+no + "_" + "number_of_term.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		String e = "";

		ArrayList list = new ArrayList();
		while ((e = BufferedStream.readLine()) != null) {
			
				list.add(e);
			
			
		}
		// System.out.println( list.toString());

		Object[] datas = list.toArray();
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for (int i = 0; i < datas.length; i++) {
			String key = ((String) datas[i]).split(",")[0]; //first keyword
			String value = ((String) datas[i]).split(",")[1];
			
			Pattern p= Pattern.compile("[(),\"\\?!:;=]");

	         Matcher m=p.matcher(key);

	         String first=m.replaceAll("");
	         
			 set.add (first + "," + value);
					 // i++;
 
			  
		}
       
			  
			  
	 
		
		Object[] objs = set.toArray();
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter("citeulike/citeulike_Stem/"+no + "_" + 
				"stem.txt", false));
		for (int j = 0; j < objs.length; j++) {

			//System.out.println(objs[j]);
			String objs_out = (String) objs[j];

			
				

				try {
					
					bw.write(objs_out);
					bw.newLine();
					bw.flush(); // 清空緩衝區
					

				} catch (IOException f) {
					// TODO Auto-generated catch block
					f.printStackTrace();
				}

			
		}
		bw.close(); // 關閉BufferedWriter物件
		System.out.println("處理檔案"+no+"處理完畢");
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	// TODO Auto-generated method stub
	public static void main(int no) throws IOException {
		//stemming(no);
	}

	public static void main(String args[]) throws IOException {
		// TODO Auto-generated method stub
		File F = new File("citeulike/citeulike_Number_of_term/");
		for(File f : F.listFiles()){
			//System.out.println(f.getName().split("_")[0]);
			stemming(f.getName().split("_")[0]);
		}
		//stemming(11);
	}

}
