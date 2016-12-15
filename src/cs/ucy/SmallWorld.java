package cs.ucy;

public class SmallWorld {

	public static void main(String[] args) {

		FileManager fm = new FileManager();
		fm.datasetFileName = "/Users/santaris/Dropbox/phd/dataset/snap/facebook/facebook_combined.txt";
		fm.extractPath = "/Users/santaris/Dropbox/phd/dataset/snap/facebook/";
//		for (int i = 0; i < 100; i++) {
			SocialNetworkManager sm = fm.loadDataset();
			//fm.loadFriendships(sm.getSocialNodes());
			System.out.println("Calculate Friendships");
			sm.calculateMutualFriendships();
			fm.writeFriendships(sm.getSocialNodes());
			
			
//			System.out.println("Construct overlay");
//			OverlayManager om = new OverlayManager(sm.getSocialNodes(), fm, i);
//			om.constructOverlay();
//			
//			System.out.println("Calculate hops");
//			om.calculateHopsStats(i, false);
//			
//			System.out.println("Modify nodeIDs");
//			om.modifyNodeIDs(i);
//			om.calculateFriendsDistance(i, true);
//
//			System.out.println("Extract stats");
//			om.extractOverlayNodeIDs(i, true);
//			om.extractDistance(i, true);
//			
//			System.out.println("Calculate hops");
//			om.calculateHopsStats(i, true);
//			
//			System.out.println("End of simulation " + i);
//			System.out.println("///////////////////////////");
//			System.out.println("///////////////////////////");
//			System.out.println("///////////////////////////");
//			System.out.println("///////////////////////////");
//			System.out.println("///////////////////////////");
//			System.out.println("///////////////////////////");
			
//		}
	}
}
