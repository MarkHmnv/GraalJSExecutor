package com.markhmnv.graaljsexecutor;

import com.markhmnv.graaljsexecutor.exception.IllegalDeletionException;
import com.markhmnv.graaljsexecutor.exception.ScriptExecutionStopException;
import com.markhmnv.graaljsexecutor.exception.ScriptNotFoundException;
import com.markhmnv.graaljsexecutor.mapper.ScriptMapper;
import com.markhmnv.graaljsexecutor.model.entity.Script;
import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import com.markhmnv.graaljsexecutor.model.response.ScriptFullInfo;
import com.markhmnv.graaljsexecutor.model.response.ScriptGeneralInfo;
import com.markhmnv.graaljsexecutor.repository.ScriptRepository;
import com.markhmnv.graaljsexecutor.service.ScriptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScriptServiceTest {
    @Mock
    private ScriptRepository scriptRepository;
    @Mock
    private ScriptMapper scriptMapper;
    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private ScriptService scriptService;

    private Script script;
    private ScriptFullInfo scriptFullInfo;
    private List<Script> scripts;
    private List<ScriptGeneralInfo> scriptGeneralInfos;

    @BeforeEach
    public void setUp(){
        script = Script.builder().id(1L).status(ScriptStatus.COMPLETED).body("console.log('Hello world');").output("Hello world\n").build();
        scriptFullInfo = ScriptFullInfo.builder().id(script.getId()).status(script.getStatus()).body(script.getBody()).output(script.getOutput()).build();
        Script script2 = Script.builder().id(2L).status(ScriptStatus.COMPLETED).body("console.log('World');").output("World\n").build();
        scripts = Arrays.asList(script, script2);
        ScriptGeneralInfo scriptGeneralInfo1 = ScriptGeneralInfo.builder().id(script.getId()).status(script.getStatus()).output(script.getOutput()).build();
        ScriptGeneralInfo scriptGeneralInfo2 = ScriptGeneralInfo.builder().id(script2.getId()).status(script2.getStatus()).output(script2.getOutput()).build();
        scriptGeneralInfos = Arrays.asList(scriptGeneralInfo1, scriptGeneralInfo2);
    }

    @Test
    void testGetAllScripts() {
        given(scriptRepository.findAll(any(Sort.class))).willReturn(scripts);
        when(scriptMapper.toScriptGeneralInfoList(scripts)).thenReturn(scriptGeneralInfos);

        List<ScriptGeneralInfo> foundScripts = scriptService.getScripts(null, "id");
        assertThat(foundScripts).isNotNull();
        assertThat(foundScripts.size()).isEqualTo(2);
        assertThat(foundScripts.get(0).getId()).isEqualTo(scriptGeneralInfos.get(0).getId());
        assertThat(foundScripts.get(0).getStatus()).isEqualTo(scriptGeneralInfos.get(0).getStatus());
        assertThat(foundScripts.get(0).getOutput()).isEqualTo(scriptGeneralInfos.get(0).getOutput());
        assertThat(foundScripts.get(1).getId()).isEqualTo(scriptGeneralInfos.get(1).getId());
        assertThat(foundScripts.get(1).getStatus()).isEqualTo(scriptGeneralInfos.get(1).getStatus());
        assertThat(foundScripts.get(1).getOutput()).isEqualTo(scriptGeneralInfos.get(1).getOutput());
    }

    @Test
    void testGetScriptsByStatus() {
        given(scriptRepository.findByStatus(any(ScriptStatus.class), any(Sort.class))).willReturn(scripts);
        when(scriptMapper.toScriptGeneralInfoList(scripts)).thenReturn(scriptGeneralInfos);

        List<ScriptGeneralInfo> foundScripts = scriptService.getScripts(ScriptStatus.COMPLETED, "id");

        assertThat(foundScripts).isNotNull();
        assertThat(foundScripts.size()).isEqualTo(2);
        assertThat(foundScripts.get(0).getId()).isEqualTo(scriptGeneralInfos.get(0).getId());
        assertThat(foundScripts.get(0).getStatus()).isEqualTo(scriptGeneralInfos.get(0).getStatus());
        assertThat(foundScripts.get(0).getOutput()).isEqualTo(scriptGeneralInfos.get(0).getOutput());
        assertThat(foundScripts.get(1).getId()).isEqualTo(scriptGeneralInfos.get(1).getId());
        assertThat(foundScripts.get(1).getStatus()).isEqualTo(scriptGeneralInfos.get(1).getStatus());
        assertThat(foundScripts.get(1).getOutput()).isEqualTo(scriptGeneralInfos.get(1).getOutput());
    }

    @Test
    public void testGetScript(){
        given(scriptRepository.findById(script.getId())).willReturn(Optional.of(script));
        when(scriptMapper.toScriptFullInfo(script)).thenReturn(scriptFullInfo);

        ScriptFullInfo foundScript = scriptService.getScript(script.getId());
        assertThat(foundScript).isNotNull();
        assertThat(foundScript.getId()).isEqualTo(scriptFullInfo.getId());
        assertThat(foundScript.getStatus()).isEqualTo(scriptFullInfo.getStatus());
        assertThat(foundScript.getBody()).isEqualTo(scriptFullInfo.getBody());
        assertThat(foundScript.getOutput()).isEqualTo(scriptFullInfo.getOutput());
    }

    @Test
    void testGetScriptNotFound() {
        given(scriptRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThrows(ScriptNotFoundException.class, () -> scriptService.getScript(script.getId()));
    }

    @Test
    public void testEvaluateScript(){
        given(scriptRepository.save(any(Script.class))).willReturn(script);
        String scriptRequest = "console.log('Hello world');";

        when(scriptMapper.toScriptFullInfo(script)).thenReturn(scriptFullInfo);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(invocation -> mock(ScheduledFuture.class));

        ScriptFullInfo evaluatedScript = scriptService.evaluateScript(scriptRequest, null, null);

        assertThat(evaluatedScript).isNotNull();
        assertThat(evaluatedScript.getId()).isEqualTo(scriptFullInfo.getId());
        assertThat(evaluatedScript.getStatus()).isEqualTo(scriptFullInfo.getStatus());
        assertThat(evaluatedScript.getBody()).isEqualTo(scriptFullInfo.getBody());
        assertThat(evaluatedScript.getOutput()).isEqualTo(scriptFullInfo.getOutput());
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    public void testEvaluateScriptWithScheduling(){
        script.setStatus(ScriptStatus.QUEUED);
        given(scriptRepository.save(any(Script.class))).willReturn(script);
        String scriptRequest = "console.log('Hello world');";
        when(scriptMapper.toScriptFullInfo(script)).thenReturn(scriptFullInfo);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(invocation -> mock(ScheduledFuture.class));

        LocalDateTime executeAt = LocalDateTime.now().plusHours(1);

        ScriptFullInfo evaluatedScript = scriptService.evaluateScript(scriptRequest, executeAt, null);

        assertThat(evaluatedScript).isNotNull();
        assertThat(evaluatedScript.getId()).isEqualTo(scriptFullInfo.getId());
        assertThat(evaluatedScript.getStatus()).isEqualTo(scriptFullInfo.getStatus());
        assertThat(evaluatedScript.getBody()).isEqualTo(scriptFullInfo.getBody());
        assertThat(evaluatedScript.getOutput()).isEqualTo(scriptFullInfo.getOutput());
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    public void testDeleteScript(){
        given(scriptRepository.findById(script.getId())).willReturn(Optional.of(script));
        scriptService.deleteScript(script.getId());
        verify(scriptRepository, times(1)).delete(script);
    }

    @Test
    public void testDeleteScriptWithExecutingStatus(){
        script.setStatus(ScriptStatus.EXECUTING);
        given(scriptRepository.findById(script.getId())).willReturn(Optional.of(script));
        assertThrows(IllegalDeletionException.class, () -> scriptService.deleteScript(script.getId()));
        verify(scriptRepository, times(0)).delete(script);
    }

    @Test
    void testStopRunningScript() {
        script.setStatus(ScriptStatus.EXECUTING);
        given(scriptRepository.findById(script.getId())).willReturn(Optional.of(script));

        scriptService.stopScript(script.getId());

        assertThat(script.getStatus()).isEqualTo(ScriptStatus.STOPPED);
        verify(scriptRepository, times(1)).save(any(Script.class));
    }

    @Test
    void testStopNonRunningScript() {
        script.setStatus(ScriptStatus.COMPLETED);
        given(scriptRepository.findById(script.getId())).willReturn(Optional.of(script));

        assertThrows(ScriptExecutionStopException.class, () -> scriptService.stopScript(script.getId()));
    }
}
