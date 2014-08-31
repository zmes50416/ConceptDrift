package tw.edu.ncu.CJ102;

public class link {
	String id;
    double weight;
    
    
    public link(String id, double weight){
    	this.weight = weight;
    	this.id = id;
    }
    
    
    public String toString() {
        return id;
    }

}
