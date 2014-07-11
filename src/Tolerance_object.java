
public class Tolerance_object {

	/**
	 * @param args
	 */
	
	String v1=""; //取代者
	String v2=""; //被取代者
	double ngd=0;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public void addv1(String addv1){
		v1=addv1;
	}
	
	public void addv2(String addv2){
		v2=addv2;
	}
	
	public void addngd(double addngd){
		ngd=addngd;
	}
	
	public void add(String addv1, String addv2, double addngd){
		addv1(addv1);
		addv2(addv2);
		addngd(addngd);
	}
	
	public String getv1(){
		return v1;
	}
	
	public String getv2(){
		return v2;
	}
	
	public double getngd(){
		return ngd;
	}

}
