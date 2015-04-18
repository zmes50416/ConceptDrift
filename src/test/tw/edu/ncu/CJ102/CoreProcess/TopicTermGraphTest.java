package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

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
		CEdge<TermNode> edge = new CEdge<>(new Pair<TermNode>(testNode1,testNode2));
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
		CEdge<TermNode> e=new CEdge<>(pair);
		assertTrue("First time add should be true",this.c.addEdge(e, testNode1, testNode2));
		this.c.addEdge(e, testNode1, testNode2);
		assertEquals("Second time the value should be 2",2,e.coScore,0.5);
	}

}
