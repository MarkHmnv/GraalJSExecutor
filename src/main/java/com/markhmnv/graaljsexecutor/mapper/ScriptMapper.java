package com.markhmnv.graaljsexecutor.mapper;

import com.markhmnv.graaljsexecutor.model.response.ScriptFullInfo;
import com.markhmnv.graaljsexecutor.model.response.ScriptGeneralInfo;
import com.markhmnv.graaljsexecutor.model.entity.Script;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ScriptMapper {
    ScriptFullInfo toScriptFullInfo(Script script);
    List<ScriptGeneralInfo> toScriptGeneralInfoList(List<Script> scripts);
}
