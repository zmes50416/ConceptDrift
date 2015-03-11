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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.swing.JFrame;

public class K_core {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	double simMin = 0.4;
	double avgSim=0;
	int max_core=0;
	BufferedReader br;

	int no;
	String line = "";
	ArrayList<String> linkList = new ArrayList<String>();
	BufferedReader br2;

	Map<String, Integer> coreMap = new HashMap<String, Integer>();
	Map<String, Integer> degreeMap = new HashMap<String, Integer>();
	Map<String, String> hitMap = new HashMap<String, String>();
	List<Map.Entry<String, Integer>> sort_data;

	public void K_core_cal(int no) throws FileNotFoundException {
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
				int degree = getDegree(key);
				coreMap.put(key, degree);//紀錄K-core值
				degreeMap.put(key, degree);//紀錄連結度
				hitMap.put(key,hits);//紀錄搜尋結果
			}
			
			sort_data = new ArrayList<Map.Entry<String, Integer>>(coreMap
					.entrySet());
			
			Collections.sort(sort_data,
					new Comparator<Map.Entry<String, Integer>>() {
						public int compare(Map.Entry<String, Integer> o1,
								Map.Entry<String, Integer> o2) {
							return (int) ((o1.getValue() - o2.getValue()));
						}
					});
			//按照初始degree值排序
			getK_code_value();
			//計算kcore值
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private int getDegree(String node) {
		int degree = 0;
		double weight=0;
		for (String t : linkList) {
			if ((t.split(",")[0].equals(node) || t.split(",")[1].equals(node))
					&& Double.parseDouble(t.split(",")[2]) < simMin) {
				degree++;
				weight=weight+Double.parseDouble(t.split(",")[2]);
			}//只有小於門檻值的才會建立連結
		}//計算各節點(字詞)的連結度(degree)
		System.out.println(node+":"+degree);
		return degree;
	}
	
	

	void getK_code_value() {
		List<Map.Entry<String, Integer>> outputList = new ArrayList<Map.Entry<String, Integer>>();
		while (!sort_data.isEmpty()) {
			System.out.println(sort_data.get(0));
			Map.Entry<String, Integer> node = sort_data.get(0);
			coreMap.remove(node.getKey());
			outputList.add(node);
			int temp=0;
			for (String t : linkList) {
				if ((t.split(",")[0].equals(node.getKey()))
						&& Double.parseDouble(t.split(",")[2]) < simMin) {
					if (coreMap.containsKey(t.split(",")[1])) {
						if (node.getValue() < coreMap.get(t.split(",")[1])) {
							temp = coreMap.get(t.split(",")[1]) - 1;
							coreMap.put(t.split(",")[1], temp);//將目前數值大於自己的鄰居-1
							System.out.println(t.split(",")[1] + " value-1");
						}
					}
				} else if ((t.split(",")[1].equals(node.getKey()))
						&& Double.parseDouble(t.split(",")[2]) < simMin) {
					// degree++;
					if (coreMap.containsKey(t.split(",")[0])) {
						if (node.getValue() < coreMap.get(t.split(",")[0])) {		
							temp = coreMap.get(t.split(",")[0]) - 1;
							coreMap.put(t.split(",")[0], temp);
							System.out.println(t.split(",")[0] + " value-1");
						}
					}
				}//檢視是否有大於自己連結度的鄰居(請去看K-core的虛擬碼)
				
				sort_data = new ArrayList<Map.Entry<String, Integer>>(coreMap
						.entrySet());
				Collections.sort(sort_data,
						new Comparator<Map.Entry<String, Integer>>() {
							public int compare(Map.Entry<String, Integer> o1,
									Map.Entry<String, Integer> o2) {
								return (int) ((o1.getValue() - o2.getValue()));
							}
						});
			}
			System.out.println(node + ": finished");
			if(sort_data.size()==1)
			{
				max_core=sort_data.get(0).getValue();
				System.out.println("max_core:"+sort_data.get(0).getValue());
			}
		}

		System.out.println(outputList);
		System.out.println("Size:" + outputList.size());
		System.out.println("Threshold:" + simMin);
		
		BufferedWriter bw;
		BufferedWriter bw2;
		String query="";
		try {
			bw = new BufferedWriter(new FileWriter("K_core/"+no + "_" + "k_core.txt"));//寫入k-core運算結果
			bw2 = new BufferedWriter(new FileWriter("Main_word/"+no + "_" + "main_word.txt"));//寫入核心特徵
			
			for (Entry<String, Integer> core : outputList) {

				bw.write(core.toString()+"="+degreeMap.get(core.toString().split("=")[0]));//MANETS=5=8(字詞=Kcore值=degree值)
			
				if(Integer.parseInt(core.toString().split("=")[1])>=max_core)
				{
					query=query+"+\""+core.toString().split("=")[0]+"\"";
					double coreV=Double.parseDouble(core.toString().split("=")[1]);
					double hit=Double.parseDouble(hitMap.get(core.toString().split("=")[0]));
					//double score=coreV*(Math.log10(hit)/9.906);
					bw2.write(core.toString().split("=")[0]+","+hitMap.get(core.toString().split("=")[0])+","+core.toString().split("=")[1]+","+degreeMap.get(core.toString().split("=")[0]));
					bw2.newLine();
					bw2.flush(); 
					
				}//只取k-core值最大的一群為核心特徵
				
				bw.newLine();
				bw.flush();
				//換行以及清空緩衝區
				
			}
			bw.close(); 
			bw2.close(); // 關閉BufferedWriter物件
			System.out.println(query);
			System.out.println(avgSim);
		} catch (IOException f) {
			// TODO Auto-generated catch block
			f.printStackTrace();
		}
		

	}

	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(System.in);
		System.out.println("Enter document index:");
		int no = input.nextInt();
		K_core kcore = new K_core();
		System.out.println("Enter threshold:");
//		kcore.simMin = input.nextDouble(); 
		kcore.K_core_cal(no);//計算K-core值
		
		try {
			KcoreGUI gui;
			gui = new KcoreGUI();
			gui.getData(no, kcore.simMin);
			gui.updateGUI();
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			gui.setSize(1024, 768);
			gui.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//繪圖
	}
}