package net.carmgate.morph.actions.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.ui.UiState;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHints {
	UiState[] uiState() default { UiState.NORMAL };

	boolean dragAction() default false;

	boolean keyboardActionAutoload() default false;

	boolean mouseActionAutoload() default false;
}
