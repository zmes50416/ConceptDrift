import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * @yoshi
 *	用以測試平均NGD 相關性門檻值
 *  for學長方法
 */
public class filteringThreshold {	
	
	public static void main(String args[]){
		
		int train_chunk_index =354;
		int test_chunk_index = 354;
		int centersize = 3;
		double Fmeasure = 0;
		target_topic t = new target_topic(101, 200);
		CompareAveNGD cal = new CompareAveNGD();
		System.out.println("UP");
		new filteringThreshold().updateCenter(
				"Applied Ergonomics/",
				"drift_center/topic_" + train_chunk_index + "/",
				"chunk/ch_" + train_chunk_index + "/"
				);
		
		
		
		try {
			Fmeasure = cal.caculBatchDistance(
					"drift_center/topic_"+ train_chunk_index + "/",
					"center.txt", 
					"all.txt",
					"chunk/ch_" + test_chunk_index + "/", 
					t,
					centersize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public ArrayList<String> updateCenter(String topic, String topicDir, String mainDir) {
		Go_Training.topicDir = topicDir;
		Go_Training.mainDir = mainDir;
		new File(topicDir).mkdir();
		new File(topicDir + "Set").mkdir();
		new File(mainDir).mkdir();
		HashSet<File> set = Go_Training.generateTrainSet(topic, 6);
		String[] newfile = new String[set.size()];
		int i = 0;
		for (File l : set) {
			System.out.println(l);
			copyfile(l, new File(Go_Training.topicDir + "Set/" + l.getName()));
			newfile[i] = l.getName();
			i++;
		}
		
		
		HashSet<File> testset = Go_Training.generateTestSet(6);
		
		for (File f : testset) {
			System.out.println(f);
			copyfile(f, new File(Go_Training.mainDir + f.getName()));
		}
		

		try {
			Go_Training.generateCandidate(newfile,true);
			Go_Training.getTermScore();
			Go_Training.Top_N();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static void copyfile(File srFile, File dtFile) {
		try {
			File f1 = srFile;
			File f2 = dtFile;
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
