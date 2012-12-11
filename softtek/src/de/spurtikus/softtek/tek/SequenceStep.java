package de.spurtikus.softtek.tek;

public class SequenceStep {

	int eventTimebase;

	int stepAction;

	int jumpToLevel;

	int numberClocks;

	int toOccur;
	
	public static final int VAL_GLITCH = 2;
	public static final int VAL_X = 3;
	/**
	 * plword value (trigger value, bits for all 72 channels 
	 * with content 0,1,Glitch and X/Dont Care (0,1,2,3)
	 */
	int prlword[];

	boolean withStorage;
	
	public SequenceStep() {
		prlword = new int[72];
	}

	public int getEventTimebase() {
		return eventTimebase;
	}

	public void setEventTimebase(int eventTimebase) {
		this.eventTimebase = eventTimebase;
	}

	public int getStepAction() {
		return stepAction;
	}

	public void setStepAction(int stepAction) {
		this.stepAction = stepAction;
	}

	public int getJumpToLevel() {
		return jumpToLevel;
	}

	public void setJumpToLevel(int jumpToLevel) {
		this.jumpToLevel = jumpToLevel;
	}

	public int getNumberClocks() {
		return numberClocks;
	}

	public void setNumberClocks(int numberClocks) {
		this.numberClocks = numberClocks;
	}

	public int getToOccur() {
		return toOccur;
	}

	public void setToOccur(int toOccur) {
		this.toOccur = toOccur;
	}

	public boolean isWithStorage() {
		return withStorage;
	}

	public void setWithStorage(boolean withStorage) {
		this.withStorage = withStorage;
	}

	public void setPrlBit( int i, int val ) {
		prlword[i]=val;
	}
	public int getPrlWord( int i ) {
		return prlword[i];
	}
}
