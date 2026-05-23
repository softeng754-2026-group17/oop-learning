# OOP Error Explainer

A Spring Boot service that turns a Java compiler or runtime error into a beginner-friendly explanation. Built for SOFTENG 754 Assignment 6, Group 17.

## Run locally

1. Copy `.env.example` to `.env` and set your Anthropic API key.
2. From the repo root: `mvn spring-boot:run`
3. Open `http://localhost:8080/` and click any error button.

## HTTP API

`POST /api/explain-error` with `{ "sourceCode", "errorOutput" }` returns `{ "errorCode", "errorType", "lineNumber", "plainLanguageExplanation", "suggestedFix" }`.

Six pre-captured Java error fixtures live under `src/main/resources/static/errors/` for demo and performance testing.
