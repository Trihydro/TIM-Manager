package com.trihydro.library.helpers;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.springframework.stereotype.Component;

@Component
public class MilepostReduction {
    /**
     * Iterates through the list of Mileposts and creates a second list keeping only
     * the minimum set of nodes required to maintain a path geo-fence that fully
     * encloses the roadway.
     * 
     * An initial node is selected and a straight line path is projected to
     * downstream nodes one-by-one. All intermediate nodes between the initial node
     * and selected downstream node are checked to determine their offset distance
     * from the projected straight line path. If an intermediate node is offset too
     * far from the straight line path (i.e. greater than distance parameter) then
     * the selected downstream node is too far and the previous selected node is
     * picked as the next initial node as well as recording this previous selected
     * node in the minimum path list. This downstream walk and projection is
     * repeated from the newly selected initial node.
     * 
     * @param mileposts
     * @param distance
     * @return
     */
    public List<Milepost> applyMilepostReductionAlorithm(List<Milepost> mileposts, Double distance) {
        if (mileposts == null || mileposts.size() == 0) {
            return mileposts;
        }

        List<Milepost> reducedPath = new ArrayList<>();
        int cn = 0;
        int on = 1;
        int nn = 2;
        int maxn = mileposts.size() - 1;
        reducedPath.add(mileposts.get(cn));

        // step through the full path
        // save the nodes that constitute the minimum path length
        while (true) {

            Milepost currentNode = mileposts.get(cn);
            Milepost offNode = mileposts.get(on);
            Milepost nextNode = mileposts.get(nn);

            double dXt = currentNode.offsetDistance(offNode, nextNode);

            if (Math.abs(dXt) <= distance) {
                on = on + 1;
                if (on == nn) {
                    nn = nn + 1;
                    on = cn + 1;
                }

            } else {
                cn = nn - 1;
                reducedPath.add(mileposts.get(cn));
                on = cn + 1;
                nn = cn + 2;
            }

            if (nn > maxn) {
                cn = nn - 1;
                reducedPath.add(mileposts.get(cn));
                break; // quit stepping down path we have reached the end
            }

        }

        return reducedPath;
    }

}