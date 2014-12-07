package tw.edu.ncu.CJ102;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.FilenameUtils;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
/**
 * Rewrite from TOM_BetwennesCentrarity
 * it is really a mess
 * @author TingWen
 *
 */
public class BCCalculator {
	String readNGDRankPath;
	String readTFPath;
	String writeConceptPath;
	
	double core_threshold = 0.75; //取多少當作核心
	double betweeness_threshold = 0.35; //去掉多少連線
	
	double simMin;
	//Bunch of field, need Comment!
	Set<String> vertices;
	Set<String> edges;
	Map<String, Double> map;
	Map<String, link> linkmap;
	List<Set<String>> concepts, allConcepts;
	Map<String,Integer> degreemap;
	Map<String,Integer> clustermap;
	
	List<Map.Entry<String, Integer>> sort_data;
	LinkedList<String> ngdList;
	Graph<String, link> g;
	Map<link, Pair<String>> edges_removed; //should not be access by main process and function at the same time
	
	BCCalculator(String NGDRankDir, String NGDTolDir,String ConceptDir){
		this.readNGDRankPath = NGDRankDir;
		this.readTFPath = NGDTolDir;
		this.writeConceptPath = ConceptDir;
	}
	public void start(){
		File[] files = new File(this.readNGDRankPath).listFiles();
		for(File f:files){
			this.betweenness_cal(this.readNGDRankPath,this.writeConceptPath,f.getName(),true);
		}
	}
	//參數: 分群資料來源, 分群後資料存放地點, 檔名
	public Map<String, Integer> betweenness_cal(String source_dir, String resultDir, String conceptFile, boolean changeSimMin){
		ngdList = new LinkedList<String>();
		vertices = new HashSet<String>();
		edges = new HashSet<String>();
		map = new HashMap<String, Double>();
		linkmap = new HashMap<String, link>();
		concepts = new LinkedList<Set<String>>();
		allConcepts = new LinkedList<Set<String>>();
		clustermap = new HashMap<String,Integer>();
		HashMap<String,Integer> TF_term = new HashMap<String,Integer>();
		//TODO We should not allowed Multi parallex Edge! should change to other class.
		g = new SparseMultigraph<String, link>();
		
		BufferedReader ngdReader,termFreqReader;
		BufferedWriter bw,bw3;
		
		String filename = FilenameUtils.removeExtension(conceptFile);
		
		String conceptsFile = filename+"_concepts.txt";
		String centerFile = filename+"_centers.txt";
		
		System.out.print("分析後檔案是"+filename+"\n");
		
		try {
			//br = new BufferedReader(new FileReader(source_dir +"/"+ conceptFile));
			//br = new BufferedReader(new FileReader(source_dir +"/"+ filename +"_TolNGD.txt"));
			ngdReader = new BufferedReader(new FileReader(source_dir+ filename+".txt"));
			termFreqReader = new BufferedReader(new FileReader(this.readTFPath+filename+".txt"));
			
			bw = new BufferedWriter(new FileWriter(resultDir + conceptsFile));//處理後的概念群
			bw3 =  new BufferedWriter(new FileWriter("Util/time/bc.txt", true));//紀錄時間
			
			String line;
			//取出此檔案的所有字詞與權重
			while((line=termFreqReader.readLine())!=null){
				String v1 = line.split(",")[0];
				TF_term.put(v1,Integer.valueOf(line.split(",")[1]));
			}
			
			while ((line = ngdReader.readLine()) != null && Double.parseDouble(line.split(",")[2])<1) {
				ngdList.add(line);
			}
			
			if(changeSimMin && ngdList.size()!=0){
				simMin = Double.parseDouble(ngdList.get(ngdList.size()/2).split(",")[2]);//取NGD中位數
			}
			double save_simMin = simMin;
			for(String ngdEdge : ngdList){
				if(Double.parseDouble(ngdEdge.split(",")[2]) <= simMin){
				String vertex1 = ngdEdge.split(",")[0];
				String vertex2 = ngdEdge.split(",")[1];
				String edge = vertex1+","+vertex2;
				
				vertices.add(vertex1);
				vertices.add(vertex2);
				
				edges.add(edge);
				
				map.put(edge, Double.parseDouble(ngdEdge.split(",")[2]));
				
				}
				
			}
			ngdReader.close();
			bw.write(""+save_simMin);//NGD中位數:
			bw.newLine();
			bw.flush();
			
			//將頂點塞進g
			for(String v : vertices){
				g.addVertex(v);
			}
			//將邊也塞入g
			for(String e : edges){
				link l = new link(e,map.get(e));
				g.addEdge(l , e.split(",")[0], e.split(",")[1]);
				linkmap.put(e,l);//Unknow useage...
			} 
			
			long t1 = System.currentTimeMillis();
			
			Set<Set<String>> clusterSet = transform(g,  (int) (map.size()*betweeness_threshold));
			
			long t2 = System.currentTimeMillis();
			
			bw3.write(g.getEdgeCount()+","+map.size()*betweeness_threshold+":SpendTime:"+(t2-t1));
			bw3.newLine();
			bw3.close();
			
			
			//移除邊後再算degree
			
			for(link l : edges_removed.keySet()){
				//g.removeEdge(l);
				boolean s = edges.remove(l.id);
				if(s){
					//System.out.println("Remove: "+l.id);
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
				//System.err.println("Cluster "+i+" :");
				degreemap = new HashMap<String,Integer>();
				
				for(String s : c){
					int degree = getDegree(s);
					
					degreemap.put(s, degree);
					
					//System.out.println(s+","+degree+","+g.degree(s)+","+i);
					
					if(TF_term.get(s)!=null){
						bw.write(s+"," + TF_term.get(s) +","+ i); //字,TF分數,群 (concepts
					}else{
						bw.write(s+"," + 0 +","+ i); //字,TF分數,群 (concepts
					}
					bw.newLine();
					bw.flush();				
					
				}
				
				//排序並取得degree排行前n的term
				//sort_data = new ArrayList<Map.Entry<String, Integer>>(degreemap.entrySet());
				double terms_cum = degreemap.size()*core_threshold;
				/*Collections.sort(sort_data,
						new Comparator<Map.Entry<String, Integer>>() {
							public int compare(Map.Entry<String, Integer> o1,
									Map.Entry<String, Integer> o2) {
								return (int) ((o2.getValue() - o1.getValue()));
							}
						});	*/
				
				
				i++;
			}
			bw.close();
			//bw2.close();
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		edges_removed = new LinkedHashMap<link, Pair<String>>();;
		
		Transformer<link, Double> wtTransformer = new Transformer<link,Double>() {
            @Override
			public Double transform(link link) {
                return link.weight;
            }
		};
		
        if (mNumEdgesToRemove < 0 || mNumEdgesToRemove > graph.getEdgeCount()) {
            throw new IllegalArgumentException("Invalid number of edges passed in.");
        }
        
        for (int k=0;k<mNumEdgesToRemove;k++) {
        	BetweennessCentrality<String,link> bc = 
            		new BetweennessCentrality<String,link>(graph, wtTransformer);
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
	// TODO what is this for? Ask 學長
	public <V,E> double computeModularity (Graph<V,E> g, Map<String,Integer> moduleMembership) {
		System.err.println("Computing Modularity...");
		
        double sum = 0;
        double m2 = (double)(2*g.getEdgeCount());
         
        for (V v1:g.getVertices()) {
        	for (V v2:g.getVertices()) {
        		int c1 = moduleMembership.get(v1);
        		int c2 = moduleMembership.get(v2);
        		//System.out.print("c1 moduleMembership => "+c1+"\n");
        		//System.out.print("c2 moduleMembership => "+c2+"\n");
        		if (c1 == c2 && !v1.equals(v2)) {
        			double delta = (g.isNeighbor(v1,v2)?1:0) - (double)g.degree(v1)*(double)g.degree(v2)/m2;
        			if(!g.isNeighbor(v1,v2)){
        				System.out.print(v1 + " 與 " + v2 + " 實際上同群，被判為非同群 \n");
        			}
        			sum = sum+delta;
        		}
        	}
        }
        //System.err.println("Modularity: "+sum/m2);
        return sum/m2;
	}
	
	public static void main(String args[]){
		
		//要計算中間度分群資料檔名的來源
		File d = new File("citeulike/citeulike_Tom_citeulike_0.4/");
		//File d = new File("Tom_reuters/single/acq");
		//File f = new File("Rank/acq_0011975_Rank.txt");
		for(File f : d.listFiles()){
			System.out.print("目前處理檔案為"+f.getName()+"\n");
			//bc.betweenness_cal("citeulike/citeulike_NGD_Tolerance_0.4/", "citeulike/citeulike_Tom_citeulike_0.4/", f.getName(), true);
			//bc.betweenness_cal("source_dir", "Tom_reuters/multi", f.getName(), true);
			//bc.betweenness_cal("citeulike/citeulike_Rank", "citeulike/citeulike_Tom_citeulike_noTolerance", f.getName(), true);
		}
	}
}
