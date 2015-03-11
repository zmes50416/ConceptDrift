import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class POS_filter {

	/**
	 * 
	 */
	public POS_filter() {
		// TODO Auto-generated constructor stub
	}

	public static void filter(String f) throws IOException {
		FileReader FileStream = new FileReader("Keyword_output_freq/"+f + "_"
				+ "keyword_output_freq.txt");
		BufferedReader BufferedStream = new BufferedReader(FileStream);
		String e = "";

		ArrayList list = new ArrayList();
		while ((e = BufferedStream.readLine()) != null) {
			list.add(e);
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
			//組合字過濾
			/*if (key1.equals(key2)) {//最後一個字
				if ((tag1.equals("NN") || tag1.equals("NNS") || tag1
						.equals("NP"))
						&& key1.length() > 2) {
					set.add(key1);
					// i++;
					System.out.println("add:" + key1);
				}
			} else if (key2.equals(key3)) {//最後兩個字
				if ((tag1.equals("NN") || tag1.equals("NP")
						|| tag1.equals("NNS") || tag1.equals("NPS") || tag1
						.equals("JJ"))
						&& key1.length() > 2) {
					if ((key1.endsWith(",") || key1.endsWith("."))||(key2.startsWith(",") || key2.startsWith("."))) {
						set.add(key1);
						System.out.println("add:" + key1);
					} else if ((tag2.equals("NN") || tag2.equals("NP")
							|| tag2.equals("NPS") || tag2.equals("NNS"))
							&& key2.length() > 2) {

						set.add(key1 + "+" + key2);
						System.out.println("add:" + key1 + "+" + key2);
						i++;
						// System.out.println(key + "_" + key1);
					} else {
						set.add(key1);
						System.out.println("add:" + key1);
					}
				}

			} else {
				if ((tag1.equals("NN") || tag1.equals("NP")
						|| tag1.equals("NNS") || tag1.equals("JJ"))
						&& key1.length() > 2) {
					if ((key1.endsWith(",") || key1.endsWith("."))||(key2.startsWith(",") || key2.startsWith("."))) {
						set.add(key1);
						System.out.println("add:" + key1);//串接一個字
					} else if ((tag2.equals("NN") || tag2.equals("NP")
							|| tag2.equals("NPS") || tag2.equals("NNS"))
							&& key2.length() > 2) {
						if ((key2.endsWith(",") || key2.endsWith("."))||(key3.startsWith(",") || key3.startsWith("."))) {
							set.add(key1 + "+" + key2);
							System.out.println("add:" + key1 + "+" + key2);
							i++;//串接兩個字
						} else if ((tag3.equals("NN") || tag3.equals("NP")
								|| tag3.equals("NPS") || tag3.equals("NNS"))
								&& key3.length() > 2) {
							set.add(key1 + "+" + key2 + "+" + key3);
							System.out.println("add:" + key1 + "+" + key2 + "+"
									+ key3);//串接三個字
							i = i + 2;
						} else {
							set.add(key1 + "+" + key2);
							System.out.println("add:" + key1 + "+" + key2);//串接兩個字
							i++;
						}

					}else
					{
						set.add(key1);
						System.out.println("add:" + key1);//串接一個字
					}

				}
			}*/
		}


		Object[] objs = set.toArray();
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter("POS_filter/"+f + "_" + "filter_output1.txt",
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
		//filter(1);
	}

}
