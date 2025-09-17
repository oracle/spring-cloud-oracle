/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.cloud.spring.genai.EmbeddingModel;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demoapp/api/genai/")
@Tag(name = "genai chat APIs")
@ConditionalOnProperty(name = "spring.cloud.oci.genai.embedding.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddingModelController {
    private final EmbeddingModel embeddingModel;

    public EmbeddingModelController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @PostMapping(value = "embed")
    ResponseEntity<?> embed(@Parameter(required = true, example = "embedding input") @RequestParam String input) {
        EmbedTextResponse embedded = embeddingModel.embed(input);
        return ResponseEntity.ok().body(Map.of(
                "input", input,
                "embedding", embeddingModel.fromResponse(embedded),
                "opcRequestId", embedded.getOpcRequestId()
        ));
    }

    @PostMapping(value = "embedAll")
    ResponseEntity<?> embed(@Parameter(required = true, example = "embedding text") @RequestBody List<String> inputs) {
        List<EmbedTextResponse> embedded = embeddingModel.embedAll(inputs);
        List<Map<String, Object>> response = embedded.stream()
            .map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("inputs", r.getEmbedTextResult() != null ? r.getEmbedTextResult().getInputs() : Collections.emptyList());
                map.put("opcRequestId", r.getOpcRequestId() != null ? r.getOpcRequestId() : "N/A");
                map.put("embeddings", embeddingModel.fromResponse(r));
                return map;
            })
            .toList();
        return ResponseEntity.ok().body(response);
    }
}
