import http from "k6/http";
import { check, sleep } from "k6";

import requests from "./requests/create-update-rc-tim.js";

export let options = {
  vus: 5,
  iterations: 100,
  insecureSkipTLSVerify: true,
};

export default function () {
  sleep(Math.random()*0.4 + 0.1); // wait 100-500ms between requests
  const url = "http://localhost:7777/create-update-rc-tim";
  const headers = { "Content-Type": "application/json" };
  const reqBody = requests[Math.floor(Math.random() * requests.length)];
  let res = http.post(url, JSON.stringify(reqBody), { headers: headers });
  check(res, { "status was 200": (r) => r.status == 200 });
}
