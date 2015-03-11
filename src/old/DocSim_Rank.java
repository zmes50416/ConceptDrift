import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

public class DocSim_Rank {
	public static void main(String args[])
	{
		main(1);
	}
	public static void main(int no) {
		try {
			String path="";
			BufferedReader br = new BufferedReader(new FileReader("PsyTopic/filter_score6.txt"));
			Map<String, Double> map_Data = new HashMap<String, Double>();
			String line = "";
			while ((line = br.readLine()) != null) {
				String key = line.split(":")[0];
				
				Double value = Double.parseDouble(line.split(":")[1]);
				
				map_Data.put(key, value);
			}

			List<Map.Entry<String, Double>> list_Data = new ArrayList<Map.Entry<String, Double>>(
					map_Data.entrySet());
			Iterator<Map.Entry<String, Double>> iterator = list_Data.iterator();

			Collections.sort(list_Data,
					new Comparator<Map.Entry<String, Double>>() {
						public int compare(Map.Entry<String, Double> o1,
								Map.Entry<String, Double> o2) {
							return (int) ((o1.getValue() - o2.getValue()) * 1000.0);
						}
					});
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("PsyTopic/filter_score7.txt"));

			while (iterator.hasNext()) {
				Map.Entry<String, Double> entry = iterator.next();
				System.out.println(entry.getKey() + ":" + entry.getValue());
				bw.write(entry.getKey() + ":" + entry.getValue());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

