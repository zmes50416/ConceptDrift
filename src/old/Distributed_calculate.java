import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Distributed_calculate {

	public static void NGD(String no) {
		try {
			FileReader FileStream1;

			FileStream1 = new FileReader("Number_of_pair/" + no + "_"
					+ "number_of_pair.txt");

			BufferedReader BufferedStream1 = new BufferedReader(FileStream1);
			String e1 = "";

			ArrayList<String> pairlist = new ArrayList<String>();
			while ((e1 = BufferedStream1.readLine()) != null) {

				pairlist.add(e1);
			}

			FileReader FileStream = new FileReader("Stem/" + no + "_"
					+ "stem.txt");
			BufferedReader BufferedStream = new BufferedReader(FileStream);
			String e = "";

			ArrayList<String> termlist = new ArrayList<String>();
			while ((e = BufferedStream.readLine()) != null) {
				termlist.add(e);
			}

			Object[] datas = termlist.toArray();
			LinkedHashSet<String> set = new LinkedHashSet<String>();
			for (int i = 0; i < datas.length; i++) {
				String key1 = String.valueOf(datas[i]).split(",")[0];
				double x = Double.parseDouble(String.valueOf(datas[i]).split(
						",")[1]);
				for (int j = i + 1; j < datas.length; j++) {
					String key2 = String.valueOf(datas[j]).split(",")[0];
					double y = Double.parseDouble(String.valueOf(datas[j])
							.split(",")[1]);
					double m = 0;
					for (String o : pairlist) {
						if (o.contains("\"" + key1 + "\"+\"" + key2 + "\"")
								|| o.contains("\"" + key2 + "\"+\"" + key1
										+ "\""))
						{
							m = Double.parseDouble(o.split(",")[1]);
							break;
						}
					}
					
					if (m == 0){
						m=1;
					}
					System.out.println("x=" + x + " y=" + y + " m=" + m);
					double NGD = (2*m)/(x+y);
					System.out.println(key1 + "," + key2 + ";" + NGD);
					set.add("x=" + x + " y=" + y + " m=" + m);
					set.add(key1 + "," + key2 + ";" + NGD);
				}
			}
			Object[] objs = set.toArray();
			File file = new File("tol/" + no + "_" + "ntol.txt");
			file.delete();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter("tol/" + no + "_" + "ntol.txt"));
			for (int j = 0; j < objs.length; j++) {

				System.out.println(objs[j]);
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws IOException {
		/*int j=50;
		File dir=new File("dataset/acq/");
		String files[]=dir.list();
		for(int i = 0 ; i <= j ; i++){
			String s = files[i];
			s=s.replace(".txt", "");
			System.out.print("filename = " +s+"\n");
			NGD(s);
			System.out.print("done\n");
		}*/
		NGD("trade_0003869");
	}
}
