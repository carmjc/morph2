package net.carmgate.morph.actions.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.UiContext;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHints {
	UiContext[] uiContext() default { UiContext.NORMAL };

	boolean dragAction() default false;

	boolean keyboardActionAutoload() default false;

	boolean mouseActionAutoload() default false;
}
