package tw.edu.ncu.CJ102.CoreProcess;

public class PerformanceMonitor {

	private double TP = 0, TN = 0, FP = 0, FN = 0;
	public PerformanceMonitor() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the true Positive
	 */
	public double getTP() {
		return TP;
	}
	/**
	 * @param tP the true positive to set
	 */
	public void setTP(double tP) {
		TP = tP;
	}
	/**
	 * @return the true negative 
	 */
	public double getTN() {
		return TN;
	}
	/**
	 * @param tN the true negative  to set
	 */
	public void setTN(double tN) {
		TN = tN;
	}
	/**
	 * @return the false Positive
	 */
	public double getFP() {
		return FP;
	}
	/**
	 * @param fP the false Positive to set
	 */
	public void setFP(double fP) {
		FP = fP;
	}
	/**
	 * @return the false negative 
	 */
	public double getFN() {
		return FN;
	}
	/**
	 * @param fN the false negative to set
	 */
	public void setFN(double fN) {
		FN = fN;
	}

	
	public void set_EfficacyMeasure(String result) {
		System.out.println("此文件被判定為" + result);
		if (result == "TP") {
			TP++;
		}
		if (result == "TN") {
			TN++;
		}
		if (result == "FP") {
			FP++;
		}
		if (result == "FN") {
			FN++;
		}
	}

	public void show_all_result() {
		System.out.println("TP=" + TP + ", TN=" + TN + ", FP=" + FP + ", FN="
				+ FN);
	}

	public double[] get_all_result() {
		double all_result[] = { TP, TN, FP, FN };
		return all_result;
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
}
