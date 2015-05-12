package tw.edu.ncu.CJ102.CoreProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tw.edu.ncu.CJ102.Data.TopicTermGraph;

public class PerformanceMonitor {
	public static double lamda = 0.15,sigma = -0.05;
	private double TP, TN, FP, FN;
	private ArrayList<Double> HistoryFMeasure  = new ArrayList<>();
	
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
		this.HistoryFMeasure.add(this.get_f_measure());
		this.phTest();
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
		double f = (2 * get_precision() * get_recall())
				/ (get_precision() + get_recall());
		if(f<0){
			return 0;
		}else if(f>1){
			return 1;
		}else{
			return f;
		}
		
	}

	public double get_accuracy() {
		double acc = (TP + TN) / (TP + TN + FP + FN);
		if(acc>1){
			return 1;
		}else if(acc <0){
			return 0;
		}else{
			return acc;
		}
	}

	public double get_error() {
		return (FP + FN) / (TP + TN + FP + FN);
	}
	public String toString(){
		return "F-measure:"+this.get_f_measure()+", Accuracy:"+this.get_accuracy()+",Recall:"+this.get_recall()+",Precision:"+this.get_precision();
	}
	public boolean phTest(){
		boolean phFlag = false;
		double pHTest  = 0;
		double totalFmeasure = 0,MT = 100000;
		for (int i = 0; i < HistoryFMeasure.size(); i++) {
			double fMeasure = HistoryFMeasure.get(i);
			totalFmeasure += fMeasure;
			double avgFmeasure = totalFmeasure / (i+1);
			double mT = (fMeasure - avgFmeasure - sigma);
			if(mT<MT){
				MT = mT;
			}
			pHTest = mT - MT;
		}
		if(pHTest>lamda){
			phFlag = true;
		}
		return phFlag;
	}
}
enum PerformanceType{
	TRUENEGATIVE,TRUEPOSTIVE,FALSENEGATIVE,FALSEPOSTIVE;
	
}
