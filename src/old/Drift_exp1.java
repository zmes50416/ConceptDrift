import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class Drift_exp1 {
	
	static String maindir = "reuters/";
	static double betweeness_threshold = 0.1;
	static int size = 5;

	/**
	 * @for single topic
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double thresholds[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		for(int i=0; i<thresholds.length; i++){
			betweeness_threshold = thresholds[i];
		
			BufferedWriter bw = null;
			File dir = new File(maindir);
		
			for(File d: dir.listFiles()){
				File testdir = new File("bc_exp/1-0/bc_"+betweeness_threshold+"/"+d.getName());
				testdir.mkdirs();
			
				File resultdir = new File("bc_exp/1-0/bc_result_"+betweeness_threshold);
				resultdir.mkdirs();
			
				Go_Training3.generateTrainSet(size, testdir.getPath(), d.getName());
			
				try {
					bw = new BufferedWriter(new FileWriter(resultdir.getPath()+"/"+d.getName()+"_result.txt"));
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
								bc.betweenness_cal(testdir.getPath(), f.getName(), f.getName()+"_center.txt", f.getName()+"_concpet.txt",true);
					
						double m = bc.computeModularity(bc.g, map);
				
						sum+=m;
				
						try {
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

}
