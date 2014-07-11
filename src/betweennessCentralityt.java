import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

//for test!!!
public class betweennessCentralityt {
	
	double core_threshold = 0.33; //取多少當作核心
	double betweeness_threshold = 0.5; //去掉多少連線
	
	double simMin;
	
	Set<String> vertices;
	Set<String> edges;
	Map<String, Double> map;
	Map<String, link> linkmap;
	List<Set<String>> concepts, allConcepts;
	Map<String,Integer> degreemap;
	Map<String,Integer> clustermap;
	
	List<Map.Entry<String, Integer>> sort_data;
	
	LinkedList<String> linkList;
	
	Graph<String, link> g;
	
	Map<link, Pair<String>> edges_removed;
	

	public Map<String, Integer> betweenness_cal(String topicdir, String conceptFile,
			String centerFile, String conceptsFile, boolean changeSimMin){
		linkList = new LinkedList<String>();
		vertices = new HashSet<String>();
		edges = new HashSet<String>();
		map = new HashMap<String, Double>();
		linkmap = new HashMap<String, link>();
		concepts = new LinkedList<Set<String>>();
		allConcepts = new LinkedList<Set<String>>();
		clustermap = new HashMap<String,Integer>();
		
		g = new SparseMultigraph<String, link>();
		
		BufferedReader br;
		BufferedWriter bw,bw2, bw3;
		
		
		try {
			
			br = new BufferedReader(new FileReader(topicdir +"/"+ conceptFile));
			new File(topicdir +"/concepts").mkdirs();
			new File(topicdir +"/centers").mkdirs();
			
			bw = new BufferedWriter(new FileWriter(topicdir +"/concepts/"+ conceptsFile));//處理後的概念群
			bw2 =  new BufferedWriter(new FileWriter(topicdir +"/centers/"+ centerFile));//各概念群挑選過的結果
			
			bw3 =  new BufferedWriter(new FileWriter("time/bc.txt", true));//紀錄時間

			String line;	
			while ((line = br.readLine()) != null && Double.parseDouble(line.split(",")[2])<1) {
				linkList.add(line);
			}
			
			if(changeSimMin)
				simMin = Double.parseDouble(linkList.get(linkList.size()/2).split(",")[2]);
			
			for(String s : linkList){
				if(Double.parseDouble(s.split(",")[2]) <= simMin){
				String vertex1 = s.split(",")[0];
				String vertex2 = s.split(",")[1];
				String edge = vertex1+","+vertex2;
				
				vertices.add(vertex1);
				vertices.add(vertex2);
				
				edges.add(edge);
				
				map.put(edge, Double.parseDouble(s.split(",")[2]));
				
				}
				
			}
			
			br.close();
			
			bw2.write(""+simMin); 
			bw2.newLine();
			bw2.flush();
			
			
			for(String v : vertices){
				g.addVertex(v);
			}
			
			for(String e : edges){
				link l = new link(e,map.get(e));
				g.addEdge(l , e.split(",")[0], e.split(",")[1]);
				linkmap.put(e,l);
			}
		     
		    //原始的分群，邊權重為1
			//EdgeBetweennessClusterer<String,link> cluster = 
			//		new EdgeBetweennessClusterer<String,link>((int) (map.size()*betweeness_threshold)); 
			
			long t1 = System.currentTimeMillis();
			
			Set<Set<String>> clusterSet = transform(g,  (int) (map.size()*betweeness_threshold));
			
			long t2 = System.currentTimeMillis();
			
			bw3.write(g.getEdgeCount()+","+map.size()*betweeness_threshold+":"+(t2-t1));
			bw3.newLine();
			bw3.close();
			
			
			//移除邊後再算degree
			
			for(link l : edges_removed.keySet()){
				//g.removeEdge(l);
				boolean s = edges.remove(l.id);
				if(s){
					System.out.println("Remove: "+l.id);
				}
			}
			
			
			int i = 1;
			for(Set<String> v :clusterSet){
				allConcepts.add(v);
				//大於兩個成員算一個概念
				if(v.size()>2){
					concepts.add(v);
				}
			}
			
			for(Set<String> v: allConcepts){
				for(String s: v){
					clustermap.put(s, i);
				}
				i++;
			}
			
			Collections.sort(concepts, new Comparator<Set<String>>(){
				public int compare(Set<String> o1, Set<String> o2) {
					return o2.size() - o1.size();
				}
			});
			
			
			i = 1;
			for(Set<String> c : concepts){
				System.err.println("Cluster "+i+" :");
				degreemap = new HashMap<String,Integer>();
				
				for(String s : c){
					int degree = getDegree(s);
					
					degreemap.put(s, degree);
					
					System.err.println(s+","+degree+","+g.degree(s)+","+i);
					
					
					bw.write(s+"," + degree +","+ i); //字,degree,群 (concepts
					bw.newLine();
					bw.flush();				
					
				}
				
				//排序並取得degree排行前n的term
				sort_data = new ArrayList<Map.Entry<String, Integer>>(degreemap.entrySet());
				
				Collections.sort(sort_data,
						new Comparator<Map.Entry<String, Integer>>() {
							public int compare(Map.Entry<String, Integer> o1,
									Map.Entry<String, Integer> o2) {
								return (int) ((o2.getValue() - o1.getValue()));
							}
						});	
				
				for(int j=0;j< sort_data.size()*core_threshold;j++ ){
					Entry<String, Integer> e = sort_data.get(j);
					bw2.write(e.getKey()+","+e.getValue()+","+i); //字,degree,群 (main_concepts
					bw2.newLine();
					bw2.flush();		
				}				
				i++;
			}
			bw.close();
			bw2.close();
	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clustermap;
	}//end of betweenness_cal
	
	public int getDegree(String node) {
		int degree = 0;
	
		for (String t : edges) {
			if ((t.split(",")[0].equals(node) || t.split(",")[1].equals(node))) {
				degree++;
			}//只有小於門檻值的才會建立連結(edges已篩選)
		}//計算各節點(字詞)的連結度(degree)
		return degree;
	}
	
	//修改自jung2 原始碼: edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer
	public Set<Set<String>> transform(Graph<String,link> graph, int mNumEdgesToRemove){
		System.err.println("Edge Betweenness Clusterering...");
		edges_removed = new LinkedHashMap<link, Pair<String>>();;
		
		Transformer<link, Double> wtTransformer = new Transformer<link,Double>() {
            public Double transform(link link) {
                return link.weight;
            }
		};
		
        if (mNumEdgesToRemove < 0 || mNumEdgesToRemove > graph.getEdgeCount()) {
            throw new IllegalArgumentException("Invalid number of edges passed in.");
        }

        for (int k=0;k<mNumEdgesToRemove;k++) {
            BetweennessCentrality<String,link> bc = new BetweennessCentrality<String,link>(graph, wtTransformer);
            link to_remove = null;
            double score = 0;
            for (link e : graph.getEdges())
                if (bc.getEdgeScore(e) > score)
                {
                    to_remove = e;
                    score = bc.getEdgeScore(e);
                }
            edges_removed.put(to_remove, graph.getEndpoints(to_remove));
            graph.removeEdge(to_remove);
        }

        WeakComponentClusterer<String,link> wcSearch = new WeakComponentClusterer<String,link>();
        Set<Set<String>> clusterSet = wcSearch.transform(graph);

        for (Map.Entry<link, Pair<String>> entry : edges_removed.entrySet())
        {
            Pair<String> endpoints = entry.getValue();
            graph.addEdge(entry.getKey(), endpoints.getFirst(), endpoints.getSecond());
        }
        return clusterSet;

	}
	
	public <V,E> double computeModularity (Graph<V,E> g, Map<String,Integer> moduleMembership) {
		System.err.println("Computing Modularity...");
		
        double sum = 0;
        double m2 = (double)(2*g.getEdgeCount());
         
        for (V v1:g.getVertices()) {
        	for (V v2:g.getVertices()) {
        		int c1 = moduleMembership.get(v1);
        		int c2 = moduleMembership.get(v2);
        		if (c1 == c2 && !v1.equals(v2)) {
        			double delta = (g.isNeighbor(v1,v2)?1:0) - (double)g.degree(v1)*(double)g.degree(v2)/m2;
        			// System.out.println("delta for vertex pair " + v1 + " , " + v2 + " is " + delta);
        			sum = sum+delta;
        		}
        	}
        }
        System.err.println("Modularity: "+sum/m2);
        return sum/m2;
	}
	
	public static void main(String args[]){
		//new betweennessCentrality().betweenness_cal(10);
		
		betweennessCentralityt bc = new betweennessCentralityt();
		bc.betweeness_threshold = 0.4; 
		
		File d = new File("reuters/acq");
		
		for(File f : d.listFiles()){
			bc.betweenness_cal("reuters/acq", f.getName(), f.getName()+"_center.txt", f.getName()+"_concept.txt", true);
			
			
		}
		
		
	}

}
