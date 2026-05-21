# Performance Test Artifacts

The A6 brief requires all performance-test artifacts to live under this directory. Drop the following here:

- `*.jmx` — Apache JMeter test plans
- `*.csv` — exported result files from JMeter listeners
- Screenshots of Summary Report / View Results Tree (PNG/JPG)

## Starter test data

`test-data.csv` maps each pre-captured Java error fixture to the `errorType` value the API is expected to return for that fixture. Use it with a JMeter **CSV Data Set Config** so each thread iteration:

1. Picks a row (`name`, `expectedErrorType`).
2. Fetches `http://localhost:8080/errors/${name}.java` and `http://localhost:8080/errors/${name}.error.txt` (HTTP Request samplers).
3. Composes the JSON body `{ "sourceCode": <java>, "errorOutput": <captured> }` and POSTs it to `http://localhost:8080/api/explain-error`.
4. Asserts:
   - Response Assertion: HTTP 200
   - Duration Assertion: response time < the threshold defined in the report
   - JSON Assertion: `$.errorType == ${expectedErrorType}`

The source `.java` and `.error.txt` files are served by Spring Boot from `src/main/resources/static/errors/` — same fixtures used by the demo UI.
