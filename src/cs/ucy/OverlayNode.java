package cs.ucy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class OverlayNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8250874626660000156L;
	private final SocialNode socialNode;
	private double nodeID;
	private List<Double> shortRangeLinks;
	private List<Double> longRangeLinks;
	private List<Double> incomingLinks;
	private Map<Double, Double> secondHopMapping;

	public OverlayNode(SocialNode socialNode) {
		this.socialNode = socialNode;
		this.socialNode.setOverlayNode(this);
		this.shortRangeLinks = new ArrayList<>();
		this.longRangeLinks = new ArrayList<>();
		this.incomingLinks = new ArrayList<>();
		secondHopMapping = new HashMap<>();
	}

	public void addShortRangeLink(Double link) {
		shortRangeLinks.add(link);
	}

	public void clearShortRangeLinks() {
		shortRangeLinks.clear();
	}

	public void clearLongRangeLinks() {
		longRangeLinks.clear();
	}
	
	public void clearIncomingLinks() {
		incomingLinks.clear();
	}
	
	public void clearSecondHopMapping() {
		secondHopMapping.clear();
	}

	public void addLongRangeLink(Double link) {
		longRangeLinks.add(link);
	}

	public List<Double> getShortRangeLinks() {
		return shortRangeLinks;
	}

	public List<Double> getLongRangeLinks() {
		return longRangeLinks;
	}

	public SocialNode getSocialNode() {
		return this.socialNode;
	}

	public void setNodeID(Double nodeID) {
		this.nodeID = nodeID;
	}

	public Double getNodeID() {
		return this.nodeID;
	}
	
	public void addIncomingLink(Double link) {
		incomingLinks.add(link);
	}
	
	public List<Double> getIncomingLinks() {
		return incomingLinks;
	}
	
	public void addSecondHopMapping(double secondHopId, double intermediateId) {
		secondHopMapping.put(secondHopId, intermediateId);
	}
	
	public Map<Double, Double> getSecondHopMapping() {
		return secondHopMapping;
	}

}
