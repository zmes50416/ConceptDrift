import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CalAveWord {

	public static void main(String args[]) {
		BufferedReader br;
		File f = new File("Number_of_term");
		File[] files = f.listFiles();
		int ind=0;
		
		double sum = 0;
		double max=0;
		double min=1000;
		double than10=0;
		for (File t : files) {
			ind++;
			double num = 0;
			
			try {
				String line;
				br = new BufferedReader(new FileReader(t));
				while ((line = br.readLine()) != null) {
					num++;
				}
				if(num>max)
				{
					max=num;
				}
				if(num<min)
				{
					min=num;
				}
				if(num>10)
					than10++;
				sum=(num)+sum;
//				System.out.println(num);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sum/600);
		System.out.println(max);
		System.out.println(min);
		System.out.println(than10);

	}

}
