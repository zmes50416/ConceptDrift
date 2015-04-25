package tw.edu.ncu.CJ102.Data;

public class TermNode{
	public double termFreq;
	String term;
	public TermNode(String _term){
		this.term = _term;
		this.termFreq = 1;
	}
	public TermNode(String _term,double _termFreq){
		this.term = _term;
		this.termFreq = _termFreq;
	}
	@Override
	public String toString(){
		return term;
		
	}
	@Override
	public boolean equals(Object o){
		TermNode anotherNode = (TermNode)o;
		if(this.term.equals(anotherNode.term)){
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode(){
		return this.term.hashCode();
	}
	
	
}