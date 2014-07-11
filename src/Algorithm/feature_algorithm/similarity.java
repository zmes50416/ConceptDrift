package Algorithm.feature_algorithm;

public class similarity {
	
	public double similarityCalculator(double[] point_one, double[] point_two, String method){
		
		double similarity = 0;
		
		if(method.equals("cosine")){
			double sum = 0;
			double o_norm = 0;
			double t_norm = 0;
			
			if(point_one.length == point_two.length){
				for(int i = 0; i < point_one.length; i++){
					sum = sum + (point_one[i] * point_two[i]);
					o_norm = o_norm + Math.pow(point_one[i], 2);
					t_norm = t_norm + Math.pow(point_two[i], 2);
				}
				o_norm = Math.sqrt(o_norm);
				t_norm = Math.sqrt(t_norm);
				similarity = sum / (o_norm * t_norm);
			}
		}
		else if(method.equals("jaccard")){
			double sum = 0;
			double o_norm = 0;
			double t_norm = 0;
			
			if(point_one.length == point_two.length){
				for(int i = 0; i < point_one.length; i++){
					sum = sum + (point_one[i] * point_two[i]);
					o_norm = o_norm + Math.pow(point_one[i], 2);
					t_norm = t_norm + Math.pow(point_two[i], 2);
				}
				o_norm = Math.pow(Math.sqrt(o_norm), 2);
				t_norm = Math.pow(Math.sqrt(t_norm), 2);
				similarity = sum / ((o_norm + t_norm) - sum);
			}
		}
		
		return similarity;
		
	}
	
	public static void main(String[] args) {
		
		double s1[] = {0.0,0.0,0.0,100.0,100.0,0.0};
		double s2[] = {0.0,0.0,0.0,200.0,100.0,0.0};
		similarity sim = new similarity();
		
		System.out.println(sim.similarityCalculator(s1, s2, "cosine"));
		//System.out.println(sim.similarityCalculator(s1, s2, "jaccard"));
	}
	
}
