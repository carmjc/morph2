package net.carmgate.morph.model.behaviors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.entities.Morph.MorphType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Need {
	int level() default 1;

	MorphType morphType();
}
