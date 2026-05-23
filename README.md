# OOP Error Explainer

A Spring Boot service that turns a Java compiler or runtime error into a beginner-friendly explanation. Built for SOFTENG 754 Assignment 6, Group 17.

## Run locally

1. Copy `.env.example` to `.env` and set your Anthropic API key.
2. From the repo root: `mvn spring-boot:run`
3. Open `http://localhost:8080/` and click any error button.

## HTTP API

`POST /api/explain-error` with `{ "sourceCode", "errorOutput" }` returns:

```json
{
  "errorCode": "JAVA_NULL_POINTER_EXCEPTION",
  "errorType": "NullPointerException",
  "lineNumber": 4,
  "plainLanguageExplanation": "...",
  "suggestedFix": "..."
}
```

Possible `errorCode` values: `JAVA_NULL_POINTER_EXCEPTION`, `JAVA_ARRAY_INDEX_OUT_OF_BOUNDS`, `JAVA_CANNOT_FIND_SYMBOL`, `JAVA_INCOMPATIBLE_TYPES`, `JAVA_SYNTAX_ERROR`, `JAVA_UNKNOWN_ERROR`.

Six pre-captured Java error fixtures live under `src/main/resources/static/errors/` for demo and performance testing.
