package tw.edu.ncu.CJ102.CoreProcess;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractUserProfile {
	double decayFactorMax;	//遺忘因子上限
	double decayFactorMin;//遺忘因子下限
	double decayFactorChange;
	double remove_rate; //興趣去除比例
	double interest_remove_rate; //主題去除的累計平均單文件總TF值比例
	double term_remove_rate; //字詞去除的累計平均單字詞TF值比例
	HashSet<TopicCluster> topics = new HashSet<>();

	public AbstractUserProfile() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * @param User_profile_term 原使用者模型(字詞)
	 * @param doc_term 文件內容資訊
	 * @param topic_mapping 主題的映射
	 */
	public abstract void add_user_profile_term(Collection<TopicCluster> docTerms, Collection<TopicCluster> topic_mapping);
	
	/**
	 * 執行的使用者模型更新，應該包含遺忘因子的作用與主題、字詞的去除
	 */
	public abstract void update();
	
	public double getAverageDocumentTermFreq(){
		//TODO implement
		return 0;
	}
	
	public boolean removeDocument(Collection<TopicCluster> doc){
		return false;
		
	}
	public boolean removeTerm(String term){
		return false;
		
	}
	public boolean removeTopic(TopicCluster topic){
		return false;
		
	}
	
	public void write(Path savePlace){
		
	}
	
	

}
