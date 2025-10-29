/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.DefaultScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.InstanceScope;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Properties;

/**
 * A table of preference settings, mapping named properties to values. Property
 * names are non-empty strings; property values can be either booleans,
 * non-null strings, or values of one of the primitive number types.
 * The table consists of two, sparse, layers: the lower layer holds default values
 * for properties, and the upper layer holds explicitly set values for properties.
 * Normal retrieval looks for an explicitly set value for the given property in
 * the upper layer; if there is nothing for that property in the upper layer, it
 * next looks for a default value for the given property in the lower layer; if
 * there is nothing for that property in the lower layer, it returns a standard
 * default-default value. The default-default values for the primitive types are
 * as follows:
 * <ul>
 * <li><code>boolean</code> = <code>false</code></li>
 * <li><code>double</code> = <code>0.0</code></li>
 * <li><code>float</code> = <code>0.0f</code></li>
 * <li><code>int</code> = <code>0</code></li>
 * <li><code>long</code> = <code>0L</code></li>
 * <li><code>String</code> = <code>""</code> (the empty string)</li>
 * </ul>
 * <p>
 * Internally, all properties values (in both layers) are stored as strings.
 * Standard conversions to and from numeric and boolean types are performed on
 * demand.
 * </p>
 * <p>
 * The typical usage is to establish the defaults for all known properties
 * and then restore previously stored values for properties whose values
 * were explicitly set. The existing settings can be changed and new properties
 * can be set (<code>setValue</code>). If the values specified is the same as
 * the default value, the explicit setting is deleted from the top layer.
 * It is also possible to reset a property value back to the default value
 * using <code>setToDefault</code>. After the properties have been modified,
 * the properties with explicit settings are written to disk. The default values
 * are never saved. This two-tiered approach
 * to saving and restoring property setting minimizes the number of properties
 * that need to be persisted; indeed, the normal starting state does not require
 * storing any properties at all. It also makes it easy to use different
 * default settings in different environments while maintaining just those
 * property settings the user has adjusted.
 * </p>
 * <p>
 * A property change event is reported whenever a property's value actually
 * changes (either through <code>setValue</code>, <code>setToDefault</code>).
 * Note, however, that manipulating default values (with <code>setDefault</code>)
 * does not cause any events to be reported.
 * </p>
 * <p>
 * Clients may instantiate this class.
 * </p>
 * <p>
 * The implementation is based on a pair of internal
 * <code>java.util.Properties</code> objects, one holding explicitly set values
 * (set using <code>setValue</code>), the other holding the default values
 * (set using <code>setDefaultValue</code>). The <code>load</code> and
 * <code>store</code> methods persist the non-default property values to
 * streams (the default values are not saved).
 * </p>
 * <p>
 * If a client sets a default value to be equivalent to the default-default for that
 * type, the value is still known to the preference store as having a default value.
 * That is, the name will still be returned in the result of the <code>defaultPropertyNames</code>
 * and <code>contains</code> methods.
 * </p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated This class is replaced by {@link IEclipsePreferences}. Setting a default
 * value is accomplished by a setting a value in the {@link DefaultScope}, and setting
 * an explicit non-default value is accomplished by setting a value in the {@link InstanceScope}.
 * To obtain a preference value, use the preference accessor methods on .
 */
@Deprecated
public class Preferences {

    /**
     * The default-default value for boolean properties (<code>false</code>).
     */
    public static final boolean BOOLEAN_DEFAULT_DEFAULT = false;

    /**
     * The default-default value for double properties (<code>0.0</code>).
     */
    public static final double DOUBLE_DEFAULT_DEFAULT = 0.0;

    /**
     * The default-default value for float properties (<code>0.0f</code>).
     */
    public static final float FLOAT_DEFAULT_DEFAULT = 0.0f;

    /**
     * The default-default value for int properties (<code>0</code>).
     */
    public static final int INT_DEFAULT_DEFAULT = 0;

    /**
     * The default-default value for long properties (<code>0L</code>).
     */
    public static final long LONG_DEFAULT_DEFAULT = 0L;

    /**
     * The default-default value for String properties (<code>""</code>).
     */
    public static final String STRING_DEFAULT_DEFAULT = ""; //$NON-NLS-1$

    /**
     * The string representation used for <code>true</code>
     * (<code>"true"</code>).
     */
    protected static final String TRUE = "true"; //$NON-NLS-1$

    /**
     * The string representation used for <code>false</code>
     * (<code>"false"</code>).
     */
    protected static final String FALSE = "false"; //$NON-NLS-1$

    /**
     * An event object describing a change to a named property.
     * <p>
     * The preferences object reports property change events for internal state
     * changes that may be of interest to external parties. A special listener
     * interface (<code>Preferences.IPropertyChangeListener</code>) is
     * defined for this purpose. Listeners are registered via the
     * <code>Preferences.addPropertyChangeListener</code> method.
     * </p>
     * <p>
     * Clients cannot instantiate or subclass this class.
     * </p>
     *
     * @see Preferences#addPropertyChangeListener(IPropertyChangeListener)
     * @see IPropertyChangeListener
     */
    public static class PropertyChangeEvent extends EventObject {
        /**
         * All serializable objects should have a stable serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        /**
         * The name of the changed property.
         */
        private final String propertyName;

        /**
         * The old value of the changed property, or <code>null</code> if
         * not known or not relevant.
         */
        private final Object oldValue;

        /**
         * The new value of the changed property, or <code>null</code> if
         * not known or not relevant.
         */
        private final Object newValue;

        /**
         * Creates a new property change event.
         *
         * @param source the object whose property has changed
         * @param property the property that has changed (must not be
         * <code>null</code>)
         * @param oldValue the old value of the property, or
         * <code>null</code> if none
         * @param newValue the new value of the property, or
         * <code>null</code> if none
         */
        protected PropertyChangeEvent(Object source, String property, Object oldValue, Object newValue) {

            super(source);
            if (property == null) {
                throw new IllegalArgumentException();
            }
            this.propertyName = property;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        /**
         * Returns the name of the property that changed.
         * <p>
         * Warning: there is no guarantee that the property name returned
         * is a constant string. Callers must compare property names using
         * <code>equals</code>, not ==.
         * </p>
         *
         * @return the name of the property that changed
         */
        public String getProperty() {
            return propertyName;
        }

        /**
         * Returns the new value of the property.
         *
         * @return the new value, or <code>null</code> if not known
         * or not relevant
         */
        public Object getNewValue() {
            return newValue;
        }

        /**
         * Returns the old value of the property.
         *
         * @return the old value, or <code>null</code> if not known
         * or not relevant
         */
        public Object getOldValue() {
            return oldValue;
        }
    }

    /**
     * Listener for property changes.
     * <p>
     * Usage:
     * </p>
     * 
     * <pre>
     * Preferences.IPropertyChangeListener listener =
     *   new Preferences.IPropertyChangeListener() {
     *      public void propertyChange(Preferences.PropertyChangeEvent event) {
     *         ... // code to deal with occurrence of property change
     *      }
     *   };
     * emitter.addPropertyChangeListener(listener);
     * ...
     * emitter.removePropertyChangeListener(listener);
     * </pre>
     * 
     * <p>
     * <em>Note:</em> Depending on the means in which the property
     * values changed, the old and new values for the property can
     * be either typed, a string representation of the value, or <code>null</code>.
     * Clients who wish to behave properly in all cases should all
     * three cases in their implementation of the property change listener.
     * </p>
     */
    @FunctionalInterface
    public interface IPropertyChangeListener extends EventListener {

        /**
         * Notification that a property has changed.
         * <p>
         * This method gets called when the observed object fires a property
         * change event.
         * </p>
         *
         * @param event the property change event object describing which
         * property changed and how
         */
        void propertyChange(PropertyChangeEvent event);
    }

    /**
     * List of registered listeners (element type:
     * <code>IPropertyChangeListener</code>).
     * These listeners are to be informed when the current value of a property
     * changes.
     */
    protected ListenerList<IPropertyChangeListener> listeners = new ListenerList<>();

    /**
     * The mapping from property name to
     * property value (represented as strings).
     */
    private final Properties properties;

    /**
     * The mapping from property name to
     * default property value (represented as strings);
     * <code>null</code> if none.
     */
    private final Properties defaultProperties;

    /**
     * Indicates whether a value has been changed by <code>setToDefault</code>
     * or <code>setValue</code>; initially <code>false</code>.
     */
    protected boolean dirty = false;

    /**
     * Creates an empty preference table.
     * <p>
     * Use the methods <code>load(InputStream)</code> and
     * <code>store(InputStream)</code> to load and store these preferences.
     * </p>
     * 
     * @see #load(InputStream)
     * @see #store(OutputStream, String)
     */
    public Preferences() {
        defaultProperties = new Properties();
        properties = new Properties(defaultProperties);
    }

    /**
     * Returns whether the given property is known to this preference object,
     * either by having an explicit setting or by having a default
     * setting. Returns <code>false</code> if the given name is <code>null</code>.
     *
     * @param name the name of the property, or <code>null</code>
     * @return <code>true</code> if either a current value or a default
     * value is known for the named property, and <code>false</code>otherwise
     */
    public boolean contains(String name) {
        return (properties.containsKey(name) || defaultProperties.containsKey(name));
    }

    /**
     * Fires a property change event corresponding to a change to the
     * current value of the property with the given name.
     *
     * @param name the name of the property, to be used as the property
     * in the event object
     * @param oldValue the old value, or <code>null</code> if not known or not
     * relevant
     * @param newValue the new value, or <code>null</code> if not known or not
     * relevant
     */
    protected void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        // Do we even need to fire an event?
        if (this.listeners.size() == 0) {
            return;
        }
        final PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
        for (final IPropertyChangeListener l : this.listeners) {
            ISafeRunnable job = new ISafeRunnable() {
                @Override
                public void handleException(Throwable exception) {
                    // already being logged in SafeRunner#run()
                }

                @Override
                public void run() throws Exception {
                    l.propertyChange(pe);
                }
            };
            SafeRunner.run(job);
        }
    }

    /**
     * Returns the current value of the boolean-valued property with the
     * given name.
     * Returns the default-default value (<code>false</code>) if there
     * is no property with the given name, or if the current value
     * cannot be treated as a boolean.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the boolean-valued property
     */
    public boolean getBoolean(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            return BOOLEAN_DEFAULT_DEFAULT;
        }
        return value.equals(Preferences.TRUE);
    }

    /**
     * Sets the current value of the boolean-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property
     */
    public void setValue(String name, boolean value) {
        boolean defaultValue = getDefaultBoolean(name);
        boolean oldValue = getBoolean(name);
        if (value == defaultValue) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, value ? Preferences.TRUE : Preferences.FALSE);
        }
        if (oldValue != value) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue ? Boolean.TRUE : Boolean.FALSE,
                value ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /**
     * Returns the default value for the boolean-valued property
     * with the given name.
     * Returns the default-default value (<code>false</code>) if there
     * is no default property with the given name, or if the default
     * value cannot be treated as a boolean.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public boolean getDefaultBoolean(String name) {
        String value = defaultProperties.getProperty(name);
        if (value == null) {
            return BOOLEAN_DEFAULT_DEFAULT;
        }
        return value.equals(Preferences.TRUE);
    }

    /**
     * Returns the current value of the double-valued property with the
     * given name.
     * Returns the default-default value (<code>0.0</code>) if there
     * is no property with the given name, or if the current value
     * cannot be treated as a double.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the double-valued property
     */
    public double getDouble(String name) {
        return convertToDouble(properties.getProperty(name));
    }

    /**
     * Sets the current value of the double-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property; must be
     * a number (not a NaN)
     */
    public void setValue(String name, double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException();
        }
        double defaultValue = getDefaultDouble(name);
        double oldValue = getDouble(name);
        if (value == defaultValue) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, Double.toString(value));
        }
        if (oldValue != value) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Returns the default value for the double-valued property
     * with the given name.
     * Returns the default-default value (<code>0.0</code>) if there
     * is no default property with the given name, or if the default
     * value cannot be treated as a double.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public double getDefaultDouble(String name) {
        return convertToDouble(defaultProperties.getProperty(name));
    }

    /**
     * Converts the given raw property value string to a double.
     *
     * @param rawPropertyValue the raw property value, or <code>null</code>
     * if none
     * @return the raw value converted to a double, or the given
     * <code>defaultValue</code> if the raw value is <code>null</code> or
     * cannot be parsed as a double
     */
    private double convertToDouble(String rawPropertyValue) {
        double result = Preferences.DOUBLE_DEFAULT_DEFAULT;
        if (rawPropertyValue != null) {
            try {
                result = Double.parseDouble(rawPropertyValue);
            } catch (NumberFormatException e) {
                // raw value cannot be treated as one of these
            }
        }
        return result;
    }

    /**
     * Returns the current value of the float-valued property with the
     * given name.
     * Returns the default-default value (<code>0.0f</code>) if there
     * is no property with the given name, or if the current value
     * cannot be treated as a float.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the float-valued property
     */
    public float getFloat(String name) {
        return convertToFloat(properties.getProperty(name));
    }

    /**
     * Sets the current value of the float-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property; must be
     * a number (not a NaN)
     */
    public void setValue(String name, float value) {
        if (Float.isNaN(value)) {
            throw new IllegalArgumentException();
        }
        float defaultValue = getDefaultFloat(name);
        float oldValue = getFloat(name);
        if (value == defaultValue) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, Float.toString(value));
        }
        if (oldValue != value) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Returns the default value for the float-valued property
     * with the given name.
     * Returns the default-default value (<code>0.0f</code>) if there
     * is no default property with the given name, or if the default
     * value cannot be treated as a float.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public float getDefaultFloat(String name) {
        return convertToFloat(defaultProperties.getProperty(name));
    }

    /**
     * Converts the given raw property value string to a float.
     *
     * @param rawPropertyValue the raw property value, or <code>null</code>
     * if none
     * @return the raw value converted to a float, or the given
     * <code>defaultValue</code> if the raw value is <code>null</code> or
     * cannot be parsed as a float
     */
    private float convertToFloat(String rawPropertyValue) {
        float result = Preferences.FLOAT_DEFAULT_DEFAULT;
        if (rawPropertyValue != null) {
            try {
                result = Float.parseFloat(rawPropertyValue);
            } catch (NumberFormatException e) {
                // raw value cannot be treated as one of these
            }
        }
        return result;
    }

    /**
     * Returns the current value of the integer-valued property with the
     * given name.
     * Returns the default-default value (<code>0</code>) if there
     * is no property with the given name, or if the current value
     * cannot be treated as an integer.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the int-valued property
     */
    public int getInt(String name) {
        return convertToInt(properties.getProperty(name));
    }

    /**
     * Sets the current value of the integer-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property
     */
    public void setValue(String name, int value) {
        int defaultValue = getDefaultInt(name);
        int oldValue = getInt(name);
        if (value == defaultValue) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, Integer.toString(value));
        }
        if (oldValue != value) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Returns the default value for the integer-valued property
     * with the given name.
     * Returns the default-default value (<code>0</code>) if there
     * is no default property with the given name, or if the default
     * value cannot be treated as an integer.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public int getDefaultInt(String name) {
        return convertToInt(defaultProperties.getProperty(name));
    }

    /**
     * Converts the given raw property value string to an int.
     *
     * @param rawPropertyValue the raw property value, or <code>null</code>
     * if none
     * @return the raw value converted to an int, or the given
     * <code>defaultValue</code> if the raw value is <code>null</code> or
     * cannot be parsed as an int
     */
    private int convertToInt(String rawPropertyValue) {
        int result = Preferences.INT_DEFAULT_DEFAULT;
        if (rawPropertyValue != null) {
            try {
                result = Integer.parseInt(rawPropertyValue);
            } catch (NumberFormatException e) {
                // raw value cannot be treated as one of these
            }
        }
        return result;
    }

    /**
     * Returns the current value of the long-valued property with the
     * given name.
     * Returns the default-default value (<code>0L</code>) if there
     * is no property with the given name, or if the current value
     * cannot be treated as a long.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the long-valued property
     */
    public long getLong(String name) {
        return convertToLong(properties.getProperty(name));
    }

    /**
     * Sets the current value of the long-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property
     */
    public void setValue(String name, long value) {
        long defaultValue = getDefaultLong(name);
        long oldValue = getLong(name);
        if (value == defaultValue) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, Long.toString(value));
        }
        if (oldValue != value) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Returns the default value for the long-valued property
     * with the given name.
     * Returns the default-default value (<code>0L</code>) if there
     * is no default property with the given name, or if the default
     * value cannot be treated as a long.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public long getDefaultLong(String name) {
        return convertToLong(defaultProperties.getProperty(name));
    }

    /**
     * Converts the given raw property value string to a long.
     *
     * @param rawPropertyValue the raw property value, or <code>null</code>
     * if none
     * @return the raw value converted to a long, or the given
     * <code>defaultValue</code> if the raw value is <code>null</code> or
     * cannot be parsed as a long
     */
    private long convertToLong(String rawPropertyValue) {
        long result = Preferences.LONG_DEFAULT_DEFAULT;
        if (rawPropertyValue != null) {
            try {
                result = Long.parseLong(rawPropertyValue);
            } catch (NumberFormatException e) {
                // raw value cannot be treated as one of these
            }
        }
        return result;
    }

    /**
     * Returns the current value of the string-valued property with the
     * given name.
     * Returns the default-default value (the empty string <code>""</code>)
     * if there is no property with the given name.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the string-valued property
     */
    public String getString(String name) {
        String value = properties.getProperty(name);
        return (value != null ? value : STRING_DEFAULT_DEFAULT);
    }

    /**
     * Sets the current value of the string-valued property with the
     * given name. The given name must not be <code>null</code>.
     * <p>
     * A property change event is reported if the current value of the
     * property actually changes from its previous value. In the event
     * object, the property name is the name of the property, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * If the given value is the same as the corresponding default value
     * for the given property, the explicit setting is deleted.
     * Note that the recommended way of re-initializing a property to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the property
     * @param value the new current value of the property
     */
    public void setValue(String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        String defaultValue = getDefaultString(name);
        String oldValue = getString(name);
        if (value.equals(defaultValue)) {
            Object removed = properties.remove(name);
            if (removed != null) {
                // removed an explicit setting
                dirty = true;
            }
        } else {
            properties.put(name, value);
        }
        if (!oldValue.equals(value)) {
            // mark as dirty since value did really change
            dirty = true;
            // report property change if getValue now returns different value
            firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Returns the default value for the string-valued property
     * with the given name.
     * Returns the default-default value (the empty string <code>""</code>)
     * is no default property with the given name, or if the default
     * value cannot be treated as a string.
     * The given name must not be <code>null</code>.
     *
     * @param name the name of the property
     * @return the default value of the named property
     */
    public String getDefaultString(String name) {
        String value = defaultProperties.getProperty(name);
        return (value != null ? value : STRING_DEFAULT_DEFAULT);
    }

    /**
     * Returns whether the property with the given name has the default value in
     * virtue of having no explicitly set value.
     * Returns <code>false</code> if the given name is <code>null</code>.
     *
     * @param name the name of the property, or <code>null</code>
     * @return <code>true</code> if the property has no explicitly set value,
     * and <code>false</code> otherwise (including the case where the property
     * is unknown to this object)
     */
    public boolean isDefault(String name) {
        return !properties.containsKey(name);
    }

    /**
     * Sets the current value of the property with the given name back
     * to its default value. Has no effect if the property does not have
     * its own current value. The given name must not be <code>null</code>.
     * <p>
     * Note that the recommended way of re-initializing a property to the
     * appropriate default value is to call <code>setToDefault</code>.
     * This is implemented by removing the named value from the object,
     * thereby exposing the default value.
     * </p>
     * <p>
     * A property change event is always reported. In the event
     * object, the property name is the name of the property, and the
     * old and new values are either strings, or <code>null</code>
     * indicating the default-default value.
     * </p>
     *
     * @param name the name of the property
     */
    public void setToDefault(String name) {
        Object oldPropertyValue = properties.remove(name);
        if (oldPropertyValue != null) {
            dirty = true;
        }
        String newValue = defaultProperties.getProperty(name, null);
        // n.b. newValue == null if there is no default value
        // can't determine correct default-default without knowing type
        firePropertyChangeEvent(name, oldPropertyValue, newValue);
    }

    /**
     * Returns whether the current values in this preference object
     * require saving.
     *
     * @return <code>true</code> if at least one of the properties
     * known to this preference object has a current value different from its
     * default value, and <code>false</code> otherwise
     */
    public boolean needsSaving() {
        return dirty;
    }

    /**
     * Saves the non-default-valued properties known to this preference object to
     * the given output stream using
     * <code>Properties.store(OutputStream,String)</code>.
     * <p>
     * Note that the output is unconditionally written, even when
     * <code>needsSaving</code> is <code>false</code>.
     * </p>
     *
     * @param out the output stream
     * @param header a comment to be included in the output, or
     * <code>null</code> if none
     * @exception IOException if there is a problem saving this preference object
     * @see Properties#store(OutputStream,String)
     */
    public void store(OutputStream out, String header) throws IOException {
        properties.store(out, header);
        dirty = false;
    }

    /**
     * Loads the non-default-valued properties for this preference object from the
     * given input stream using
     * <code>java.util.Properties.load(InputStream)</code>. Default property
     * values are not affected.
     *
     * @param in the input stream
     * @exception IOException if there is a problem loading this preference
     * object
     * @see Properties#load(InputStream)
     */
    public void load(InputStream in) throws IOException {
        properties.load(in);
        dirty = false;
    }
}
