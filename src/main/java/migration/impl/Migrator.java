package migration.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import migration.Migration;
import migration.MigrationStatus;
import migration.MigrationTiming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Migrator implements ApplicationListener<ContextStartedEvent> {
    @Autowired
    private List<Migration> migrations;
    @Autowired
    private Bucket bucket;

    private Set<String> migrationsDone = new HashSet<>();
    private Queue<String> migrationsToDo = new LinkedList<>();
    private Map<String, Migration> migrationMap = new HashMap<>();

    @PostConstruct
    public void migrateStartupMigrations() throws InterruptedException {
        for (Migration migration : migrations) {
            migrationMap.put(migration.getIdentifier(), migration);
        }

        migrationsToDo.addAll(getMigrationsToDo(MigrationTiming.DURING_RUNTIME));
        migrationLoop();
    }

    private void migrationLoop() throws InterruptedException {
        while (!migrationsToDo.isEmpty()) {
            processScript(migrationsToDo.remove());
        }
    }

    private void processScript(String migrationName) throws InterruptedException {
        if(migrationsDone.contains(migrationName)) {
            return;
        }
        Migration migration = migrationMap.get(migrationName);
        JsonDocument document = getOrCreateDocument(); //DO NOT CACHE

        JsonObject changelog = document.content();
        JsonObject script = changelog.getObject(migrationName);
        if(script != null) {
            if("IN_PROGRESS".equals(script.get("status"))) {
                switch (migration.getStatus()) {
                    case RUNNING:
                        migrationsToDo.add(migrationName);
                        bucket.replace(document);
                        return;
                    case SUCCESS:
                        document = getOrCreateDocument();
                        script = document.content().getObject(migrationName);
                        script.put("status", "SUCCESS");
                        script.put("stop", Instant.now());
                        bucket.replace(document);
                        migrationsDone.add(migrationName);
                        return;
                    case FAILED:
                        document = getOrCreateDocument();
                        script = document.content().getObject(migrationName);
                        script.put("status", "FAILED");
                        script.put("stop", Instant.now());
                        bucket.replace(document);
                        throw new RuntimeException("A script failed");
                }
            }
            if("FAILED".equals(script.get("status"))) {
                bucket.replace(document); //Release lock
                throw new RuntimeException("A script failed");
            }
        }

        script = JsonObject.create();
        script.put("status", "IN_PROGRESS");
        script.put("start", Instant.now());
        changelog.put(migrationName, script);
        bucket.replace(document);


        for (String dependency : migration.getDependencies()) {
            processScript(dependency);
        }
        migration.start();

    }

    private JsonDocument getOrCreateDocument() {
        JsonDocument doc = bucket.getAndLock("changelog", 1);

        if(doc != null) {
            return doc;
        } else  {
            JsonDocument newDoc = JsonDocument.create("changelog", JsonObject.create());
            bucket.insert(newDoc);
            return getOrCreateDocument();
        }
    }

    private Set<String> getMigrationsToDo(MigrationTiming migrationTiming) {
        return migrations.stream()
                .filter(m -> m.getMigrationTiming() == migrationTiming)
                .map(Migration::getIdentifier)
                .collect(Collectors.toSet());
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        try {
            migrateRuntimeMigrations();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void migrateRuntimeMigrations() throws InterruptedException {
        migrationsToDo.addAll(getMigrationsToDo(MigrationTiming.DURING_RUNTIME));
        migrationLoop();
    }
}
