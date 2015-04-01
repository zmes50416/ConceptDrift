package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class TopicTermGraphTest {
	TopicTermGraph c;
	TopicTermGraph c2;
	HashSet<TopicTermGraph> set;
	@Before
	public void setUp() throws Exception {
		c = new TopicTermGraph(1);
		c2 = new TopicTermGraph(1);
		set = new HashSet<>();
	}

	@Test
	public void testEquals() {//To Test if equal are override correctly
		
		assertEquals("same id should be the same cluster",c ,c2);
	}
	
	@Test
	public void testHashCode(){
		set.add(c);
		assertTrue("same id should not be able to added into same HashSet",!set.add(c2));
	}
	
	@Test
	public void testAddNode(){
		fail("not yet implement");
	}
	
	@Test
	public void testAddEdge(){
		fail("Not yet implement");
	}

}
