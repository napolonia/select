package cs.ucy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SocialNetworkManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 380766458970200490L;
	private Map<Integer, SocialNode> socialNodeIdMap;

	public SocialNetworkManager() {
		socialNodeIdMap = new HashMap<>();
	}

	public boolean nodeIdExist(int nodeId) {
		return socialNodeIdMap.containsKey(nodeId);
	}

	public void addSocialNode(SocialNode socialNode) {
		socialNodeIdMap.put(socialNode.getNodeId(), socialNode);
	}

	public SocialNode getSocialNode(int nodeId) {
		return socialNodeIdMap.get(nodeId);
	}

	public Map<Integer, SocialNode> getSocialNodes() {
		return socialNodeIdMap;
	}

	public void calculateMutualFriendships() {
		int counter = 0;
		for (Map.Entry<Integer, SocialNode> entrySet : socialNodeIdMap
				.entrySet()) {
			if((counter++ % 100) == 0) 
				System.out.println(counter);
			SocialNode value = entrySet.getValue();
			Map<Integer, SocialNode> neighbors = value.getNeighbors();

			for (Map.Entry<Integer, SocialNode> neighborSet : neighbors
					.entrySet()) {

				Map<Integer, SocialNode> tmpNeighbors = new HashMap<>();
				tmpNeighbors.putAll(neighbors);

				int neighborKey = neighborSet.getKey();
				SocialNode neighborValue = neighborSet.getValue();
				tmpNeighbors.entrySet().retainAll(
						neighborValue.getNeighbors().entrySet());
				
				if (tmpNeighbors.isEmpty()) {
					value.addTriangle(neighborKey, 0);
				} else {
					value.addTriangle(neighborKey, tmpNeighbors.size());
				}
			}

		}
	}

}
