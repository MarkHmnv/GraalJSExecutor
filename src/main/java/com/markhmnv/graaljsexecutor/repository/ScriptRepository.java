package com.markhmnv.graaljsexecutor.repository;

import com.markhmnv.graaljsexecutor.model.entity.Script;
import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {
    List<Script> findByStatus(ScriptStatus status, Sort sort);
}
