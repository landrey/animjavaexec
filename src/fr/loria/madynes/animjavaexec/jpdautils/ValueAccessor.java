package fr.loria.madynes.animjavaexec.jpdautils;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.VoidValue;

public interface ValueAccessor {

		void accessBoolean(BooleanValue b);
		void accesInt(IntegerValue i);
		void accessString(StringReference s);
		void accesInstance(ObjectReference  oi);
		void accessVoid(VoidValue v);
		void accessNull();
		void accessArrayReference(ArrayReference v);
}
