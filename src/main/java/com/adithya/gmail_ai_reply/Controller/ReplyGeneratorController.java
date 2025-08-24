package com.adithya.gmail_ai_reply.Controller;

import com.adithya.gmail_ai_reply.DTO.EmailRequest;
import com.adithya.gmail_ai_reply.Service.ReplyGeneratorService;
import com.adithya.gmail_ai_reply.Utility.RateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


/**
 * REST Controller for handling email content
 */
@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ReplyGeneratorController {

    private final ReplyGeneratorService replyGeneratorService;
    private final RateLimiter rateLimiter;


    /**
     * @param emailRequest - email content
     * @param request - used to extract IP address
     * @return ResponseEntity with AI-generated reply or HttpStatus.TOO_MANY_REQUESTS
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest , HttpServletRequest request) {

        //check if user exceeds the no of requests
        String userIP = request.getRemoteAddr();

        int rateLimitStatus = rateLimiter.checkRequest(userIP);

        if (rateLimitStatus == 2) {
            return ResponseEntity.status(429).body("Minute limit exceeded");
        } else if (rateLimitStatus == 3) {
            return ResponseEntity.status(429).body("Daily limit exceeded");
        }

        String response = replyGeneratorService.generateReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}