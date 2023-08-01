package com.markhmnv.graaljsexecutor.model.entity;

import com.markhmnv.graaljsexecutor.model.enums.ScriptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Script {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ScriptStatus status;

    private Long executionTime;

    @Column(length = 1000000)
    private String body;

    @Column(length = 1000000)
    private String output;

    private LocalDateTime executeAt;
}
