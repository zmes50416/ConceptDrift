package tw.edu.ncu.CJ102;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class POS_Filter {
	static SettingManager setting;
	final String POSDIRPATH;
	/**
	 * offer Filter of Tag Of Speech will also filter length of Words
	 */
	public POS_Filter() {
		// TODO Auto-generated constructor stub
		setting = SettingManager.getSettingManager();
		POSDIRPATH = setting.getSetting(SettingManager.POSFilterDIR);
	}
	public void filterDir(String POSFileDirPath){
		File files = new File(POSFileDirPath);
		for(File file : files.listFiles()){
			//this.filter(file.getName());
			try {
				this.filter(file.getName(),POSFileDirPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	//filter one file at a time
	public void filter(String fileName,String path) throws IOException {
		FileReader FileStream = new FileReader(path +fileName);
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		String line = "";

		ArrayList list = new ArrayList();
		while ((line = BufferedStream.readLine()) != null) {
			list.add(line);
		}

		Object[] datas = list.toArray();
		LinkedHashSet<String> set = new LinkedHashSet<String>();

		for (int i = 0; i < datas.length; i++) {
			int j, k;
			if (i == datas.length - 1)
				j = i;
			else
				j = i + 1;

			if (j == datas.length - 1)
				k = j;
			else
				k = j + 1;

			String key1 = ((String) datas[i]).split(", ")[0]; // Algorithm
			String tag1 = ((String) datas[i]).split(", ")[1]; // NN
			//String count1 = ((String) datas[i]).split(", ")[2]; // 1

			String key2 = ((String) datas[j]).split(", ")[0]; 
			String tag2 = ((String) datas[j]).split(", ")[1]; 
			//String count2 = ((String) datas[j]).split(", ")[2]; 

			String key3 = ((String) datas[k]).split(", ")[0]; 
			String tag3 = ((String) datas[k]).split(", ")[1]; 
			//String count3 = ((String) datas[k]).split(", ")[2]; 
			System.out.println("key1:" + key1 + " " + tag1);
			System.out.println("key2:" + key2 + " " + tag2);
			System.out.println("key3:" + key3 + " " + tag3);
			//單字過濾，根據D. Tufis and O. Mason於1998提出的Qtag
			if(tag1.equals("NN") || tag1.equals("NP") ||tag1.equals("JJ")){
				set.add(key1);
				System.out.println("add:" + key1);
			}
		}


		Object[] objs = set.toArray();
		if(!new File(this.POSDIRPATH).exists()){
			boolean mkdirSuccess = new File(POSDIRPATH).mkdirs();	
		}
		
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(POSDIRPATH+fileName + "_" + "filter_output1.txt",
				false));
		String objs_out = "";
		for (int i = 0; i < objs.length; i++) {

			System.out.println(objs[i]);
			objs_out = (String) objs[i];
			objs_out = objs_out.replace("]", "");
			objs_out = objs_out.replace("[", "");
			Pattern p = Pattern.compile("[(),.\"\\?!:;']");

			Matcher m = p.matcher(objs_out);

			objs_out = m.replaceAll("");
			try {
				bw.write("\"" + objs_out + "\"");
				bw.newLine();

				bw.flush(); // 清空緩衝區

			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

		}

		bw.close(); // 關閉BufferedWriter物件
	}


	public static void main(int no) throws IOException {
		//filter(no);
	}

	public static void main(String args[]) throws IOException {
		//String fileName="acq_0000005_qtag";
		//new POS_Filter().filter(fileName);
	}

}
