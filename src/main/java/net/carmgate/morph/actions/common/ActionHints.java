package net.carmgate.morph.actions.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.ui.UIState;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHints {
	UIState[] uiState() default { UIState.NORMAL };

	boolean dragAction() default false;

	boolean keyboardActionAutoload() default false;

	boolean mouseActionAutoload() default false;
}
