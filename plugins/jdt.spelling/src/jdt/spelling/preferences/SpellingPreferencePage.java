package jdt.spelling.preferences;

import java.io.File;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.dictionary.CodeWordStatus;
import jdt.spelling.messages.Messages;

@SuppressWarnings("restriction")
public class SpellingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button enableButton;

	private Text smallWordText;

	private Button localVariablesButton;

	private Button codeWordOffButton;

	private Button codeWordIgnoreButton;

	private Button codeWordSuggestButton;

	private Text additionText;

	private Text ignoreText;

	private LocalesCombo mainDictionaryCombo;

	private Group optionsGroup;

	private Group codeWordsGroup;

	private Group dictionariesGroup;

	private Label codeWordsLabel;

	public SpellingPreferencePage() {
		setPreferenceStore(Plugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// DO NOTHING
	}

	@Override
	protected void performDefaults() {
		Preferences.restoreDefaults();
		setValues();
		super.performDefaults();
	}

	public void setValues() {
		boolean enabled = Preferences.getBoolean(Preferences.JDT_SPELLING_ENABLED);
		enableButton.setSelection(enabled);
		setEnabled(enabled);

		smallWordText.setText(Preferences.getString(Preferences.JDT_SPELLING_IGNORE_SMALL_WORDS));
		localVariablesButton.setSelection(Preferences.getBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL));
		additionText.setText(Preferences.getString(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY));
		ignoreText.setText(Preferences.getString(Preferences.JDT_SPELLING_IGNORE_DICTIONARY));
		Locale locale = Preferences.getDictionaryLocale();
		mainDictionaryCombo.setSelectedLocale(locale);

		switch (Preferences.getCodeWordStatus()) {
		case SUGGEST:
			codeWordSuggestButton.setSelection(true);
			break;
		case IGNORE:
			codeWordIgnoreButton.setSelection(true);
			break;
		case OFF:
			codeWordOffButton.setSelection(true);
			break;
		default:
			break;
		}

		updateCodeWordEnabled();

		validate();
	}

	private void setEnabled(boolean enabled) {
		optionsGroup.setEnabled(enabled);
		codeWordOffButton.setEnabled(enabled);
		codeWordIgnoreButton.setEnabled(enabled);
		codeWordSuggestButton.setEnabled(enabled);
		dictionariesGroup.setEnabled(enabled);
		smallWordText.setEnabled(enabled);
		localVariablesButton.setEnabled(enabled);
		additionText.setEnabled(enabled);
		ignoreText.setEnabled(enabled);
		mainDictionaryCombo.setEnabled(enabled);
	}

	@Override
	public Control createContents(Composite ancestor) {

		Composite parentComposite = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parentComposite);

		enableButton = createCheckButton(parentComposite, Messages.SpellingPreferencePage_enable);
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(enableButton.getSelection());
			}
		});

		optionsGroup = createOptionsGroup(parentComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(optionsGroup);
		codeWordsGroup = createCodeWordsGroup(parentComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(codeWordsGroup);

		dictionariesGroup = createDictionariesGroup(parentComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(dictionariesGroup);

		setValues();

		return dictionariesGroup;
	}

	private Group createDictionariesGroup(Composite parentComposite) {
		Group dictionariesGroup = new Group(parentComposite, SWT.NONE);
		dictionariesGroup.setText(Messages.SpellingPreferencePage_dictionary_group_label);

		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(dictionariesGroup);

		mainDictionaryCombo = addLocalesField(dictionariesGroup,
				Messages.SpellingPreferencePage_locale_dictionary_label);

		mainDictionaryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateCodeWordEnabled();
			}
		});

		additionText = addTextField(dictionariesGroup, Messages.SpellingPreferencePage_user_dictionary_label,
				Messages.SpellingPreferencePage_user_dictionary_hint);
		ignoreText = addTextField(dictionariesGroup, Messages.SpellingPreferencePage_ignore_dictionary_label,
				Messages.SpellingPreferencePage_ignore_dictionary_hint);
		return dictionariesGroup;
	}

	private Group createOptionsGroup(Composite parentComposite) {
		Group optionsGroup = new Group(parentComposite, SWT.NONE);
		optionsGroup.setText(Messages.SpellingPreferencePage_options_label);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(optionsGroup);

		smallWordText = createNumericTextField(optionsGroup, Messages.SpellingPreferencePage_ignore_small_words_label, Messages.SpellingPreferencePage_ignore_small_words_hint);		
		localVariablesButton = createCheckButton(optionsGroup, Messages.SpellingPreferencePage_check_local_variables);
		localVariablesButton.setToolTipText(Messages.SpellingPreferencePage_check_local_variables_tooltip);
		return optionsGroup;
	}

	private Group createCodeWordsGroup(Composite parentComposite) {
		Group codeWordsGroup = new Group(parentComposite, SWT.NONE);
		codeWordsGroup.setText(Messages.SpellingPreferencePage_code_words_label);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(codeWordsGroup);

		codeWordOffButton = createRadioButton(codeWordsGroup, Messages.SpellingPreferencePage_code_words_off);
		codeWordIgnoreButton = createRadioButton(codeWordsGroup, Messages.SpellingPreferencePage_code_words_ignore);
		codeWordSuggestButton = createRadioButton(codeWordsGroup, Messages.SpellingPreferencePage_code_words_suggest);

		codeWordsLabel = new Label(codeWordsGroup, SWT.NONE);
		codeWordsLabel.setText(Messages.SpellingPreferencePage_code_words_tooltip);

		return codeWordsGroup;
	}

	private LocalesCombo addLocalesField(Composite dictionariesComposite, String label) {
		Label dictionaryLabel = new Label(dictionariesComposite, SWT.NONE);
		dictionaryLabel.setText(label);
		LocalesCombo combo = new LocalesCombo(dictionariesComposite, SWT.NONE, Preferences.getAvailableLocales());
		GridDataFactory.fillDefaults().span(2, 1).applyTo(combo);

		return combo;
	}

	private Button createCheckButton(Composite configComposite, String label) {
		Button button = new Button(configComposite, SWT.CHECK);
		button.setText(label);
		return button;
	}

	private Button createRadioButton(Composite configComposite, String label) {
		Button button = new Button(configComposite, SWT.RADIO);
		button.setText(label);
		return button;
	}
	
	private Text createNumericTextField(Composite parent, String label, String toolTip) {
		
		Label additionsLabel = new Label(parent, SWT.NONE);
		additionsLabel.setText(label);

		final Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		if (toolTip != null) {
			text.setToolTipText(toolTip);
		}
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(text);

		text.addListener(SWT.Verify, new Listener() {  
			  @Override  
			  public void handleEvent(Event event) {  
		          event.doit = true;  
			      String length = ((Text)event.widget).getText().trim();
			      if (length.length() > 0) {
				      try {
				          Integer.parseInt(length);
				          event.doit = true;  
				      } catch(Exception ex){  
				         event.doit = false;  
				      }
			      }
			   }  
		}); 
		
		return text;
	}

	private Text addTextField(Composite parent, String label, String toolTip) {
		Label additionsLabel = new Label(parent, SWT.NONE);
		additionsLabel.setText(label);

		final Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);

		if (toolTip != null) {
			text.setToolTipText(toolTip);
		}

		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		Composite buttons = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).applyTo(buttons);
		buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		Button browseButton = new Button(buttons, SWT.PUSH);
		browseButton.setText(Messages.SpellingPreferencePage_browse_label);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleBrowseButtonSelected(text);
			}
		});

		SWTUtil.setButtonDimensionHint(browseButton);
		browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		Button variablesButton = new Button(buttons, SWT.PUSH);
		variablesButton.setText(Messages.SpellingPreferencePage_variables_label);
		variablesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleVariablesButtonSelected(text);
			}

		});
		SWTUtil.setButtonDimensionHint(variablesButton);
		variablesButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		return text;
	}

	private void validate() {
		IStatus additions = validateAbsoluteFilePath(additionText.getText());
		IStatus ignore = validateAbsoluteFilePath(ignoreText.getText());
		if (additions.isOK() && ignore.isOK()) {
			setValid(true);
			setErrorMessage(null);
		} else {
			setValid(false);
			if (!additions.isOK()) {
				setErrorMessage(additions.getMessage());
			} else {
				setErrorMessage(ignore.getMessage());
			}
		}
	}

	protected void handleVariablesButtonSelected(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(text.getShell());
		if (dialog.open() == Window.OK) {
			text.setText(text.getText() + dialog.getVariableExpression());
		}
	}

	/**
	 * Handles selections of the browse button.
	 */
	protected void handleBrowseButtonSelected(Text text) {
		final FileDialog dialog = new FileDialog(text.getShell(), SWT.OPEN);
		dialog.setFilterPath(text.getText());

		final String path = dialog.open();
		if (path != null) {
			text.setText(path);
		}
	}

	/**
	 * Validates that the file with the specified absolute path exists and can be opened.
	 *
	 * @param path
	 *            The path of the file to validate
	 * @return a status without error if the path is valid
	 */
	private IStatus validateAbsoluteFilePath(String path) {

		final StatusInfo status = new StatusInfo();
		if (path == null || path.isEmpty()) {
			return status;
		}
		IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			path = variableManager.performStringSubstitution(path);
			if (path.length() > 0) {

				final File file = new File(path);
				if (!file.exists() && (!file.isAbsolute() || !file.getParentFile().canWrite())) {
					status.setError(Messages.SpellingPreferencePage_dictionary_error);
				} else if (file.exists()
						&& (!file.isFile() || !file.isAbsolute() || !file.canRead() || !file.canWrite())) {
					status.setError(Messages.SpellingPreferencePage_dictionary_error);
				}
			}
		} catch (CoreException e) {
			status.setError(e.getLocalizedMessage());
		}
		return status;
	}

	private void updateCodeWordEnabled() {
		boolean hasCodeWords = Preferences.hasCodeWords(mainDictionaryCombo.getSelectedLocale());
		codeWordsLabel.setVisible(!hasCodeWords);
		codeWordOffButton.setEnabled(hasCodeWords);
		codeWordIgnoreButton.setEnabled(hasCodeWords);
		codeWordSuggestButton.setEnabled(hasCodeWords);
	}

	private CodeWordStatus getSelectedCodeWordStatus() {
		if (codeWordSuggestButton.getSelection()) {
			return CodeWordStatus.SUGGEST;
		}
		if (codeWordIgnoreButton.getSelection()) {
			return CodeWordStatus.IGNORE;
		}
		return CodeWordStatus.OFF;
	}

	@Override
	public boolean performOk() {
		try {
			Preferences.setBoolean(Preferences.JDT_SPELLING_ENABLED, enableButton.getSelection());
			Preferences.setString(Preferences.JDT_SPELLING_IGNORE_SMALL_WORDS, smallWordText.getText());
			Preferences.setBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL, localVariablesButton.getSelection());
			Preferences.setString(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY, additionText.getText());
			Preferences.setString(Preferences.JDT_SPELLING_IGNORE_DICTIONARY, ignoreText.getText());
			Preferences.setDictionaryLocale(mainDictionaryCombo.getSelectedLocale());
			Preferences.setCodeWordStatus(getSelectedCodeWordStatus());
			Preferences.flush();

		} catch (Exception e) {
			Plugin.log(e);
		}

		return true;
	}

}