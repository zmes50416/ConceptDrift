package tw.edu.ncu.CJ102.Data;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Collection;
import java.util.HashMap;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
public class MemoryBasedUserProfileTest extends EasyMockSupport{
	public MemoryBasedUserProfile user;

	@Before
	public void setUp() throws Exception {
		user = new MemoryBasedUserProfile();
		
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test 
	public void testValue(){
		TopicTermGraph shortTopic = new TopicTermGraph(1);
		shortTopic.addVertex(new TermNode("Test"));
		shortTopic.numberOfDocument = 1;
		shortTopic.setUpdateDate(1);
		TopicTermGraph longTopic = new TopicTermGraph(1);
		longTopic.setLongTermInterest(true);
		longTopic.setUpdateDate(1);
//		longTopic.numberOfDocument = 100;
		user.userTopics.add(shortTopic);
		user.userTopics.add(longTopic);

		double factorShort = 1;
		double factorLong = 1;
		for(int day=2;day<=7;day++){
			shortTopic.numberOfDocument++;
			double shortDecayFactor = user.updateDecayRate(shortTopic, day);
			double longDecayFactor = user.updateDecayRate(longTopic, day);
			factorLong *= longDecayFactor;
			factorShort *= shortDecayFactor;
//			System.out.println("Day"+day+",ShortTermTopic:"+factorShort);
//			System.out.println("Day "+day+", longTermTopic:"+factorLong);
			System.out.println("Day "+day+", long:"+longDecayFactor);
			System.out.println("Day"+day+",short:"+shortDecayFactor);
		}
	}

	@Test
	public void testDecayFactor() {
		TopicTermGraph aTopic = new TopicTermGraph(1);
		TopicTermGraph anotherTopic = new TopicTermGraph(1);
		anotherTopic.setLongTermInterest(true);
		user.userTopics.add(aTopic);
		user.userTopics.add(anotherTopic);
		aTopic.numberOfDocument = 1;
//		System.out.println(user.updateDecayRate(aTopic, 1));
//		System.out.println(user.updateDecayRate(anotherTopic, 1));
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
		//TODO test whether a topic will be longTerm or not
	}
	
	@Test
	public void testLongTermThreshold(){
		for(int i=1;i<=10;i++){
			TopicTermGraph topic = createMock(TopicTermGraph.class);
			expect(topic.getStrength()).andReturn((double)i).anyTimes();
			replay(topic);
			user.userTopics.add(topic);
		}
		user.percentageOfLongTerm = 0.5;
		user.computeLongTermThreshold();
		assertEquals(5.0,user.getLongTermThreshold(),0);
	}

}
