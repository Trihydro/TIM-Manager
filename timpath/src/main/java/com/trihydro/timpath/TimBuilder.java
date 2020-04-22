package com.trihydro.timpath;

import java.lang.Double;
import java.lang.Math;
import java.util.ArrayList;

public class TimBuilder {

	private ArrayList<Pathnode> fullpath;
	private ArrayList<Pathnode> reducedpath;

	public TimBuilder() {

		fullpath = new ArrayList<Pathnode>();
		reducedpath = new ArrayList<Pathnode>();
	}

	public void addPathnode(Pathnode n) {
		fullpath.add(n);
	}

	private void savePathnode(Pathnode n) {
		Pathnode nodeCopy = new Pathnode(n);
		reducedpath.add(nodeCopy);
	}


	public void printFullPath() {

		int max = fullpath.size();
		int i = 0;
		double bearing = 0.0;
		double dXt = 0.0;
		
		for(Pathnode n : fullpath) {
			i = i + 1;
			if (i < max) {
				bearing = n.bearingTo(fullpath.get(i));
			} else {
				bearing = 0.0;
			}

			if (i + 1 < max) {
				dXt = n.offsetDistance(fullpath.get(i), fullpath.get(i + 1));
			} else {
				dXt = 0.0;
			}
			System.out.println(n.getIndex() + " lat: " + n.getLat() + "  lon: " + n.getLon() + "  bearing: " + bearing + "  cross track dist: " + dXt);
		}
			
	}

	

	public void printReducedPath() {

		int max = reducedpath.size();
		int i = 0;
		double bearing = 0.0;
		double dXt = 0.0;
		
		for(Pathnode n : reducedpath) {
			i = i + 1;
			if (i < max) {
				bearing = n.bearingTo(reducedpath.get(i));
			} else {
				bearing = 0.0;
			}

			if (i + 1 < max) {
				dXt = n.offsetDistance(reducedpath.get(i), reducedpath.get(i + 1));
			} else {
				dXt = 0.0;
			}
			System.out.println(n.getIndex() + " lat: " + n.getLat() + "  lon: " + n.getLon() + "  bearing: " + bearing + "  cross track dist: " + dXt);
		}
			
	}




	/**
	 *
	 *
	 * @param limitDistance maximum allowed offset distance for intermediate path nodes
	 */
	public void generatePath(double limitDistance) {

		int cn = 0;
		int on = 1;
		int nn = 2;
		int maxn = fullpath.size() - 1;

		savePathnode(fullpath.get(cn));

		// step through the full path 
		// save the nodes that constitute the minimum path length
		while(true){

			Pathnode currentNode = fullpath.get(cn);
			Pathnode offNode = fullpath.get(on);
			Pathnode nextNode = fullpath.get(nn);

			double dXt = currentNode.offsetDistance(offNode, nextNode);

			if (Math.abs(dXt) <= limitDistance) {
				on = on + 1;
				if (on == nn) {
					nn = nn + 1;
					on = cn + 1;
				}	

			} else {
				cn = nn - 1;
				savePathnode(fullpath.get(cn));
				on = cn + 1;
				nn = cn + 2;
			}

			if (nn > maxn) {
				cn = nn - 1;
				savePathnode(fullpath.get(cn));
				break;  // quit stepping down path we have reached the end
			}

		}

	}

}