package com.markhmnv.graaljsexecutor;

import com.markhmnv.graaljsexecutor.controller.ScriptController;
import com.markhmnv.graaljsexecutor.exception.IllegalDeletionException;
import com.markhmnv.graaljsexecutor.exception.ScriptExecutionStopException;
import com.markhmnv.graaljsexecutor.exception.ScriptNotFoundException;
import com.markhmnv.graaljsexecutor.model.entity.Script;
import com.markhmnv.graaljsexecutor.model.response.ScriptFullInfo;
import com.markhmnv.graaljsexecutor.model.response.ScriptGeneralInfo;
import com.markhmnv.graaljsexecutor.service.ScriptService;
import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ScriptController.class)
public class ScriptControllerTest {
    @MockBean
    private ScriptService scriptService;
    @Autowired
    MockMvc mockMvc;

    private Script script;
    private ScriptFullInfo scriptFullInfo;
    private List<ScriptGeneralInfo> scriptGeneralInfos;

    @BeforeEach
    public void setUp(){
        script = Script.builder().id(1L).status(ScriptStatus.COMPLETED).body("console.log('Hello world');").output("Hello world\n").build();
        scriptFullInfo = ScriptFullInfo.builder().id(script.getId()).status(script.getStatus()).body(script.getBody()).output(script.getOutput()).build();
        Script script2 = Script.builder().id(2L).status(ScriptStatus.COMPLETED).body("console.log('World');").output("World\n").build();
        ScriptGeneralInfo scriptGeneralInfo1 = ScriptGeneralInfo.builder().id(script.getId()).status(script.getStatus()).output(script.getOutput()).build();
        ScriptGeneralInfo scriptGeneralInfo2 = ScriptGeneralInfo.builder().id(script2.getId()).status(script2.getStatus()).output(script2.getOutput()).build();
        scriptGeneralInfos = Arrays.asList(scriptGeneralInfo1, scriptGeneralInfo2);
    }

    @Test
    public void testGetAllScripts() throws Exception {
        given(scriptService.getScripts(null, "id"))
                .willReturn(scriptGeneralInfos);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/scripts")
                        .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].status").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].output").exists());
    }

    @Test
    void testGetScriptsByStatus() throws Exception {
        given(scriptService.getScripts(ScriptStatus.COMPLETED, "id"))
                .willReturn(scriptGeneralInfos);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/scripts")
                        .param("status", ScriptStatus.COMPLETED.name())
                        .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].status").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].output").exists());
    }

    @Test
    public void testGetScript() throws Exception {
        given(scriptService.getScript(script.getId()))
                .willReturn(scriptFullInfo);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/scripts/{id}", script.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(script.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(script.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(script.getBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.output").value(script.getOutput()));
    }

    @Test
    public void testGetScriptNotFound() throws Exception {
        given(scriptService.getScript(anyLong())).willThrow(new ScriptNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/scripts/{id}", anyLong())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEvaluateScript() throws Exception {
        String scriptRequest = "console.log('Hello world');";
        given(scriptService.evaluateScript(scriptRequest, null, null)).willReturn(scriptFullInfo);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/scripts/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scriptRequest));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(script.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(script.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(script.getBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.output").value(script.getOutput()));
    }

    @Test
    public void testEvaluateScriptWithScheduling() throws Exception {
        String scriptRequest = "console.log('Hello world');";
        LocalDateTime executeAt = LocalDateTime.now().plusHours(1);
        given(scriptService.evaluateScript(scriptRequest, executeAt, null))
                .willReturn(scriptFullInfo);

        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/scripts/evaluate")
                        .param("executeAt", executeAt.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scriptRequest)
        );

        response.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(script.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(script.getStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(script.getBody()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.output").value(script.getOutput()));
    }

    @Test
    public void testEvaluateScriptWithMissingBody() throws Exception {
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/scripts/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));

        response.andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteScript() throws Exception {
        doNothing().when(scriptService).deleteScript(script.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/scripts/{id}", script.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(scriptService, times(1)).deleteScript(script.getId());
    }

    @Test
    public void testDeleteScriptWithExecutingStatus() throws Exception {
        script.setStatus(ScriptStatus.EXECUTING);
        doThrow(IllegalDeletionException.class).when(scriptService).deleteScript(script.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/scripts/{id}", script.getId())
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isConflict());
    }

    @Test
    public void testStopRunningScript() throws Exception {
        doNothing().when(scriptService).stopScript(script.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/scripts/{id}/cancel", script.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(scriptService, times(1)).stopScript(script.getId());
    }

    @Test
    public void testStopNonRunningScript() throws Exception {
        doThrow(ScriptExecutionStopException.class).when(scriptService).stopScript(script.getId());

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/scripts/{id}/cancel", script.getId())
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest());
    }
}
