package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.easymock.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

public class UserProfileManagerTest{
	private TopicMappingAlgorithm algo;
	private TopicMappingTool tool;
	private AbstractUserProfile user;
	private HashSet<TopicTermGraph> mockdata;
	private TopicTermGraph topic1;
	UserProfileManager manager;
	
	@Before
	public void setUp() throws Exception {
		algo = createMock(TopicMappingAlgorithm.class);
		tool = createMock(TopicMappingTool.class);
		manager = new UserProfileManager(tool);
		topic1 = new TopicTermGraph(0, 0);
		user = createMock(AbstractUserProfile.class);
		mockdata = new HashSet<TopicTermGraph>();
		mockdata.add(topic1);
		

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserProfileManager() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateUserProfile() {

		expect(user.getDecayRate(null, 0)).andReturn(0.5);
		expect(user.getUserTopics()).andReturn(mockdata);
		replay(user);
		
		this.manager.updateUserProfile(2, user);
		double score = 0;
		for(TermNode t:topic1.getVertices()){
			score += t.termFreq;
		}
		assertEquals("This is not ",0.0,score,0.1);
		//fail("Not yet implemented");
	}

	@Test
	public void testRemoveTerm() {
		TermNode term = new TermNode("google");
		topic1.addVertex(term);
		expect(user.getUserTopics()).andReturn(mockdata);
		replay(user);
		this.manager.removeTerm(topic1, term);
		assertTrue("Topic term"+term.toString()+"should have been removed",!topic1.containsVertex(term));
	}

	@Test
	public void testRemoveTopic() {
		
		expect(user.getUserTopics()).andReturn(mockdata);
		replay(user);
		
		manager.removeTopic(user, topic1);
		assertTrue(mockdata.isEmpty());
		
	}

	@Test
	public void testGetAverageDocumentTermFreq() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddTopic() {
		topic1 = createMock(TopicTermGraph.class);
		expect(tool.map(notNull(TopicTermGraph.class), notNull(AbstractUserProfile.class))).andReturn(topic1);
		replay(tool);
		
		Collection<TopicTermGraph> documentTopics = new ArrayList<>();
		TopicTermGraph x = new TopicTermGraph(1,0);
		documentTopics.add(x);
		expect(user.getUserTopics()).andReturn(mockdata);
		replay(user);
		
		manager.addTopic(documentTopics , user);
		
		replay(topic1);
		
		HashSet<TopicTermGraph> topics = user.getUserTopics();
		assertTrue("topic should not be empty",!topics.isEmpty());
	}

}
