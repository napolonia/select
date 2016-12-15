package cs.ucy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class OverlayManager {

	private int NUM_OF_LONG_RANGE_LINKS = 0;
	private final double SATISFACTION_THRESHOLD = 0.001;
	// private final String path = "/Users/santaris/Desktop/Google+_results/";
	private final String path = "C:\\Users\\Stefanos\\Desktop\\Gplus_OurResults\\";

	private Map<Double, OverlayNode> nodeIdMap;
	private List<OverlayNode> ring;
	private Map<Integer, SocialNode> socialNodeIdMap;
	private Map<Double, Integer> ringIDMap;
	private List<Integer> remainingInactiveSocialNodeIDs;
	private List<Integer> inactiveSocialNodes;
	private FileManager fm;
	private int version;

	public OverlayManager(Map<Integer, SocialNode> socialNodeIdMap,
			FileManager fm, int version) {
		this.fm = fm;
		this.version = version;
		nodeIdMap = new HashMap<>();
		this.socialNodeIdMap = socialNodeIdMap;
		this.inactiveSocialNodes = new ArrayList<>();
		this.remainingInactiveSocialNodeIDs = new ArrayList<>();
		for (Map.Entry<Integer, SocialNode> entry : this.socialNodeIdMap
				.entrySet()) {
			this.remainingInactiveSocialNodeIDs.add(entry.getKey());
			this.inactiveSocialNodes.add(entry.getKey());
		}

		Collections.shuffle(this.inactiveSocialNodes);
		ring = new ArrayList<>();
		ringIDMap = new HashMap<>();
	}

	public void constructOverlay() {
		int counter = 0;
		int size = remainingInactiveSocialNodeIDs.size();
		System.out.println("Number of Nodes in the Network : " + size);
		Random random = new Random(new Date().getTime());
		while (counter < size) {
			int index = random.nextInt(remainingInactiveSocialNodeIDs.size());
			int randomSocialNode = remainingInactiveSocialNodeIDs.get(index);
			SocialNode socialNode = socialNodeIdMap.get(randomSocialNode);
			int neighborId = getMostImportantNeighborID(socialNode);
			if (neighborId == -1) {
				double nodeId = random.nextDouble();
				while (nodeIdMap.containsKey(nodeId)) {
					nodeId = random.nextDouble();
				}
				createOverlayNode(socialNode, nodeId);
			} else {

				double neighborOverlayId = socialNodeIdMap.get(neighborId)
						.getOverlayNode().getNodeID();
				double nodeId = Math.nextUp(neighborOverlayId);

				while (nodeIdMap.containsKey(nodeId)) {
					nodeId = Math.nextUp(nodeId);
				}
				createOverlayNode(socialNode, nodeId);
			}
			remainingInactiveSocialNodeIDs.remove(index);
			counter++;
		}
		System.out.println("Sort Node IDs");
		sortNodeIDs();
		System.out.println("Extract stats");
		fm.extractOverlayNodeIDs(version, false, nodeIdMap);
		// fm.extractDistance(version, false, socialNodeIdMap);
		fm.calculateFriendsDistance(version, false, nodeIdMap, socialNodeIdMap,
				ringIDMap);
	}

	private void createFigers() {
		for (int i = 0; i < ring.size(); i++) {
			ring.get(i).clearLongRangeLinks();
			ring.get(i).clearShortRangeLinks();
			ring.get(i).clearIncomingLinks();
			ring.get(i).clearSecondHopMapping();
		}

		ring.get(0).addShortRangeLink(ring.get(1).getNodeID());
		ring.get(0).addShortRangeLink(ring.get(ring.size() - 1).getNodeID());

		ring.get(ring.size() - 1).addShortRangeLink(ring.get(0).getNodeID());
		ring.get(ring.size() - 1).addShortRangeLink(
				ring.get(ring.size() - 2).getNodeID());

		for (int i = 1; i < (ring.size() - 1); i++) {
			ring.get(i).addShortRangeLink(ring.get(i - 1).getNodeID());
			ring.get(i).addShortRangeLink(ring.get(i + 1).getNodeID());
		}

		createLongRangeFingers();

		System.out.println("Links created");

	}

	private void createLongRangeFingers() {

		int shortRangeLinks = 0;
		int incomingLinksConflict = 0;
		int providedByMutualLink = 0;
		int routeWithMutualFriend = 0;
		for (int i = 0; i < inactiveSocialNodes.size(); i++) {
			SocialNode tmpNode = socialNodeIdMap
					.get(inactiveSocialNodes.get(i));
			OverlayNode tmpOverlayNode = tmpNode.getOverlayNode();
			List<Entry<Integer, Integer>> orderedTriangles = orderTriangles(tmpNode
					.getTriangles());
			int counter = 0;
			while (tmpOverlayNode.getLongRangeLinks().size() < NUM_OF_LONG_RANGE_LINKS) {
				if (counter < orderedTriangles.size()) {
					OverlayNode candidateOverlayNode = socialNodeIdMap.get(
							orderedTriangles.get(
									orderedTriangles.size() - counter - 1)
									.getKey()).getOverlayNode();
					if (!tmpOverlayNode.getShortRangeLinks().contains(
							candidateOverlayNode.getNodeID())
							&& !tmpOverlayNode.getLongRangeLinks().contains(
									candidateOverlayNode.getNodeID())) {
						if (!mutualFriendshipLinkExist(tmpOverlayNode,
								candidateOverlayNode)) {
							if (candidateOverlayNode.getIncomingLinks().size() < (2 * NUM_OF_LONG_RANGE_LINKS)) {

								candidateOverlayNode
										.addIncomingLink(tmpOverlayNode
												.getNodeID());
								tmpOverlayNode
										.addLongRangeLink(candidateOverlayNode
												.getNodeID());
							} else {
								incomingLinksConflict++;
							}
						} else {
							List<Double> mutualLinks = checkForMutualIncomingLinks(
									candidateOverlayNode.getIncomingLinks(),
									tmpOverlayNode);
							tmpOverlayNode.addSecondHopMapping(
									candidateOverlayNode.getNodeID(),
									mutualLinks.get(0));
						}

					} else {
						shortRangeLinks++;
					}
					counter++;
				} else {
					break;
				}
			}
		}

		System.out.println("Short range links conflict = " + shortRangeLinks);
		System.out
				.println("Incoming Links conflict = " + incomingLinksConflict);
		System.out
				.println("Provided by mutual links = " + providedByMutualLink);
		System.out.println("Route with mutual friend = "
				+ routeWithMutualFriend);
	}

	private boolean mutualFriendshipLinkExist(OverlayNode tmpOverlayNode,
			OverlayNode candidateOverlayNode) {
		List<Double> mutualIncomingLinks = checkForMutualIncomingLinks(
				candidateOverlayNode.getIncomingLinks(), tmpOverlayNode);
		return !mutualIncomingLinks.isEmpty();
	}

	private List<Double> checkForMutualIncomingLinks(
			List<Double> incomingLinks, OverlayNode tmpOverlayNode) {
		List<Double> mutualIncomingLinks = new ArrayList<>();
		for (int j = 0; j < incomingLinks.size(); j++) {
			for (Map.Entry<Integer, SocialNode> neighborEntry : tmpOverlayNode
					.getSocialNode().getNeighbors().entrySet()) {
				if (neighborEntry.getValue().getOverlayNode().getNodeID()
						.equals(incomingLinks.get(j))) {
					if (tmpOverlayNode.getShortRangeLinks().contains(
							incomingLinks.get(j))
							|| tmpOverlayNode.getLongRangeLinks().contains(
									incomingLinks.get(j))) {
						mutualIncomingLinks.add(incomingLinks.get(j));
					}
				}
			}
		}
		return mutualIncomingLinks;
	}

	public static List<Entry<Integer, Integer>> orderTriangles(
			Map<Integer, Integer> triangles) {
		List<Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(
				triangles.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		// TODO Auto-generated method stub
		return list;
	}

	private void sortNodeIDs() {
		List<Double> nodeIdList = new ArrayList<>();
		for (Map.Entry<Double, OverlayNode> entry : nodeIdMap.entrySet()) {
			nodeIdList.add(entry.getKey());
		}
		Collections.sort(nodeIdList);
		ring.clear();
		ringIDMap.clear();
		for (int i = 0; i < nodeIdList.size(); i++) {
			ring.add(nodeIdMap.get(nodeIdList.get(i)));
			ringIDMap.put(nodeIdList.get(i), i);
		}
	}

	private void createOverlayNode(SocialNode socialNode, double nodeId) {
		OverlayNode node = new OverlayNode(socialNode);
		node.setNodeID(nodeId);
		nodeIdMap.put(nodeId, node);
		socialNode.setState(true);
	}

	private int getMostImportantNeighborID(SocialNode socialNode) {
		Map<Integer, SocialNode> neighbors = socialNode.getNeighbors();
		Map<Integer, Integer> trianglesMap = socialNode.getTriangles();

		List<Entry<Integer, Integer>> orderedTriangles = orderTriangles(trianglesMap);
		for (int i = 0; i < orderedTriangles.size(); i++) {
			if (neighbors.get(
					orderedTriangles.get(orderedTriangles.size() - i - 1)
							.getKey()).isActive()) {
				return (orderedTriangles.get(orderedTriangles.size() - i - 1))
						.getKey();
			}
		}
		return -1;
	}

	public static double getStandardDeviation(double[] distance, double mean) {
		Double std = 0.0;
		for (int i = 0; i < distance.length; i++) {
			std += Math.pow(distance[i] - mean, 2);
		}
		return std / distance.length;
	}

	public static double getMean(double[] distance) {
		double mean = 0.0;
		for (int j = 0; j < distance.length; j++) {
			mean += distance[j];
		}
		double meanValue = (double) ((double) mean / (double) distance.length);

		return meanValue;
	}

	private void extractHopsStats(int version, int policy, boolean modified) {
		double[] hops = new double[nodeIdMap.size()];
		double[] stdHops = new double[nodeIdMap.size()];

		int counter = 0;
		for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap.entrySet()) {
			SocialNode srcNode = entry.getValue();
			OverlayNode srcOverlayNode = srcNode.getOverlayNode();
			Map<Integer, SocialNode> neighbors = srcNode.getNeighbors();
			double[] tmpHops = new double[neighbors.size()];

			int tmpCounter = 0;
			for (Map.Entry<Integer, SocialNode> neighborEntry : neighbors
					.entrySet()) {
				int nHops = calculateHops(srcOverlayNode, neighborEntry
						.getValue().getOverlayNode());

				tmpHops[tmpCounter] = nHops;
				tmpCounter++;
			}
			Double mean = getMean(tmpHops);
			double std = getStandardDeviation(tmpHops, mean);

			hops[counter] = mean;
			stdHops[counter] = std;
			counter++;
		}

		fm.writeHopsToFile(hops, stdHops, version, policy, modified,
				NUM_OF_LONG_RANGE_LINKS, socialNodeIdMap);
	}

	private int calculateHops(OverlayNode srcOverlayNode,
			OverlayNode targetOverlayNode) {
		int srcIndex = ringIDMap.get(srcOverlayNode.getNodeID());
		int targetIndex = ringIDMap.get(targetOverlayNode.getNodeID());
		double srcNodeId = srcOverlayNode.getNodeID();

		OverlayNode srcTmpNode = nodeIdMap.get(srcNodeId);
		List<Double> shortRangeLinks = srcTmpNode.getShortRangeLinks();
		List<Double> longRangeLinks = srcTmpNode.getLongRangeLinks();
		Map<Double, Double> secondHopMapping = srcTmpNode.getSecondHopMapping();
		int nHops = 0;
		if (shortRangeLinks.contains(targetOverlayNode.getNodeID())
				|| longRangeLinks.contains(targetOverlayNode.getNodeID())) {
			return 1;
		} else if (secondHopMapping.containsKey(targetOverlayNode.getNodeID())) {
			return 2;
		} else {

			while (!shortRangeLinks.contains(targetOverlayNode.getNodeID())
					&& !longRangeLinks.contains(targetOverlayNode.getNodeID())) {
				int minDistance = Integer.MAX_VALUE;
				double minNodeID = Double.MAX_VALUE;
				for (int i = 0; i < shortRangeLinks.size(); i++) {
					int candidateIndex = ringIDMap.get(shortRangeLinks.get(i));
					int distance = Math.abs(targetIndex - candidateIndex);
					distance = Math.min(distance, ring.size() - distance);
					if (minDistance > distance) {
						minDistance = distance;
						minNodeID = shortRangeLinks.get(i);
					}
				}

				for (int i = 0; i < longRangeLinks.size(); i++) {
					int candidateIndex = ringIDMap.get(longRangeLinks.get(i));
					int distance = Math.abs(targetIndex - candidateIndex);
					if (minDistance > distance) {
						minDistance = distance;
						minNodeID = longRangeLinks.get(i);
					}
				}
				srcTmpNode = nodeIdMap.get(minNodeID);
				shortRangeLinks = srcTmpNode.getShortRangeLinks();
				longRangeLinks = srcTmpNode.getLongRangeLinks();
				nHops++;
			}
		}
		return ++nHops;
	}

	public void calculateHopsStats(int version, boolean modified) {
		for (int links = 5; links <= 60; links += 5) {
			NUM_OF_LONG_RANGE_LINKS = links;
			System.out.println("NUMBER OF LINKS " + NUM_OF_LONG_RANGE_LINKS);
			System.out.println("Create Fingers");
			System.out.println("First Policy");
			createFigers();

			fm.extractDegree(links, 0, version, modified, socialNodeIdMap);
			System.out.println("Calculate hops");

			extractHopsStats(version, 1, modified);
			fm.extractLinksNetworkCoverage(version, 0, modified, links,
					nodeIdMap, ringIDMap);

		}

	}
	//
	// public void modifyNodeIDs(int version) {
	// int numOfIterations = 0;
	// List<Integer> numOfNodesModified = new ArrayList<>();
	// while (!allNodesAreHappy()) {
	// System.out.println("Iteration Num " + numOfIterations);
	// numOfIterations++;
	// numOfNodesModified.add(reassignNodeIDs());
	// }
	//
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// System.out.println("Convergence achieved in " + numOfIterations
	// + " iterations");
	// writeIterationStatsToFile(version, numOfNodesModified);
	//
	// System.out.println("Sort Node IDs");
	// sortNodeIDs();
	// }
	//
	// private void writeIterationStatsToFile(int version,
	// List<Integer> numOfNodesModified) {
	// try {
	//
	// File file = new File(path + "NumOfNodesModified_" + "_" + version
	// + ".csv");
	//
	// // if file doesnt exists, then create it
	// if (!file.exists()) {
	// file.createNewFile();
	// }
	//
	// FileWriter fw = new FileWriter(file.getAbsoluteFile());
	// BufferedWriter bw = new BufferedWriter(fw);
	// String line = "";
	// for (int i = 0; i < numOfNodesModified.size(); i++) {
	// line += numOfNodesModified.get(i) + ",";
	// }
	// line = line.substring(0, line.length() - 1);
	// bw.write(line);
	// bw.newLine();
	// bw.close();
	//
	// System.out.println("Done");
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private int reassignNodeIDs() {
	//
	// int numOfNodesModified = 0;
	// for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap.entrySet()) {
	// OverlayNode srcOverlayNode = entry.getValue().getOverlayNode();
	// SocialNode srcSocialNode = entry.getValue();
	// Double bestCandidateOverlayNodeID = srcSocialNode
	// .getMaximumNeighborId();
	//
	// if (Math.abs(srcOverlayNode.getNodeID()
	// - bestCandidateOverlayNodeID) > SATISFACTION_THRESHOLD) {
	// if ((1 - Math.abs(srcOverlayNode.getNodeID()
	// - bestCandidateOverlayNodeID)) > SATISFACTION_THRESHOLD) {
	// nodeIdMap.remove(srcOverlayNode.getNodeID());
	// Double newNodeId = Math.nextUp(bestCandidateOverlayNodeID);
	// while (nodeIdMap.containsKey(newNodeId)) {
	// newNodeId = Math.nextUp(newNodeId);
	// }
	//
	// srcOverlayNode.setNodeID(newNodeId);
	// nodeIdMap.put(newNodeId, srcOverlayNode);
	// numOfNodesModified++;
	//
	// }
	// }
	// }
	// System.out.println("Number of nodes modified in this iteration is : "
	// + numOfNodesModified);
	// return numOfNodesModified;
	// }
	//
	// private boolean allNodesAreHappy() {
	// for (Map.Entry<Double, OverlayNode> entry : nodeIdMap.entrySet()) {
	// Double srcOverlayNodeID = entry.getKey();
	// SocialNode srcSocialNode = entry.getValue().getSocialNode();
	//
	// Double bestCandidateOverlayNodeID = srcSocialNode
	// .getMaximumNeighborId();
	//
	// if (Math.abs(srcOverlayNodeID - bestCandidateOverlayNodeID) >
	// SATISFACTION_THRESHOLD) {
	// if ((1 - Math
	// .abs(srcOverlayNodeID - bestCandidateOverlayNodeID)) >
	// SATISFACTION_THRESHOLD)
	// return false;
	// }
	// }
	// return true;
	// }

}
