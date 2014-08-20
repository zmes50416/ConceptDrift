import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * 
 */

/**
 * @author user
 * 用以評估與學長前處理的差異
 */
public class preprocessTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			
		File d = new File("preprocess/pre");
		
		double t = 1;
		
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		try {
			bw2 = new BufferedWriter(new FileWriter("preprocess/results/"+t+".txt"));;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		

		BufferedReader br;
		LinkedList<String> linkList;
		HashSet<String> vertices = new HashSet<String>();
		HashSet<String> ans = new HashSet<String>();
		
		
		for(File f : d.listFiles()){
			try {
				br = new BufferedReader(new FileReader(f));
				linkList = new LinkedList<String>();
				String line;
				
				//修改後的做法
				while((line = br.readLine())!=null && Double.parseDouble(line.split(",")[2])<1){
				
				//學長作法
				//while((line = br.readLine())!=null){
					linkList.add(line);
				}
				
				br.close();
				
				double simMin = Double.parseDouble(linkList.get((int) (linkList.size()*t)-1).split(",")[2]);
				
				for(String s : linkList){
					if(Double.parseDouble(s.split(",")[2]) <= simMin){
						String vertex1 = s.split(",")[0];
						String vertex2 = s.split(",")[1];
						
						vertex1 = vertex1.replace("+", " ");
						vertex2 = vertex2.replace("+", " ");
					
						vertices.add(vertex1);
						vertices.add(vertex2);					
					}
				}
				
				bw1 = new BufferedWriter(new FileWriter("preprocess/results/"+f.getName()+"_"+t+"_words.txt"));
				
				for(String v : vertices){
					bw1.write(v);
					bw1.newLine();
					bw1.flush();
				}
				
				bw1.close();
				
				br = new BufferedReader(new FileReader("preprocess/answers/"+f.getName().split("_")[0]+"_keyword.txt"));
				
				while((line = br.readLine()) != null){
					ans.add(line);
				}
				
				int hit = 0;
				
				for(String s1 :ans)
					for(String s2 : vertices)
						if(s1.equals(s2))
							hit++;

				
				bw2.write(f.getName() + ":" + (double)hit/ans.size()+":"+ (double)hit/vertices.size());
				bw2.newLine();
				bw2.flush();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			bw2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
