package net.carmgate.morph.model.behaviors.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.entities.Morph.MorphType;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActivatedMorph {
	int level() default 1;

	MorphType morphType();
}
