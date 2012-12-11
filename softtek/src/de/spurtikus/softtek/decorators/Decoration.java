package de.spurtikus.softtek.decorators;

public class Decoration {
	int type;
	int begin;
	int end;
	String value;
	String[] comment;

	public Decoration() {
		init("default", -1, -1, -1, "default", (String[]) null);
	}

	public Decoration(String clazz, int type, int begin, int end, String value,
			String[] comment) {
		init(clazz, type, begin, end, value, comment);
	}

	private void init(String clazz, int type, int begin, int end, String value,
			String[] comment) {
		this.type = type;
		this.begin = begin;
		this.end = end;
		this.value = value;
		this.comment = comment;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setName(int type) {
		this.type = type;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String[] getComment() {
		return comment;
	}

	public void setComment(String[] comment) {
		this.comment = comment;
	}

	public void print() {
		if (!value.equals("-1"))
			System.out.println(" value: " + value);
		System.out.println(" type: " + type);
		System.out.println(" begins at: " + begin);
		System.out.println(" ends at: " + end);
		if (comment != null)
			for (int i = 0; i < comment.length; i++)
				System.out.println(" comment: " + comment[i]);
		System.out.println();
	}

	public static String Event2Message(int event) {
		String ret = "Unknown event";
		return ret;
	}

}
