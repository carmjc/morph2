package net.carmgate.morph.ui;

/**
 * This class stores the context of the UI.
 * Among other things :
 * <ul><li>The current {@link UIState}</li>
 * <li>The pause state : true if the game is paused</li>
 * <li>Some debug specific ui flags.</li></ul>
 */
public class UiContext {

	private boolean paused;
	private UIState uiState = UIState.NORMAL;
	private boolean debugMode = false;
	private boolean debugMorphsShown = false;
	private boolean debugSelectViewMode = false;

	public UIState getUiState() {
		return uiState;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public boolean isDebugMorphsShown() {
		return debugMorphsShown;
	}

	public boolean isDebugSelectViewMode() {
		return debugSelectViewMode;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setUiState(UIState uiState) {
		this.uiState = uiState;
	}

	public void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public void toggleDebugMorphsShown() {
		debugMorphsShown = !debugMorphsShown;
	}

	public void toggleDebugSelectViewMode() {
		debugSelectViewMode = !debugSelectViewMode;
	}

	public void togglePaused() {
		paused = !paused;
	}

}
