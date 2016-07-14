package com.github.dwendelen.couchbase_spring_migration.impl.changelog;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Changelog {
    @Autowired
    private Bucket bucket;

    private JsonDocument document;
    private JsonObject entries;

    public void fetchAndlock() {
        JsonDocument changelog = bucket.getAndLock("changelog", 1);
        this.document = changelog;
        this.entries = changelog.content();
    }

    public void saveAndReleaseLock() {
        bucket.replace(document);
    }

    public void fetch() {
        JsonDocument changelog = bucket.get("changelog");

        if (changelog == null) {
            this.entries = JsonObject.create();
            this.document = JsonDocument.create("changelog", this.entries);
            bucket.insert(this.document);
        } else {
            this.document = changelog;
            this.entries = changelog.content();
        }
    }

    public MigrationEntry getEntry(String migrationName) {
        JsonObject entry = entries.getObject(migrationName);
        if(entry == null) {
            entry = JsonObject.create();
            entry.put("status", MigrationStatus.NOT_STARTED.toString());
            entries.put(migrationName, entry);
        }

        return new MigrationEntry(entry);
    }
}
