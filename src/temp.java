import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class temp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//try {
			int count=0;
		
		/*
			String line1="",line2="",name1="",name2="";
			double all=0,yes=0;
			
			BufferedReader br2= new BufferedReader(new FileReader("citeulike/all_train_0_test.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("citeulike/scal_of_yestoall.txt"));
			while((line1=br1.readLine())!=null){
				line2=br2.readLine();
				name1=line1.split("-")[0];
				name2=line2.split(",")[0];
				if(!name1.equals(name2)){
					System.out.println("人名不匹配");
				}else{
					all=Double.valueOf(line1.split(",")[1])+Double.valueOf(line1.split(",")[2]);
					yes=Double.valueOf(line2.split(",")[1]);
					bw.write(line1.split("-")[0]+","+(yes/all));
					bw.newLine();
					bw.flush();
					
				}
			}
			br1.close();
			br2.close();
			bw.close();*/
			
			File d = new File("citeulike/citeulike_sTF_score/");
			for(File f : d.listFiles()){
				System.out.println(f.getName());
				//System.out.println(f.getPath());
				File toFile = new File("citeulike/citeulike_sTF_score/cite_0"+f.getName());
            	f.renameTo(toFile);
			}
			System.out.println(count);
			
			/*File F = new File("citeulike/citeulike_Search1/");
			for(File f : F.listFiles()){
				File file = new File("citeulike/citeulike_Pairs/"+f.getName().split("_")[0]+"_pairs.txt");
				if(!file.exists()){
					System.out.println(file.getName());
					count++;
				}
			}
			System.out.println(count);*/
		/*} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
