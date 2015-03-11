import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;


public class Copy_train_test {
	static String trainDir_relate1;
	static String trainDir_relate2;
	static String testDir1;
	static String testDir2;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String baseDir = "../ConceptDrift_Tom/exp4/";
		String baseDir = "exp_modle_test1_NGDt0.4_學長方法/";
		String fromDir = "exp_modle_test1_NGDt0.4/";
		int check = 10;
		//new File(baseDir).mkdirs();
		for(int i=1;i<=check;i++){
			trainDir_relate1 = baseDir+"chunck"+i+"/training/related";;
			//trainDir_relate1 = baseDir+"chunck"+i+"/training/related";
			trainDir_relate2 = "../ConceptDrift_Tom/"+fromDir+"training/day_"+i;
			new File(trainDir_relate1).mkdirs();
			ArrayList<File> list = new ArrayList<File>();
			File cdir = new File(trainDir_relate2);
			for(File f : cdir.listFiles()){
				System.out.println(f.getName()+"+++");
				list.add(f);
			}
			for(int j=0;j<list.size();j++){
				//System.out.println("準備複製"+"Rank" + "/" + list.get(j).getName()+"到"+trainDir_relate1 + "/" + list.get(j).getName());
				//copyfile(list.get(j), new File(trainDir_relate1 + "/" + list.get(j).getName()));
				//copyfile(new File("Rank" + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"), new File(trainDir_relate1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"));
				copyfile(new File("../ConceptDrift_Tom/NGD_Tolerance_0.4" + "/" +list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_TolNGD.txt"), new File(trainDir_relate1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"));
				//copyfile(new File("../ConceptDrift_Tom/Tom_reuters_v2/single/" + list.get(j).getName().split("_")[0] + "/" +list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_concepts.txt"), new File(trainDir_relate1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_concepts.txt"));
			}
		}
		
		for(int i=1;i<=check;i++){
			testDir1 = baseDir+"chunck"+i+"/testing";
			//testDir1 = baseDir+"chunck"+i+"/testing";
			testDir2 = "../ConceptDrift_Tom/"+fromDir+"testing/day_"+i;
			new File(testDir1).mkdirs();
			ArrayList<File> list = new ArrayList<File>();
			File cdir = new File(testDir2);
			for(File f : cdir.listFiles()){
				System.out.println(f.getName()+"+++");
				list.add(f);
			}
			for(int j=0;j<list.size();j++){
				//copyfile(list.get(j), new File(testDir1 + "/" + list.get(j).getName()));
				//copyfile(new File("Rank" + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"), new File(testDir1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"));
				copyfile(new File("../ConceptDrift_Tom/NGD_Tolerance_0.4" + "/" +list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_TolNGD.txt"), new File(testDir1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_Rank.txt"));
				//copyfile(new File("../ConceptDrift_Tom/Tom_reuters_v2/single/" + list.get(j).getName().split("_")[0] + "/" +list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_concepts.txt"), new File(testDir1 + "/" + list.get(j).getName().split("_")[0] + "_" + list.get(j).getName().split("_")[1] + "_concepts.txt"));
			}
		}
	}
	
	public static void copyfile(File srFile, File dtFile) {
		try {
			File f1 = srFile;
			File f2 = dtFile;
			InputStream in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
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
	
		
		
		/*for(int i=1; i<=1; i++){
			String trainDir_relate = "exp4/chunck"+i+"/training/related";
			String trainDir_unrelate = "exp4/chunck"+i+"/training/unrelated";
			String testDir = "exp4/chunck"+i+"/testing";
			Go_Training3.generateTrainSet_t(60, trainDir_relate, "acq", i);
			Go_Training3.generateTrainSet_t(60, testDir, "earn", i);
		}*/
	}
}
