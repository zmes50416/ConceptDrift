package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.PropertyConfigurator;
import org.easymock.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TermNode;
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
		mockUserTopics = new HashSet<TopicTermGraph>();
		mockUserTopics.add(topic1);
		
		user = createMock("mockUserProfile",AbstractUserProfile.class);
		expect(user.getUserTopics()).andReturn(mockUserTopics).anyTimes();

		
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
		expect(user.updateDecayRate(notNull(TopicTermGraph.class), anyInt())).andReturn(0.5);
		expect(user.getTermRemoveThreshold()).andReturn(0.0).anyTimes();
		replay(user);
		
		this.manager.updateUserProfile(2, user);
		double score = 0;
		for(TermNode term:topic1.getVertices()){//sum up
			score += term.termFreq;
		}
		assertEquals("Decay are not function normally",5.0,score,0.1);
	}
	@Test
	public void testRemoveForgottenTopics(){
		expect(user.getTopicRemoveThreshold()).andReturn(3.0).anyTimes();
		expect(user.getTermRemoveThreshold()).andReturn(3.0).anyTimes();
		replay(user);
		topic1.addVertex(new TermNode("test1",10.0));
		topic1.addVertex(new TermNode("test2",1.0));
		TopicTermGraph topic2 = new TopicTermGraph(0);
		this.mockUserTopics.add(topic2);
		this.manager.removeForgottenTopics(user);
		assertTrue("Should have remove the topic2",!user.getUserTopics().contains(topic2));
	}
	
	@Test
	public void testCheckType(){
		user.computeLongTermThreshold();
		expect(user.getLongTermThreshold()).andReturn(5.0).anyTimes();
		TopicTermGraph topic1 = createMock(TopicTermGraph.class);
		expect(topic1.getStrength()).andReturn(7.0);
		expect(topic1.isLongTermInterest()).andReturn(false);
		topic1.setLongTermInterest(true);
		expectLastCall().once();
		replay(topic1);
		replay(user);
		this.mockUserTopics.add(topic1);
		this.manager.checkTopicType(user);
		verify(topic1);
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
		
		expectLastCall();
		replayAll();
		
		Map<TopicTermGraph, TopicTermGraph> topicMap = manager.mapTopics(documentTopics , user);
		assertEquals(2,topicMap.size());
		assertSame(topic1,topicMap.get(x));
		assertSame(topic2,topicMap.get(y));
		
	}

}
