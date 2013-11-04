package jdt.spelling.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.checker.JavaNameType;
import jdt.spelling.checker.JavaType;
import jdt.spelling.messages.Messages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SpellingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final Map<Combo, JavaType> comboMap = new HashMap<Combo, JavaType>();

	private Button singleLetter;

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
		for (Entry<Combo, JavaType> entry : comboMap.entrySet()) {
			Combo combo = entry.getKey();
			JavaType javaType = entry.getValue();
			combo.setText(Preferences.getJavaNameType(javaType).getDisplayName());
		}

		singleLetter.setSelection(Preferences.getBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER));
	}

	@Override
	public Control createContents(Composite ancestor) {
		Composite parentComposite = new Composite(ancestor, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parentComposite);
		GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, false);

		JavaNameType[] values = JavaNameType.values();
		String[] names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			names[i] = values[i].getDisplayName();
		}

		for (JavaType type : JavaType.values()) {
			Label label = new Label(parentComposite, SWT.NONE);
			label.setText(type.getDisplayName());
			label.setToolTipText(type.getTooltip());

			Combo combo = new Combo(parentComposite, SWT.NONE);
			combo.setItems(names);
			comboMap.put(combo, type);
			grab.applyTo(combo);
		}

		Label label = new Label(parentComposite, SWT.NONE);
		label.setText(Messages.SpellingPreferencePage_ignore_single_letter);

		singleLetter = new Button(parentComposite, SWT.CHECK);

		setValues();

		return parentComposite;
	}

	@Override
	public boolean performOk() {
		try {
			JavaNameType[] values = JavaNameType.values();

			for (Entry<Combo, JavaType> entry : comboMap.entrySet()) {
				Combo combo = entry.getKey();
				JavaType javaType = entry.getValue();
				int selectionIndex = combo.getSelectionIndex();
				if (selectionIndex > -1) {
					JavaNameType value = values[selectionIndex];
					Preferences.setJavaNameType(javaType, value);
				}
			}

			Preferences.setBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER, singleLetter.getSelection());

			// rebuild?

		} catch (Exception e) {
			Plugin.log(e);
		}

		return true;
	}
}