package tw.edu.ncu.CJ102.CoreProcess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.Data.*;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

/**
 * use for interact with user profile,ex:update decay factor or add document into
 * @author TingWen
 *
 */
public class UserProfileManager {
//use for manipulate user profile, decoupling user profile's high dependency
	TopicMappingTool mapper;
	Logger loger = LoggerFactory.getLogger(UserProfileManager.class);
	
	public UserProfileManager(TopicMappingTool _mapper) {
		if(_mapper == null){
			throw new NullPointerException("Cant work without a mapper algorithm");
		}
		this.mapper = _mapper;
	}
	/**
	 * 執行的使用者模型的一日更新
	 * 包含遺忘因子的作用與主題、字詞的去除
	 */
	public void updateUserProfile(int theDay, AbstractUserProfile user) {
		Collection<TopicTermGraph> userTopics = user.getUserTopics();
		
		if (userTopics.isEmpty()) {
			if(loger.isInfoEnabled()){
				loger.info("Day{} , System have no topic to update",theDay);
			}
			return;
		}
		
		for (TopicTermGraph topic: userTopics) {// 遺忘因子流程
			topic.setDecayRate(user.updateDecayRate(topic, theDay));
			for (TermNode term : topic.getVertices()) { //update every term in topic
				term.termFreq = term.termFreq * topic.getDecayRate();
			}
			for(CEdge edge:topic.getEdges()){//decay Edge weight
				edge.setCoScore(edge.getCoScore()*topic.getDecayRate());
			}
			loger.debug("Day{} ,Topic:{}, decay factory:{}",theDay,topic,topic.getDecayRate());

		}//end of while
		
	}
	public void identifyBelowRemoveAndLongTermThreshold(AbstractUserProfile user){
		Iterator<TopicTermGraph> iter = user.getUserTopics().iterator();
		while(iter.hasNext()){
			TopicTermGraph topic = iter.next();
			double topicInterest = 0;
			HashSet<TermNode> termsToRemove = new HashSet<TermNode>();
			for (TermNode term : topic.getVertices()) {
				if(term.termFreq<user.getTermRemoveThreshold()){//avoid modify exception
					termsToRemove.add(term);
					continue;//don't add up the topic value 
				}
				topicInterest += term.termFreq;
			}
			//TODO should not depend on low level implement detail, but no time to fix it
			if(topic.isLongTermInterest()&& topicInterest < user.longTermThreshold/2.0){
				topic.setLongTermInterest(false);
			}
			if(topicInterest < user.getTopicRemoveThreshold()){
				iter.remove();
				loger.debug("System remove a topic:{}, Interest value = {}",topic.toString(),topicInterest);
				continue;
			}
			for(TermNode term:termsToRemove){
				topic.removeVertex(term);
				if(loger.isDebugEnabled()){
					loger.debug("Term {} remove from topic because value too low",term);
				}
			}
			
			
		}
		
	}
	/**
	 * 將文件內的每一個主題進行主題映射找出最相似的使用者主題
	 * @param user 使用者模型
	 * @param doc_term 文件內容資訊
	 * @return 文件主題(Key)與該使用者主題(Value)的配對
	 */
	public Map<TopicTermGraph,TopicTermGraph> mapTopics(Collection<TopicTermGraph> documentTopics, AbstractUserProfile user){
		HashMap<TopicTermGraph,TopicTermGraph> mappedTopics = new HashMap<>();
		for(TopicTermGraph topic:documentTopics){
			TopicTermGraph mappedTopic = this.mapper.map(topic, user);
			mappedTopics.put(topic, mappedTopic);
		}//end of for
		return mappedTopics;
		
	}
	
	public void draw(AbstractUserProfile user,Path outputDir) throws IOException{
			try{
			Collection<TopicTermGraph> userTopics = user.getUserTopics();
			for (TopicTermGraph topic : userTopics) {
				final Collection<TermNode> coreTerms = topic.getCoreTerm();
				Layout<TermNode, CEdge> layout = new ISOMLayout<>(topic);
				layout.setSize(new Dimension(1600, 1600));
				VisualizationImageServer<TermNode, CEdge> vis = new VisualizationImageServer<TermNode, CEdge>(
						layout, layout.getSize());
				vis.setBackground(Color.WHITE);
				vis.getRenderContext().setVertexLabelTransformer(
						new VertexContentTransformer());
				vis.getRenderContext().setVertexFillPaintTransformer(
						new Transformer<TermNode, Paint>() {

							@Override
							public Paint transform(TermNode input) {
								if (coreTerms.contains(input)) {
									return Color.RED;
								} else {
									return Color.BLUE;
								}
							}

						});

				BufferedImage image = (BufferedImage) vis.getImage(
						new Point2D.Double(layout.getSize().getWidth() / 2,
								layout.getSize().getHeight() / 2),
						new Dimension(layout.getSize()));

				// Write image to a png file
				File outputfile = outputDir.resolve(topic.toString() + ".png").toFile();
				ImageIO.write(image, "png", outputfile);

			}
			}catch(Exception e){
				if(e instanceof IOException){
					throw e;
				}
				loger.error("Drawing failed in {}",e.getMessage());
			}
	}
	
	private class VertexContentTransformer implements Transformer<TermNode, String>{

		@Override
		public String transform(TermNode input) {
			return input.getTerm();
		}
	}

}
