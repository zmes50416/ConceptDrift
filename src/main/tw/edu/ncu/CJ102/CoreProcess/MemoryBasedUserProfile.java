package tw.edu.ncu.CJ102.CoreProcess;
/**
 * 基於長期以及短期記憶區塊的使用者模型，
 * @author TingWen
 *
 */
public class MemoryBasedUserProfile extends AbstractUserProfile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MemoryBasedUserProfile() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getTopicRemoveThreshold() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTermRemoveThreshold() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @return the sizeOfShortTerm
	 */
	public int getSizeOfShortTerm() {
		int sizeOfShortTerm = 0;
		for(TopicTermGraph topic : userTopics){
			if(!topic.isLongTermInterest()){
				sizeOfShortTerm++;
			}
		}
		return sizeOfShortTerm;
	}

	@Override
	public double getDecayRate(TopicTermGraph topic,int length) {
		double decayRate;
		if(topic.isLongTermInterest()){
			decayRate = Math.pow(Math.E, -Math.log10(length-topic.getBirthDate())*0.02);
		}else{
			decayRate = Math.pow(Math.E, -Math.log10(length-topic.getUpdateDate())*this.getSizeOfShortTerm()/Math.log10(topic.numberOfDocument));
		}
		return 0;
	}


}
