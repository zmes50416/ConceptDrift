package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.easymock.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicCoOccuranceGraph;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

public class UserProfileManagerTest extends EasyMockSupport{
	private TopicMappingTool tool;
	private AbstractUserProfile user;
	private HashSet<TopicTermGraph> mockUserTopics;
	private TopicTermGraph topic1;
	UserProfileManager manager;
	
	@Before
	public void setUp() throws Exception {
		createMock(TopicMappingAlgorithm.class);
		tool = createMock("mockMappingTool",TopicMappingTool.class);
		manager = new UserProfileManager(tool);
		topic1 = new TopicTermGraph(0);
		user = createMock("mockUserProfile",AbstractUserProfile.class);
		mockUserTopics = new HashSet<TopicTermGraph>();
		mockUserTopics.add(topic1);
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
		expect(user.getUserTopics()).andReturn(mockUserTopics);
		expect(user.getTopicRemoveThreshold()).andReturn(0.0);
		expect(user.getTopicCOGraph()).andReturn(new TopicCoOccuranceGraph());
		replay(user);
		
		this.manager.updateUserProfile(2, user);
		double score = 0;
		for(TermNode term:topic1.getVertices()){//sum up
			score += term.termFreq;
		}
		
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
		
		expect(user.getUserTopics()).andReturn(mockUserTopics);
		replay(user);
		
		manager.removeTopic(user, topic1);
		assertTrue(mockUserTopics.isEmpty());
		
	}
	
	@Test
	public void testInsertTopic() {
		topic1 = new TopicTermGraph(0);
		TopicTermGraph topic2 = new TopicTermGraph(0);
		mockUserTopics.add(topic2);

		expect(tool.map(notNull(TopicTermGraph.class), notNull(AbstractUserProfile.class))).andReturn(topic1).times(1).andReturn(topic2).times(1);
		//First Topic will be map to topic1, second will be map to topic2
		
		Collection<TopicTermGraph> documentTopics = new ArrayList<>();
		TopicTermGraph x = new TopicTermGraph(0);
		TopicTermGraph y = new TopicTermGraph(0);
		documentTopics.add(x);
		documentTopics.add(y);
		
		TopicCoOccuranceGraph tcoGraph = new TopicCoOccuranceGraph();

		expect(user.getUserTopics()).andReturn(mockUserTopics).anyTimes();
		user.addDocument(notNull(Map.class));
		expectLastCall();
		replayAll();
		
		Map<TopicTermGraph, TopicTermGraph> topicMap = manager.mapTopics(documentTopics , user);
		assertEquals(2,topicMap.size());
		assertSame(topic1,topicMap.get(x));
		assertSame(topic2,topicMap.get(y));
		
	}

}
