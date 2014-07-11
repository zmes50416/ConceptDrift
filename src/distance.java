import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;

public class distance
{
	double simMin = 0.4;
	double avgSim=0;
	int max_core=0;
	BufferedReader br;

	int no;
	String line = "";
	ArrayList<String> linkList = new ArrayList<String>();
	BufferedReader br2;

	Map<String, Double> coreMap = new HashMap<String, Double>();
	Map<String, Double> degreeMap = new HashMap<String, Double>();
	Map<String, String> hitMap = new HashMap<String, String>();
	List<Map.Entry<String, Double>> sort_data;
	
	public void cc_cal(int no) throws FileNotFoundException
	{
		this.no = no;
		br = new BufferedReader(new FileReader("Rank/"+no + "_" + "Rank.txt"));//讀出經過排序的NGD
		br2 = new BufferedReader(new FileReader("Stem/"+no + "_" + "stem.txt"));//讀出字詞
		try {
			double sum = 0;
			while ((line = br.readLine()) != null) {
				linkList.add(line);
			}
			simMin=Double.parseDouble(linkList.get(linkList.size()/4).split(",")[2]);
			
			while ((line = br2.readLine()) != null) {
				String key = line.split(",")[0];
				String hits = line.split(",")[1];
				double cc = getCC(key); //取得這個字的中間度程度
				coreMap.put(key, cc);//紀錄中間度程度
				degreeMap.put(key, cc);//紀錄連結度?
				hitMap.put(key,hits);//紀錄搜尋結果
			}
			
			sort_data = new ArrayList<Map.Entry<String, Double>>(coreMap.entrySet());
			Iterator<Map.Entry<String, Double>> iterator = sort_data.iterator();
			
			//按照cc值排序(小->大)
			Collections.sort(sort_data,
					new Comparator<Map.Entry<String, Double>>() {
						public int compare(Map.Entry<String, Double> o1,
								Map.Entry<String, Double> o2) {
							return (int) ((o1.getValue() - o2.getValue()));
						}
					});
			//System.out.println("--"+sort_data);
			System.out.println("--"+sort_data.get(sort_data.size()-1).getValue()); //印出CCmax
			
			//寫入CC值到檔案
			BufferedWriter bw;
			try
			{
				bw = new BufferedWriter(new FileWriter("CC/"+no + "_" + "cc.txt"));//寫入cc運算結果
				while (iterator.hasNext()) {
					Map.Entry<String, Double> entry = iterator.next();
					System.out.println(entry.getKey() + "," + entry.getValue());
					bw.write(entry.getKey() + "," + entry.getValue());
					//rel_loader.rel_map.put(entry.getKey(), entry.getValue());
					bw.newLine();
				}
				bw.flush();
				bw.close();
			}
			catch(IOException f)
			{f.printStackTrace();}
			
			//getK_code_value();
			//計算kcore值
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double getCC(String node)
	{
		double weight=0, sig_distance = 0, cc = 0;
		for (String t : linkList) {
			if ((t.split(",")[0].equals(node) || t.split(",")[1].equals(node)) 
					&& Double.parseDouble(t.split(",")[2]) < simMin)
			{
				sig_distance += Double.parseDouble(t.split(",")[2]);
				//weight=weight+Double.parseDouble(t.split(",")[2]);
			}//只有小於門檻值的才會建立連結
		}//計算各節點(字詞)的連結度(degree)
		if(sig_distance!=0)
			cc = (linkList.size()-1) / sig_distance;
		else
			cc = 0;
		System.out.println(node+":"+cc);
		return cc;
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		Scanner input = new Scanner(System.in);
		System.out.println("Enter document index:");
		int no = input.nextInt();
		distance dis = new distance();
		System.out.println("Enter threshold:");
		//kcore.simMin = input.nextDouble(); 
		dis.cc_cal(no);//計算K-core值
		
		try {
			distanceGUI gui;
			gui = new distanceGUI();
			gui.getData(no, dis.simMin);
			gui.updateGUI();
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			gui.setSize(1024, 768);
			gui.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}//繪圖
	}
}
