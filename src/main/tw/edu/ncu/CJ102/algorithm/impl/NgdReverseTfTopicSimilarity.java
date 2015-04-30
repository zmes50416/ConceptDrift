package tw.edu.ncu.CJ102.algorithm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tw.edu.ncu.CJ102.NGD_calculate;
import tw.edu.ncu.CJ102.SolrSearcher;
import tw.edu.ncu.CJ102.CoreProcess.*;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

public class NgdReverseTfTopicSimilarity implements TopicMappingAlgorithm,Callable<Double>{

	ExecutorService executor = Executors.newFixedThreadPool(15);
	CompletionService<Double> cs = new ExecutorCompletionService<Double>(executor);
	ArrayList<Future<Double>> results = new ArrayList<>();

	public double computeSimilarity(TopicTermGraph theTopic,
			TopicTermGraph userTopic) {
		double sumScore = 0;
		Collection<TermNode> topicTerms = theTopic.getVertices();
		Collection<TermNode> userTopicTerms = userTopic.getCoreTerm();
		
		for (TermNode term : topicTerms) {
			for (TermNode keyTerm : userTopicTerms) {
				results.add(executor.submit(new NgdReverseTfComputingTask(term.toString(),keyTerm.toString(),term.termFreq,keyTerm.termFreq)));
			}
		}
		
		for(Future<Double> result:results){
			try {
				sumScore += result.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}
		double similarity = sumScore / (topicTerms.size()*userTopicTerms.size());
		return similarity;
	}

	@Override
	public Double call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
class NgdReverseTfComputingTask implements Callable<Double> {
	String termA, termB;
	double termAFreq, termBFreq;
	public NgdReverseTfComputingTask(String term,String anotherTerm,double termFreq,double anotherTermFreq) {
		termA = term;
		termB = anotherTerm;
		this.termAFreq = termFreq;
		this.termBFreq = anotherTermFreq;
	}

	@Override
	public Double call() throws Exception {
		double a = SolrSearcher.getHits("\"" + termA + "\"");
		double b = SolrSearcher.getHits("\"" + termB + "\"");
		double mValue = SolrSearcher.getHits("+\"" + termA + "\" +\""
				+ termB + "\"");
		double ngdDistance = NGD_calculate.NGD_cal(a, b, mValue);
		double termScore = (1 - ngdDistance)
				* ((this.termAFreq + this.termBFreq) / 2);
		return termScore;
	}

}
