package net.carmgate.morph.ui.renderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Renders {
	Class<?>[] value();
}
