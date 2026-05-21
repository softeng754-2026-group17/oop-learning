package com.group17.explainer;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolChoiceTool;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExplainErrorController {

    private static final String TOOL_NAME = "return_explanation";

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.model}")
    private String model;

    @Value("classpath:prompt.txt")
    private Resource promptResource;

    private String systemPrompt;
    private AnthropicClient client;
    private Tool tool;

    @PostConstruct
    public void init() throws IOException {
        this.systemPrompt = new String(
                promptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(30))
                .build();

        this.tool = Tool.builder()
                .name(TOOL_NAME)
                .description("Return a structured explanation of the Java error.")
                .inputSchema(Tool.InputSchema.builder()
                        .type(JsonValue.from("object"))
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("errorType",
                                        JsonValue.from(Map.of("type", "string")))
                                .putAdditionalProperty("lineNumber",
                                        JsonValue.from(Map.of("type", "integer")))
                                .putAdditionalProperty("plainExplanation",
                                        JsonValue.from(Map.of("type", "string")))
                                .putAdditionalProperty("suggestion",
                                        JsonValue.from(Map.of("type", "string")))
                                .build())
                        .required(List.of(
                                "errorType", "lineNumber", "plainExplanation", "suggestion"))
                        .build())
                .build();
    }

    @PostMapping("/api/explain-error")
    public ExplainResponse explain(@RequestBody ExplainRequest req) {
        if (req == null
                || req.sourceCode() == null || req.sourceCode().isBlank()
                || req.errorOutput() == null || req.errorOutput().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sourceCode and errorOutput are both required and must be non-blank.");
        }

        String userMessage = "Source code:\n```java\n" + req.sourceCode() + "\n```\n\n"
                + "Compiler/runtime output:\n```\n" + req.errorOutput() + "\n```";

        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024)
                .system(systemPrompt)
                .addUserMessage(userMessage)
                .addTool(tool)
                .toolChoice(ToolChoiceTool.builder().name(TOOL_NAME).build())
                .build();

        Message message = client.messages().create(params);

        for (ContentBlock block : message.content()) {
            Optional<ToolUseBlock> maybeToolUse = block.toolUse();
            if (maybeToolUse.isPresent() && TOOL_NAME.equals(maybeToolUse.get().name())) {
                Map<String, Object> input = maybeToolUse.get()._input()
                        .convert(new TypeReference<Map<String, Object>>() {});
                return new ExplainResponse(
                        String.valueOf(input.getOrDefault("errorType", "Unknown")),
                        ((Number) input.getOrDefault("lineNumber", 0)).intValue(),
                        String.valueOf(input.getOrDefault("plainExplanation", "")),
                        String.valueOf(input.getOrDefault("suggestion", ""))
                );
            }
        }

        throw new IllegalStateException(
                "Anthropic response did not include a " + TOOL_NAME + " tool call.");
    }
}
