package com.dmc.lplates.inbound.dtos;

import java.util.List;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.User;

import lombok.Getter;

@Getter
public class LearnerProfile {

    private final User user;
    private final List<Booking> bookings;
    private final List<EdtProgress> edtProgress;
    private final EdtSummary edtSummary;

    public LearnerProfile(User user, List<Booking> bookings, List<EdtProgress> edtProgress) {
        this.user = user;
        this.bookings = bookings;
        this.edtProgress = edtProgress;
        this.edtSummary = new EdtSummary(edtProgress);
    }

    @Getter
    public static class EdtSummary {
        private final int totalModules = 12;
        private final long completedModules;
        private final long remainingModules;
        private final boolean fullyCompleted;

        public EdtSummary(List<EdtProgress> edtProgress) {
            this.completedModules = edtProgress == null ? 0
                    : edtProgress.stream().filter(p -> Boolean.TRUE.equals(p.getCompleted())).count();
            this.remainingModules = totalModules - completedModules;
            this.fullyCompleted = completedModules == totalModules;
        }
    }
}
