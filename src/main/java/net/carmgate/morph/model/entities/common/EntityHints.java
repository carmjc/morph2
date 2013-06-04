package net.carmgate.morph.model.entities.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EntityHints {

	boolean actionSelectable() default true;

	EntityType entityType();

	boolean selectable() default true;

}
