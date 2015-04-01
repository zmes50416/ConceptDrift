package tw.edu.ncu.CJ102.CoreProcess;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * New 主題映射 程式
 * @author TingWen
 *
 */
public class TopicMappingTool {

	AbstractUserProfile profile;
	public TopicMappingTool(AbstractUserProfile _profile) {
		this.profile = _profile;
	}
	public void setUserProfile(AbstractUserProfile _profile){
		this.profile = _profile;
	}
	
	public void map(ArrayList<TopicTermGraph> documentTopics){
		
	}
	
	public void log(Path writePath){
		
	}

}
