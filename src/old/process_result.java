import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class process_result {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileReader FileStream;
		String topic="Topic_Cs/";
		try {
			FileStream = new FileReader(topic+"all.txt");
			BufferedReader BufferedStream = new BufferedReader(FileStream);
			String line="";
			BufferedWriter posBw = new BufferedWriter(new FileWriter(topic+"pos2.txt", true));
			BufferedWriter negBw = new BufferedWriter(new FileWriter(topic+"neg2.txt", true));
			while ((line = BufferedStream.readLine()) != null) {
				if(Integer.parseInt(line.split("_")[0])>=201&&Integer.parseInt(line.split("_")[0])<=400)
				{
					System.out.println("pos:"+line);
					posBw.write(line);
					posBw.newLine();
					posBw.flush();
				}else
				{
					System.out.println("neg:"+line);
					negBw.write(line);
					negBw.newLine();
					negBw.flush();
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
