package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
@RunWith(value=Parameterized.class)
public class AbstractUserProfileTest{
	@Parameter
	public AbstractUserProfile user;
	@Parameters
	public static Collection<AbstractUserProfile[]> getTestParameters(){
		AbstractUserProfile[][] initArray = new AbstractUserProfile[][]{
				new AbstractUserProfile[]{new MemoryBasedUserProfile()},new AbstractUserProfile[]{new MemoryBasedUserProfile()
				}
		};
		return Arrays.asList(initArray);
	}

	@Before
	public void setUp() throws Exception {
		user = new MemoryBasedUserProfile();
		Collection<TopicTermGraph> topics = user.getUserTopics();
		topics.add(new TopicTermGraph(0));
		topics.add(new TopicTermGraph(1));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDecay() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetSizeOfShortTerm(){
		fail("Not yet implemented");
	}

}
