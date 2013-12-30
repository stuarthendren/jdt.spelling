package jdt.spelling.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * This class looks for local declarations of local variables
 * 
 * <p>
 * Adapted from Thomas Kuhn article <a href=
 * "http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html"
 * >Article-JavaCodeManipulation_AST</a>
 * </p>
 * 
 * 
 */
public class LocalVariableDetector extends ASTVisitor {

	private final List<ILocalVariable> localVariables = new ArrayList<ILocalVariable>();

	private final CompilationUnit compilationUnit;

	public LocalVariableDetector(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		compilationUnit = (CompilationUnit) parser.createAST(null);
	}

	/**
	 * Looks for local variable declarations as method parameters.
	 * 
	 * @param node
	 *            the node to visit
	 * @return static {@code false} to prevent that the simple name in the declaration is understood
	 *         by {@link #visit(SimpleName)} as reference
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean visit(MethodDeclaration node) {
		List<SingleVariableDeclaration> parameters = node.parameters();
		for (SingleVariableDeclaration declaration : parameters) {
			resolve(declaration);
		}
		return true;
	}

	/**
	 * Looks for local variable declarations.
	 * 
	 * @param node
	 *            the node to visit
	 * @return static {@code false} to prevent that the simple name in the declaration is understood
	 *         by {@link #visit(SimpleName)} as reference
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Iterator<?> iter = node.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
			resolve(fragment);
		}
		return false;
	}

	/**
	 * For every occurrence of a local variable, the binding is resolved to an ILocalVariable and
	 * added to the list.
	 * 
	 * @param declaration
	 */
	private void resolve(VariableDeclaration declaration) {
		IVariableBinding binding = declaration.resolveBinding();
		if (!binding.isField() && !binding.isEnumConstant()) {
			ILocalVariable local = (ILocalVariable) binding.getJavaElement();
			localVariables.add(local);
		}
	}

	/**
	 * Getter for the resulting map.
	 * 
	 * @return a map with variable bindings as keys and {@link VariableBindingManager} as values
	 */
	public List<ILocalVariable> getLocalVariables() {
		return localVariables;
	}

	/**
	 * Starts the process.
	 * 
	 * @param unit
	 *            the AST root node. Bindings have to have been resolved.
	 */
	public void process() {
		compilationUnit.accept(this);
	}
}