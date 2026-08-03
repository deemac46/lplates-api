package com.dmc.lplates.inbound.models;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feedback {

    private Long id;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;
    private Long authorId;
    private Long lessonId;

    public Feedback() {
    }
}
