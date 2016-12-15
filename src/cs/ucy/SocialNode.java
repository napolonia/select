package cs.ucy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2466803387945540655L;
	private OverlayNode overlayNode;
	private final int nodeID;
	private Map<Integer, SocialNode> neighborNodeMap;
	private int maximumTriangle = 0;
	private double maximumFloatId = 0;
	private double longestDistanceNodeID = 0.0;
	
	private List<Integer> friendDistribution;
	private Map<Integer, Integer> triangleMap;
	
	private boolean active;
	
	public SocialNode(int nodeId) {
		this.nodeID = nodeId;
		this.triangleMap = new HashMap<>();
		this.neighborNodeMap = new HashMap<>();
		this.friendDistribution = new ArrayList<>();
		this.active = false;
	}
	
	public void addNeighbor(SocialNode neighborNode) {
		if(!neighborNodeMap.containsKey(neighborNode.getNodeId())) {
			neighborNodeMap.put(neighborNode.getNodeId(), neighborNode);
		}
	}
	
	public Map<Integer, SocialNode> getNeighbors() {
		return neighborNodeMap;
	}
	
	public SocialNode getNeighbor(int nodeId) {
		if(neighborNodeMap.containsKey(nodeId)) {
			return neighborNodeMap.get(nodeId);
		}
		return null;
	}
	
	public int getNodeId() {
		return this.nodeID;
	}
	
	public void addTriangle(int neighborId, int value) {
		triangleMap.put(neighborId, value);
		if(value > maximumTriangle) {
			maximumTriangle = value;
			maximumFloatId = neighborId;
		}
	}
	
	public Map<Integer, Integer> getTriangles() {
		return triangleMap;
	}
	
	public int getMaximumTriangle() {
		return maximumTriangle;
	}
	
	public double getMaximumNeighborId() {
		return maximumFloatId;
	}
	
	public void setState(boolean state) {
		active = state;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setOverlayNode(OverlayNode overlayNode) {
		this.overlayNode = overlayNode;
	}
	
	
	public OverlayNode getOverlayNode() {
		return overlayNode;
	}
	
	public void calculateFriendDistribution(Map<Double, Integer> ringIDMap) {
		friendDistribution.clear();
		int index1 = ringIDMap.get(this.overlayNode.getNodeID());
		longestDistanceNodeID = 0.0;
		int maxDistance = 0;
		for(Map.Entry<Integer, SocialNode> entry : neighborNodeMap.entrySet()) {
			int index2 = ringIDMap.get(entry.getValue().getOverlayNode().getNodeID());
			int distance = Math.abs(index1 - index2);
			distance = Math.min(distance, ringIDMap.size() - distance);
			if(distance > maxDistance) {
				maxDistance = distance;
				longestDistanceNodeID = entry.getValue().getOverlayNode().getNodeID();
			}
			friendDistribution.add(distance);
		}
	}

	public List<Integer> getFriendDistribution() {
		return friendDistribution;
	}
	
	public double getLongestDistanceNodeId() {
		return longestDistanceNodeID;
	}

}
