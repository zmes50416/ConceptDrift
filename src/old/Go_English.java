import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Go_English {
	public static void main(String[] args) throws ParseException {
		
		
		long AverageTime =0; // 計算平均處理時間使用

		long StartTime = System.currentTimeMillis(); // 取出目前時間	
		try {
			Scanner input=new Scanner(System.in);
			System.out.println("Select document:");
			int j=input.nextInt();
				System.out.println("Choose the step:");
				System.out.println("1.Qtag");
				System.out.println("2.Freq");
				System.out.println("3.POS filter");
				System.out.println("4.Search");
				System.out.println("5.Google filter");
				System.out.println("6.Stem");
				System.out.println("7.NGD");
				System.out.println("8.Rank");
				System.out.println("9.K core");
				int step=input.nextInt();
				
				switch(step){
				case 1:
					//new Qtag().tagging(j);
				case 2:
					//Term_freq_count.counting(j);//計算freq 
//					input.next();
				case 3:
					//POS_filter.filter(j);
					//new cache_choose().doit(j);
//					input.next();
					
				case 4:
					//我的分類cache
					//new Google_Search1_cache().doit(j);
					
					//我的timestamp cache
					//new Google_Search1_cache_timestamp().doit(j);
					
					//我的count cache
					//new Google_Search1_cache_count().doit(j);
					
					//我的Lucene search1
					//new Lucene_Search1().doit(j);
					
					//小尖的
					//new Google_Search1().doit(j);
					
				case 5:
					//google_filter1.search_filter(j);
					
					//我的分類cache
					//new Google_Search2_cache().doit(j);
					
					//我的Lucene search2
					//new Lucene_Search2().doit(j);
					
					//小尖的
					//new Google_Search2().doit(j);
					
					//google_filter2.search_filter(j);
				case 6:
					//Stem.stemming(j);
				case 7:
					//NGD_calculate.NGD(j);
				case 8:
				    //Result_Rank.ranking(j);
				case 9:
					//小尖的
					K_core kcore=new K_core();
					kcore.K_core_cal(j);
					KcoreGUI gui = new KcoreGUI();
					gui.getData(j,kcore.simMin);
					
					//我的CC
					//distance dis = new distance();
					//dis.cc_cal(j);
					//distanceGUI gui = new distanceGUI();
					//gui.getData(j,dis.simMin);
					
					gui.updateGUI();
					gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					gui.setSize(1024, 768);
					gui.setVisible(true);
				}				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("ERROR HAPPEN");
				e.printStackTrace();
				//JOptionPane.showMessageDialog(null, "ERROR");
			}
			long ProcessTime = System.currentTimeMillis() - StartTime; // 計算處理時間
			//AverageTime += ProcessTime; // 累積計算時間
			System.out.println("總花費時間為" + ProcessTime);
	}
}
