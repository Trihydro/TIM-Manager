package com.trihydro.timpath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Double;

public class Timpath {

    public static void main(String[] args) {

        String tsvFile = args[0];
        double limit = Double.parseDouble(args[1]);

        
        String line = "";
        String tsvSplitBy = "\t";  // tab separator

        // create TimBuilder to produce TIM from configuration data
        TimBuilder tbuild = new TimBuilder();

        // populate TimBuilder with initial path
        try (BufferedReader br = new BufferedReader(new FileReader(tsvFile))) {

            int i = 0;

            while ((line = br.readLine()) != null) {

                // split line entries at separator
                String[] pathnode = line.split(tsvSplitBy);
                //System.out.println("Route: " + pathnode[0] + "  lat: " + pathnode[3] + "  lon: " + pathnode[4]);

                Pathnode pn = new Pathnode(i,
                                            pathnode[0], 
                							pathnode[1], 
                							pathnode[2], 
                							pathnode[3], 
                							pathnode[4], 
                							pathnode[5], 
                							pathnode[6]);


                tbuild.addPathnode(pn);
                i = i + 1;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // debug print the paths from the TimBuilder
        System.out.println("-------------------------------------------------------------------------");
        tbuild.printFullPath();

        tbuild.generatePath(limit);

        System.out.println("*************************************************************************");
        tbuild.printReducedPath();

    }

}

