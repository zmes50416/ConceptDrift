package tw.edu.ncu.CJ102.algorithm;

import tw.edu.ncu.CJ102.NGD_calculate;
import tw.edu.ncu.CJ102.SolrSearcher;
import tw.edu.ncu.CJ102.CoreProcess.*;

public class NgdReverseTfTopicSimilarity implements TopicMappingAlgorithm {

	
	public NgdReverseTfTopicSimilarity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double computeSimilarity(TopicTermGraph theTopic,
			TopicTermGraph userTopic) {
		double sumScore = 0;
		for (TermNode term : theTopic.getVertices()) {
			for (TermNode keyTerm : userTopic.getCoreTerm()) {
					double a = SolrSearcher.getHits("\"" + keyTerm + "\"");
					double b = SolrSearcher.getHits("\"" + term + "\"");
					double mValue = SolrSearcher
							.getHits("+\"" + term + "\" +\""
									+ keyTerm + "\"");

					double ngdDistance = NGD_calculate.NGD_cal(a, b, mValue);
					double termScore = (1-ngdDistance)*((term.termFreq+keyTerm.termFreq)/2)	; // TF方法
					sumScore  += termScore;
				}
			
			}
		double similarity = sumScore / theTopic.getVertexCount()*userTopic.getVertexCount();
		return similarity;
	}

}
