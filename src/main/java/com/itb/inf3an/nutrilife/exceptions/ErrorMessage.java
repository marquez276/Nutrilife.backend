package com.itb.inf3an.nutrilife.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorMessage {

    private LocalDateTime timestamp;
    private String[] messages;
    private HttpStatus title;
    private int status;

    public ErrorMessage(LocalDateTime timestamp, String[] messages, HttpStatus title) {
        this.timestamp = timestamp;
        this.messages = messages;
        this.title = title;
        this.status = title.value();
    }

    /** Campo único "message", que é o que o frontend lê. */
    public String getMessage() {
        return String.join("; ", messages);
    }
}
