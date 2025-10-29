/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences.PrefsMessages;
import java.util.Objects;

/**
 * The preference metadata provides the information needed to configure
 * everything about the preference except the preference value itself.
 *
 * @param <V> the value type for the preference
 *
 * @see IPreferenceMetadataStore
 *
 * @since 3.8
 */
public final class PreferenceMetadata<V> {

    private final V defaultValue;
    private final String name;
    private final String description;

    /**
     * Created an instance of {@link PreferenceMetadata} of all the the given
     * parameters
     *
     * @param clazz the value type of the preference, must not be
     * <code>null</code>
     * @param identifier the identifier of the preference, must not be
     * <code>null</code>
     * @param defaultValue the default value of the preference, must not be
     * <code>null</code>
     * @param name the name of the preference, must not be <code>null</code>
     * @param description the description of the preference, must not be
     * <code>null</code>
     */
    public PreferenceMetadata(Class<V> clazz, String identifier, V defaultValue, String name, String description) {
        Objects.requireNonNull(clazz, PrefsMessages.PreferenceMetadata_e_null_value_type);
        Objects.requireNonNull(identifier, PrefsMessages.PreferenceMetadata_e_null_identifier);
        Objects.requireNonNull(defaultValue, PrefsMessages.PreferenceMetadata_e_null_default_value);
        Objects.requireNonNull(name, PrefsMessages.PreferenceMetadata_e_null_name);
        Objects.requireNonNull(description, PrefsMessages.PreferenceMetadata_e_null_description);
        this.defaultValue = defaultValue;
        this.name = name;
        this.description = description;
    }

    /**
     * The default value for the preference. Must not be <code>null</code>.
     *
     * @return the default value
     */
    public V defaultValue() {
        return defaultValue;
    }

    /**
     * Briefly describes the preference purpose, intended to be used in UI. Must not
     * be <code>null</code> and should be localized. Should not be blank.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Widely describes the preference purpose, intended to be used in UI. Must not
     * be <code>null</code> and should be localized. May be blank.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

}
