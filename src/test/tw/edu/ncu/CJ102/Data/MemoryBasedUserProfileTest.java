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
		for(int i=0;i<=8;i++){
			TopicTermGraph t = new TopicTermGraph(1);
			if(i<=9){
				t.setLongTermInterest(true);
			}
			user.userTopics.add(t);
		}
		double factorShort = 1;
		double factorLong = 1;
		for(int day=2;day<=8;day++){
			shortTopic.numberOfDocument++;
			double decayFactor = user.updateDecayRate(shortTopic, day);
			factorLong *= user.updateDecayRate(longTopic, day);
			factorShort *= decayFactor;
			System.out.println("Day "+day+", ShortTermTopic:"+factorShort);
			System.out.println(decayFactor);
			System.out.println("Day "+day+", longTermTopic:"+factorLong);
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

}
