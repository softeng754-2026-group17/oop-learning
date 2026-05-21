package com.group17.explainer;

public record ExplainResponse(
        String errorType,
        int lineNumber,
        String plainExplanation,
        String suggestion
) {}
