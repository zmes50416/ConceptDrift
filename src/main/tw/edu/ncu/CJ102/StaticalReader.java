package tw.edu.ncu.CJ102;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 測試學長所撰寫的前處理所留下的群數與字數
 * @author TingWen
 *
 */
public class StaticalReader {

	public static void main(String[] args){
		
		File oldExp = new File(SettingManager.chooseProject());
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"staticOf"+oldExp.getName()+".txt")))) {
			
			for (File topic : oldExp.listFiles()) {
				System.out.println("topic:"+topic);
				if(!topic.isDirectory()){
					continue;
				}
				double clusterSize = 0;
				double termSize = 0;
				double avgNgd= 0;
				int documentSize = 0;
				
				for (File document : topic.listFiles()) {
					try (BufferedReader br = new BufferedReader(new FileReader(
							document))) {
						double ngd = Double.parseDouble(br.readLine());
						if(ngd != Double.NaN){
							avgNgd += ngd;
						}
						int terms = 0;
						int maxCluster = 0;
						for (String line = br.readLine(); line != null; line = br
								.readLine()) {
							int group = Integer.valueOf(line.split(",")[2]); // 字詞所屬群別
							if (maxCluster < group) {
								maxCluster = group;
							}
							terms++;
						}
						clusterSize += maxCluster;
						termSize += terms;
					} catch (IOException e) {
						e.printStackTrace();
					}
					documentSize++;
				}
				bw.write("Topic:" + topic.getName());
				bw.newLine();
				bw.write("Size of terms:" + termSize / documentSize);
				bw.newLine();
				bw.write("clusters:" + clusterSize / documentSize);
				bw.newLine();
				bw.write("NGD:" + avgNgd / documentSize);
				bw.newLine();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
