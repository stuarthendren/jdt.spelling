package jspell;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

public enum JavaType {
	TYPE, ANNOTATION, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE_DECLARATION, CONSTANT, UNKNOWN;

	public static JavaType convert(IJavaElement javaElement) {
		switch (javaElement.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			return null;
		case IJavaElement.ANNOTATION:
			return ANNOTATION;
		case IJavaElement.FIELD:
			IMember member = (IMember) javaElement;
			try {
				if (Flags.isFinal(member.getFlags())) {
					return CONSTANT;
				}
			} catch (JavaModelException e) {
				return UNKNOWN;
			}
			return FIELD;
		case IJavaElement.LOCAL_VARIABLE:
			return LOCAL_VARIABLE;
		case IJavaElement.METHOD:
			return METHOD;
		case IJavaElement.PACKAGE_DECLARATION:
			return PACKAGE_DECLARATION;
		case IJavaElement.TYPE:
			return TYPE;
		default:
			return null;
		}
	}
}
