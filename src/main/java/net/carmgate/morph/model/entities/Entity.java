package net.carmgate.morph.model.entities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	int uniqueId();
}
