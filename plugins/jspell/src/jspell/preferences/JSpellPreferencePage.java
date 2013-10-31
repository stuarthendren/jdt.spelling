package jspell.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jspell.JSpellPlugin;
import jspell.JSpellPreferences;
import jspell.JavaType;
import jspell.messages.Messages;
import jspell.spelling.JavaNameType;

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

public class JSpellPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final Map<Combo, JavaType> comboMap = new HashMap<Combo, JavaType>();

	private Button singleLetter;

	public JSpellPreferencePage() {
		setPreferenceStore(JSpellPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// DO NOTHING
	}

	@Override
	protected void performDefaults() {
		JSpellPreferences.restoreDefaults();
		setValues();
		super.performDefaults();
	}

	public void setValues() {
		for (Entry<Combo, JavaType> entry : comboMap.entrySet()) {
			Combo combo = entry.getKey();
			JavaType javaType = entry.getValue();
			combo.setText(JSpellPreferences.getJavaNameType(javaType).getDisplayName());
		}

		singleLetter.setSelection(JSpellPreferences.getBoolean(JSpellPreferences.JSPELL_IGNORE_SINGLE_LETTER));
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
		label.setText(Messages.JSpellPluginPrefs_ignore_single_letter);

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
					JSpellPreferences.setJavaNameType(javaType, value);
				}
			}

			JSpellPreferences.setBoolean(JSpellPreferences.JSPELL_IGNORE_SINGLE_LETTER, singleLetter.getSelection());

			// rebuild?

		} catch (Exception e) {
			JSpellPlugin.log(e);
		}

		return true;
	}
}