package tw.edu.ncu.CJ102.CoreProcess;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserProfileTest {
	UserProfile m = new UserProfile(false);
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	@Test
	public void testStore(){
		Path tempProject = null;
		ObjectInputStream  reader = null;
		ObjectInputStream  readerR = null;
		try {
			tempProject = Files.createTempDirectory("UnitTest_");
			Files.createDirectories(Paths.get(tempProject.toString(), "user_porfile"));
			m.store(tempProject.toString());
			Path termDFile = Paths.get(tempProject.toString()+ UserProfile.TDF_FILENAME);
			Path termRFile = Paths.get(tempProject.toString()+ UserProfile.TR_FILENAME);
			
			reader = new ObjectInputStream(new FileInputStream(termDFile.toFile()));
			Object o=reader.readObject();
			@SuppressWarnings("unchecked")
			HashMap<String,Double> terms = (HashMap<String,Double>)o;
			assertEquals("Should the same obj",terms,m.terms);
			readerR = new ObjectInputStream(new FileInputStream(termRFile.toFile()));
			Object o2 =readerR.readObject();
			@SuppressWarnings("unchecked")
			HashSet<TopicCluster> topics = (HashSet<TopicCluster>)o2;
			assertEquals("Should be the same obj",topics,m.topics);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				reader.close();
				readerR.close();
				FileUtils.deleteDirectory(tempProject.toFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
