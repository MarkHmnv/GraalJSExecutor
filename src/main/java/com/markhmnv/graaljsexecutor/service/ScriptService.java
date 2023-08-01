package com.markhmnv.graaljsexecutor.service;

import com.markhmnv.graaljsexecutor.exception.EvaluationException;
import com.markhmnv.graaljsexecutor.exception.ScriptExecutionStopException;
import com.markhmnv.graaljsexecutor.exception.ScriptNotFoundException;
import com.markhmnv.graaljsexecutor.exception.IllegalDeletionException;
import com.markhmnv.graaljsexecutor.mapper.ScriptMapper;
import com.markhmnv.graaljsexecutor.model.response.ScriptGeneralInfo;
import com.markhmnv.graaljsexecutor.model.response.ScriptFullInfo;
import com.markhmnv.graaljsexecutor.model.entity.Script;
import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import com.markhmnv.graaljsexecutor.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static com.markhmnv.graaljsexecutor.model.enums.ScriptStatus.*;

@Service
@RequiredArgsConstructor
public class ScriptService {
    private final ScriptRepository scriptRepository;
    private final ScriptMapper scriptMapper;
    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> runningScripts = new ConcurrentHashMap<>();


    /**
     * Retrieves a list of scripts based on the specified status and sorting criteria.
     *
     * @param status The status of the scripts to retrieve. If null, all scripts will be retrieved.
     * @param sortBy The criteria by which the scripts should be sorted in descending order.
     *              Valid values are: "createdAt", "updatedAt", "status", "id".
     * @return A list of ScriptGeneralInfo objects representing the retrieved scripts.
     */
    public List<ScriptGeneralInfo> getScripts(ScriptStatus status, String sortBy) {
        List<Script> scripts;
        Sort sortOrder = Sort.by(sortBy).descending();
        if (status == null)
            scripts = scriptRepository.findAll(sortOrder);
        else
            scripts = scriptRepository.findByStatus(status, sortOrder);
        return scriptMapper.toScriptGeneralInfoList(scripts);
    }

    public ScriptFullInfo getScript(Long id) {
        Script script = getScriptById(id);
        return scriptMapper.toScriptFullInfo(script);
    }

    public void deleteScript(Long id) {
        Script script = getScriptById(id);
        if(script.getStatus() == EXECUTING)
            throw new IllegalDeletionException();
        scriptRepository.delete(script);
    }

    /**
     * Evaluation of a script.
     *
     * @param scriptRequest The script request containing the script to be evaluated.
     * @param executeAt The time when the script should be executed. If null, the current time will be used.
     * @return The ScriptFullInfo object representing the evaluated script.
     */
    public ScriptFullInfo evaluateScript(String scriptRequest, LocalDateTime executeAt, String timezone) {
        Instant now = Instant.now();
        ZoneId zoneId = (timezone == null || timezone.isEmpty())
                ? ZoneId.systemDefault()
                : ZoneId.of(timezone);
        Instant executeAtInstant = executeAt == null
                ? now
                : executeAt.atZone(zoneId).toInstant();

        ScriptStatus initialStatus = executeAtInstant.isAfter(now) ? QUEUED : EXECUTING;
        Script script = createAndSave(scriptRequest, executeAt, initialStatus);

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> executeScript(script.getId(), scriptRequest), executeAtInstant);
        runningScripts.put(script.getId(), scheduledFuture);

        return scriptMapper.toScriptFullInfo(script);
    }

    /**
     * Executes a script and returns the result.
     *
     * @param id   The ID of the script to be executed.
     * @param body The body of the script to be executed.
     * @throws EvaluationException if an error occurs during script execution.
     */
    private void executeScript(Long id, String body){
        Script script = getScriptById(id);
        script.setStatus(EXECUTING);
        scriptRepository.save(script);

        long startTime = System.currentTimeMillis();

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Context context = contextWith(printStream)) {

            context.eval("js", body);
            long executionTime = System.currentTimeMillis() - startTime;
            String output = outputStream.toString(StandardCharsets.UTF_8);

            updateAndSaveScript(script, executionTime, output, COMPLETED);
        } catch (Exception e){
            String message = e.getMessage();
            long executionTime = System.currentTimeMillis() - startTime;
            updateAndSaveScript(script, executionTime, message, FAILED);
            throw new EvaluationException(message);
        } finally {
            runningScripts.remove(id);
        }
    }

    public void stopScript(Long id) {
        Script script = getScriptById(id);
        if(script.getStatus() != EXECUTING && script.getStatus() != QUEUED)
            throw new ScriptExecutionStopException();

        ScheduledFuture<?> future = runningScripts.remove(id);
        if(future != null)
            future.cancel(true);

        script.setStatus(ScriptStatus.STOPPED);
        scriptRepository.save(script);
    }

    private Script createAndSave(String body, LocalDateTime executeAt, ScriptStatus status) {
        Script script = Script.builder()
                .status(status)
                .body(body)
                .executeAt(executeAt)
                .build();
        return scriptRepository.save(script);
    }

    /**
     * Creates a Context object with the specified PrintStream for output.
     *
     * @param printStream The PrintStream to be used for printing script output.
     * @return A Context object with the specified PrintStream.
     */
    private Context contextWith(PrintStream printStream) {
        return Context.newBuilder()
                .allowExperimentalOptions(true)
                .option("js.print", "true")
                .option("engine.WarnInterpreterOnly", "false")
                .out(printStream)
                .err(printStream)
                .build();
    }

    private void updateAndSaveScript(Script script, long executionTime, String output, ScriptStatus status) {
        script.setExecutionTime(executionTime);
        script.setOutput(output);
        script.setStatus(status);
        scriptRepository.save(script);
    }

    private Script getScriptById(Long id){
        return scriptRepository.findById(id).orElseThrow(ScriptNotFoundException::new);
    }
}
