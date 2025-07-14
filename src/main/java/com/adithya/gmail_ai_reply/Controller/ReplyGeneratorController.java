package com.adithya.gmail_ai_reply.Controller;

import com.adithya.gmail_ai_reply.DTO.EmailRequest;
import com.adithya.gmail_ai_reply.Service.ReplyGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST Controller for handling email content
 */
@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ReplyGeneratorController {

    private final ReplyGeneratorService replyGeneratorService;

    /**
     * @param emailRequest - email content
     * @return ResponseEntity with AI-generated reply
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest) {
        String response = replyGeneratorService.generateReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}

