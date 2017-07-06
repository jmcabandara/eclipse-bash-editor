package de.jcup.basheditor.preferences;
/*
 * Copyright 2016 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.jcup.basheditor.BashEditor;
import de.jcup.basheditor.BashEditorActivator;
import de.jcup.basheditor.ColorUtil;
import de.jcup.basheditor.EclipseUtil;

public class BashEditorPreferences {

	private static BashEditorPreferences INSTANCE = new BashEditorPreferences();
	private IPreferenceStore store;

	private BashEditorPreferences() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, BashEditorActivator.PLUGIN_ID);
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event==null){
					return;
				}
				String property = event.getProperty();
				if (property==null){
					return;
				}
				boolean colorChanged = false;
				for (BashEditorSyntaxColorPreferenceConstants c: BashEditorSyntaxColorPreferenceConstants.values()){
					if (property.equals(c.getId())){
						colorChanged=true;
						break;
					}
				}
				if (colorChanged){
					updateColorsInEGradleEditors();
				}
				
				
			}

			private void updateColorsInEGradleEditors() {
				/* inform all EGradle editors about color changes*/
				IWorkbenchPage activePage = EclipseUtil.getActivePage();
				if (activePage==null){
					return;
				}
				IEditorReference[] references = activePage.getEditorReferences();
				for (IEditorReference ref: references){
					IEditorPart editor = ref.getEditor(false);
					if (editor==null){
						continue;
					}
					if (! (editor instanceof BashEditor)){
						continue;
					}
					BashEditor geditor = (BashEditor) editor;
					geditor.handleColorSettingsChanged();
				}
			}
		});
			
	}

	public String getStringPreference(BashEditorPreferenceConstants id) {
		String data = getPreferenceStore().getString(id.getId());
		if (data == null) {
			data = "";
		}
		return data;
	}

	public boolean getBooleanPreference(BashEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getBoolean(id.getId());
		return data;
	}

	public void setBooleanPreference(BashEditorPreferenceConstants id, boolean value) {
		getPreferenceStore().setValue(id.getId(), value);
	}

	public IPreferenceStore getPreferenceStore() {
		return store;
	}

	public boolean getDefaultBooleanPreference(BashEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getDefaultBoolean(id.getId());
		return data;
	}

	public RGB getColor(PreferenceIdentifiable identifiable) {
		RGB color = PreferenceConverter.getColor(getPreferenceStore(), identifiable.getId());
		return color;
	}
	
	/**
	 * Returns color as a web color in format "#RRGGBB"
	 * @param identifiable
	 * @return web color string
	 */
	public String getWebColor(PreferenceIdentifiable identifiable) {
		RGB color = getColor(identifiable);
		if (color==null){
			return null;
		}
		String webColor= ColorUtil.convertToHexColor(color);
		return webColor;
	}

	public void setDefaultColor(PreferenceIdentifiable identifiable, RGB color) {
		PreferenceConverter.setDefault(getPreferenceStore(), identifiable.getId(), color);
	}
	
	public static BashEditorPreferences getInstance() {
		return INSTANCE;
	}

	
}
