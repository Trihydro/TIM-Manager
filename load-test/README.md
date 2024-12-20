# Load Testing Resources
This folder contains assets to help us understand how the TIMM applications perform under load. These tests are executed locally using an open source load testing application, k6.
## How to use
1. Install k6 ([see here](https://k6.io/docs/getting-started/installation))
2. View k6 documentation for authoring new test scripts ([see here](https://k6.io/docs/getting-started/running-k6))
3. To run a test (in this case, `test_create-update-rc-tim`), run the following command:
   ```
   k6 run test_create-update-rc-tim.js
   ```
