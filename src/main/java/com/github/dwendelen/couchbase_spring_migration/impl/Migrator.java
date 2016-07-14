package com.github.dwendelen.couchbase_spring_migration.impl;

import com.github.dwendelen.couchbase_spring_migration.Migration;
import com.github.dwendelen.couchbase_spring_migration.MigrationStatus;
import com.github.dwendelen.couchbase_spring_migration.MigrationTiming;
import com.github.dwendelen.couchbase_spring_migration.impl.changelog.Changelog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Migrator {
    private Changelog changelog;

    private Set<MigrationTraverser> migrations = new HashSet<>();

    @Autowired
    public Migrator(Changelog changelog) {
        this.changelog = changelog;
    }

    public void setMigrations(List<Migration> migrations) {
        Map<String, MigrationTraverser> migrationMap = new HashMap<>();

        for (Migration migration : migrations) {
            migrationMap.put(migration.getIdentifier(), new MigrationTraverser(migration, changelog));
        }

        for (MigrationTraverser dependent : migrationMap.values()) {
            for (String dependencyName : dependent.getMigration().getDependencies()) {
                MigrationTraverser dependency = migrationMap.get(dependencyName);
                if(dependency == null) {
                    throw new UnsupportedOperationException("Unkown dependency. Unkown dependencies are unsupported at this moment, could be solved with a NullMigration");
                }
                dependent.addDependency(dependency);
            }
        }
    }

    public void migrate(MigrationTiming timing) {
        RootTraverser rootTraverser = createRootTraverserFor(timing);

        changelog.fetch();

        rootTraverser.traverse(false, false);
        rootTraverser.traverse(true, true);

        if(rootTraverser.getStatus() == MigrationStatus.FAILED) {
            throw new RuntimeException("At least one migration failed");
        }
    }

    private RootTraverser createRootTraverserFor(MigrationTiming timing) {
        RootTraverser rootTraverser = new RootTraverser();
        migrations
                .stream()
                .filter(migration -> migration.getMigration().getMigrationTiming() == timing)
                .forEach(migration -> rootTraverser.addDependency(migration));
        return rootTraverser;
    }
}
