import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CalResult {
	// double threshold = 0.5;
	static double correct_pos_predict = 0;
	static double pos_predict = 0;
	// Precision
	static double total_pos = 0;
	static double correct_predict = 0;
	static double total = 0;

	public void calPRF(String simResult, double t, boolean ex) {
		FileReader FileStream;
		try {
			FileStream = new FileReader(simResult);
			BufferedReader reader = new BufferedReader(FileStream);
			String line = "";
			// double correct=0;
			// double total=0;
			while ((line = reader.readLine()) != null) {
				
				total++;
				if (ex == true) {
					total_pos++;
				}
				if (Double.parseDouble(line.split(":")[1]) <= t) {
					pos_predict++;
					if (ex == true) {
						correct_pos_predict++;
						correct_predict++;
					}
				} else {
					if (ex == false) {
						correct_predict++;
					}
				}
			}

			// System.out.println(correct/total*100+"%("+correct+"/"+total+")");

			

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(FileStream);
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {
//		double threshold=0.649159472877746965;
//		threshold=0.6072700972762202;
//		double threshold=0.6648963085068484;
//		double threshold=0.5063784516402107;
//		double threshold=0.611734370249859;
		double threshold=0.75;
		
		ArrayList<String> neg = new ArrayList<String>();
		ArrayList<String> pos = new ArrayList<String>();
		pos.add("Topic_Cs//pos6.txt");
		neg.add("Topic_Cs//neg6.txt");
		CalResult cal = new CalResult();
		for (String path : pos)
			cal.calPRF(path, threshold,true);
		for (String path : neg)
			cal.calPRF(path, threshold,false);
		double accuracy = (double) correct_predict / total * 100;
		System.out.println("Accuracy = " + accuracy + "% ("
				+ correct_predict + "/" + total + ")");
		double precision = (double) correct_pos_predict / pos_predict * 100;
		System.out.println("Precision = " + precision + "% ("
				+ correct_pos_predict + "/" + pos_predict + ")");

		double recall = (double) correct_pos_predict / total_pos * 100;
		System.out.println("Recall = " + recall + "% ("
				+ correct_pos_predict + "/" + total_pos + ")");
		double F = (double) (2 * precision * recall * 0.0001)
				/ (precision * 0.01 + recall * 0.01);
		System.out.println("F-measure = " + F);

	}

}
