package de.spurtikus.softtek.tek;

public class Channel {
	/**
	 * position of this channel in DisplayGroup
	 */
	int pos;
	/**
	 * Pod id associated to this channel
	 */
	int pod;
	/**
	 * Pin number in pod associated to this channel
	 */
	int bit;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public Channel(int pos, int pod, int bit) {
		this.pos = pos;
		this.pod = pod;
		this.bit = bit;
	}

	public int getPod() {
		return pod;
	}

	public void setPod(int pod) {
		this.pod = pod;
	}

	public int getBit() {
		return bit;
	}

	public void setBit(int bit) {
		this.bit = bit;
	}

}
