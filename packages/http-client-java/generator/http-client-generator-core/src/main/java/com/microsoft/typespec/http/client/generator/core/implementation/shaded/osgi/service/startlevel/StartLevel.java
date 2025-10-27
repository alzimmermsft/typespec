/*
 * Copyright (c) OSGi Alliance (2002, 2013). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.startlevel;

/**
 * The StartLevel service allows management agents to manage a start level
 * assigned to each bundle and the active start level of the Framework. There is
 * at most one StartLevel service present in the OSGi environment.
 * 
 * <p>
 * A start level is defined to be a state of execution in which the Framework
 * exists. StartLevel values are defined as unsigned integers with 0 (zero)
 * being the state where the Framework is not launched. Progressively higher
 * integral values represent progressively higher start levels. e.g. 2 is a
 * higher start level than 1.
 * <p>
 * Access to the StartLevel service is protected by corresponding
 * {@code ServicePermission}. In addition {@code AdminPermission}
 * is required to actually modify start level information.
 * <p>
 * Start Level support in the Framework includes the ability to control the
 * beginning start level of the Framework, to modify the active start level of
 * the Framework and to assign a specific start level to a bundle. How the
 * beginning start level of a Framework is specified is implementation
 * dependent. It may be a command line argument when invoking the Framework
 * implementation.
 * <p>
 * When the Framework is first started it must be at start level zero. In this
 * state, no bundles are running. This is the initial state of the Framework
 * before it is launched.
 * 
 * When the Framework is launched, the Framework will enter start level one and
 * all bundles which are assigned to start level one and whose autostart setting
 * indicates the bundle should be started are started as described in the
 * {@code Bundle.start} method. The Framework will continue to increase
 * the start level, starting bundles at each start level, until the Framework
 * has reached a beginning start level. At this point the Framework has
 * completed starting bundles and will then fire a Framework event of type
 * {@code FrameworkEvent.STARTED} to announce it has completed its
 * launch.
 * 
 * <p>
 * Within a start level, bundles may be started in an order defined by the
 * Framework implementation. This may be something like ascending
 * {@code Bundle.getBundleId} order or an order based upon dependencies
 * between bundles. A similar but reversed order may be used when stopping
 * bundles within a start level.
 * 
 * <p>
 * The StartLevel service can be used by management bundles to alter the active
 * start level of the framework.
 * 
 * @ThreadSafe
 * @noimplement
 * @author $Id: 42f3c6bbf682a69ea3914c737d5b0001694383db $
 * @deprecated This service has been replaced by the
 *             <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel</code> package.
 */
public interface StartLevel {
	/**
	 * Return the active start level value of the Framework.
	 * 
	 * If the Framework is in the process of changing the start level this
	 * method must return the active start level if this differs from the
	 * requested start level.
	 * 
	 * @return The active start level value of the Framework.
	 */
    int getStartLevel();

}
