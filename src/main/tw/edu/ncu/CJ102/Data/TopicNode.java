package tw.edu.ncu.CJ102.Data;

/**
 * customized Topic Node to use in JUNG Graph
 * @author TingWen
 *
 */
public class TopicNode{
	private String id;
	public TopicNode(String id){
		this.setId(id);
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof TopicNode){
			TopicNode anotherEdge = (TopicNode)o;
			return this.getId().equals(anotherEdge.getId());
		}else{
			return false;
		}
	}
	
	@Override
	public String toString(){
		return getId();
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}