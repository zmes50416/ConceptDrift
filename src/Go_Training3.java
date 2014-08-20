import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class Go_Training3 {

	/**
	 * @param args
	 */
	
	static String maindir = "reuters";
	static HashSet<String> set;
	static HashMap<String, Integer> term_vertice_times = new HashMap<String, Integer>();
	//產生指定主題訓練集
	public static HashSet<String> generateTrainSet(int size, String dir, String cat){
		set = new HashSet<String>();
		cat = "trade";
		size = 1;
		File cdir = new File(maindir+"/"+cat);
		ArrayList<File> list = new ArrayList<File>();
		System.out.print(maindir+"/"+cat);
		
		new File(dir).mkdirs();
		
		/*for(File f : cdir.listFiles())
			list.add(f);
		
		Collections.shuffle(list); //隨機排序
		
		for(int i=0; i<size; i++){*/
			String[] txt1 = new String[5];
			
			txt1[0] = "trade_0003869_Rank.txt";
			txt1[1] = "trade_0003166_Rank.txt";
			txt1[2] = "trade_0003658_Rank.txt";
			txt1[3] = "trade_0005223_Rank.txt";
			txt1[4] = "trade_0008069_Rank.txt";
			/*txt1[0] = "trade_0003869_m0.25_Rank.txt";
			txt1[1] = "trade_0003166_m0.25_Rank.txt";
			txt1[2] = "trade_0003658_m0.25_Rank.txt";
			txt1[3] = "trade_0005223_m0.25_Rank.txt";
			txt1[4] = "trade_0008069_m0.25_Rank.txt";*/
			/*txt1[0] = "trade_0003869_m0.40_Rank.txt";
			txt1[1] = "trade_0003166_m0.40_Rank.txt";
			txt1[2] = "trade_0003658_m0.40_Rank.txt";
			txt1[3] = "trade_0005223_m0.40_Rank.txt";
			txt1[4] = "trade_0008069_m0.40_Rank.txt";*/
			for(int i=0;i<5;i++){
				//copyfile(new File(maindir+"/"+cat + "/" + txt1[i]) , new File(dir + "/" + txt1[i]));
				//copyfile(new File("reuters_m/trade_m0.25/" + txt1[i]) , new File(dir + "/" + txt1[i]));
				set.add(txt1[i]);
			}
			
			//copyfile(list.get(i), new File(dir + "/" + list.get(i).getName()));
			//set.add(list.get(i).getName());
		//}
			String[] txt2 = new String[5];
			
			/*String txt6 = "crude_0000175_Rank.txt";
			String txt7 = "crude_0002216_Rank.txt";
			String txt8 = "crude_0003651_Rank.txt";
			String txt9 = "crude_0004297_Rank.txt";
			String txt10 = "crude_0009220_Rank.txt";*/
			/*String txt6 = "crude_0000175_m0.25_Rank.txt";
			String txt7 = "crude_0002216_m0.25_Rank.txt";
			String txt8 = "crude_0003651_m0.25_Rank.txt";
			String txt9 = "crude_0004297_m0.25_Rank.txt";
			String txt10 = "crude_0009220_m0.25_Rank.txt";*/
			/*txt2[0] = "crude_0000175_m0.40_Rank.txt";
			txt2[1] = "crude_0002216_m0.40_Rank.txt";
			txt2[2] = "crude_0003651_m0.40_Rank.txt";
			txt2[3] = "crude_0004297_m0.40_Rank.txt";
			txt2[4] = "crude_0009220_m0.40_Rank.txt";*/
			
			/*txt2[0] = "trade_0001142_Rank.txt";
			txt2[1] = "trade_0006454_Rank.txt";
			txt2[2] = "trade_0004106_Rank.txt";
			txt2[3] = "trade_0005675_Rank.txt";
			txt2[4] = "trade_0007777_Rank.txt";*/
			/*txt2[0] = "trade_0001142_m0.25_Rank.txt";
			txt2[1] = "trade_0006454_m0.25_Rank.txt";
			txt2[2] = "trade_0004106_m0.25_Rank.txt";
			txt2[3] = "trade_0005675_m0.25_Rank.txt";
			txt2[4] = "trade_0007777_m0.25_Rank.txt";*/
			/*txt2[0] = "trade_0001142_m0.40_Rank.txt";
			txt2[1] = "trade_0006454_m0.40_Rank.txt";
			txt2[2] = "trade_0004106_m0.40_Rank.txt";
			txt2[3] = "trade_0005675_m0.40_Rank.txt";
			txt2[4] = "trade_0007777_m0.40_Rank.txt";*/
			
			/*for(int i=0;i<5;i++){
				//copyfile(new File(maindir+"/"+cat + "/" + txt2[i]) , new File(dir + "/" + txt2[i]));
				copyfile(new File("reuters_m/trade_m0.25/" + txt2[i]) , new File(dir + "/" + txt2[i]));
				set.add(txt2[i]);
			}*/
		
		return set;
		
		
	}
	
	//產生指定資料夾訓練集
	public static void generateTrainSet(String maindir, int size, String dir){
		set = new HashSet<String>();
		
		File cdir = new File(maindir);
		ArrayList<File> list = new ArrayList<File>();
		
		new File(dir).mkdirs();
		
		for(File f : cdir.listFiles())
			list.add(f);
		
		Collections.shuffle(list);
		
		for(int i=0; i<size; i++){
			
			copyfile(list.get(i), new File(dir + "/" + list.get(i).getName()));
			set.add(list.get(i).getName());
		}
		
		
	}
	
	//隨機產生n個主題，每個主題m個文件的訓練集於dir
	public static void generateTrainSet(String maindir, int size,int topic_count,String dir){
		set = new HashSet<String>();
		
		File tdir = new File(maindir);
		ArrayList<File> list = new ArrayList<File>();
		ArrayList<File> topicList = new ArrayList<File>();
		
		new File(dir).mkdirs();
		
		for(File f : tdir.listFiles())
			list.add(f);
		
		Collections.shuffle(list);
		
		for(int i=0; i<topic_count; i++){
			topicList.add(list.get(i));
		}
		
		for(File d: topicList)
			generateTrainSet(size, dir, d.getName());

	}
	
	public static void generateTestSet_multi(int size, String dir, String cat){
		
		File multiDir = new File("multi");
		
		new File(dir).mkdirs();
		
		ArrayList<File> list_multi = new ArrayList<File>();
		ArrayList<File> list_else = new ArrayList<File>();

		for(File f:multiDir.listFiles() ){
			if(f.getName().split("_")[0].equals(cat) || f.getName().split("_")[1].equals(cat))
				list_multi.add(f);
			else
				list_else.add(f);
		}
		Random ran = new Random();
		ran.setSeed(1);
		Collections.shuffle(list_multi,ran);
		Collections.shuffle(list_else,ran);
		
		for(int i=0; i<size; i++)
			copyfile(list_multi.get(i), new File(dir + "/" + list_multi.get(i).getName()));
		
		for(int j=0; j<40; j++)
			copyfile(list_else.get(j), new File(dir + "/" + list_else.get(j).getName()));

	}

	
	public static void generateTestSet(int size, String dir, String cat[]){
		
		File mdir = new File(maindir);
		ArrayList<File> list = new ArrayList<File>();
		
		new File(dir).mkdirs();
		
		for(File d: mdir.listFiles()){
			
			File[] datas = d.listFiles();
			for(File f : datas){
				list.add(f);
			}
			Random ran = new Random();
			ran.setSeed(3);
			Collections.shuffle(list,ran);
			
			boolean isCat = false;
			for(String c:cat)
				if(d.getName().equals(c))
					isCat = true;
			
			//如果是訓練集的類別要避免重複
			if(isCat){
				int i = 0;
				int j = 0;
				
				while(j<size){
					if(!set.contains(list.get(i).getName())){
						copyfile(list.get(i), new File(dir + "/" + list.get(i).getName()));
						i++;
						j++;
					}
					i++;
				}
			}
			else{
				for(int i=0; i<size; i++)
					copyfile(list.get(i), new File(dir + "/" + list.get(i).getName()));
			}
			list.clear();
		}
	}
	
	
	public static Map<String, Double> featureExtract(File f){
		Map<String, Double> ngds = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			List<String> list = new LinkedList<String>();
			ngds = new HashMap<String, Double>();
			
			String line;
			while ((line=br.readLine()) !=null ){
				//去除NDG分數大於1的結果
				if(Double.parseDouble(line.split(",")[2])<1 )
					list.add(line);
			}
			br.close();
			
			//前50% NGD分數資料，當作強烈字詞
			double simMin = Double.parseDouble(list.get(list.size()/2).split(",")[2]);
			System.out.print("simMin = " + simMin+"\n");
			//simMin = 0.679174915480266;
			for(String s : list){
				double ngd = Double.parseDouble(s.split(",")[2]);
				if(ngd <= simMin){
					String vertex1 = s.split(",")[0];
					String vertex2 = s.split(",")[1];
					String edge = vertex1+","+vertex2;
					ngds.put(edge, ngd);
					if(term_vertice_times.get(vertex1)==null){
						term_vertice_times.put(vertex1, 1);
					}else{
						term_vertice_times.put(vertex1, term_vertice_times.get(vertex1)+1);
					}
					if(term_vertice_times.get(vertex2)==null){
						term_vertice_times.put(vertex2, 1);
					}else{
						term_vertice_times.put(vertex2, term_vertice_times.get(vertex2)+1);
					}
					//System.out.print("ngds.put("+edge+", "+ngd+")\n");
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ngds;
		
	}
	
	public static HashMap<String, Integer> get_term_appear_times(){
		return term_vertice_times;
	}
		
	public static void copyfile(File srFile, File dtFile) {
		try {
			File f1 = srFile;
			File f2 = dtFile;
			InputStream in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		generateTrainSet(5, "training", "acq");
		//generateTestSet(2, "testing", "acq");
		
		

	}

}
