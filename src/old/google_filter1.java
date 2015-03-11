import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class google_filter1 {

	/**
	 * 
	 */
	public google_filter1() {
		// TODO Auto-generated constructor stub

	}

	public static void search_filter(String no) throws IOException {
		FileReader FileStream = new FileReader("citeulike/citeulike_Search1/"+no + "_" + "google_output1.txt");
		BufferedReader reader = new BufferedReader(FileStream);
		String line = "";
		FileReader FileStream1 = new FileReader("citeulike/citeulike_POS_filter/"+no + "_" + "filter_output1.txt");
		BufferedReader BufferedStream1 = new BufferedReader(FileStream1);
		ArrayList<String> list1 = new ArrayList<String> ();
		while ((line = BufferedStream1.readLine()) != null) {
			list1.add(line);
		}

		Object[] datas = list1.toArray();

		LinkedHashSet<String> set = new LinkedHashSet<String>();
		int count = 0;
		while ((line = reader.readLine()) != null) {
			// String key = null;
			String value1 = null;
			if (count < datas.length) {
				if (line.contains("約有 ") || line.contains(" 項結果")) {

					String value = line.replaceAll(",", ""); // 約有 173,000 項結果

					value1 = value.split(" ")[value.split(" ").length - 2];
					// set.add(value1);
					String key = ((String) datas[count]);
					set.add(key + "," + value1);
					System.out.println(key + "==>" + value1);
					count++;

				}

			}

		}

		Object[] arry1 = set.toArray();
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter("citeulike/citeulike_Number_of_term/"+no + "_" + "number_of_term.txt",
				false));
		ArrayList<String> filtered_key = new ArrayList<String>();
		ArrayList<String> filtered_line = new ArrayList<String>();
		for (int i = 0; i < arry1.length; i++) {
			String key = ((String) arry1[i]).split(",")[0];
			String value = ((String) arry1[i]).split(",")[1];
			int k = 0;
			int n = arry1.length;
			;
//			if (n < 12) {
//				k = 12;
//			} else if (11 < n && n < 16) {
//				k = 11;
//			} else if (16 < n && n < 20) {
//				k = 10;
//			} else {
//				k = 9;r
//			}
			//上下限
			double upper = 4;  //google 8  wiki 4
			double lower = 2;  //google 5  wiki 1
//			System.out.println("K=" + k);
			
			//根據搜尋結果過濾字詞
			//wiki要考慮回傳值不能為0
			if (lower <= value.length() && Long.valueOf(value)!=0) {
			//google版
			//if (lower <= value.length()) {
				if (!key.contains("+")) {
					if (value.length() <= upper) {
						System.out.println(arry1[i] + " add");
						String arry_out = (String) arry1[i];

						filtered_key.add(key);
						filtered_line.add((String) arry1[i]);
						try {

							bw.write(arry_out);
							bw.newLine();
							bw.flush();
						} catch (IOException f) {
							// TODO Auto-generated catch block
							f.printStackTrace();
						}
					}else
					{
						System.out.println(arry1[i] + " out");
					}
				}else
				{//可以設定不同個字詞組成的狀況不同門檻
					if (value.length() <= upper) {
						System.out.println(arry1[i] + " add");
						String arry_out = (String) arry1[i];

						filtered_key.add(key);
						filtered_line.add((String) arry1[i]);
						try {

							bw.write(arry_out);
							bw.newLine();
							bw.flush(); 
						} catch (IOException f) {
							// TODO Auto-generated catch block
							f.printStackTrace();
						}
					}else
					{
						System.out.println(arry1[i] + " out");
					}
				}

			} else {
				System.out.println(arry1[i] + " out");
			}

		}
		Collections.sort(filtered_line, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				double a = Double.parseDouble(o1.split(",")[1]);
				double b = Double.parseDouble(o2.split(",")[1]);
				
				if(a>b)
					return 1;
				else if (a<b)
					return -1;
				else
					return 0;

			}
		});
		Object[] objline = filtered_line.toArray();
		bw = new BufferedWriter(new FileWriter("citeulike/citeulike_TermRank/"+no + "_" + "termRank.txt", false));
		for (int i = 0; i < objline.length; i++) {
			bw.write((String) objline[i]);
			bw.newLine();
			bw.flush(); // 清空緩衝區
		}
		Object[] obj = filtered_key.toArray();
		bw = new BufferedWriter(new FileWriter("citeulike/citeulike_Pairs/"+no + "_" + "pairs.txt", false));
		for (int i = 0; i < obj.length; i++) {
			for (int j = i + 1; j < obj.length; j++) {
				System.out.println(obj[i] + "+" + obj[j]);
				bw.write(obj[i] + "+" + obj[j]);
				bw.newLine();
				bw.flush(); // 清空緩衝區
			}
		}

		bw.close();

	}
	public static void main(String no) throws IOException {
		search_filter(no);
	}

	public static void main(String args[]) throws IOException {
		
		File F = new File("citeulike/citeulike_Search1/");
		for(File f : F.listFiles()){
			//System.out.println(f.getName().split("_")[0]);
			main(f.getName().split("_")[0]);
		}
		
		/*Scanner input = new Scanner(System.in);
		int no = input.nextInt();
		//search_filter(no);*/
	}

}
