/*
 * Copyright (c) OSGi Alliance (2005, 2014). All Rights Reserved.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.condpermadmin;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.annotation.versioning.ProviderType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.permissionadmin.PermissionInfo;

/**
 * Framework service to administer Conditional Permissions. Conditional
 * Permissions can be added to, retrieved from, and removed from the framework.
 * Conditional Permissions are conceptually managed in an ordered table called
 * the Conditional Permission Table.
 * 
 * @ThreadSafe
 * @author $Id: eee978dba2ab9c5fa093df5a82deaced38e296af $
 */
@ProviderType
public interface ConditionalPermissionAdmin {
    /**
     * Creates a new update for the Conditional Permission Table. The update is
     * a working copy of the current Conditional Permission Table. If the
     * running Conditional Permission Table is modified before commit is called
     * on the returned update, then the call to commit on the returned update
     * will fail. That is, the commit method will return false and no change
     * will be made to the running Conditional Permission Table. There is no
     * requirement that commit is eventually called on the returned update.
     * 
     * @return A new update for the Conditional Permission Table.
     * @since 1.1
     */
    ConditionalPermissionUpdate newConditionalPermissionUpdate();

    /**
     * Creates a new ConditionalPermissionInfo with the specified fields
     * suitable for insertion into a {@link ConditionalPermissionUpdate}. The
     * {@code delete} method on {@code ConditionalPermissionInfo} objects
     * created with this method must throw UnsupportedOperationException.
     * 
     * @param name The name of the created {@code ConditionalPermissionInfo} or
     * {@code null} to have a unique name generated when the returned
     * {@code ConditionalPermissionInfo} is committed in an update to the
     * Conditional Permission Table.
     * @param conditions The conditions that need to be satisfied to enable the
     * specified permissions. This argument can be {@code null} or an
     * empty array indicating the specified permissions are not guarded
     * by any conditions.
     * @param permissions The permissions that are enabled when the specified
     * conditions, if any, are satisfied. This argument must not be
     * {@code null} and must specify at least one permission.
     * @param access Access decision. Must be one of the following values:
     * <ul>
     * <li>{@link ConditionalPermissionInfo#ALLOW allow}</li>
     * <li>{@link ConditionalPermissionInfo#DENY deny}</li>
     * </ul>
     * The specified access decision value must be evaluated case
     * insensitively.
     * @return A {@code ConditionalPermissionInfo} object suitable for insertion
     * into a {@link ConditionalPermissionUpdate}.
     * @throws IllegalArgumentException If no permissions are specified or if
     * the specified access decision is not a valid value.
     * @since 1.1
     */
    ConditionalPermissionInfo newConditionalPermissionInfo(String name, ConditionInfo[] conditions,
        PermissionInfo[] permissions, String access);

    /**
     * Creates a new {@code ConditionalPermissionInfo} from the specified
     * encoded {@code ConditionalPermissionInfo} string suitable for insertion
     * into a {@link ConditionalPermissionUpdate}. The {@code delete} method on
     * {@code ConditionalPermissionInfo} objects created with this method must
     * throw UnsupportedOperationException.
     * 
     * @param encodedConditionalPermissionInfo The encoded
     * {@code ConditionalPermissionInfo}. White space in the encoded
     * {@code ConditionalPermissionInfo} is ignored. The access decision
     * value in the encoded {@code ConditionalPermissionInfo} must be
     * evaluated case insensitively. If the encoded
     * {@code ConditionalPermissionInfo} does not contain the optional
     * name, {@code null} must be used for the name and a unique name
     * will be generated when the returned
     * {@code ConditionalPermissionInfo} is committed in an update to the
     * Conditional Permission Table.
     * @return A {@code ConditionalPermissionInfo} object suitable for insertion
     * into a {@link ConditionalPermissionUpdate}.
     * @throws IllegalArgumentException If the specified
     * {@code encodedConditionalPermissionInfo} is not properly
     * formatted.
     * @see ConditionalPermissionInfo#getEncoded()
     * @since 1.1
     */
    ConditionalPermissionInfo newConditionalPermissionInfo(String encodedConditionalPermissionInfo);
}
