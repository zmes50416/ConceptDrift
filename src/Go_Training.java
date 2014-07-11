import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Go_Training {
	
	static HashSet<File> trainSet;
	static String topicDir="Topic1/";
	static String mainDir="mainDir/";
	public static void main(String[] args) {

		long AverageTime = 0; // 計算平均處理時間使用

		long StartTime = System.currentTimeMillis(); // 取出目前時間
		try {
			Scanner input = new Scanner(System.in);
			System.out.println("輸入開始步驟:(1.隨機選擇訓練文件2.字詞計分3.建立語意中心)");
			int step = input.nextInt();
			long start=0;
			switch (step) {
			case 1:
				int size = input.nextInt();
				generateTrainSet(size);
				
			case 2:
				System.out.println("2");
				start=System.currentTimeMillis();
				try {
		
					generateCandidate(new File(topicDir+"Set/").list(),false);
					getTermScore();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			case 3:
				System.out.println("3");
				Top_N();
				System.out.println(System.currentTimeMillis()-start);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR HAPPEN");
			e.printStackTrace();
			// JOptionPane.showMessageDialog(null, "ERROR");
		}
		long ProcessTime = System.currentTimeMillis() - StartTime; // 計算處理時間
		// AverageTime += ProcessTime; // 累積計算時間
		System.out.println(ProcessTime);
	}

	public static void Top_N() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(
				topicDir+"score.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				topicDir+"center.txt"));
		String line = "";
		Map<String, Double> map_Data = new HashMap<String, Double>();
		Map<String, Double> map_Data2 = new HashMap<String, Double>();
		try {
			while ((line = br.readLine()) != null) {

				String key = line.split(",")[0];
				double score = Double.parseDouble(line.split(",")[1]);//分數
				double hits = Double.parseDouble(line.split(",")[2]);//搜尋結果
				map_Data.put(key, score);
				map_Data2.put(key, hits);
			}

			List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(
					map_Data.entrySet());
			Iterator<Map.Entry<String, Double>> iterator = list_Data.iterator();

			Collections.sort(list_Data,
					new Comparator<Map.Entry<String, Double>>() {
						public int compare(Map.Entry<String, Double> o1,
								Map.Entry<String, Double> o2) {
							return (int) -((o1.getValue() - o2.getValue()) * 1000.0);
						}
					});
			int i=0;
			while (iterator.hasNext()) {
				Map.Entry<String, Double> entry = iterator.next();
				System.out.println(entry.getKey() + "," + entry.getValue());
				i++;
				bw.write(entry.getKey() + "," + entry.getValue()+","+map_Data2.get(entry.getKey()));
				bw.newLine();
				bw.flush(); // 清空緩衝區

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public static void getTermScore() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		TermToArt t2a = new TermToArt();
		BufferedReader br = new BufferedReader(new FileReader(
				topicDir+"candidate.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				topicDir+"score.txt"));
		String line = "";
		int i = 1;
		try {
			while ((line = br.readLine()) != null) {
				System.out.println(line.split(",")[1] + "=============");
				bw
						.write(line.split(",")[0]
								+ ","
								+ t2a.Scoring(line.split(",")[0], 
										Double.parseDouble(line.split(",")[1]),
										topicDir+"Set/",
										new File(topicDir+"Set/").list().length)
								+","+Double.parseDouble(line.split(",")[1]));
				bw.newLine();
				bw.flush(); // 清空緩衝區

				i++;
				

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// t2a.Scoring(term, path);
		bw.close();

	}

	public static HashSet<String> generateTrainSet(int size) {
		File dir = new File(topicDir+"Set/");
		for (String s : dir.list()) {
			new File(topicDir+"Set/" + s).renameTo(new File(mainDir + s));
		}
		Random rand = new Random();
		dir = new File(mainDir);
		String files[] = dir.list();
		System.out.println(rand.nextInt(files.length));
		HashSet<String> trainSet = new HashSet<String>();
		while (trainSet.size() < size) {
			trainSet.add(files[rand.nextInt(files.length)]);
			
		}
		System.out.println(trainSet);
		for (String s:trainSet) {
//			trainSet.add(files[rand.nextInt(files.length)]);
			File file = new File(mainDir + s);
			file.renameTo(new File(topicDir+"Set/" + s));
		}
		
		return trainSet;
	}
	public static HashSet<File> generateTrainSet(int lower,int upper) {
		
		
//		Random rand = new Random();
		File dir = new File(mainDir);
		File files[] = dir.listFiles();
//		System.out.println(rand.nextInt(files.length));
		HashSet<File> trainSet = new HashSet<File>();
		for(File s:files)
		{
			int index=Integer.parseInt(s.getName().split("_")[0]);
			if(index>=lower&&index<=upper)
			{
				trainSet.add(s);
				
			}
		}
		return trainSet;		
	}
	
	public static HashSet<File> generateTrainSet(String topic, int size) {
		
		
		Random rand = new Random();
		File dir = new File(topic);
		File files[] = dir.listFiles();
//		System.out.println(rand.nextInt(files.length));
		trainSet = new HashSet<File>();
		
		while (trainSet.size() < size) {
			trainSet.add(files[rand.nextInt(files.length)]);
			
		}
		return trainSet;		
	}
	
	public static HashSet<File> generateTestSet(int size) {
		String topics[] = {
				"Ad hoc/",
				"Addictive Behaviors/",
				"Applied Ergonomics/",
				"Journal of Quantitative Spectroscopy and Radiative Transfer/",
				"Knowledge-Based Systems/",
				"Planetary and Space Science/"};
		HashSet<File> testSet = new HashSet<File>();;
		Random rand = new Random();
		for(String topic : topics){
			File dir = new File(topic);
			File files[] = dir.listFiles();
//		System.out.println(rand.nextInt(files.length));
			int i = 0;
			while (i < size) {
				int index = rand.nextInt(files.length);
				if(!trainSet.contains(files[index]) && !testSet.contains(files[index])){
					testSet.add(files[index]);
					i++;
				}
			}
		
		}
		return testSet;		
	}
	
	
	public static void generateCandidate(String[] trainSet,boolean history)
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				topicDir+"candidate.txt"));
		if(new File(topicDir+"center.txt").exists()&&history)
		{
			BufferedReader br = new BufferedReader(new FileReader(topicDir+"center.txt"));
			String line = "";
			while ((line = br.readLine()) != null) {
				if(Double.parseDouble(line.split(",")[1])>1)
				{
					System.out.println(line);
					bw.write(line.split(",")[0] + "," + line.split(",")[2]);
					bw.newLine();
					bw.flush(); // 清空緩衝區
				}
				
			}
			
		}
		
		for (String s : trainSet) {
			
			BufferedReader br = new BufferedReader(new FileReader(topicDir+"Set/"+ s));
			String line = "";
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				bw.write(line.split(",")[0] + "," + line.split(",")[1]);
				bw.newLine();
				bw.flush(); // 清空緩衝區
			}
		}
	}

	public static void IP_change() throws IOException, InterruptedException {
		String ip1 = InetAddress.getLocalHost().toString().split("/")[1];
		// URL test=new URL("http://www.google.com/");
		// boolean flag;
		System.out.println("ip1=" + ip1);
		InetAddress test = InetAddress.getByName("140.115.1.254");
		while (ip1.equals(InetAddress.getLocalHost().toString().split("/")[1])
				|| !test.isReachable(5000)) {
			try {
				Process p = Runtime.getRuntime().exec("IP_1.bat");
				System.out.println(InetAddress.getLocalHost().toString().split(
						"/")[1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(5000);
		}
		System.out.println("final ip="
				+ InetAddress.getLocalHost().toString().split("/")[1]);
	}
}
