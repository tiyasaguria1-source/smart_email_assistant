package com.emailAssistant.writer.controller;


import com.emailAssistant.writer.model.EmailRequest;
import com.emailAssistant.writer.service.EmailGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
//@CrossOrigin(origins = "*")
public class EmailGeneratorController {

    @Autowired
    private final EmailGeneratorService service;

    public EmailGeneratorController(EmailGeneratorService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generateEmail(@RequestBody EmailRequest req) {
        return service.generateEmailReply(req)
                .map(ResponseEntity::ok);
    }
}
