package tw.edu.ncu.CJ102.Data;

import edu.uci.ics.jung.graph.util.Pair;

/***
 * customized Co-ouccrence Edge to use In JUNG Graph
 *
 * @author TingWen
 *
 */
public class CEdge<T> {//Putting generic T type in decarlaction may not be a good idea but still have to do it
	String id;
	double coScore;
	Pair<T> terms;
	public CEdge(){
		
	}
	public CEdge(Pair<T> _terms) {
		this(_terms, 1.0);
	}

	public CEdge(Pair<T> _terms, double score) {
		this.terms = _terms;
		this.coScore = score;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (o instanceof CEdge) {
			CEdge<T> anotherEdge = (CEdge<T>) o;
			if(this.terms==null){
				return false;
			}
			T anotherFirst = anotherEdge.terms.getFirst();
			T anotherSecond = anotherEdge.terms.getSecond();
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
