import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import tw.edu.ncu.sia.util.ServerUtil;

public class test {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		HashMap<String,Double> TR_NGD = new HashMap<String,Double>(); //NGD計算後的主題關係
		TR_NGD.put("z", 0.3);
		TR_NGD.put("d", 0.4);
		TR_NGD.put("a", 0.5);
		TR_NGD.put("r", 0.6);
		TR_NGD.put("y", 0.1);
		boolean y = false;
		/*for(String edge1: TR_NGD.keySet()){
			//System.out.println(TR_NGD.get(edge1));
			for(String edge2: TR_NGD.keySet()){
				//System.out.println(edge1+",  "+edge2);
				if(edge1 == edge2){
					y = true;
				}
				if(y){
					System.out.println(TR_NGD.get(edge2));
				}
			}
			y=false;
		}*/
	}
}
