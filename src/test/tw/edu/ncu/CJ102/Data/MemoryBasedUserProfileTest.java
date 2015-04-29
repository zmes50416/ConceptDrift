package tw.edu.ncu.CJ102.Data;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
public class MemoryBasedUserProfileTest{
	public MemoryBasedUserProfile user;

	@Before
	public void setUp() throws Exception {
		user = new MemoryBasedUserProfile();
		
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDecayFactor() {
		TopicTermGraph aTopic = new TopicTermGraph(1);
		TopicTermGraph anotherTopic = new TopicTermGraph(1);
		anotherTopic.setLongTermInterest(true);
		user.userTopics.add(aTopic);
		user.userTopics.add(anotherTopic);
		aTopic.numberOfDocument = 1;
		System.out.println(user.getDecayRate(aTopic, 1));
		System.out.println(user.getDecayRate(anotherTopic, 1));
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetSizeOfShortTerm(){
		Collection<TopicTermGraph> topics = user.getUserTopics();
		TopicTermGraph topic = new TopicTermGraph(0);
		topic.setLongTermInterest(true);
		topics.add(topic);
		fail("Not yet implemented");
	}
	
	@Test
	public void testAddDocument(){
		HashMap<TopicTermGraph, TopicTermGraph> topicMap = new HashMap<TopicTermGraph,TopicTermGraph>();
		Collection<TopicTermGraph> topics = user.getUserTopics();
		
		TopicTermGraph userTopic1 = new TopicTermGraph(0);
		TopicTermGraph userTopic2 = new TopicTermGraph(0);
		
		topics.add(userTopic1);
		topics.add(userTopic2);
		
		TopicTermGraph documentTopic1 = new TopicTermGraph(1);
		TopicTermGraph documentTopic2  = new TopicTermGraph(1);
		TopicTermGraph documentTopic3 = new TopicTermGraph(1);
		topicMap.put(documentTopic1, userTopic1);
		topicMap.put(documentTopic2, userTopic2);
		topicMap.put(documentTopic3, documentTopic3);//Doesn't map to any exist user topic
		this.user.addDocument(topicMap,1);
		
		assertEquals("user should have 3 user topic now",3,topics.size());
		assertNotNull("Co Occurance Topic should have topic1 and 2",this.user.getTopicCOGraph().findEdge(userTopic1, userTopic2)); 
		//TODO test whether a topic will be longTerm or not
	}

}
