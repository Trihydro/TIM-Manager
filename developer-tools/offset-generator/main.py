import json
import math
from geographiclib.geodesic import Geodesic

RE = 6371000.0  # Earth radius for distance calculations

def offset_distance(currNode, offNode, endNode):
    bse = bearing_to(currNode, endNode)  # bearing start to end node
    bso = bearing_to(currNode, offNode)  # bearing start to offset node
    dso = angular_distance_to(currNode, offNode)  # angular distance start to offset node

    dXt = math.asin(math.sin(dso) * math.sin(bso - bse)) * RE

    return dXt

def bearing_to(currNode, nextNode):
    brng = Geodesic.WGS84.Inverse(currNode["latitude"], currNode["longitude"], nextNode["latitude"], nextNode["longitude"])['azi1']
    return brng

def angular_distance_to(currNode, nextNode):
    lat1 = math.radians(currNode["latitude"])
    lat2 = math.radians(nextNode["latitude"])
    dlat = lat2 - lat1

    lon1 = math.radians(currNode["longitude"])
    lon2 = math.radians(nextNode["longitude"])
    dlon = lon2 - lon1

    a = (math.sin(dlat / 2.0) * math.sin(dlat / 2.0)) + (
            math.cos(lat1) * math.cos(lat2) * math.sin(dlon / 2.0) * math.sin(dlon / 2.0))
    c = 2.0 * (math.atan2(math.sqrt(a), math.sqrt(1.0 - a)))

    return c

def apply_milepost_reduction_algorithm(mileposts, distance):
    if mileposts is None or len(mileposts) <= 3:  # min 3 to iterate over
        return mileposts

    reduced_path = []
    cn = 0
    on = 1
    nn = 2
    maxn = len(mileposts) - 1
    reduced_path.append(mileposts[cn])

    # step through the full path
    # save the nodes that constitute the minimum path length
    while True:
        current_node = mileposts[cn]
        off_node = mileposts[on]
        next_node = mileposts[nn]

        dXt = offset_distance(current_node, off_node, next_node)

        if abs(dXt) <= distance:
            on += 1
            if on == nn:
                nn += 1
                on = cn + 1
        else:
            cn = nn - 1
            reduced_path.append(mileposts[cn])
            on = cn + 1
            nn = cn + 2

        if nn > maxn:
            cn = nn - 1
            reduced_path.append(mileposts[cn])
            break  # quit stepping down path we have reached the end

    return reduced_path


def calculate_offset_path(anchor):
    offset_array = []
    prevPt = anchor
    with open("sampledata.json") as file:
        data = json.load(file)
        lane_width = 50
        distance=lane_width*0.2
        reduced_path = apply_milepost_reduction_algorithm(data, distance=distance)

        print("Data size:", len(data))
        print("Reduced path size:", len(reduced_path))

        for item in reduced_path:
            node = {}
            lat = '{:.20f}'.format(float(item["latitude"]) - float(prevPt["latitude"]))
            lon = '{:.20f}'.format(float(item["longitude"]) - float(prevPt["longitude"]))
            node["latitude"] = lat
            node["longitude"] = lon
            offset_array.append(node)
            prevPt = item
    return offset_array


def main():
    # Open sampledata.json and read in the JSON array
    anchor = {"longitude": -107.98775319, "latitude": 44.79758293}
    offset_array = calculate_offset_path(anchor)
    #[{'latitude': '0.123456789', 'longitude': '0.987654321'}]

    feat_collection = {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {"description": "Anchor Point"},
                "geometry": {
                    "coordinates": [anchor["longitude"], anchor["latitude"]],
                    "type": "Point",
                },
            },
            {
                "type": "Feature",
                "geometry": {"type": "LineString", "coordinates": []},
            },
        ],
    }

    prevPt = anchor
    for item in offset_array:
        new_longitude = '{:.5f}'.format(float(prevPt["longitude"]) + float(item["longitude"]))
        new_latitude = '{:.5f}'.format(float(prevPt["latitude"]) + float(item["latitude"]))
        feat_collection["features"][1]["geometry"]["coordinates"].append(
            [new_longitude, new_latitude]
        )
        prevPt = {
            "longitude": new_longitude,
            "latitude": new_latitude,
        }

    with open("output.json", "w") as file:
        json.dump(feat_collection, file, indent=4)
    print("Done!")


if __name__ == "__main__":
    main()
