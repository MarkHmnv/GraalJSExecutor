package com.markhmnv.graaljsexecutor.model.response;

import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ScriptFullInfo {
    private long id;
    private String output;
    private long executionTime;
    private String body;
    private ScriptStatus status;
    private LocalDateTime executeAt;
}
