package de.spurtikus.softtek.decorators;

import de.spurtikus.softtek.tek.Tek1241Device;

public interface TimeLineDecorator {
	
	void init( Tek1241Device tekDevice );
	
	void decorate();
	
	void draw();
	
	void print();
	
	void setClazz( String clazz );
	
	String getClazz();
	
}
