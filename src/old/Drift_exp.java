import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class Drift_exp {

	//小尖程式
	public static void main(String args[]) {
		int train_chunk_index = 0;
		int test_chunk_index = 0;
		int end_chunk = 1;
		int centersize = 5;
		double Fmeasure = 0;
		target_topic t = new target_topic(1, 200);
		CompareAveNGD cal = new CompareAveNGD();
		System.out.println("UP");
		new Drift_exp().updateCenter(
				"drift_center/topic_" + train_chunk_index + "/",
				"chunk/ch_" + train_chunk_index + "/",
				t);
		test_chunk_index = 1;
		try {
			while (true) {
				if (test_chunk_index <= 1)
					t = new target_topic(1, 200);
				else
					t = new target_topic(401, 600);

				Fmeasure = cal.caculBatchDistance(
						"drift_center/topic_"+ train_chunk_index + "/",
						"center.txt", 
						"all.txt",
						"chunk/ch_" + test_chunk_index + "/", 
						t,
						centersize);
				System.out.println("Used topic_" + train_chunk_index
						+ " filter chunk_" + test_chunk_index + ":" + Fmeasure);
				if (Fmeasure >= 0.7) {
					test_chunk_index++;

				} else if (Fmeasure >= 0.5 && Fmeasure < 0.7) {
					new Drift_exp().updateCenter("drift_center/topic_"
							+ train_chunk_index + "/", "chunk/ch_"
							+ test_chunk_index + "/", t);
					test_chunk_index++;
				} else {
					train_chunk_index = test_chunk_index;
					new Drift_exp().updateCenter("drift_center/topic_"
							+ train_chunk_index + "/", "chunk/ch_"
							+ train_chunk_index + "/", t);
					System.out.println("Build new topic_" + train_chunk_index);
					test_chunk_index++;
				}
				if (test_chunk_index >= end_chunk)
					break;
				// System.out.println(test_chunk_index+" "+train_chunk_index);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public ArrayList<String> updateCenter(String topicDir, String mainDir,
			target_topic t) {
		Go_Training.topicDir = topicDir;
		Go_Training.mainDir = mainDir;
		new File(topicDir).mkdir();
		new File(topicDir + "Set").mkdir();
		HashSet<File> set = Go_Training.generateTrainSet(t.lower, t.upper);
		String[] newfile = new String[set.size()];
		int i = 0;
		for (File l : set) {
			System.out.println(l);
			copyfile(l, new File(Go_Training.topicDir + "Set/" + l.getName()));
			newfile[i] = l.getName();
			i++;
		}

		try {
			Go_Training.generateCandidate(newfile,true);
			Go_Training.getTermScore();
			Go_Training.Top_N();
			//重新更新語意中心
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
