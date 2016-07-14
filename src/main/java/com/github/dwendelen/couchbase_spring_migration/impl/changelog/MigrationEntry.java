package com.github.dwendelen.couchbase_spring_migration.impl.changelog;

import com.couchbase.client.java.document.json.JsonObject;
import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;
import org.springframework.util.Assert;

import java.time.Instant;

public class MigrationEntry {
    private JsonObject entry;

    public MigrationEntry(JsonObject entry) {
        Assert.notNull(entry);
        this.entry = entry;
    }

    public MigrationStatus getStatus() {
        return MigrationStatus.valueOf(entry.getString("status"));
    }

    public void setStatus(MigrationStatus status) {
        entry.put("status", status.toString());
    }

    public void markStop(MigrationStatus status) {
        setStatus(status);
        entry.put("stop", getNowAsString());
    }

    private String getNowAsString() {
        return Instant.now().toString();
    }

    public void markStart() {
        setStatus(MigrationStatus.RUNNING);
        entry.put("start", getNowAsString());
    }
}
