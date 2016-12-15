package cs.ucy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileManager {
	public static String datasetFileName;
	public static String extractPath;
	private boolean datasetLoaded;
	private SocialNetworkManager sm;
	private static long size = 340000000;

	public FileManager() {
		datasetLoaded = false;
		sm = new SocialNetworkManager();
	}

	public SocialNetworkManager loadDataset() {
		BufferedReader br = null;

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(datasetFileName));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] splitLine = sCurrentLine.split(" ");
				int nodeId1 = Integer.parseInt(splitLine[0]);
				int nodeId2 = Integer.parseInt(splitLine[1]);
				if (nodeId1 != nodeId2) {
					SocialNode sn1 = getSocialNode(nodeId1);
					SocialNode sn2 = getSocialNode(nodeId2);
					constructFriendship(sn1, sn2);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		datasetLoaded = true;
		return sm;
	}

	public void writeFriendships(Map<Integer, SocialNode> socialNodeIdMap) {
		try {

			File file = new File(extractPath + "friendship.csv");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap
					.entrySet()) {
				String line = entry.getKey() + "-";
				for (Map.Entry<Integer, Integer> entryTriangles : entry.getValue().getTriangles().entrySet()) {
					line += entryTriangles.getKey() + "," + entryTriangles.getValue() + "-";
				}
				line = line.substring(0, line.length() - 1);
				bw.write(line);
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void loadFriendships(Map<Integer, SocialNode> socialNodeIdMap) {
		BufferedReader br = null;

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader("/Users/santaris/Desktop/testme/friendship.csv"));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] splitLine = sCurrentLine.split("-");
				int nodeId = Integer.parseInt(splitLine[0]);
				for(int i = 1; i < splitLine.length; i++) {
					String[] triangleSplit = splitLine[i].split(",");
					int targetId = Integer.parseInt(triangleSplit[0]);
					int triangles = Integer.parseInt(triangleSplit[1]);
					socialNodeIdMap.get(nodeId).addTriangle(targetId, triangles);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void constructFriendship(SocialNode sn1, SocialNode sn2) {
		sn1.addNeighbor(sn2);
		sn2.addNeighbor(sn1);
	}

	private SocialNode getSocialNode(int nodeId1) {
		SocialNode sn;
		if (!sm.nodeIdExist(nodeId1)) {
			sn = new SocialNode(nodeId1);
			sm.addSocialNode(sn);
		} else {
			sn = sm.getSocialNode(nodeId1);
		}
		return sn;
	}

	public void extractOverlayNodeIDs(int version, boolean modified,
			Map<Double, OverlayNode> nodeIdMap) {
		try {

			File file;
			if (!modified)
				file = new File(extractPath + "resultsNodeIDs" + version
						+ ".csv");
			else
				file = new File(extractPath + "resultsModifiedNodeIDs"
						+ version + ".csv");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Double, OverlayNode> entry : nodeIdMap.entrySet()) {
				bw.write(entry.getKey() + ","
						+ entry.getValue().getSocialNode().getNodeId());
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extractDistance(int version, boolean modified,
			Map<Integer, SocialNode> socialNodeIdMap) {

		try {

			File file;
			if (!modified)
				file = new File(extractPath + "distanceNodeIDs" + version
						+ ".csv");
			else
				file = new File(extractPath + "distanceModifiedNodeIDs"
						+ version + ".csv");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap
					.entrySet()) {
				SocialNode srcNode = entry.getValue();
				Map<Integer, SocialNode> neighborhood = srcNode.getNeighbors();
				double[] distance = new double[neighborhood.size()];
				int i = 0;
				for (Map.Entry<Integer, SocialNode> neighborEntry : neighborhood
						.entrySet()) {
					Double distance1 = Math.abs(srcNode.getOverlayNode()
							.getNodeID()
							- neighborEntry.getValue().getOverlayNode()
									.getNodeID());
					Double distance2 = 1 - distance1;
					if (distance1 > distance2) {
						distance[i++] = distance2;
					} else {
						distance[i++] = distance1;
					}
				}
				Double mean = OverlayManager.getMean(distance);
				Double std = OverlayManager
						.getStandardDeviation(distance, mean);
				bw.write(srcNode.getNodeId() + "," + mean + "," + std);
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeHopsToFile(double[] hops, double[] stdHops, int version,
			int policy, boolean modified, int nLongRangeLinks,
			Map<Integer, SocialNode> socialNodeIdMap) {
		try {

			File file;

			if (!modified) {
				file = new File(extractPath + "hopsNodeIDs_" + nLongRangeLinks
						+ "_" + policy + "_" + version + ".csv");
			} else {
				file = new File(extractPath + "hopsModifiedNodeIDs_"
						+ nLongRangeLinks + "_" + policy + "_" + version
						+ ".csv");
			}

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			int counter = 0;
			for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap
					.entrySet()) {
				bw.write(entry.getValue().getNodeId() + "," + hops[counter]
						+ "," + stdHops[counter]);
				counter++;
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extractLinksNetworkCoverage(int version, int policy,
			boolean modified, int nLongRangeLinks,
			Map<Double, OverlayNode> nodeIdMap, Map<Double, Integer> ringIDMap) {
		try {

			File file;
			if (modified) {
				file = new File(extractPath + "modifiedLinkCoverage_"
						+ nLongRangeLinks + "_" + policy + "_" + version
						+ ".csv");
			} else {
				file = new File(extractPath + "LinkCoverage_" + nLongRangeLinks
						+ "_" + policy + "_" + version + ".csv");
			}

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Double, OverlayNode> entry : nodeIdMap.entrySet()) {
				List<Double> longRangeLinks = entry.getValue()
						.getLongRangeLinks();
				String line = entry.getKey() + ",";
				for (int i = 0; i < longRangeLinks.size(); i++) {
					int index1 = ringIDMap.get(entry.getValue().getNodeID());
					int index2 = ringIDMap.get(nodeIdMap.get(
							longRangeLinks.get(i)).getNodeID());
					int nNodes = Math.abs(index1 - index2);
					nNodes = Math.min(nNodes, ringIDMap.size() - nNodes);
					line += nNodes + ",";
				}
				line = line.substring(0, line.length() - 1);
				bw.write(line);
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extractDegree(int nLinks, int policy, int version,
			boolean modified, Map<Integer, SocialNode> socialNodeIdMap) {
		try {

			File file;
			if (modified) {
				file = new File(extractPath + "modifiedIncomingDegree_"
						+ nLinks + "_" + policy + "_" + version + ".csv");
			} else {
				file = new File(extractPath + "IncomingDegree_" + nLinks + "_"
						+ policy + "_" + version + ".csv");
			}

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Integer, SocialNode> entry : socialNodeIdMap
					.entrySet()) {
				bw.write(entry.getKey()
						+ ","
						+ entry.getValue().getOverlayNode().getIncomingLinks()
								.size());
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calculateFriendsDistance(int version, boolean modified,
			Map<Double, OverlayNode> nodeIdMap,
			Map<Integer, SocialNode> socialNodeIdMap,
			Map<Double, Integer> ringIDMap) {
		try {

			File file;
			if (modified) {
				file = new File(extractPath + "modifiedSocialFriendsDistance_"
						+ version + ".csv");
			} else {
				file = new File(extractPath + "SocialFriendsDistance_"
						+ version + ".csv");
			}

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Double, OverlayNode> entry : nodeIdMap.entrySet()) {
				String line = entry.getKey() + ",";
				SocialNode socialNode = entry.getValue().getSocialNode();
				Map<Integer, Integer> triangles = socialNode.getTriangles();
				List<Map.Entry<Integer, Integer>> trianglesList = OverlayManager
						.orderTriangles(triangles);
				for (int i = 0; i < trianglesList.size(); i++) {
					OverlayNode neighborOverlayNode = socialNodeIdMap.get(
							trianglesList.get(i).getKey()).getOverlayNode();
					int index1 = ringIDMap.get(entry.getValue().getNodeID());
					int index2 = ringIDMap.get(neighborOverlayNode.getNodeID());
					int nNodes = Math.abs(index1 - index2);
					nNodes = Math.min(nNodes, ringIDMap.size() - nNodes);
					line += nNodes + ",";
				}
				line = line.substring(0, line.length() - 1);
				bw.write(line);
				bw.newLine();
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
