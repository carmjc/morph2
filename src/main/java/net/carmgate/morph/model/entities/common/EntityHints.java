package net.carmgate.morph.model.entities.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface EntityHints {

	EntityType entityType();

}
