import json
import math
from geographiclib.geodesic import Geodesic


def get_geometry():
    offset_array = []
    prevPt = {}
    with open("sampledata.json") as file:
        data = json.load(file)
        # get dataframes[0].regions
        regions = data["tim"]["dataframes"][0]["regions"]

        # iterate regions and get path
        for item in regions:
            #get anchor
            prevPt = item["anchorPosition"]
            # get path.nodes
            nodes = item["path"]["nodes"]
            for node in nodes:
                # get latitude and longitude offsets
                lat_off = node["nodeLat"]
                lon_off = node["nodeLong"]
                node = {
                    "latitude": prevPt["latitude"] + lat_off,
                    "longitude": prevPt["longitude"] + lon_off,
                }
                prevPt = node
                offset_array.append(node)
    return offset_array


def main():
    # Open sampledata.json and read in the JSON array
    offset_array = get_geometry()
    #[{'latitude': '0.123456789', 'longitude': '0.987654321'}]

    feat_collection = {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {},
                "geometry": {"type": "LineString", "coordinates": []},
            },
        ],
    }

    for item in offset_array:
        feat_collection["features"][0]["geometry"]["coordinates"].append(
            [item["longitude"], item["latitude"]]
        )

    with open("output.json", "w") as file:
        json.dump(feat_collection, file, indent=4)
    print("Done!")


if __name__ == "__main__":
    main()
