package tw.edu.ncu.CJ102;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import tw.edu.ncu.CJ102.Data.CEdge;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.im.Preprocess.RouterNewsPreprocessor;
import tw.edu.ncu.im.Preprocess.Decorator.FilteredTermLengthDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.NGDistanceDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.NgdEdgeFilter;
import tw.edu.ncu.im.Preprocess.Decorator.PartOfSpeechFilter;
import tw.edu.ncu.im.Preprocess.Decorator.SearchResultFilter;
import tw.edu.ncu.im.Preprocess.Decorator.StemmingDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.TermFreqDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.TermToLowerCaseDecorator;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.Graph;

/**
 * 因為時間因素無法重新設計一個streaming 前處理流程，此類別用於批次將全部做處理，救急用，請後人重新設計實驗
 * @author TingWen
 *
 */
public class Preprocessor {
	static double documentTermSize;
	static double totalClusterSize;
	public Preprocessor() {
		// TODO Auto-generated constructor stub
	}
	public static void process(File doc,File writePlace){
		Transformer<CEdge<Double>, Double> edgeTransformer = new Transformer<CEdge<Double>,Double>(){

			@Override
			public Double transform(CEdge<Double> input) {
				double weight = input.getCoScore();
				return weight;
			}
			
		};
		
		RouterNewsPreprocessor<TermNode,CEdge<Double>> c = new RouterNewsPreprocessor<TermNode,CEdge<Double>>(new Factory<TermNode>(){

			@Override
			public TermNode create() {
				return new TermNode();
			}
			
		},new Factory<CEdge<Double>>(){

			@Override
			public CEdge<Double> create() {
				return new CEdge<Double>();
			}
			
		});
		PartOfSpeechFilter<TermNode,CEdge<Double>> posComp = new PartOfSpeechFilter<TermNode,CEdge<Double>>(c, c.getVertexContent());
		TermToLowerCaseDecorator<TermNode,CEdge<Double>> lowerComp = new TermToLowerCaseDecorator<TermNode,CEdge<Double>>(posComp, posComp.getVertexTerms());
		FilteredTermLengthDecorator<TermNode,CEdge<Double>> termLengthComp = new FilteredTermLengthDecorator<TermNode,CEdge<Double>>(lowerComp, posComp.getVertexTerms(), 3);
		
		StemmingDecorator<TermNode,CEdge<Double>> stemmedC = new StemmingDecorator<TermNode,CEdge<Double>>(termLengthComp, posComp.getVertexTerms());
		TermFreqDecorator<TermNode,CEdge<Double>> tfComp = new TermFreqDecorator<TermNode,CEdge<Double>>(stemmedC, posComp.getVertexTerms());
		SearchResultFilter<TermNode,CEdge<Double>> filitedTermComp = new SearchResultFilter<TermNode,CEdge<Double>>(tfComp,  posComp.getVertexTerms(), 10, 1000, new EmbeddedIndexSearcher());
		NGDistanceDecorator<TermNode,CEdge<Double>> ngdComp = new NGDistanceDecorator<TermNode,CEdge<Double>>(filitedTermComp,posComp.getVertexTerms(),new EmbeddedIndexSearcher());
		NgdEdgeFilter<TermNode,CEdge<Double>> ngdflitedComp = new NgdEdgeFilter<TermNode,CEdge<Double>>(ngdComp, ngdComp.getEdgeDistance(), 0.5);
		Graph<TermNode,CEdge<Double>> docGraph = ngdflitedComp.execute(doc);
		
		HashSet<TermNode> termsToRemove = new HashSet<>();
		for(TermNode term:docGraph.getVertices()){
			if(docGraph.getNeighborCount(term)==0){
				termsToRemove.add(term);
			}else{
				term.termFreq = tfComp.getTermFreqMap().get(term);
				term.setTerm(posComp.getVertexTerms().get(term));
			}
		}
		double totalNGD = 0;
		for(CEdge<Double> edge:docGraph.getEdges()){
			double edgeDistance = ngdComp.getEdgeDistance().get(edge);
			edge.setCoScore(edgeDistance);
			totalNGD += edgeDistance;
		}
		double avgNGD = totalNGD/docGraph.getEdgeCount();
		for(TermNode term:termsToRemove){
			docGraph.removeVertex(term);
			tfComp.getTermFreqMap().remove(term);
			posComp.getVertexTerms().remove(term);
		}
		int numOfEdgeToRemove = (int) (docGraph.getEdgeCount()*0.35);
		EdgeBetweennessClusterer<TermNode,CEdge<Double>> bc = new EdgeBetweennessClusterer<TermNode,CEdge<Double>>(numOfEdgeToRemove);

		Set<Set<TermNode>> clusters = bc.transform(docGraph);
		try(BufferedWriter bf = new BufferedWriter(new FileWriter(writePlace))){
			bf.write(String.valueOf(avgNGD));
			bf.newLine();
			int group = 1;
			
			for(Set<TermNode> cluster:clusters){
				for(TermNode term:cluster){
					bf.write(term.getTerm()+","+term.termFreq+","+group);
					bf.newLine();
				}
				group = group+1;
				
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		documentTermSize += docGraph.getVertexCount();
		totalClusterSize += clusters.size();
		
	}
	
	public static void main(String[] args) {
		EmbeddedIndexSearcher.SolrHomePath = "D:\\Documents\\NGD\\webpart\\solr";
		EmbeddedIndexSearcher.solrCoreName = "collection1";
		File dataFile = new File("usedData");
		long startTime = System.currentTimeMillis(); 
		try {
			Path test = new File("test").toPath();
			Files.createDirectories(test);
			
			for(File topicDir : dataFile.listFiles()){
				documentTermSize =0;
				totalClusterSize = 0;
				Path testTopic = test.resolve(topicDir.getName());
				Files.createDirectories(testTopic);
				System.out.println("topic:"+topicDir.getName());
				for(File doc:topicDir.listFiles()){
					Path write = testTopic.resolve(doc.getName());
//					File writeFile= new File(write.toString());
					process(doc,write.toFile());
					
				}
				System.out.println(documentTermSize/topicDir.listFiles().length);
				System.out.println(totalClusterSize/topicDir.listFiles().length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			long total = System.currentTimeMillis() - startTime;
			System.out.println("Total time:"+total);
		}
		
	}
}
