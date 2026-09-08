package com.dmc.lplates.inbound.models;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EdtProgress {

    private Long id;
    private Long studentId;
    private Integer moduleNumber;   // 1-12
    private String moduleName;
    private Boolean completed;
    private Long lessonId;          // nullable — the lesson that completed this module
    private Timestamp completedAt;
    private String note;
    private Long loggedByInstructorId;
    private Timestamp loggedAt;

    public EdtProgress() {
    }
}
