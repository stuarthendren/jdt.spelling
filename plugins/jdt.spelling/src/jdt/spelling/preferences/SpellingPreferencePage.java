package jdt.spelling.preferences;

import java.io.File;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.enums.JavaNameType;
import jdt.spelling.messages.Messages;

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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

@SuppressWarnings("restriction")
public class SpellingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button singleLetter;

	private Button localVariables;

	private Text additionText;

	private Text ignoreText;

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
		singleLetter.setSelection(Preferences.getBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER));
		localVariables.setSelection(Preferences.getBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL));
		additionText.setText(Preferences.getString(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY));
		ignoreText.setText(Preferences.getString(Preferences.JDT_SPELLING_IGNORE_DICTIONARY));
		validate();
	}

	@Override
	public Control createContents(Composite ancestor) {

		Composite parentComposite = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parentComposite);

		Composite configComposite = new Composite(parentComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(configComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(configComposite);

		GridDataFactory grab = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		JavaNameType[] values = JavaNameType.values();
		String[] names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			names[i] = values[i].getDisplayName();
		}

		singleLetter = createCheckButton(configComposite, Messages.SpellingPreferencePage_ignore_single_letter);

		localVariables = createCheckButton(configComposite, Messages.SpellingPreferencePage_check_local_variables);
		localVariables.setToolTipText(Messages.SpellingPreferencePage_check_local_variables_tooltip);

		Composite dictionariesComposite = new Composite(parentComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(dictionariesComposite);
		grab.applyTo(dictionariesComposite);

		additionText = addTextField(dictionariesComposite, Messages.SpellingPreferencePage_user_dictionary_label);
		ignoreText = addTextField(dictionariesComposite, Messages.SpellingPreferencePage_ignore_dictionary_label);

		setValues();

		return dictionariesComposite;
	}

	private Button createCheckButton(Composite configComposite, String spellingPreferencePage_ignore_single_letter) {
		Label label = new Label(configComposite, SWT.NONE);
		label.setText(spellingPreferencePage_ignore_single_letter);

		return new Button(configComposite, SWT.CHECK);
	}

	private Text addTextField(Composite dictionariesComposite, String label) {
		Label additionsLabel = new Label(dictionariesComposite, SWT.NONE);
		additionsLabel.setText(label);

		final Text text = new Text(dictionariesComposite, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		Composite buttons = new Composite(dictionariesComposite, SWT.NONE);
		buttons.setLayout(new GridLayout(2, true));
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

	@Override
	public boolean performOk() {
		try {
			Preferences.setBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER, singleLetter.getSelection());
			Preferences.setBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL, localVariables.getSelection());
			Preferences.setString(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY, additionText.getText());
			Preferences.setString(Preferences.JDT_SPELLING_IGNORE_DICTIONARY, ignoreText.getText());

			Preferences.flush();

		} catch (Exception e) {
			Plugin.log(e);
		}

		return true;
	}
}