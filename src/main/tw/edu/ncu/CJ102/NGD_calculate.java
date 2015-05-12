package tw.edu.ncu.CJ102;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class NGD_calculate {
	
	public static double NGD_cal(double x, double y, double m) {
		double logX = Math.log10(x);
		double logY = Math.log10(y);
		double logM=0.0;
		
		logM = Math.log10(m);
		//當X=0的時候要處理Log(0)的問題，在此先改成m為1，讓LogM=0

		//9.906是Google的
		//double logN = 5.507;
		double logN = 6.627;//LogN 為總體文件(N)的Log10值 TODO 應該設定自動取得此值(用Solr *:* query應該即可)
		//4.64是Lucnen的
		//double logN = 4.64;

		double NGD = (Math.max(logX, logY) - logM)
				/ (logN - Math.min(logX, logY));
		
		if (m == 0)
			NGD = 1;//避免無限大
		if (NGD > 1)
			NGD = 1;
		if (NGD < 0)
			NGD = 0;
		return NGD;
	}

}
