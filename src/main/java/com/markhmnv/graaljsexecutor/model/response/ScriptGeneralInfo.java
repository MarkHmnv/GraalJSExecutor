package com.markhmnv.graaljsexecutor.model.response;

import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ScriptGeneralInfo {
    private long id;
    private String output;
    private ScriptStatus status;
}
