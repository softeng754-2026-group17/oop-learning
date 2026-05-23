# Development

Everything a teammate needs to spin this up locally, verify it works, and find the right place for Task 2/3 artifacts.

## Prerequisites

| Tool | Version | Check |
|---|---|---|
| Java | 21 | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Anthropic API key | any | Sign in at <https://console.anthropic.com/> and create one under *Settings → API Keys* |

## First-time setup

```bash
# 1. From the repo root, copy the env template and paste your key in.
cp .env.example .env
# Open .env and replace sk-ant-... with your real key.

# 2. Verify everything compiles.
mvn clean package -DskipTests
```

The `.env` file is gitignored — it never reaches the repo. The committed template is `.env.example`.

## Run the app

```bash
mvn spring-boot:run
```

You should see Spring Boot start on port 8080 and finish with a line like:

```
Started ExplainerApplication in 2.4 seconds
```

Stop with **Ctrl+C**.

## Verify it works

### Via the UI

Open <http://localhost:8080/> in a browser. Click any of the six error buttons; after ~2–4 s you should see three panels: the Java source, the captured compiler/runtime output, and the LLM explanation.

### Via curl

```bash
curl -s -X POST http://localhost:8080/api/explain-error \
  -H "Content-Type: application/json" \
  -d '{"sourceCode":"String s = null; s.length();",
       "errorOutput":"Exception in thread \"main\" java.lang.NullPointerException"}' \
  | jq
```

You should get back a JSON object with `errorType: "NullPointerException"` and a populated `plainExplanation`.

### Malformed-input sanity check

```bash
curl -s -w "\n[HTTP %{http_code}]\n" -X POST \
  -H "Content-Type: application/json" \
  -d '{}' http://localhost:8080/api/explain-error
```

Should return **HTTP 400** with a Spring error envelope (`error: "Bad Request"`).

## What lives where

| Path | Purpose |
|---|---|
| `src/main/java/com/group17/explainer/` | Backend (controller, DTOs, `ExplainerApplication`) |
| `src/main/resources/application.properties` | Port, model, env-import |
| `src/main/resources/prompt.txt` | System prompt for the LLM — edit and restart to iterate |
| `src/main/resources/static/` | Daniel's UI (HTML/CSS/JS) and the six fixture pairs |
| `src/main/resources/static/errors/manifest.json` | Lists fixtures the UI exposes as buttons |
| `src/test/resources/performancetest/` | **Task 3 artifacts go here**: `.jmx`, result `.csv`, screenshots |
| `src/test/resources/performancetest/test-data.csv` | `name,expectedErrorType` rows for JMeter CSV Data Set Config |

## Model and cost

The model is `claude-haiku-4-5` (Anthropic's cheapest current Claude). A single explain-error request costs roughly **$0.001–$0.002**. A 1000-request load test costs ~$1–$2.

To switch models, edit `anthropic.model` in `application.properties`.

## Troubleshooting

**Port 8080 already in use** — stop whatever else is bound to it, or change `server.port` in `application.properties`.

**`Unauthorized` / `401` from Anthropic** — your key in `.env` is missing, malformed, or expired. Verify with:

```bash
grep ANTHROPIC_API_KEY .env
```

The value should start with `sk-ant-`.

**App starts but `/api/explain-error` returns 500** — usually means the LLM call is failing. Watch the console for stack traces; the most common causes are an invalid key, a network failure to `api.anthropic.com`, or rate limiting.

**JS shows "HTTP 404"** when you click a button — confirm the app is actually running on port 8080 (visit <http://localhost:8080/actuator> if Actuator is enabled, or just refresh the page).
