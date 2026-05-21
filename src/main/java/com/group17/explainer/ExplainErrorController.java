package com.group17.explainer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ExplainErrorController {

    @PostMapping("/api/explain-error")
    public ExplainResponse explain(@RequestBody ExplainRequest req) {
        if (req == null
                || req.sourceCode() == null || req.sourceCode().isBlank()
                || req.errorOutput() == null || req.errorOutput().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sourceCode and errorOutput are both required and must be non-blank.");
        }

        // Stub response — replaced in a follow-up commit with a real LLM call.
        return new ExplainResponse(
                "Unknown",
                0,
                "Stub explanation — the LLM call has not been wired yet.",
                "Wire the LLM integration in ExplainErrorController.explain()."
        );
    }
}
