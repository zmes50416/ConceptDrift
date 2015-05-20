package tw.edu.ncu.CJ102;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

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
import tw.edu.ncu.im.Preprocess.Decorator.StandfordPartOfSpeechFiliter;
import tw.edu.ncu.im.Preprocess.Decorator.StemmingDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.TermFreqDecorator;
import tw.edu.ncu.im.Preprocess.Decorator.TermToLowerCaseDecorator;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * 因為時間因素無法重新設計一個streaming 前處理流程，此類別用於批次將全部做處理，救急用，請後人重新設計實驗
 * @author TingWen
 *
 */
public class Preprocessor {
	static double documentTermSize;
	static double totalClusterSize;
	static boolean shouldDraw;
	public Preprocessor() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		File dataFile = new File("usedData");
		long startTime = System.currentTimeMillis(); 
		try {
			Path outputDir = new File(SettingManager.chooseProject()).toPath();
			Files.createDirectories(outputDir);
			System.out.println("Enable the drawing function?(true/false)");
			Scanner sc = new Scanner(System.in);
			Preprocessor.shouldDraw = sc.nextBoolean();
			sc.close();
			for(File topicDir : dataFile.listFiles()){
				ExecutorService executor = Executors.newCachedThreadPool();
				documentTermSize =0;
				totalClusterSize = 0;
				Path outputTopic = outputDir.resolve(topicDir.getName());
				Files.createDirectories(outputTopic);
				System.out.println("topic:"+topicDir.getName());
				for(File doc:topicDir.listFiles()){
					Path write = outputTopic.resolve(doc.getName());
					executor.execute(new PreprocessTopicTask(doc, write.toString(), 0.2));
				}
				executor.shutdown();
				try {
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				File topicStaticData = outputTopic.resolve("staticData.txt").toFile();
				try(BufferedWriter settingWriter = new BufferedWriter(new FileWriter(topicStaticData))){
					double avgDocTerm = (documentTermSize/topicDir.listFiles().length);
					double avgCluster = (totalClusterSize/topicDir.listFiles().length);
					settingWriter.write("Average Document Term size:" + avgDocTerm);
					settingWriter.write("Average Document Cluster size:" + avgCluster);
				}catch(IOException e){
					e.printStackTrace();
				}
				System.out.println("Topic Finished!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			long total = System.currentTimeMillis() - startTime;
			System.out.println("Total time:"+total);
		}
		
	}
	
	
}
class PreprocessTopicTask implements Runnable{
	
	private File doc;
	private double betweenessThreshold;
	private String saveName;
	private static Color[] colors = {Color.GREEN,Color.RED,Color.BLUE,Color.YELLOW,Color.gray,Color.CYAN,Color.ORANGE,Color.PINK};
	public PreprocessTopicTask(File doc,String savePlace,double betweenessThreshold){
		this.doc = doc;
		this.saveName = savePlace;
		this.betweenessThreshold = betweenessThreshold;
	}

	@Override
	public void run() {
		Transformer<CEdge, Double> edgeTransformer = new Transformer<CEdge,Double>(){

			@Override
			public Double transform(CEdge input) {
				double weight = input.getCoScore();
				return weight;
			}
			
		};
		
		RouterNewsPreprocessor<TermNode,CEdge> c = new RouterNewsPreprocessor<TermNode,CEdge>(new Factory<TermNode>(){

			@Override
			public TermNode create() {
				return new TermNode();
			}
			
		},new Factory<CEdge>(){

			@Override
			public CEdge create() {
				return new CEdge();
			}
			
		});
		PartOfSpeechFilter<TermNode,CEdge> posComp = new PartOfSpeechFilter<TermNode,CEdge>(c,c.getStringOfVertex());
//		StandfordPartOfSpeechFiliter<TermNode,CEdge> posComp = new StandfordPartOfSpeechFiliter<TermNode,CEdge>(c,c.getVertexContent());
		TermToLowerCaseDecorator<TermNode,CEdge> lowerComp = new TermToLowerCaseDecorator<TermNode,CEdge>(posComp, posComp.getVertexResultsTerms());
		FilteredTermLengthDecorator<TermNode,CEdge> termLengthComp = new FilteredTermLengthDecorator<TermNode,CEdge>(lowerComp, posComp.getVertexResultsTerms(), 3);
		
//		StemmingDecorator<TermNode,CEdge> stemmedComp = new StemmingDecorator<TermNode,CEdge>(termLengthComp, posComp.getVertexResultsTerms());
		TermFreqDecorator<TermNode,CEdge> tfComp = new TermFreqDecorator<TermNode,CEdge>(termLengthComp, posComp.getVertexResultsTerms());
		SearchResultFilter<TermNode,CEdge> filitedTermComp = new SearchResultFilter<TermNode,CEdge>(tfComp,  posComp.getVertexResultsTerms(), 10, 1000, new EmbeddedIndexSearcher());
		NGDistanceDecorator<TermNode,CEdge> ngdComp = new NGDistanceDecorator<TermNode,CEdge>(filitedTermComp,posComp.getVertexResultsTerms(),filitedTermComp.getTermsSearchResult(),new EmbeddedIndexSearcher());
		NgdEdgeFilter<TermNode,CEdge> ngdflitedComp = new NgdEdgeFilter<TermNode,CEdge>(ngdComp, ngdComp.getEdgeDistance(), 0.5);
		Graph<TermNode,CEdge> docGraph = ngdflitedComp.execute(doc);
		
		HashSet<TermNode> termsToRemove = new HashSet<>();
		for(TermNode term:docGraph.getVertices()){
			if(docGraph.getNeighborCount(term)==0){
				termsToRemove.add(term);
			}else{
				term.termFreq = tfComp.getTermFreqMap().get(term);
				term.setTerm(posComp.getVertexResultsTerms().get(term));
			}
		}
		double totalNGD = 0;
		for(CEdge edge:docGraph.getEdges()){
			double edgeDistance = ngdComp.getEdgeDistance().get(edge);
			edge.setCoScore(edgeDistance);
			totalNGD += edgeDistance;
		}
		double avgNGD = totalNGD/docGraph.getEdgeCount();
		for(TermNode term:termsToRemove){
			docGraph.removeVertex(term);
			tfComp.getTermFreqMap().remove(term);
			posComp.getVertexResultsTerms().remove(term);
		}
		int numOfEdgeToRemove = (int) (docGraph.getEdgeCount()*betweenessThreshold);
		EdgeBetweennessClusterer<TermNode,CEdge> bc = new EdgeBetweennessClusterer<TermNode,CEdge>(numOfEdgeToRemove);

		Set<Set<TermNode>> clusters = bc.transform(docGraph);
		final HashMap<Set<TermNode>,Color> clustersColor = new HashMap<>();
		Iterator<Set<TermNode>> iterator = clusters.iterator();
		for(int i=0;i<clusters.size();i++){
			Set<TermNode> cluster = iterator.next();
			try{
				clustersColor.put(cluster, colors[i]);
			}catch(ArrayIndexOutOfBoundsException e){
				//System.err.println("Too many cluster, color set to the last one");
				clustersColor.put(cluster, colors[colors.length-1]);
			}
		}
	    if(Preprocessor.shouldDraw){
			Layout<TermNode, CEdge> layout = new KKLayout<>(docGraph);
		    layout.setSize(new Dimension(800,800));
			VisualizationImageServer<TermNode, CEdge> vis =
				    new VisualizationImageServer<TermNode, CEdge>(layout, layout.getSize());
			vis.setBackground(Color.WHITE);
			vis.getRenderContext().setVertexLabelTransformer(new Transformer<TermNode,String>(){

				@Override
				public String transform(TermNode input) {
					return input.getTerm();
				}
				
			});
			vis.getRenderContext().setVertexFillPaintTransformer(new Transformer<TermNode,Paint>(){

				@Override
				public Paint transform(TermNode input) {
					for(Entry<Set<TermNode>, Color> clusterPair:clustersColor.entrySet()){					
						if(clusterPair.getKey().contains(input)){
							return clusterPair.getValue();
						}
					}
					return Color.WHITE;
				}
				
			});
			vis.getRenderContext().setEdgeLabelTransformer(new Transformer<CEdge,String>(){

				@Override
				public String transform(CEdge input) {
					String i = String.valueOf((float)input.getCoScore());
					return i;
				}
				
			});

			try {
				// Create the buffered image
				BufferedImage image = (BufferedImage) vis.getImage(
				    new Point2D.Double(layout.getSize().getWidth() / 2,
				    layout.getSize().getHeight() / 2),
				    new Dimension(layout.getSize()));

				// Write image to a png file
				File outputfile = new File(saveName+".png");
			    ImageIO.write(image, "png", outputfile);
			} catch (IOException | NullPointerException e) {
				System.err.println("problem happened when drawing graph of "+saveName);
			}
	    }

		
		try(BufferedWriter bf = new BufferedWriter(new FileWriter(saveName))){
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
		synchronized(Preprocessor.class){
			Preprocessor.documentTermSize += docGraph.getVertexCount();
			Preprocessor.totalClusterSize += clusters.size();
		}
		
	}
	
}
