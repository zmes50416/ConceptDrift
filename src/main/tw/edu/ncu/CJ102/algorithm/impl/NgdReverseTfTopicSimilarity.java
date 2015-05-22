package tw.edu.ncu.CJ102.algorithm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class NgdReverseTfTopicSimilarity implements TopicMappingAlgorithm{
	
	IndexSearchable tool;
	ExecutorService executor = Executors.newFixedThreadPool(15);
	CompletionService<Double> tasker = new ExecutorCompletionService<Double>(executor);
	List<Future<Double>> results = new LinkedList<>();

	public NgdReverseTfTopicSimilarity(){
		tool = new EmbeddedIndexSearcher(); 
	}
	public NgdReverseTfTopicSimilarity(IndexSearchable indexTool){
		this.tool = indexTool;
	}
	
	public double computeSimilarity(TopicTermGraph theTopic,
			TopicTermGraph userTopic) {
		double sumScore = 0;
		Collection<TermNode> topicTerms = theTopic.getVertices();
		Collection<TermNode> userTopicTerms = userTopic.getCoreTerm();
		int taskSize = (topicTerms.size()*userTopicTerms.size());
		for (TermNode term : topicTerms) {
			for (TermNode keyTerm : userTopicTerms) {
				results.add(tasker.submit(new NgdReverseTfComputingTask(term,keyTerm)));
			}
		}
		int taskCount = 0;

		while(taskCount<taskSize){
			try {
				sumScore += tasker.take().get();
				taskCount++;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}
		double similarity = sumScore / taskSize ;
		return similarity;
	}
	private class NgdReverseTfComputingTask implements Callable<Double> {
		TermNode termA, termB;
		public NgdReverseTfComputingTask(TermNode term,TermNode anotherTerm) {
			termA = term;
			termB = anotherTerm;
		}

		@Override
		public Double call() throws Exception {
			
			double a = tool.searchTermSize(termA.toString());
			double b = tool.searchTermSize(termB.toString());
			double mValue = tool.searchMultipleTerm(new String[]{termA.toString(),termB.toString()});
			double ngdDistance = NGD_calculate.NGD_cal(a, b, mValue);
			double termScore = (1 - ngdDistance)
					* ((this.termA.termFreq + this.termB.termFreq) / 2);
			return termScore;
		}

	}
}

