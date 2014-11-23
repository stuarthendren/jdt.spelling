package jdt.spelling.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdt.spelling.dictionary.CodeWordStatus;
import jdt.spelling.dictionary.LocaleSensitiveSpellDictionary;
import jdt.spelling.dictionary.PersistentSpellDictionary;
import jdt.spelling.messages.Messages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.spelling.engine.RankedWordProposal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class CheckerTest {

	private static final String INCORRECT = "incorect";

	private static final String SINGLE = "sSingle";

	private static final String CORRECT = "incorrect";

	private Checker checker;
	private PersistentSpellDictionary additionsDictionary;
	private PersistentSpellDictionary ignoreDictionary;
	private LocaleSensitiveSpellDictionary mainDictionary;
	private LocaleSensitiveSpellDictionary codeWordDictionary;
	private IJavaElement element;

	private LocaleSensitiveSpellDictionary incorrectDictionary;

	private Set<RankedWordProposal> rankedProposals;

	@Before
	public void setUp() throws Exception {
		additionsDictionary = mock(PersistentSpellDictionary.class);
		ignoreDictionary = mock(PersistentSpellDictionary.class);
		mainDictionary = mock(LocaleSensitiveSpellDictionary.class);
		codeWordDictionary = mock(LocaleSensitiveSpellDictionary.class);

		incorrectDictionary = mock(LocaleSensitiveSpellDictionary.class);
		when(incorrectDictionary.isCorrect(INCORRECT)).thenReturn(true);

		RankedWordProposal proposal = new RankedWordProposal(CORRECT, 1);
		rankedProposals = new HashSet<RankedWordProposal>();
		rankedProposals.add(proposal);

		element = mock(IJavaElement.class);
		checker = new Checker(additionsDictionary, ignoreDictionary, mainDictionary, codeWordDictionary);
	}

	@After
	public void tearDown() throws Exception {
		checker = null;
		element = null;
		rankedProposals = null;
		incorrectDictionary = null;
		codeWordDictionary = null;
		mainDictionary = null;
		ignoreDictionary = null;
		additionsDictionary = null;
	}

	@Test
	public void testInEmptyCheckerIncorrectIsFalse() {
		assertFalse(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testSetMainDictionary() {
		checker.setMainDictionary(incorrectDictionary);
		assertTrue(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testSetCodeWordsNull() {
		checker.setCodeWordDictionary(null);
		assertFalse(checker.isCorrect(INCORRECT));
		assertNotNull(checker.getProposals(INCORRECT));
	}

	@Test
	public void testCodeWordsOff() {
		checker.setCodeWordDictionary(incorrectDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.OFF);
		assertFalse(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testCodeWordsIgnore() {
		checker.setCodeWordDictionary(incorrectDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.IGNORE);
		assertTrue(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testCodeWordsSuggest() {
		checker.setCodeWordDictionary(incorrectDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.SUGGEST);
		assertTrue(checker.isCorrect(INCORRECT));
	}

	@Test
	public void testAddWord() {
		when(additionsDictionary.acceptsWords()).thenReturn(true);
		checker.addWord(INCORRECT);
		verify(additionsDictionary).addWord(INCORRECT);
	}

	@Test
	public void testSingle() {
		Collection<SpellingEvent> events = new HashSet<SpellingEvent>();
		when(element.getElementType()).thenReturn(IJavaElement.METHOD);
		when(element.getElementName()).thenReturn(SINGLE);
		checker.execute(events, element);
		assertEquals(1, events.size());
		SpellingEvent event = events.iterator().next();
		assertEquals(element, event.getJavaElement());
		assertTrue(event.getLength() > 1);
	}

	@Test
	public void testExecute() {
		Collection<SpellingEvent> events = new HashSet<SpellingEvent>();
		when(element.getElementType()).thenReturn(IJavaElement.METHOD);
		when(element.getElementName()).thenReturn(INCORRECT);
		checker.execute(events, element);
		assertEquals(1, events.size());
		SpellingEvent event = events.iterator().next();
		assertEquals(element, event.getJavaElement());
		assertEquals(INCORRECT, event.getWord());
		assertEquals(8, event.getLength());
		assertEquals(0, event.getOffset());
		assertEquals(INCORRECT + SpellingEvent.SPACE + Messages.Checker_has_incorrect_spelling, event.getMessage());
	}

	@Test
	public void testGetMainProposals() {
		when(additionsDictionary.getProposals(INCORRECT, false)).thenReturn(rankedProposals);
		List<String> proposals = checker.getProposals(INCORRECT);
		assertEquals(1, proposals.size());
		assertTrue(proposals.contains(CORRECT));
	}

	@Test
	public void testGetCodeProposalsSuggest() {
		when(codeWordDictionary.getProposals(INCORRECT, false)).thenReturn(rankedProposals);
		checker.setCodeWordDictionary(codeWordDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.SUGGEST);
		List<String> proposals = checker.getProposals(INCORRECT);
		assertEquals(1, proposals.size());
		assertTrue(proposals.contains(CORRECT));
	}

	@Test
	public void testGetCodeProposalsOff() {
		when(codeWordDictionary.getProposals(INCORRECT, false)).thenReturn(rankedProposals);
		checker.setCodeWordDictionary(codeWordDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.OFF);
		List<String> proposals = checker.getProposals(INCORRECT);
		assertEquals(0, proposals.size());
	}

	@Test
	public void testGetCodeProposalsIgnore() {
		when(codeWordDictionary.getProposals(INCORRECT, false)).thenReturn(rankedProposals);
		checker.setCodeWordDictionary(codeWordDictionary);
		checker.setCodeWordsStatus(CodeWordStatus.IGNORE);
		List<String> proposals = checker.getProposals(INCORRECT);
		assertEquals(0, proposals.size());
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
