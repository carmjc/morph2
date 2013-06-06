package net.carmgate.morph.model.player;

public class Player {
	public static enum FOF {
		NO_ONE,
		SELF,
		FRIEND,
		NEUTRAL,
		FOE;
	}

	public static enum PlayerType {
		NOONE,
		HUMAN,
		AI;
	}

	public static final Player NO_ONE = new Player(PlayerType.NOONE, "No one", FOF.NO_ONE);

	final PlayerType playerType;
	private final String name;
	/** IMPROVE This is rather simplistic, it only defines fof against "SELF", not against any couple of player. */
	private final FOF fof;

	public Player(PlayerType playerType, String name, FOF fof) {
		this.playerType = playerType;
		this.name = name;
		this.fof = fof;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Player other = (Player) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (playerType != other.playerType) {
			return false;
		}
		return true;
	}

	public FOF getFof() {
		return fof;
	}

	public String getName() {
		return name;
	}

	public PlayerType getPlayerType() {
		return playerType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (playerType == null ? 0 : playerType.hashCode());
		return result;
	}
}
