package jspell;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

public enum JavaType {
	TYPE("Type", "Classes, Interfaces..."), ANNOTATION("Annotation"), FIELD("Field"), LOCAL_VARIABLE("Local variable"), METHOD(
			"Method"), PACKAGE_DECLARATION("Package"), CONSTANT("Constant", "static final field");

	private final String displayName;

	private final String tooltip;

	JavaType(String displayName) {
		this.displayName = displayName;
		this.tooltip = "";
	}

	JavaType(String displayName, String tooltip) {
		this.displayName = displayName;
		this.tooltip = tooltip;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getTooltip() {
		return tooltip;
	}

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
				return null;
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
