import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class copyFile {
	
	public static void main(String args[]){
		
		File dir = new File("reuters/");
		
		for(File t: dir.listFiles()){
			
			File datadir = new File("dataset/"+t.getName());
			
			new File("usedData/"+t.getName()).mkdirs();
			
			for(File d1 : t.listFiles()){
				for(File d2 :datadir.listFiles()){
					
					String n1 = d1.getName().split("_")[1];
					//System.out.println(n1);
					
					String n2 = d2.getName().split("_")[1];
					n2 = n2.replace(".txt", "");
					//System.out.println(n2);
					
					if(n1.equals(n2))
						copyfile(d2, new File("usedData/"+t.getName()+"/"+d2.getName()));
					
				}
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
	}
}
