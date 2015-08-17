package tw.edu.ncu.CJ102.algorithm.impl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.colt.Arrays;
import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.ClusterSimility;
import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class TopicSimilarityTest {
	TopicMappingAlgorithm cs ;
	TopicTermGraph userTopic;
	TopicTermGraph documentTopic;
	@Before
	public void setUp() throws Exception {
		IndexSearchable indexer = new EmbeddedIndexSearcher();
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
//		IndexSearchable indexer = createMock(IndexSearchable.class);
//		expect(indexer.searchTermSize(notNull(String.class))).andReturn(10L).anyTimes();
//		expect(indexer.searchMultipleTerm(notNull(String[].class))).andReturn(5L).anyTimes();
//		replay(indexer);
		cs = new NgdReverseTfTopicSimilarity(indexer);
		userTopic = new TopicTermGraph(0);
		documentTopic = new TopicTermGraph(0);
		userTopic.addVertex(new TermNode("Google",1));
		documentTopic.addVertex(new TermNode("Yahoo",1));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		double sim = cs.computeSimilarity(documentTopic, userTopic);
		
		fail("Not yet implemented");
	}

}
