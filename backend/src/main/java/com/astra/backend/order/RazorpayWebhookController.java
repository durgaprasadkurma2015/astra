package com.astra.backend.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/razorpay")
public class RazorpayWebhookController {
    private final RazorpayWebhookService webhookService;

    public RazorpayWebhookController(RazorpayWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(name = "X-Razorpay-Signature", required = false)
            String signature) {
        webhookService.accept(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
