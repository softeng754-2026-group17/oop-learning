package com.group17.explainer;

public record ExplainResponse(
        String errorCode,
        String errorType,
        int lineNumber,
        String plainLanguageExplanation,
        String suggestedFix
) {}
