/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import java.util.Map;

import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.cloud.spring.genai.ChatModel;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demoapp/api/genai/")
@Tag(name = "genai chat APIs")
public class ChatModelController {
    private final ChatModel chatModel;

    public ChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostMapping(value = "chat")
    ResponseEntity<?> chat(@Parameter(required = true, example = "chat prompt") @RequestParam String prompt) {
        ChatResponse chat = chatModel.chat(prompt);
        return ResponseEntity.ok().body(Map.of(
                "chat", chatModel.extractText(chat),
                "opcRequestId", chat.getOpcRequestId()
        ));
    }
}

