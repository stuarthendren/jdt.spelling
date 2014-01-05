package jdt.spelling.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;

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
@SuppressWarnings("restriction")
public class LocalVariableDetector extends ASTVisitor {

	private final List<ILocalVariable> localVariables = new ArrayList<ILocalVariable>();

	private final CompilationUnit compilationUnit;

	public LocalVariableDetector(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
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
		if (binding != null && !binding.isField() && !binding.isEnumConstant()) {
			ILocalVariable local = (ILocalVariable) binding.getJavaElement();

			local = adjustNameRange(local);

			localVariables.add(local);
		}
	}

	/*
	 * Name range in some locals includes the '= 0' this stops the refactoring working so adjust to
	 * the simple name only
	 */
	private ILocalVariable adjustNameRange(ILocalVariable local) {
		try {
			ISourceRange sourceRange = local.getSourceRange();
			local = new LocalVariable((JavaElement) local.getParent(), local.getElementName(), sourceRange.getOffset(),
					sourceRange.getLength(), local.getNameRange().getOffset(), local.getNameRange().getOffset()
							+ local.getElementName().length() - 1, local.getTypeSignature(), null, local.getFlags(),
					local.isParameter());
		} catch (JavaModelException e) {
			// IGNORE
		}
		return local;
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