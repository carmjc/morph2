package net.carmgate.morph.model;

public class UiContext {

	private boolean paused;
	private UiState uiState = UiState.NORMAL;
	private boolean debugMode = false;
	private boolean morphsShown = false;

	public UiState getUiState() {
		return uiState;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public boolean isMorphsShown() {
		return morphsShown;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setUiState(UiState uiState) {
		this.uiState = uiState;
	}

	public void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public void toggleMorphsShown() {
		morphsShown = !morphsShown;
	}

	public void togglePaused() {
		paused = !paused;
	}

}
