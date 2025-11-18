package com.smartatt.core.model;

import java.time.LocalDate;

public class ClassSession {
    private int sessionId;
    private int subjectId;
    private LocalDate sessionDate;
    private int createdBy;

    public ClassSession() {}

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    @Override
    public String toString() {
        return "ClassSession[id=" + sessionId + ", subject=" + subjectId + ", date=" + sessionDate + "]";
    }
}
