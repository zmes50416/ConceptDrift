import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class google_filter2 {

	/**
	 * 
	 */
	public google_filter2() {
		// TODO Auto-generated constructor stub

	}

	public static void search_filter(String no)  {
		FileReader FileStream;
		System.out.println("處理檔案"+no+"中...");
		try {
			FileStream = new FileReader("citeulike/citeulike_Search2/"+no + "_"
					+ "google_output2.txt");
			// System.out.println(FileStream);

			BufferedReader reader = new BufferedReader(FileStream);
			String line = "";
			// System.out.println(line);

			FileReader FileStream1 = new FileReader("citeulike/citeulike_Pairs/"+no + "_" + "pairs.txt");
			BufferedReader BufferedStream1 = new BufferedReader(FileStream1);
			String e1 = "";

			ArrayList list1 = new ArrayList();
			while ((e1 = BufferedStream1.readLine()) != null) {

				list1.add(e1);
			}

			Object[] datas = list1.toArray();

			LinkedHashSet<String> set = new LinkedHashSet<String>();
			LinkedHashSet<String> set1 = new LinkedHashSet<String>();

			int count = 0;
			while ((line = reader.readLine()) != null) {
				// String key = null;
				String value1 = null;
				if (count < datas.length) {

					if (line.contains("約有 ")||line.contains(" 項結果")) {

						String value = line.replaceAll(",", ""); // 約有 173,000 項結果

						value1 = value.split(" ")[value.split(" ").length-2];
						// set.add(value1);
						String key = ((String) datas[count]);
						set.add(key + "," + value1);
						//System.out.println(key+"==>"+value1);
						count++;

					}

				}

			}

			Object[] arry1 = set.toArray();
			// Integer sum = 0;
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter("citeulike/citeulike_Number_of_pair/"+no + "_"
					+ "number_of_pair.txt", false));
			//ArrayList<String> filtered_key=new ArrayList<String>();
			for (int i = 0; i < arry1.length; i++) {
				String key = ((String) arry1[i]).split(",")[0];
				String value = ((String) arry1[i]).split(",")[1];
				// Integer value = Integer.parseInt(((String)
				// arry[i]).split(",")[1]);
				// sum += value;
				//int k = 0;
				int n = arry1.length;
				
				
				
					//System.out.println(arry1[i]+" add");
					String arry_out = (String) arry1[i];
					//filtered_key.add(key);
					try {
						

						bw.write(arry_out);
						bw.newLine();
						bw.flush(); // 清空緩衝區
						
						// 建立運用緩衝區輸出資料至data.txt檔的BufferedWriter物件
						// ，並由bw物件參考引用
						// 將字串寫入檔案
					} catch (IOException f) {
						// TODO Auto-generated catch block
						f.printStackTrace();
					}
				
					System.out.println("處理檔案"+no+"處理完畢");
			}
			
			
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	// TODO Auto-generated method stub
	public static void main(int no) throws IOException {
		//search_filter(no);
	}

	public static void main(String args[]) throws IOException {
		// TODO Auto-generated method stub
		File F = new File("citeulike/citeulike_Search2/");
		for(File f : F.listFiles()){
			//System.out.println(f.getName().split("_")[0]);
			search_filter(f.getName().split("_")[0]);
		}
		//search_filter(1);
	}

}
