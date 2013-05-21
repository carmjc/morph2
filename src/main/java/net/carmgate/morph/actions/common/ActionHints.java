package net.carmgate.morph.actions.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHints {
	boolean dragAction() default false;

	boolean keyboardActionAutoload() default false;

	boolean mouseActionAutoload() default false;
}
