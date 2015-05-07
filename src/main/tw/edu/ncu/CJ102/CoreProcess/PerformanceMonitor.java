package tw.edu.ncu.CJ102.CoreProcess;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tw.edu.ncu.CJ102.Data.TopicTermGraph;

public class PerformanceMonitor {
	private double TP, TN, FP, FN;
	
	public PerformanceMonitor(){
		TP = 0;
		TN = 0;
		FP = 0;
		FN = 0;
	}

	/**
	 * @return the true Positive
	 */
	public double getTP() {
		return TP;
	}

	/**
	 * @return the true negative 
	 */
	public double getTN() {
		return TN;
	}

	/**
	 * @return the false Positive
	 */
	public double getFP() {
		return FP;
	}

	/**
	 * @return the false negative 
	 */
	public double getFN() {
		return FN;
	}


	
	public void set_EfficacyMeasure(PerformanceType type) {
		if (type == PerformanceType.TRUEPOSTIVE) {
			TP++;
		}else if (type == PerformanceType.TRUENEGATIVE) {
			TN++;
		}else if (type == PerformanceType.FALSEPOSTIVE) {
			FP++;
		}else if (type == PerformanceType.FALSENEGATIVE) {
			FN++;
		}else{
			throw new IllegalArgumentException("The type:"+type+" is not in any correct PerformanceType");
		}
	}

	public Map<PerformanceType,Double> get_all_result() {
		Map<PerformanceType,Double> results = new HashMap<>();
		results.put(PerformanceType.TRUENEGATIVE, this.TN);
		results.put(PerformanceType.TRUEPOSTIVE, this.TP);
		results.put(PerformanceType.FALSENEGATIVE, this.FN);
		results.put(PerformanceType.FALSEPOSTIVE, this.FP);
		return results;
	}

	public double get_precision() {
		return TP / (TP + FP);
	}

	public double get_recall() {
		return TP / (TP + FN);
	}

	public double get_f_measure() {
		return (2 * get_precision() * get_recall())
				/ (get_precision() + get_recall());
	}

	public double get_accuracy() {
		return (TP + TN) / (TP + TN + FP + FN);
	}

	public double get_error() {
		return (FP + FN) / (TP + TN + FP + FN);
	}
	public String toString(){
		return "F-measure:"+this.get_f_measure()+", Accuracy:"+this.get_accuracy()+",Recall:"+this.get_recall()+",Precision:"+this.get_precision();
	}
	public boolean phTest(){
		return true;
	}
}
enum PerformanceType{
	TRUENEGATIVE,TRUEPOSTIVE,FALSENEGATIVE,FALSEPOSTIVE;
	
}
