package de.spurtikus.softtek;

import java.util.ArrayList;

import javax.swing.text.Position;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * TimeCursor implements a "cursor" class to be used inside timelines like the
 * time lines from a logic analyzer display.
 * 
 * A canvas has a visible view port and a larger world area. The view port shows
 * a subsection of the world area by an offset (x,y) and a width and a height.
 * The offset is changed via scroll bars, the size by resizing the parent of the
 * canvas.
 * 
 * Behaviour of a cursor as implemented:
 * 
 * - a cursor is visible if defined and its canvas is visible and the cursor is
 * in the visible part of the canvas world area.
 * 
 * - a cursor can be moved around by moving the mouse pointer onto the cursor
 * and pressing mouse button 1. Then the object can be moved while still
 * pressing mouse button 1. By deselecting mouse button 1, the movment of the
 * cursor is finished.
 * 
 * Note: the implementation assumes that more than one cursor is defined, all
 * cursors are used inside the same canvas.
 * 
 * @author dennis
 * 
 */
public class TimeCursor {

	String name;
	/**
	 * current cursor position (World coordinates)
	 */
	private int yPos;
	private int xPos;
	/**
	 * new cursor position (World coordinates)
	 */
	private int newX;
	private int newY;
	/**
	 * start values for cursor. New values are clipped against these values.
	 * (World coordinates)
	 */
	private int yStartPos;
	private int xStartPos;
	/**
	 * dimension of cursor object
	 */
	private int cursorWidth;
	private int cursorHeight;

	/**
	 * step width for movements of cursor.
	 */
	private int modulo;
	
	/**
	 * dy
	 */
	private int dy;
	
	/**
	 * The next four values are used to slightly enlarge the cursor area by some
	 * margin. This eases the selection of a cursor object. Furthermore, they
	 * define the size of the area that is redrawed during movements of cursors
	 */
	int xPositionMargin = 5;
	int yPositionMargin = 15;
	// TODO wMargin: calculate the value from the text strings drawn below
	// cursor rectangle
	int wMargin = 100;
	// TODO hMargin: calculate the value from the text strings drawn below
	// cursor rectangle
	int hMargin = 40;

	String hexValue = "";
	String dualValue = "";
	int bytePosition = 0;

	private GC gc;
	private Color cursorColor = null;
	private Color bgColor = null;

	/**
	 * Cursor enabled state
	 */
	private boolean enabled;

	/**
	 * the canvas for all cursors
	 */
	static Canvas canvas = null;
	/**
	 * static list of all defined cursor objects
	 */
	static ArrayList<TimeCursor> cursors = null;
	/**
	 * static listener for all cursors
	 */
	static MouseMoveListener moveListener = null;
	static MouseListener mouseListener = null;
	/**
	 * state of TimeCursor
	 */
	final int S_NONE = 0;
	final int S_MOVE = 1;

	static int state;
	static TimeCursor currentCursor = null;

	/**
	 * Create a time cursor object.
	 * 
	 * A time cursor is a rectangle that can be overlaid to a time line. The
	 * time line has to be present in a Canvas object. The cursor is dragable by
	 * mouse and can be defined to for smooth movement (modulo=1) or in steps of
	 * 'modulo' pixels.
	 * 
	 * @param name
	 *            Name of cursor. Used as a label at the Cursor.
	 * @param canvas
	 *            the canvas the cursor will be drawn into.
	 * @param xPos
	 *            start x position of cursor
	 * @param yPos
	 *            start y position of cursor
	 * @param width
	 *            width of cursor
	 * @param height
	 *            height of cursor
	 * @param modulo
	 *            size in pixels of a single movement step for cursor. Set to 1
	 *            for smooth movement, or to any larger pixel value for
	 *            "jumping" cursor.
	 * @param xMinPos
	 *            minimum allowed x position of cursor
	 * @param yMinPos
	 *            minimum allowed y position of cursor
	 * @param cursorColor
	 *            Color of cursor
	 * @param bgColor
	 *            Default color of background the cursor is drawn to. This value
	 *            is used as background for text output.
	 */
	public TimeCursor(String name, Canvas canvas, int xPos, int yPos,
			int width, int height, int modulo, int xMinPos, int yMinPos,
			Color cursorColor, Color bgColor) {

		this.name = name;
		this.canvas = canvas;
		this.xPos = xPos;
		this.yPos = yPos;
		xStartPos = xMinPos;
		yStartPos = yMinPos;
		cursorWidth = width;
		cursorHeight = height;
		this.modulo = modulo;
		this.cursorColor = cursorColor;
		this.bgColor = bgColor;
		enabled = false;
		dy=0;

		// init newX, newY
		newX = xPos;
		newY = yPos;

		// init object
		if (cursors == null) {
			cursors = new ArrayList<TimeCursor>();

			/**
			 * mouse move listener
			 */
			moveListener = new MouseMoveListener() {
				public void mouseMove(MouseEvent e) {
					// if we are not already in a move of a cursor...
					if (state == S_NONE) {
						// if correct mouse button is pressed ...
						if ((e.stateMask & SWT.BUTTON1) != 0) {
							// if cursor is not already set ...
							if (currentCursor == null) {
								// get cursor at position or a null...
								currentCursor = cursorAtPosition(e);
								// if not null, change state to movement
								if (currentCursor != null) {
									// ... then we are at the start of a cursor
									// movement
									state = S_MOVE;
									// System.out.println("cursor "
									// + currentCursor.name
									// + " started move");
									currentCursor.dy=currentCursor.yPos-Vc2Wc_Y(e.y);
								} else
									return;
							}
						} else
							return;
					}
					// if we are in the state of moving a cursor ...
					if (state == S_MOVE) {
						// if correct mouse button is released ...
						if ((e.stateMask & SWT.BUTTON1) == 0) {
							// leave movement state
							// System.out.println("cursor " + currentCursor.name
							// + " released");
							state = S_NONE;
							currentCursor = null;
							return;
						} else {
							// continue moving
							// System.out.println("cursor " + currentCursor.name
							// + " continues movement");
							// System.out.println("x=" + e.x + ", y=" + e.y);
							// extract new position for use in redraw
							int x = e.x /*- cursorWidth / 2*/;
							int y = e.y /*- cursorHeight / 2*/;

							// create redraw request for canvas to delete
							// (later) current (then: OLD) cursor
							if (currentCursor.enabled)
								currentCursor.redrawCanvas(x, y);
						}
					}
				}
			};

			/**
			 * general mouse event listener, used for mouse button up check
			 */
			mouseListener = new MouseListener() {

				@Override
				public void mouseUp(MouseEvent arg0) {
					if (state == S_MOVE) {
						// mouse up -> end cursor movement
						// System.out.println("cursor released by mouse event");
						state = S_NONE;
						currentCursor = null;
					}
				}

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
					// unused
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
					// unused
				}
			};

			// init cursor state variable
			state = S_NONE;

			/*
			 * mouse listener extracts the new position requested by the user
			 * and check for button down/up
			 */
			canvas.addMouseMoveListener(moveListener);
			canvas.addMouseListener(mouseListener);
		}

		// create own GC
		gc = new GC(canvas);

		// add the new cursor object to the global list
		cursors.add(this);
	}

	/**
	 * Returns first enabled cursor that contains the mouse pointer
	 * 
	 * @param e
	 *            mouse event, contains position in viewport coordinates
	 * @return cursor object or null
	 */
	protected TimeCursor cursorAtPosition(MouseEvent e) {
		TimeCursor ret = null;

		// find out what cursor is targeted by the user
		for (TimeCursor c : cursors) {
			if (c.contains(e)) {
				// System.out.println("cursor " + c.name + " found at " + "(" +
				// e.x + ", " + e.y+")");
				// end loop at first hit
				return c;
			}
		}
		return ret;
	}

	/**
	 * Redraw cursor only
	 * 
	 */
	public void redraw(String hexValue, String dualValue, int position) {
		if (enabled) {
			this.hexValue = hexValue;
			this.dualValue = dualValue;
			bytePosition = position;
			redrawCursor();
		}
	}

	/**
	 * Create a redraw event for canvas; this deletes OLD cursor
	 * 
	 * A rectangle area is calculated. For that area, a redraw event is
	 * generated. The canvas object will redraw only this small part of its view
	 * port.
	 * 
	 * @param x
	 *            mouse x position
	 * @param y
	 *            mouse y position
	 * 
	 */
	public void redrawCanvas(int x, int y) {
		// convert viewport to world coordinates
		this.newX = Vc2Wc_X(x);
		this.newY = Vc2Wc_Y(y)+dy;
		// create redraw event, using old position of cursor
		canvas.redraw(Wc2Vc_X(getLastXPos()), Wc2Vc_Y(getLastYPos()),
				getWidth(), getHeight(), false);
	}

	/**
	 * Redraw cursor - this draws the NEW cursor
	 * 
	 * Redraw can have two reasons.
	 * 
	 * Cursor movement (state==S_MOVE).
	 * 
	 * Movement must honor a canvas with an offset i.e. that canvas is scrolled.
	 * 
	 * Scrolling of canvas viewport (state==S_NONE)
	 * 
	 */
	public void redrawCursor() {
		int x = newX, y = newY;

		System.out.println("r1: " + name + " (" + x + "," + y + ")" + xStartPos
				+ " " + yStartPos);

		if (state == S_MOVE) {
			// honor start position
			if (x < xStartPos)
				x = xStartPos;
			if (y < yStartPos)
				y = yStartPos;
			// honor size of canvas
			if (Wc2Vc_X(x) > canvas.getSize().x - getWidth())
				x = Vc2Wc_X(canvas.getSize().x - getWidth());
			if (Wc2Vc_Y(y) > canvas.getSize().y - getHeight())
				y = Vc2Wc_Y(canvas.getSize().y - getHeight());
		}

		// convert world coordinate position to viewport
		x = Wc2Vc_X(x);
		y = Wc2Vc_Y(y);
		System.out.println("r2: " + name + " (" + x + "," + y + ")");

		// increment only in steps
		x = modulize(x);

		// no redraw if we would leave visible area
		if (x <= 0 || y <= 0)
			return;

		// print cursor
		int points[] = { x, y, x, y + cursorHeight, x + cursorWidth,
				y + cursorHeight, x + cursorWidth, y, x, y };
		gc.setForeground(cursorColor);
		gc.setBackground(cursorColor);
		gc.setLineWidth(4);
		gc.setAlpha(100);
		gc.fillPolygon(points);

		// print cursors name string
		gc.setAlpha(200);
		gc.setBackground(bgColor);
		gc.drawString(name, x, y + cursorHeight + 5);
		gc.drawString("" + bytePosition, x, y /* + cursorHeight - 25 */- 15);
		gc.drawString(hexValue, x + 15, y + cursorHeight - 25);
		gc.drawString(dualValue, x + 15, y + cursorHeight - 10);

		if (state == S_MOVE) {
			xPos = Vc2Wc_X(x);
			yPos = Vc2Wc_Y(y);
			System.out.println("r3: " + name + " (" + xPos + "," + yPos + ")");
		}
		// System.out
		// .println("Redrawed cursor " + name + " (" + x + "," + y + ")");
	}

	/**
	 * Return world position of cursor
	 * 
	 * @return cursor position.
	 */
	public int getPosition() {
		return xPos - xPositionMargin;
	}

	public int getLastXPos() {
		return xPos - xPositionMargin;
	}

	public int getLastYPos() {
		return yPos - yPositionMargin;
	}

	public int getWidth() {
		return cursorWidth + wMargin;
	}

	public int getHeight() {
		return cursorHeight + hMargin;
	}

	/**
	 * Returns modulized value for a value v, i.e. returns (v%modulo) for a
	 * value v.
	 * 
	 * @param v
	 *            value v to modulize
	 * @return modulized value
	 */
	public int modulize(int v) {
		// v = v - (v % modulo);
		return v;
	}

	/**
	 * Check if a position is contained "inside" a cursor.
	 * 
	 * To make the cursor moveable fast, we enlarge the test area in all
	 * directions by some value "moveMargin". This value may need to be
	 * calculated, currently a fixed value is used.
	 * 
	 * 
	 * @param e
	 *            The mouse event e
	 * @return true or false
	 */
	protected boolean contains(MouseEvent e) {

		if (!enabled)
			return false;

		boolean ret = false;
		int moveMargin = 5;
		// convert viewport coordinates to world coordinates
		int x = Vc2Wc_X(e.x);
		int y = Vc2Wc_Y(e.y);

		System.out.println("Test (" + x + "," + y + ") against (" + xPos + ","
				+ yPos + ")");
		if ((x >= xPos - moveMargin && x <= xPos + cursorWidth + moveMargin)
				&& (y >= yPos - moveMargin && y <= xPos + cursorHeight
						+ moveMargin))
			ret = true;
		return ret;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.redrawCanvas(xPos, yPos);
	}

	/**
	 * Viewport coordinate X -> World coordinate X
	 * 
	 * @param x
	 * @return
	 */
	public int Vc2Wc_X(int x) {
		ScrollBar hBar = canvas.getHorizontalBar();
		int hSelection = hBar.getSelection();
		x += hSelection;
		return x;
	}

	public int Vc2Wc_Y(int y) {
		// adjust x position for correct placement in visible area
		ScrollBar vBar = canvas.getVerticalBar();
		int vSelection = vBar.getSelection();
		y += vSelection;
		return y;
	}

	/**
	 * World coordinate X -> Viewport coordinate X
	 * 
	 * @param x
	 * @return
	 */
	public int Wc2Vc_X(int x) {
		ScrollBar hBar = canvas.getHorizontalBar();
		int hSelection = hBar.getSelection();
		x -= hSelection;
		return x;
	}

	public int Wc2Vc_Y(int y) {
		// adjust x position for correct placement in visible area
		ScrollBar vBar = canvas.getVerticalBar();
		int vSelection = vBar.getSelection();
		y -= vSelection;
		return y;
	}

}
