package com.github.dwendelen.couchbase_spring_migration;

import com.github.dwendelen.couchbase_spring_migration.impl.MigrationConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MigrationConfiguration.class)
public @interface EnableMigration {
}
