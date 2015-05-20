package tw.edu.ncu.CJ102.Data;

import java.util.UUID;

import edu.uci.ics.jung.graph.util.Pair;

/***
 * customized Co-ouccrence Edge to use In JUNG Graph
 *
 * @author TingWen
 *
 */
public class CEdge {//Putting generic T type in decarlaction may not be a good idea but still have to do it
	String id;
	double coScore;
	Pair<TermNode> terms;
	public CEdge(){
		id = UUID.randomUUID().toString();
		this.coScore = 1.0;
	}
	public CEdge(Pair<TermNode> _terms) {
		this(_terms, 1.0);
	}

	public CEdge(Pair<TermNode> _terms, double score) {
		this();
		this.terms = _terms;
		this.coScore = score;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CEdge) {
			CEdge anotherEdge = (CEdge) o;
			if(this.terms==null){
				return false;
			}
			TermNode anotherFirst = anotherEdge.terms.getFirst();
			TermNode anotherSecond = anotherEdge.terms.getSecond();
			if (this.terms.getFirst().equals(anotherFirst)
					&& this.terms.getSecond().equals(anotherSecond)
					|| this.terms.getFirst().equals(anotherSecond)
					&& this.terms.getSecond().equals(anotherFirst)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return "id:" + id + " - " + (float) coScore;

	}

	public double getCoScore() {
		return this.coScore;
	}

	public void setCoScore(double d) {
		this.coScore = d;
	}

	public String getId() {
		return this.id;
	}
}
