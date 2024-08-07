# County Roads Testing Documentation
This document provides information on how to test the functionality of the system involving cascading road conditions to county roads associated with trigger roads.

It should be noted that this document is written from the developer's perspective and the instructions involve using a mock version of the triggered county roads view.

## Table of Contents
- [Test Cases](#test-cases)
- [Testing](#testing)
  - [Preparation](#preparation)
  - [Execution & Verification](#execution--verification)
    - [Part 1: Cascade Condition](#part-1-cascade-condition)
    - [Part 2: Clear Condition](#part-2-clear-condition)
  - [Cleanup](#cleanup)

## Test Cases
The following table depicts the test cases that will be executed to verify the functionality of the system.

| Name | Involved Road Codes | Condition To Be Cascaded | Expected Result | Example County Road |
| ---- | ------------------- | --------------- | --------------- | ------------------- |
| TC1.1 | EVANI80WUTD, EVANI80WUTI | closed | County roads associated with the road codes should be closed. | Uinta CR 111 Airport Rd (cr_id 179) |
| TC1.2 | EVANI80WUTD, EVANI80WUTI | none | County roads associated with the road codes should have no conditions. | Uinta CR 111 Airport Rd (cr_id 179) |
| TC2.1 | CHEYU85NB | loct | County roads associated with the road code should be Local Traffic Only. | Laramie CR 138 Hutton Rd (cr_id 138) |
| TC2.2 | CHEYU85NB | none | County roads associated with the road code should have no conditions. | Laramie CR 138 Hutton Rd (cr_id 138) |
| TC3.1 | ARLI80WD, ARLI80WI | closed | County roads associated with the road codes should be closed. | Carbon CR 404 Pass Creek Rd (cr_id 474, 475, 476, 477) |
| TC3.2 | ARLI80WD, ARLI80WI | none | County roads associated with the road codes should have no conditions. | Carbon CR 404 Pass Creek Rd (cr_id 474, 475, 476, 477) |
| TC4.1 | LABUS189NB | c2lhpv | County roads associated with the road code should be Closed to Light High Profile Vehicles. | Sublette CR 198 Dry Piney Rd (cr_id 151) |
| TC4.2 | LABUS189NB | none | County roads associated with the road code should have no conditions. | Sublette CR 198 Dry Piney Rd (cr_id 151) |
| TC5.1 | RAWI80ELARI, RAWI80ELARD | loct | County roads associated with the road codes should be Local Traffic Only. | Carbon CR 500 (cr_id 453, 454) |
| TC5.2 | RAWI80ELARI, RAWI80ELARD | none | County roads associated with the road codes should have no conditions. | Carbon CR 500 (cr_id 453, 454) |
| TC6.1 | CHEYI80EPBD, CHEYI80EPBI | loct | County roads associated with the road codes should be Local Traffic Only. | Laramie CR 203 Carpenter South Rd (cr_id 868, 869) |
| TC6.2 | CHEYI80EPBD, CHEYI80EPBI | none | County roads associated with the road codes should have no conditions. | Laramie CR 203 Carpenter South Rd (cr_id 868, 869) |
| TC7.1 | EMI80EI, EMI80ED | closed | County roads associated with the road codes should be closed. | Carbon CR 215 Stock Driveway (cr_id 482, 483, 484) |
| TC7.2 | EMI80EI, EMI80ED | none | County roads associated with the road codes should have no conditions. | Carbon CR 215 Stock Driveway (cr_id 482, 483, 484) |
| TC8.1 | SARW70BMRSEAB | loct | County roads associated with the road code should be Local Traffic Only. | Carbon CR 500 (cr_id 453, 454) |
| TC8.2 | SARW70BMRSEAB | none | County roads associated with the road code should have no conditions. | Carbon CR 500 (cr_id 453, 454) |

## Testing
### Preparation
To prepare for testing, the following steps should be taken:

1. SSH into the Test VM.
1. Navigate to /home/wyocvadmin/wyocv.
1. Run `nano .env` to open the environment file.
1. Set `CONTROLLER_CONFIG_COUNTY_ROADS_TRIGGER_VIEW_NAME=mock_triggered_county_roads_v1_vw` in the environment file.
1. Save and exit the file.
1. Run `./clean-build-and-deploy.sh` to re-deploy the wyocv apps with the new configuration.
1. Run `docker compose logs -f ode_wrapper` to monitor the logs of the ODE Wrapper.
1. Open PgAdmin & Postman locally to monitor the database and send requests to the ODE Wrapper.
1. Open the `countyroads.postman_collection.json` collection provided in the `ode-wrapper/docs/countyroads` directory in Postman. This collection contains the requests that will be sent to the ODE Wrapper, so you can easily send them by importing the collection instead of creating the requests manually.
1. Open SDX Beta (https://sdxbeta.trihydro.com/) to monitor the TIMs on the map.

### Execution & Verification
For each test case, conditions will be cascaded to the county roads associated with the road codes. The conditions will then be cleared, and the TIMs will be removed from the county roads. The steps to execute and verify the test cases are as follows:

#### Part 1: Cascade Condition
1. Modify road conditions in the database to match the condition to be cascaded with the following query:
    ```sql
    update mock_triggered_county_roads_v1_vw set <condition>=1 where road_code='<road code>';
    ```
1. Send a POST request to the `/create-update-rc-tim` endpoint with the following payload:
    ```json
    {
        "timRcList": [
            {
                "direction": "B",
                "startPoint": {
                    "latitude": 41.361402, 
                    "longitude": -104.587991,
                    "valid": true
                },
                "endPoint": {
                    "latitude": 41.378278, 
                    "longitude": -104.535463,
                    "valid": true
                },
                "segment": "1",
                "route": "US 85",
                "roadCode": "<ROAD CODE>",
                "advisory": ["4885"],

                // not needed
                "clientId": "",
                "itisCodes": []
            }
        ]
    }
    ```

    Replace `<ROAD CODE>` with the road code for the test case.

    Alternatively, you can use the Postman collection to send the request by selecting the appropriate request and clicking the "Send" button.

1. Verify that active TIMs are created for the county roads with the condition to be cascaded.
    1. Check the database with the following query:
        ```sql
        select * from active_tim where client_id like '%<road code>_trgd_%';
        ```
    1. Check SDX Beta (https://sdxbeta.trihydro.com/) for the created TIMs. The TIMs should be displayed on the map.

#### Part 2: Clear Condition
1. Modify road conditions in the database so that no conditions are present with the following query:
    ```sql
    update mock_triggered_county_roads_v1_vw set <condition>=0 where road_code='<road code>';
    ```
1. Send a POST request to the `/submit-rc-clear` endpoint with the following payload:
    ```json
    {
        "timRcList": [
            {
                "direction": "B",
                "startPoint": {
                    "latitude": 41.361402, 
                    "longitude": -104.587991,
                    "valid": true
                },
                "endPoint": {
                    "latitude": 41.378278, 
                    "longitude": -104.535463,
                    "valid": true
                },
                "segment": "1",
                "route": "US 85",
                "roadCode": "<ROAD CODE>",
                "advisory": ["4885"],

                // not needed
                "clientId": "",
                "itisCodes": []
            }
        ]
    }
    ```

    Replace `<ROAD CODE>` with the road code for the test case.

    Alternatively, you can use the Postman collection to send the request by selecting the appropriate request and clicking the "Send" button.

1. Verify that the TIMs are cleared for the county roads.
    1. Check the database with the following query:
        ```sql
        select * from active_tim where client_id like '%<road code>_trgd_%';
        ```

        The previously created records should now have a non-null value in the `end_date` column and a value of '1' in the `marked_for_deletion` column.

    1. Check SDX Beta (https://sdxbeta.trihydro.com/) to see if the TIMs were cleared.
    
        The TIMs should be removed from the map. You may need to refresh the page to see the changes.

### Cleanup
After testing is complete, the system should be cleaned up by following these steps:

1. SSH into the Test VM.
1. Navigate to /home/wyocvadmin/wyocv.
1. Run `nano .env` to open the environment file.
1. Set `CONTROLLER_CONFIG_COUNTY_ROADS_TRIGGER_VIEW_NAME=countyrds.triggered_county_roads_v1_vw` in the environment file.
1. Save and exit the file.
1. Run `./clean-build-and-deploy.sh` to re-deploy the wyocv apps with the new configuration.
1. Run `docker compose logs -f ode_wrapper` to monitor the logs of the ODE Wrapper until the service is up and running.
