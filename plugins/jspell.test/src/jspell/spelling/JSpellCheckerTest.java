package jspell.spelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jspell.JSpellConfiguration;
import jspell.JavaType;
import jspell.dictionary.PersistentSpellDictionary;
import jspell.messages.Messages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.RankedWordProposal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JSpellCheckerTest {

	private static final String INCORRECT = "incorect";

	private static final String CORRECT = "incorrect";

	private static final Locale LOCALE = Locale.getDefault();

	private JSpellChecker checker;
	private PersistentSpellDictionary additionsDictionary;
	private PersistentSpellDictionary ignoreDictionary;
	private JSpellConfiguration configuration;
	private IJavaElement element;

	@Before
	public void setUp() throws Exception {
		additionsDictionary = mock(PersistentSpellDictionary.class);
		ignoreDictionary = mock(PersistentSpellDictionary.class);
		configuration = mock(JSpellConfiguration.class);
		element = mock(IJavaElement.class);
		checker = new JSpellChecker(configuration, additionsDictionary, ignoreDictionary, LOCALE);
	}

	@After
	public void tearDown() throws Exception {
		additionsDictionary = null;
		ignoreDictionary = null;
		checker = null;
	}

	@Test
	public void testAddDictionary() {
		ISpellDictionary dict = mock(ISpellDictionary.class);
		when(dict.isCorrect(INCORRECT)).thenReturn(true);
		assertFalse(checker.isCorrect(INCORRECT));
		checker.addDictionary(dict);
		assertTrue(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testAddWord() {
		when(additionsDictionary.acceptsWords()).thenReturn(true);
		checker.addWord(INCORRECT);
		verify(additionsDictionary).addWord(INCORRECT);
	}

	@Test
	public void testExecute() {
		Collection<JSpellEvent> events = new HashSet<JSpellEvent>();
		when(element.getElementType()).thenReturn(IJavaElement.METHOD);
		when(configuration.getJavaNameType(JavaType.METHOD)).thenReturn(JavaNameType.LOWER_CAMEL_CASE);
		when(element.getElementName()).thenReturn(INCORRECT);
		checker.execute(events, element);
		assertEquals(1, events.size());
		JSpellEvent event = events.iterator().next();
		assertEquals(element, event.getJavaElement());
		assertEquals(INCORRECT, event.getWord());
		assertEquals(8, event.getLength());
		assertEquals(0, event.getOffset());
		assertEquals(INCORRECT + Messages.JSpellChecker_has_incorrect_spelling, event.getMessage());
		assertTrue(event.isError());
	}

	@Test
	public void testGetProposals() {
		RankedWordProposal rwp = new RankedWordProposal(CORRECT, 1);
		Set<RankedWordProposal> rankedProposals = new HashSet<RankedWordProposal>();
		rankedProposals.add(rwp);
		when(additionsDictionary.getProposals(INCORRECT, false)).thenReturn(rankedProposals);
		List<String> proposals = checker.getProposals(INCORRECT);
		assertEquals(1, proposals.size());
		assertTrue(proposals.contains(CORRECT));
	}

	@Test
	public void testIgnoreWord() {
		when(ignoreDictionary.acceptsWords()).thenReturn(true);
		when(ignoreDictionary.isCorrect(INCORRECT)).thenReturn(true);
		checker.ignoreWord(INCORRECT);
		assertTrue(checker.isCorrect(INCORRECT));
		verify(ignoreDictionary).addWord(INCORRECT);
	}

	@Test
	public void testRemoveDictionary() {
		ISpellDictionary dict = mock(ISpellDictionary.class);
		when(dict.isCorrect(INCORRECT)).thenReturn(true);
		assertFalse(checker.isCorrect(INCORRECT));
		checker.addDictionary(dict);
		checker.removeDictionary(dict);
		assertFalse(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testClearIgnoredWords() {
		checker.clearIgnoredWords();
		verify(ignoreDictionary).clear();
	}

	@Test
	public void testClearAddedWords() {
		checker.clearAddedWords();
		verify(additionsDictionary).clear();
	}

}
