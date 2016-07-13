package com.github.dwendelen.couchbasespringmigration;

import com.github.dwendelen.couchbasespringmigration.impl.MigrationConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MigrationConfiguration.class)
public @interface EnableMigration {
}
