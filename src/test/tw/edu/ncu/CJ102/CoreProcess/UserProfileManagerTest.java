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
		
		expect(user.getUserTopics()).andReturn(mockUserTopics);
		replay(user);
		
		manager.removeTopic(user, topic1);
		assertTrue(mockUserTopics.isEmpty());
		
	}
	
	@Test
	public void testAddTopic() {
		topic1 = new TopicTermGraph(0);
		TopicTermGraph topic2 = new TopicTermGraph(0);
		mockUserTopics.add(topic2);

		expect(tool.map(notNull(TopicTermGraph.class), notNull(AbstractUserProfile.class))).andDelegateTo(new stubTool()).anyTimes();
		
		Collection<TopicTermGraph> documentTopics = new ArrayList<>();
		TopicTermGraph x = new TopicTermGraph(0);
		TopicTermGraph y = new TopicTermGraph(0);
		documentTopics.add(x);
		documentTopics.add(y);
		documentTopics.add(new TopicTermGraph(0));
		
		TopicCoOccuranceGraph tcoGraph = new TopicCoOccuranceGraph();

		expect(user.getUserTopics()).andReturn(mockUserTopics).anyTimes();		
		expect(user.getTopicCOGraph()).andReturn(tcoGraph).anyTimes();
		replayAll();
		
		manager.addTopic(documentTopics , user);
		
		Collection<TopicTermGraph> topics = user.getUserTopics();
		
		assertTrue("topic should not be empty",!topics.isEmpty());
		assertNotEquals("Co Occurance Topic should not be empty",0,tcoGraph.getEdgeCount()); //There are chances that 
	}
	
	private class stubTool extends TopicMappingTool{

		public stubTool() {
			super(null, 0);
		}
		@Override
		public TopicTermGraph map(TopicTermGraph theTopic,AbstractUserProfile user){ //simulate method : random topic in user profile will be return or the topic itself
			int size = mockUserTopics.size();
			int randPosition = new Random().nextInt(size+1);
			int i = 0;
			for(TopicTermGraph topic:mockUserTopics){
				if(i++ == randPosition){
					return topic;
				}
			}
			return theTopic;//should never reach this line
		}
		
	}

}
