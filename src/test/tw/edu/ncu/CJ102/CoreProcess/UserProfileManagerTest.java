package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.easymock.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

public class UserProfileManagerTest{
	private TopicMappingTool tool;
	private AbstractUserProfile user;
	private HashSet<TopicTermGraph> mockdata;
	private TopicTermGraph topic1;
	UserProfileManager manager;
	
	@Before
	public void setUp() throws Exception {
		createMock(TopicMappingAlgorithm.class);
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

	@Test(expected=NullPointerException.class)
	public void testUserProfileManager() {
		this.manager = new UserProfileManager(null);
	}

	@Test
	public void testUpdateUserProfile() {
		topic1.addVertex(new TermNode("test1",5.0));
		topic1.addVertex(new TermNode("test2",5.0));
		expect(user.getDecayRate(notNull(TopicTermGraph.class), anyInt())).andReturn(0.5);
		expect(user.getUserTopics()).andReturn(mockdata);
		Map<TopicTermGraph,Double> mockInterset = new HashMap<>();
		mockInterset.put(topic1, 10.0);
		expect(user.getInterestValueMap()).andReturn(mockInterset);
		expect(user.getTopicRemoveThreshold()).andReturn(0.0);
		replay(user);
		
		this.manager.updateUserProfile(2, user);
		double score = mockInterset.get(topic1);
		assertEquals("Decay are not function normally",5.0,score,0.1);
	}

	@Test
	public void testRemoveTerm() {
		String termName = "google";
		topic1.addVertex(new TermNode(termName));

		this.manager.removeTerm(topic1, new TermNode(termName));
		assertTrue("Topic term "+termName+" should have been removed",!topic1.containsVertex(new TermNode(termName)));
	}
	@Test(expected=IllegalArgumentException.class)
	public void testRemoveTermDoNotExsitInTheTopic(){
		TermNode term = new TermNode("Google");
		
		this.manager.removeTerm(topic1,term);
	}

	@Test
	public void testRemoveTopic() {
		
		expect(user.getUserTopics()).andReturn(mockdata);
		replay(user);
		
		manager.removeTopic(user, topic1);
		assertTrue(mockdata.isEmpty());
		
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
		
		Collection<TopicTermGraph> topics = user.getUserTopics();
		assertTrue("topic should not be empty",!topics.isEmpty());
	}

}
