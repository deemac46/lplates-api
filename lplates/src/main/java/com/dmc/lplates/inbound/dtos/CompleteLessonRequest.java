package com.dmc.lplates.inbound.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteLessonRequest {

    private Integer edtModuleNumber;
    private String edtNote;

    public CompleteLessonRequest() {
    }
}