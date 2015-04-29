package tw.edu.ncu.CJ102.algorithm.impl;

import java.util.Collection;

import tw.edu.ncu.CJ102.NGD_calculate;
import tw.edu.ncu.CJ102.SolrSearcher;
import tw.edu.ncu.CJ102.CoreProcess.*;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

public class NgdReverseTfTopicSimilarity implements TopicMappingAlgorithm {

	
	public NgdReverseTfTopicSimilarity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double computeSimilarity(TopicTermGraph theTopic,
			TopicTermGraph userTopic) {
		double sumScore = 0;
		Collection<TermNode> topicTerms = theTopic.getVertices();
		Collection<TermNode> userTopicTerms = userTopic.getCoreTerm();
		for (TermNode term : topicTerms) {
			for (TermNode keyTerm : userTopicTerms) {
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
		double similarity = sumScore / (topicTerms.size()*userTopicTerms.size());
		return similarity;
	}

}
