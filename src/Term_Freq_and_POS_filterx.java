import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Term_Freq_and_POS_filterx {
	private static Map concordance;
	private static File file;
	private static FileReader reader;
	private static BufferedReader in;
	
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		counting();
	}
	
	public static void counting() {
		File dir = new File( "Qtag/");
		System.out.println("頻率計算與磁性過濾程序開始");
		System.out.println("來源資料匣為"+dir.getName());
		File[] fileslist = dir.listFiles();
		for (File files : fileslist){
			System.out.println("處理文件"+files.getName()+"中...");
			loadMap(files);
		}
		/*File f = new File("Qtag/003_qtag.txt");
		loadMap(f);*/
	}

	private static void loadMap(File file){
		try{
			concordance = new TreeMap();
			reader = new FileReader(file);
			in = new BufferedReader(reader);
			String line = null;
			String word = "";
			String frequency = "";
			String filename = "";
			for(int i=0; i<file.getName().split("_").length;i++){
				//System.out.println("filename = "+ filename);
				if(i==0){
					filename=file.getName().split("_")[0];
				}else{
					char[] filename_temp = file.getName().split("_")[i].toCharArray();
					if(!Character.isDigit(filename_temp[0])){ //如果第一個字元是數字代表到檔名結尾了
						filename=filename+"_"+file.getName().split("_")[i];
					}else{
						filename=filename+"_"+file.getName().split("_")[i];
						break;
					}
				}
			}
			int lineNumber = 0;
			String key1 = ""; //字詞1
			String tag1 = ""; //字詞1的詞性
			String key2 = ""; //字詞1
			String tag2 = ""; //字詞1的詞性
			String key3 = ""; //字詞1
			String tag3 = ""; //字詞1的詞性
			LinkedHashSet<String> set = new LinkedHashSet<String>(); //儲存符合詞性過濾的字詞
			BufferedWriter bw;
			ArrayList list = new ArrayList(); //儲存qtag文件內容
			bw = new BufferedWriter(new FileWriter("Keyword_output_freq/"+filename+"_"+"keyword_output_freq.txt",false));
			while((line = in.readLine()) != null){
				list.add(line);
			}
			Object[] datas = list.toArray();
			for(int i = 0; i < datas.length; i++){
				int j, k;
				if(i == datas.length-1){ //最後一個字
					j = i;
				}else{
					j = i + 1;
				}
				if(j == datas.length-1){ //最後兩個字
					k = j;
				}else{
					k = j + 1;
				}
				if(i==0){ //第一個字
					key1 = ((String) datas[i]).split(", ")[0].toUpperCase();
					tag1 = ((String) datas[i]).split(", ")[1];
				}else if(i==1){ //第二個字
					key1 = ((String) datas[i]).split(", ")[0].toUpperCase();
					tag1 = ((String) datas[i]).split(", ")[1];

					key2 = ((String) datas[j]).split(", ")[0].toUpperCase(); 
					tag2 = ((String) datas[j]).split(", ")[1]; 
				}else{
					key1 = ((String) datas[i]).split(", ")[0].toUpperCase();
					tag1 = ((String) datas[i]).split(", ")[1];

					key2 = ((String) datas[j]).split(", ")[0].toUpperCase(); 
					tag2 = ((String) datas[j]).split(", ")[1]; 

					key3 = ((String) datas[k]).split(", ")[0].toUpperCase(); 
					tag3 = ((String) datas[k]).split(", ")[1];
				}
				System.out.println("key1:" + key1 + " " + tag1);
				System.out.println("key2:" + key2 + " " + tag2);
				System.out.println("key3:" + key3 + " " + tag3);
				
				//組合字過濾
				if(key1.equals(key2)){ //最後一個字
					if ((tag1.equals("NN") || tag1.equals("NNS") || tag1.equals("NP")) && key1.length()>2){
						word = key1;
					}
				}else if(key2.equals(key3)){ //最後兩個字
					if ((tag1.equals("NN") || tag1.equals("NP") || tag1.equals("NNS") || tag1.equals("NPS") || tag1.equals("JJ")) && key1.length()>2){
						if ((key1.endsWith(",") || key1.endsWith("."))||(key2.startsWith(",") || key2.startsWith("."))) {
							word = key1;
						}else if((tag2.equals("NN") || tag2.equals("NP") || tag2.equals("NPS") || tag2.equals("NNS")) && key2.length()>2){
							word = key1 + "+" + key2;
							i++;
						}else{
							word = key1;
						}
					}
				}else{
					if((tag1.equals("NN") || tag1.equals("NP") || tag1.equals("NNS") || tag1.equals("JJ")) && key1.length()>2){
						if ((key1.endsWith(",") || key1.endsWith("."))||(key2.startsWith(",") || key2.startsWith("."))) {
							word = key1; //串接一個字
						}else if((tag2.equals("NN") || tag2.equals("NP") || tag2.equals("NPS") || tag2.equals("NNS")) && key2.length()>2){
							if((key2.endsWith(",") || key2.endsWith("."))|| (key3.startsWith(",") || key3.startsWith("."))){
								word = key1 + "+" + key2; //串接兩個字
								i++;
							}else if((tag3.equals("NN") || tag3.equals("NP") || tag3.equals("NPS") || tag3.equals("NNS")) && key3.length()>2){
								word = key1 + "+" + key2 + "+" + key3; //串接三個字
								i = i+2;
							}else{
								word = key1 + "+" + key2; //串接兩個字
								i++;
							}
						}else{
							word = key1;
						}
					}
				}
				if(word!=""){
					set.add(word);
					System.out.println("add:" + word);
					frequency = (String) concordance.get(word);
					if(frequency == null){
						frequency = "1"; //若該字沒出現則次數為0
					}else{
						int n = Integer.parseInt(frequency);
						++n; //若出現則++
						frequency = "" + n;
					}
					concordance.put(word, frequency);
					bw.write(word + ", " + frequency);
					bw.newLine();
					bw.flush();
					word="";
				}
			}
			bw.close(); // 關閉BufferedWriter物件
			bw = new BufferedWriter(new FileWriter("POS_filter/"+filename + "_" + "filter_output1.txt", false));
			String objs_out = "";
			Object[] objs = set.toArray();
			for (int i = 0; i < objs.length; i++){
				System.out.println(objs[i]);
				objs_out = (String) objs[i];
				objs_out = objs_out.replace("]", "");
				objs_out = objs_out.replace("[", "");
				Pattern p = Pattern.compile("[(),.\"\\?!:;']");
				Matcher m = p.matcher(objs_out);
				objs_out = m.replaceAll("");
				bw.write("\"" + objs_out + "\"");
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
