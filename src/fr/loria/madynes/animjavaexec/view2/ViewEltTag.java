package fr.loria.madynes.animjavaexec.view2;

import fr.loria.madynes.animjavaexec.execution.model.StackEltTag;

enum ViewEltTag {
	THIS, PARAM, VAR, RETURN, SEPARATOR, ARRAY_HEADER, ARRAY_LENGTH, ARRAY_ELT, INSTANCE_HEADER, INSTANCE_VAR;
	
	public static ViewEltTag convert(StackEltTag modelTag){
		switch (modelTag){
		case THIS:
			return THIS;
		case PARAM:
			return PARAM;
		case VAR:
			return VAR;
		case SEPARATOR:
			return SEPARATOR;
		default:
			return SEPARATOR; // Useless by eclipse/javac complain...
		}
	}
}
