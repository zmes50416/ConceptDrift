import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class process_chunks {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// File[] datas = new File("Main_word").listFiles();
		File[] datas = new File("Ad hoc").listFiles();
		File[] datas2 = new File("Knowledge-Based Systems").listFiles();
		File[] datas3 = new File("Planetary and Space Science").listFiles();
		File[] datas4 = new File(
				"Journal of Quantitative Spectroscopy and Radiative Transfer")
				.listFiles();
		File[] datas5 = new File("Addictive Behaviors").listFiles();
		File[] datas6 = new File("Applied Ergonomics").listFiles();
		ArrayList<File> list = new ArrayList<File>();
		ArrayList<File> list2 = new ArrayList<File>();
		ArrayList<File> list3 = new ArrayList<File>();
		for (File s : datas) {
			// System.out.println(s);
			list.add(s);
		}
		for (File s : datas2) {
			// System.out.println(s);
			list.add(s);
		}
		for (File s : datas3) {
			// System.out.println(s);
			list2.add(s);
		}

		for (File s : datas4) {
			// System.out.println(s);
			list2.add(s);
		}
		for (File s : datas5) {
			// System.out.println(s);
			list3.add(s);
		}

		for (File s : datas6) {
			// System.out.println(s);
			list3.add(s);
		}
		// ArrayList test=new ArrayList();
		// test.
		Collections.shuffle(list);
		Collections.shuffle(list2);
		// Collections.shuffle(list3);
		int i = 0;
		int j = 0;
		String dirPath = "";
		for (File s : list) {
			if (i % 3 == 0) {
				dirPath = "chunk/ch_" + j;
				File toDir = new File(dirPath);
				toDir.mkdir();
				System.out.println();
				j++;
			}

			System.out.println(s.getName());
			copyfile(s, new File(dirPath + "/" + s.getName()));

			i++;
		}
		i = 0;
		j = 0;
		for (File s : list2) {
			if (i % 3 == 0) {
				dirPath = "chunk/ch_" + j;
				File toDir = new File(dirPath);
				// toDir.mkdir();
				System.out.println();
				j++;
			}

			System.out.println(s.getName());
			copyfile(s, new File(dirPath + "/" + s.getName()));

			i++;
		}
		i = 0;
		j = 0;
		for (File s : list3) {
			if (i % 4 == 0) {
				dirPath = "chunk/ch_" + j;
				File toDir = new File(dirPath);
				// toDir.mkdir();
				System.out.println();
				j++;
			}

			System.out.println(s.getName());
			copyfile(s, new File(dirPath + "/" + s.getName()));

			i++;
		}

	}

	private static void copyfile(File srFile, File dtFile) {
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
