package tw.edu.ncu.CJ102.CoreProcess;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public abstract class AbstractUserProfile implements Serializable {
	String id;
	double decayFactorMAXIMUM;	//遺忘因子上限
	double decayFactorMin;//遺忘因子下限
	double decayFactorChange;
	double remove_rate; //興趣去除比例
	double interest_remove_rate; //主題去除的累計平均單文件總TF值比例
	double term_remove_rate; //字詞去除的累計平均單字詞TF值比例
	private HashSet<TopicTermGraph> userTopics = new HashSet<>();
	private HashMap<TopicTermGraph,Double> forgettingFactorMap = new HashMap<>();
	public AbstractUserProfile() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the userTopics
	 */
	public HashSet<TopicTermGraph> getUserTopics() {
		return userTopics;
	}
	
	public HashMap<TopicTermGraph,Double> getForgettingFactorMap() {
		return forgettingFactorMap;
	}

	public abstract void updateTopicRemoveThreshold();
	public abstract void updateTermRemoveThreshold();
	

	
	public void write(Path savePlace){
		
		
	}
	
	

}
