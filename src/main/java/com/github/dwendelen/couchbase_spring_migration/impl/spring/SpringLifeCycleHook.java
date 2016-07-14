package com.github.dwendelen.couchbase_spring_migration.impl.spring;

import com.github.dwendelen.couchbase_spring_migration.Migration;
import com.github.dwendelen.couchbase_spring_migration.MigrationTiming;
import com.github.dwendelen.couchbase_spring_migration.impl.Migrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class SpringLifeCycleHook implements ApplicationListener<ContextStartedEvent> {
    @Autowired
    private List<Migration> migrations;
    @Autowired
    private Migrator migrator;

    @PostConstruct
    public void migrateStartupMigrations() throws InterruptedException {
        migrator.setMigrations(migrations);
        migrator.migrate(MigrationTiming.DURING_STARTUP);
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        migrator.migrate(MigrationTiming.DURING_RUNTIME);
    }
}
