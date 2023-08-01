package com.markhmnv.graaljsexecutor.controller;

import com.markhmnv.graaljsexecutor.model.response.ScriptFullInfo;
import com.markhmnv.graaljsexecutor.model.response.ScriptGeneralInfo;
import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import com.markhmnv.graaljsexecutor.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scripts")
@RequiredArgsConstructor
@Tag(name = "Script", description = "Script APIs documentation")
public class ScriptController {
    private final ScriptService scriptService;

    @GetMapping("/script")
    public String getScript(){
        return "script tesedfwfwt ";
    }

    @Operation(summary = "Get a list of available scripts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))),
    })
    @GetMapping
    public List<ScriptGeneralInfo> getScripts(@RequestParam(required = false) ScriptStatus status,
                                              @RequestParam(defaultValue = "id") String sortBy){
        return scriptService.getScripts(status, sortBy);
    }

    @Operation(summary = "Get a script by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the script",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScriptFullInfo.class))),
            @ApiResponse(responseCode = "404", description = "Script with the specified id does not exist",
                    content = @Content),
    })
    @GetMapping("/{id}")
    public ScriptFullInfo getScript(@PathVariable Long id){
        return scriptService.getScript(id);
    }

    @Operation(summary = "Get a script by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the script",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScriptFullInfo.class))),
            @ApiResponse(responseCode = "404", description = "Script with the specified id does not exist",
                    content = @Content),
    })
    @PostMapping("/evaluate")
    public ScriptFullInfo evaluateJavascript(@RequestBody String scriptRequest,
                                             @RequestParam(required = false) LocalDateTime executeAt,
                                             @RequestParam(required = false) String timezone){
        return scriptService.evaluateScript(scriptRequest, executeAt, timezone);
    }

    @Operation(summary = "Delete a script by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the script"),
            @ApiResponse(responseCode = "404", description = "Script with the specified id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Cannot delete an executing script",
                    content = @Content),
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScript(@PathVariable Long id){
        scriptService.deleteScript(id);
    }

    @Operation(summary = "Stop executing or scheduled script by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully stopped the script"),
            @ApiResponse(responseCode = "404", description = "Script with the specified id does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Script is not currently executing",
                    content = @Content),
    })
    @PostMapping("/{id}/cancel")
    public void cancelScript(@PathVariable Long id) {
        scriptService.stopScript(id);
    }
}
