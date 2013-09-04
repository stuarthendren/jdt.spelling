package jspell;

public class JSpellConfiguration {

	private static final JSpellConfiguration INSTANCE = new JSpellConfiguration();

	public static JSpellConfiguration getInstance() {
		return INSTANCE;
	}

	private JSpellConfiguration() {
	}

	private JavaNameType type = JavaNameType.UPPER_CAMEL_CASE;
	private JavaNameType annotation = JavaNameType.UPPER_CAMEL_CASE;
	private JavaNameType field = JavaNameType.LOWER_CAMEL_CASE;
	private JavaNameType localVarable = JavaNameType.LOWER_CAMEL_CASE;
	private JavaNameType method = JavaNameType.LOWER_CAMEL_CASE;
	private JavaNameType packageDeclaration = JavaNameType.DOT;
	private JavaNameType constant = JavaNameType.UPPER;
	private JavaNameType unknown = JavaNameType.LOWER_CAMEL_CASE;

	public JavaNameType getType() {
		return type;
	}

	public void setType(JavaNameType type) {
		this.type = type;
	}

	public JavaNameType getAnnotation() {
		return annotation;
	}

	public void setAnnotation(JavaNameType annotation) {
		this.annotation = annotation;
	}

	public JavaNameType getField() {
		return field;
	}

	public void setField(JavaNameType field) {
		this.field = field;
	}

	public JavaNameType getLocalVarable() {
		return localVarable;
	}

	public void setLocalVarable(JavaNameType localVarable) {
		this.localVarable = localVarable;
	}

	public JavaNameType getMethod() {
		return method;
	}

	public void setMethod(JavaNameType method) {
		this.method = method;
	}

	public JavaNameType getPackageDeclaration() {
		return packageDeclaration;
	}

	public void setPackageDeclaration(JavaNameType packageDeclaration) {
		this.packageDeclaration = packageDeclaration;
	}

	public JavaNameType getConstant() {
		return constant;
	}

	public void setConstant(JavaNameType constant) {
		this.constant = constant;
	}

	public JavaNameType getUnknown() {
		return unknown;
	}

	public void setUnknown(JavaNameType unknown) {
		this.unknown = unknown;
	}

	public JavaNameType getJavaNameType(JavaType javaType) {
		switch (javaType) {
		case ANNOTATION:
			return annotation;
		case CONSTANT:
			return constant;
		case FIELD:
			return field;
		case LOCAL_VARIABLE:
			return localVarable;
		case METHOD:
			return method;
		case PACKAGE_DECLARATION:
			return packageDeclaration;
		case TYPE:
			return type;
		default:
			return unknown;
		}
	}

}
