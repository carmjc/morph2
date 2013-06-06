package net.carmgate.morph.model.behaviors.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Needs {
	ActivatedMorph[] value();
}
