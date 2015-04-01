package tw.edu.ncu.CJ102.CoreProcess;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public abstract class AbstractUserProfile implements Serializable {

	double decayFactorMax;	//遺忘因子上限
	double decayFactorMin;//遺忘因子下限
	double decayFactorChange;
	double remove_rate; //興趣去除比例
	double interest_remove_rate; //主題去除的累計平均單文件總TF值比例
	double term_remove_rate; //字詞去除的累計平均單字詞TF值比例
	HashSet<TopicTermGraph> topics = new HashSet<>();

	public AbstractUserProfile() {
		// TODO Auto-generated constructor stub
	}


	public double getAverageDocumentTermFreq(){
		//TODO implement
		return 0;
	}
	

	
	public void write(Path savePlace){
		
	}
	
	

}
