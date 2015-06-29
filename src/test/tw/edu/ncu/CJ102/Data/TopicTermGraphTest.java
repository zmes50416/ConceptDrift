package tw.edu.ncu.CJ102.Data;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.CoreProcess.Experiment;
import edu.uci.ics.jung.graph.util.Pair;

public class TopicTermGraphTest {
	TopicTermGraph c;
	TopicTermGraph c2;
	HashSet<TopicTermGraph> set;
	@Before
	public void setUp() throws Exception {
		c = new TopicTermGraph(0);
		c2 = new TopicTermGraph(0);
		set = new HashSet<>();
	}
	
	@Test
	public void testMerge(){
		TermNode testNode = new TermNode("test");
		c.addVertex(testNode);
		
		TermNode testNode1 = new TermNode("Google");
		TermNode testNode2 = new TermNode("Samsung");
		c2.addVertex(new TermNode("test"));
		c2.addVertex(testNode1);
		CEdge edge = new CEdge(new Pair<TermNode>(testNode1,testNode2));
		c2.addEdge(edge, testNode1,testNode2);
		c.merge(c2);
		assertEquals("Should have 1 edge after merge",1,c.getEdgeCount());
		assertEquals("Should have 3 Node after merge",3,c.getVertexCount());
		assertEquals("Test node should have TF = 2",2.0,testNode.termFreq,0.1);
	}
	@Test
	public void testAddVertex(){
		TermNode testNode = new TermNode("test");
		this.c.addVertex(testNode);
		assertTrue("Should be false",!this.c.addVertex(new TermNode("test")));
		assertEquals("Graph testNode should have 2 TF",2.0, testNode.termFreq, 0.5);
		assertEquals("Graph should only have one node",1,this.c.getVertexCount());
		
	}
	
	@Test
	public void testAddEdge(){
		TermNode testNode1 = new TermNode("Google");
		TermNode testNode2 = new TermNode("Samsung");
		Pair<TermNode> pair = new Pair<TermNode>(testNode1,testNode2);
		CEdge e=new CEdge(pair);
		assertTrue("First time add should be true",this.c.addEdge(e, testNode1, testNode2));
		this.c.addEdge(e, testNode1, testNode2);
		assertEquals("Second time the value should be 2",2,e.getCoScore(),0.5);
	}
	
	@Test
	public void testGetCoreTerm(){
		TopicTermGraph.MAXCORESIZE = 2;
		TermNode[] nodes = {new TermNode("Google",1),new TermNode("Apple",2),new TermNode("Samsung",3),new TermNode("Galaxy S4",1),new TermNode("iPhone",1)};
		CEdge edgeiPGa = new CEdge();
		edgeiPGa.setCoScore(1);
		CEdge edgeSamApp = new CEdge();
		edgeSamApp.setCoScore(2);
		CEdge edgeGaSa =  new CEdge();
		edgeGaSa.setCoScore(1);
		CEdge edgeiPAp = new CEdge();
		edgeiPAp.setCoScore(1);
		CEdge edgeApGo = new CEdge();
		edgeApGo.setCoScore(1);
		CEdge edgeSamGo = new CEdge();
		edgeSamGo.setCoScore(1);
		
		c.addEdge(edgeSamGo, nodes[2], nodes[0]);
		c.addEdge(edgeApGo, nodes[1],nodes[0]);
		c.addEdge(edgeiPGa,nodes[4],nodes[3]);
		c.addEdge(edgeiPAp, nodes[4],nodes[1]);
		c.addEdge(edgeSamApp, nodes[2],nodes[1]);
		c.addEdge(edgeGaSa, nodes[0],nodes[2]);
		Collection<TermNode> cores = null;
		for(int i=0;i<=2;i++){
			TopicTermGraph.METHODTYPE = i;
			cores = this.c.getCoreTerm();
			System.out.println(cores);
//			System.out.println(c.scoreSheet);
		}
		assertEquals(2,cores.size());
//		assertTrue(!cores.contains(testNode));
		
	}

}
