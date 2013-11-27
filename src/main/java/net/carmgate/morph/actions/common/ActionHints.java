package net.carmgate.morph.actions.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.carmgate.morph.model.ui.UIState;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHints {
	/** true if the action needs to be wired to handle drags. */
	boolean dragAction() default false;

	/** indicates that the action should be run when there is a key stroke. */
	boolean keyboardActionAutoload() default false;

	/** indicates that the action should be run when there is a mouse event. */
	boolean mouseActionAutoload() default false;

	/** The {@link UIState} in which the action can be loaded. */
	UIState[] uiState() default { UIState.NORMAL };
}
