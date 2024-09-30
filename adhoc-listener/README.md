# Adhoc Listener
The `adhoc-listener` module is responsible for consuming messages representing changes to adhoc conditions for county roads and creating, updating or deleting those conditions represented by Traveler Information Messages (TIMs). The module listens for messages on a Kafka topic or PostgreSQL channel and processes them accordingly.

It should be noted that pg notifications are not used in the management of regular TIMs. The listener is only used for cascade TIMs which are specific to county roads.

## Listener Types
### Kafka
When LISTENER_TYPE is set to 'kafka', the listener will consume messages from the Kafka topic `adhoc_conditions_monitor.countyrds.county_roads_v1_h`.

### Listen Notify
When LISTENER_TYPE is set to 'listen-notify', the listener will listen for notifications on the adhoc_listener_channel channel using the PostgreSQL LISTEN/NOTIFY feature.

To send a notification, execute the following SQL statement:
```
notify adhoc_listener_channel, '{"before": null, "after": {"objectid": 1, "globalid": "1", "cr_id": 1, "authority_organization_id": 1, "county": "County", "type": "Type", "rd_no": "RD1", "name": "Name", "description": "Description", "point_from": "Point From", "point_to": "Point To", "active": 1, "closed": 0, "c2lhpv": 0, "ntt": 0, "info_last_updated_user": "User", "conditions_last_updated_date": "2021-01-01T00:00:00Z", "gdb_from_date": "2021-01-01T00:00:00Z", "gdb_to_date": "2021-01-01T00:00:00Z", "gdb_archive_old": 0, "shape": "POINT (0 0)"}}'
```

Ideally, a trigger should be created on the adhoc_conditions table to send a notification whenever a row is inserted, updated or deleted.

## Message Format
Regardless of the listener type, the message format is the same. The message must be a JSON object with the following fields:
- `before`: The previous state of the adhoc condition. If the adhoc condition is new, this field will be null.
- `after`: The new state of the adhoc condition. If the adhoc condition is being deleted, this field will be null.

Both elements will be JSON objects with the following fields:

| Field | Type | Description |
| --- | --- | --- |
| objectid | integer | The unique identifier of the adhoc condition |
| globalid | character varying(38) | The global identifier of the adhoc condition |
| cr_id | integer | The unique identifier of the county road |
| authority_organization_id | integer | The unique identifier of the authority organization |
| county | character varying(16) | The name of the county |
| type | character varying(8) | The type of the adhoc condition |
| rd_no | character varying(8) | The road number of the county road |
| name | character varying(64) | The name of the county road |
| description | character varying(255) | The description of the adhoc condition |
| point_from | character varying(64) | The starting point of the adhoc condition |
| point_to | character varying(64) | The ending point of the adhoc condition |
| active | smallint | The active status of the road condition |
| closed | smallint | The closed status of the adhoc condition |
| c2lhpv | smallint | The c2lhpv status of the adhoc condition |
| ntt | smallint | The ntt status of the adhoc condition |
| info_last_updated_user | character varying(255) | The user who last updated the adhoc condition |
| conditions_last_updated_date | timestamp without time zone | The date the adhoc condition was last updated |
| gdb_from_date | timestamp without time zone | The start date of the adhoc condition |
| gdb_to_date | timestamp without time zone | The end date of the adhoc condition |
| gdb_archive_old | smallint | The archive status of the adhoc condition |
| shape | geometry | The shape of the adhoc condition |

Example:
```
{"before": null, "after": {"objectid": 1, "globalid": "1", "cr_id": 1, "authority_organization_id": 1, "county": "County", "type": "Type", "rd_no": "RD1", "name": "Name", "description": "Description", "point_from": "Point From", "point_to": "Point To", "active": 1, "closed": 0, "c2lhpv": 0, "ntt": 0, "info_last_updated_user": "User", "conditions_last_updated_date": "2021-01-01T00:00:00Z", "gdb_from_date": "2021-01-01T00:00:00Z", "gdb_to_date": "2021-01-01T00:00:00Z", "gdb_archive_old": 0, "shape": "POINT (0 0)"}}
```

Since 'before' is null, this message represents the creation of a new adhoc condition. The 'after' object contains the new state of the adhoc condition.