package com.dmc.lplates.inbound.dtos;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteLessonResponse {

    private Booking lesson;
    private EdtProgress edtProgress;

    public CompleteLessonResponse(Booking lesson, EdtProgress edtProgress) {
        this.lesson = lesson;
        this.edtProgress = edtProgress;
    }
}