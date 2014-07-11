import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;


public class Go_Training2 {

	/**
	 * @param args
	 */
	
	//產生訓練資料於dir
	public static HashSet<String> generateTrainSet(int size, int low, int up, String dir){
		BufferedReader br;
		
		BufferedWriter bw;
		
		Random rand = new Random();
		HashSet<String> trainSet = new HashSet<String>();
		String line;
		
		while(trainSet.size()<size){
			LinkedList<String> linkList = new LinkedList<String>();

			int seq = low+rand.nextInt(up-low);
			
			try {
				br = new BufferedReader(new FileReader("Rank/"+seq + "_" + "Rank.txt"));
				
				
				//可修改成距離非1的前1/n				
				while((line = br.readLine()) != null && Double.parseDouble(line.split(",")[2])<1){
					linkList.add(line);
				}
				br.close();
				
				double simMin = Double.parseDouble(linkList.get(linkList.size()/4).split(",")[2]);
				
				bw = new BufferedWriter(new FileWriter(dir+"concepts/"+seq+"_concept"+".txt"));
				
				for(String l :linkList){
					if(Double.parseDouble(l.split(",")[2]) < simMin){
						String vertex1 = l.split(",")[0];
						String vertex2 = l.split(",")[1];
						
						String edge = vertex1+","+vertex2;
						
						bw.write(edge+","+l.split(",")[2]);
						bw.newLine();
						bw.flush();
					}
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
		return trainSet;
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
