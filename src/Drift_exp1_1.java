import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class Drift_exp1_1 {
	
	static String maindir = "multi/";
	static double betweeness_threshold = 0.9;
	static int size = 60;

	/**
	 * for multi-topic
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double thresholds[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		for(int i=0; i<thresholds.length; i++){
			betweeness_threshold = thresholds[i];
			BufferedWriter bw = null;
		
		
			File testdir = new File("bc_exp/1-x/bc_multi_3_"+betweeness_threshold);
			//testdir.mkdirs();
			
			File resultdir = new File("bc_exp/1-x/bc_multi_result_3_"+betweeness_threshold);
			resultdir.mkdirs();
			
			//Go_Training3.generateTrainSet(maindir, size, testdir.getPath());
			
			try {
				bw = new BufferedWriter(new FileWriter(resultdir.getPath()+"/"+betweeness_threshold+"_result.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			double avg = 0.0;
			double sum = 0.0;
			
			for(File f: testdir.listFiles()){
				if(!f.isDirectory()){
					betweennessCentralityt bc = new betweennessCentralityt();
					bc.betweeness_threshold = betweeness_threshold;
				
					Map<String, Integer> map = 
						bc.betweenness_cal(testdir.getPath(), f.getName(), f.getName()+"_center.txt", f.getName()+"_concpet.txt", true);
					
					double m = bc.computeModularity(bc.g, map);
				
					sum+=m;
				
					try {
						System.out.print("目前文件為 = "+f.getName()+":"+m+"\n");
						bw.write(f.getName()+":"+m);
						bw.newLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			avg = sum/size;
			
			try {
				bw.write("avg:"+avg);
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		
		}
	}

}
