/*******************************************************************************
 * Copyright (c) 2008, 2020 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.permadmin;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxBundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.PermissionData;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AdminPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.condpermadmin.ConditionInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.condpermadmin.ConditionalPermissionInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.permissionadmin.PermissionAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.permissionadmin.PermissionInfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SecurityAdmin implements PermissionAdmin, ConditionalPermissionAdmin {
	private static final PermissionCollection DEFAULT_DEFAULT;
	static {
		AllPermission allPerm = new AllPermission();
		DEFAULT_DEFAULT = allPerm.newPermissionCollection();
        DEFAULT_DEFAULT.add(allPerm);
	}

	// Base implied permissions for all bundles
	private static final String OSGI_BASE_IMPLIED_PERMISSIONS = "implied.permissions"; //$NON-NLS-1$

	private static final String ADMIN_IMPLIED_ACTIONS = AdminPermission.RESOURCE + ',' + AdminPermission.METADATA + ','
			+ AdminPermission.CLASS + ',' + AdminPermission.CONTEXT;
	private static final PermissionInfo[] EMPTY_PERM_INFO = new PermissionInfo[0];
	/* @GuardedBy(lock) */
	private final PermissionAdminTable permAdminTable = new PermissionAdminTable();
	/* @GuardedBy(lock) */
	private SecurityTable condAdminTable;
	/* @GuardedBy(lock) */
	private PermissionInfoCollection permAdminDefaults;
	/* @GuardedBy(lock) */
	private long timeStamp = 0;
	/* @GuardedBy(lock) */
	private long nextID = System.currentTimeMillis();
	/* @GuardedBy(lock) */
	private final PermissionData permissionStorage;
	private final Object lock = new Object();
	// private final EquinoxContainer container;
	private final PermissionInfo[] impliedPermissionInfos;
	private final EquinoxSecurityManager supportedSecurityManager;

	public SecurityAdmin(EquinoxSecurityManager supportedSecurityManager, PermissionData permissionStorage) {
		this.supportedSecurityManager = supportedSecurityManager;
		this.permissionStorage = permissionStorage;
		this.impliedPermissionInfos = SecurityAdmin
				.getPermissionInfos(getClass().getResource(OSGI_BASE_IMPLIED_PERMISSIONS));
		String[] encodedDefaultInfos = permissionStorage.getPermissionData(null);
		PermissionInfo[] defaultInfos = getPermissionInfos(encodedDefaultInfos);
		if (defaultInfos != null)
			permAdminDefaults = new PermissionInfoCollection(defaultInfos);
		String[] locations = permissionStorage.getLocations();
		if (locations != null) {
			for (String location : locations) {
				String[] encodedLocationInfos = permissionStorage.getPermissionData(location);
				if (encodedLocationInfos != null) {
					PermissionInfo[] locationInfos = getPermissionInfos(encodedLocationInfos);
					permAdminTable.setPermissions(location, locationInfos);
				}
			}
		}
		String[] encodedCondPermInfos = permissionStorage.getConditionalPermissionInfos();
		if (encodedCondPermInfos == null)
			condAdminTable = new SecurityTable(this, new SecurityRow[0]);
		else {
			SecurityRow[] rows = new SecurityRow[encodedCondPermInfos.length];
			try {
				for (int i = 0; i < rows.length; i++)
					rows[i] = SecurityRow.createSecurityRow(this, encodedCondPermInfos[i]);
			} catch (IllegalArgumentException e) {
				// TODO should log
				// bad format persisted in storage; start clean
				rows = new SecurityRow[0];
			}
			condAdminTable = new SecurityTable(this, rows);
		}
	}

	private static PermissionInfo[] getPermissionInfos(String[] encodedInfos) {
		if (encodedInfos == null)
			return null;
		PermissionInfo[] results = new PermissionInfo[encodedInfos.length];
		for (int i = 0; i < results.length; i++)
			results[i] = new PermissionInfo(encodedInfos[i]);
		return results;
	}

	boolean checkPermission(Permission permission, BundlePermissions bundlePermissions) {
		// check permissions by location
		PermissionInfoCollection locationCollection;
		SecurityTable curCondAdminTable;
		PermissionInfoCollection curPermAdminDefaults;
		// save off the current state of the world while holding the lock
		synchronized (lock) {
			// get location the hard way to avoid permission check
			Bundle bundle = bundlePermissions.getBundle();
			locationCollection = bundle instanceof EquinoxBundle
					? permAdminTable.getCollection(((EquinoxBundle) bundle).getModule().getLocation())
					: null;
			curCondAdminTable = condAdminTable;
			curPermAdminDefaults = permAdminDefaults;
		}
		if (locationCollection != null)
			return locationCollection.implies(bundlePermissions, permission);
		// if conditional admin table is empty the fall back to defaults
		if (curCondAdminTable.isEmpty())
			return curPermAdminDefaults != null ? curPermAdminDefaults.implies(permission)
					: DEFAULT_DEFAULT.implies(permission);
		// check the condition table
		int result = curCondAdminTable.evaluate(bundlePermissions, permission);
		if ((result & SecurityTable.GRANTED) != 0)
			return true;
		if ((result & SecurityTable.DENIED) != 0)
			return false;
        return (result & SecurityTable.POSTPONED) != 0;
    }

    @Override
	public String[] getLocations() {
		synchronized (lock) {
			String[] results = permAdminTable.getLocations();
			return results.length == 0 ? null : results;
		}
	}

    private static void checkAllPermission() {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
			sm.checkPermission(new AllPermission());
	}

    @Override
	public ConditionalPermissionInfo newConditionalPermissionInfo(String name, ConditionInfo[] conditions,
			PermissionInfo[] permissions, String decision) {
		return new SecurityRowSnapShot(name, conditions, permissions, decision);
	}

	@Override
	public ConditionalPermissionInfo newConditionalPermissionInfo(String encoded) {
		return SecurityRow.createSecurityRowSnapShot(encoded);
	}

	@Override
	public ConditionalPermissionUpdate newConditionalPermissionUpdate() {
        synchronized (lock) {
            return new SecurityTableUpdate(this, condAdminTable.getRows(), timeStamp);
        }
    }

    boolean commit(List<ConditionalPermissionInfo> rows, long updateStamp) {
		checkAllPermission();
		synchronized (lock) {
			if (updateStamp != timeStamp)
				return false;
			SecurityRow[] newRows = new SecurityRow[rows.size()];
			Collection<String> names = new ArrayList<>();
			for (int i = 0; i < newRows.length; i++) {
				Object rowObj = rows.get(i);
				if (!(rowObj instanceof ConditionalPermissionInfo))
					throw new IllegalStateException(
							"Invalid type \"" + rowObj.getClass().getName() + "\" at row: " + i); //$NON-NLS-1$//$NON-NLS-2$
				ConditionalPermissionInfo infoBaseRow = (ConditionalPermissionInfo) rowObj;
				String name = infoBaseRow.getName();
				if (name == null)
					name = generateName();
				if (names.contains(name))
					throw new IllegalStateException("Duplicate name \"" + name + "\" at row: " + i); //$NON-NLS-1$//$NON-NLS-2$
				names.add(name);
				newRows[i] = new SecurityRow(this, name, infoBaseRow.getConditionInfos(),
						infoBaseRow.getPermissionInfos(), infoBaseRow.getAccessDecision());
			}
			condAdminTable = new SecurityTable(this, newRows);
			permissionStorage.saveConditionalPermissionInfos(condAdminTable.getEncodedRows());
			timeStamp += 1;
			return true;
		}
	}

	/* GuardedBy(lock) */
	private String generateName() {
		return "generated_" + Long.toString(nextID++); //$NON-NLS-1$ ;
	}

	public ProtectionDomain createProtectionDomain(Bundle bundle) {
		return createProtectionDomain(bundle, this);
	}

	private ProtectionDomain createProtectionDomain(Bundle bundle, SecurityAdmin sa) {
		PermissionInfoCollection impliedPermissions = getImpliedPermission(bundle);
		URL permEntry = null;
		try {
			permEntry = bundle.getEntry("OSGI-INF/permissions.perm"); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			// bundle may be uninstalled
		}
		PermissionInfo[] restrictedInfos = getFileRelativeInfos(SecurityAdmin.getPermissionInfos(permEntry), bundle);
		PermissionInfoCollection restrictedPermissions = restrictedInfos == null ? null
				: new PermissionInfoCollection(restrictedInfos);
		BundlePermissions bundlePermissions = new BundlePermissions(bundle, sa, impliedPermissions,
				restrictedPermissions);
		return new ProtectionDomain(null, bundlePermissions);
	}

	private PermissionInfoCollection getImpliedPermission(Bundle bundle) {
		if (impliedPermissionInfos == null)
			return null;
		// create the implied AdminPermission actions for this bundle
		PermissionInfo impliedAdminPermission = new PermissionInfo(AdminPermission.class.getName(),
				"(id=" + bundle.getBundleId() + ")", ADMIN_IMPLIED_ACTIONS); //$NON-NLS-1$ //$NON-NLS-2$
		PermissionInfo[] bundleImpliedInfos = new PermissionInfo[impliedPermissionInfos.length + 1];
		System.arraycopy(impliedPermissionInfos, 0, bundleImpliedInfos, 0, impliedPermissionInfos.length);
		bundleImpliedInfos[impliedPermissionInfos.length] = impliedAdminPermission;
		return new PermissionInfoCollection(getFileRelativeInfos(bundleImpliedInfos, bundle));
	}

	private PermissionInfo[] getFileRelativeInfos(PermissionInfo[] permissionInfos, Bundle bundle) {
		if (permissionInfos == null)
			return permissionInfos;
		PermissionInfo[] results = new PermissionInfo[permissionInfos.length];
		for (int i = 0; i < permissionInfos.length; i++) {
			results[i] = permissionInfos[i];
			if (PermissionInfoCollection.FILE_PERMISSION_NAME.equals(permissionInfos[i].getType())) {
				if (!PermissionInfoCollection.ALL_FILES.equals(permissionInfos[i].getName())) {
					File file = new File(permissionInfos[i].getName());
					if (!file.isAbsolute()) { // relative name
						try {
							File target = bundle.getDataFile(permissionInfos[i].getName());
							if (target != null)
								results[i] = new PermissionInfo(permissionInfos[i].getType(), target.getPath(),
										permissionInfos[i].getActions());
						} catch (IllegalStateException e) {
							// can happen if the bundle has been uninstalled;
							// we just keep the original permission in this case.
						}
					}
				}
			}
		}
		return results;
	}

	public void clearCaches() {
		PermissionInfoCollection[] permAdminCollections;
		SecurityRow[] condAdminRows;
		synchronized (lock) {
			permAdminCollections = permAdminTable.getCollections();
			condAdminRows = condAdminTable.getRows();
		}
		for (PermissionInfoCollection permAdminCollection : permAdminCollections) {
			permAdminCollection.clearPermissionCache();
		}
		for (SecurityRow condAdminRow : condAdminRows) {
			condAdminRow.clearCaches();
		}
		condAdminTable.clearEvaluationCache();
	}

	EquinoxSecurityManager getSupportedSecurityManager() {
		return supportedSecurityManager != null ? supportedSecurityManager : getSupportedSystemSecurityManager();
	}

	static private EquinoxSecurityManager getSupportedSystemSecurityManager() {
		try {
			EquinoxSecurityManager equinoxManager = (EquinoxSecurityManager) System.getSecurityManager();
			return equinoxManager != null && equinoxManager.inCheckPermission() ? equinoxManager : null;
		} catch (ClassCastException e) {
			return null;
		}
	}

	private static PermissionInfo[] getPermissionInfos(URL resource) {
		if (resource == null)
			return null;
		PermissionInfo[] info = EMPTY_PERM_INFO;
		List<PermissionInfo> permissions = new ArrayList<>();
		try (DataInputStream in = new DataInputStream(resource.openStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

			while (true) {
				String line = reader.readLine();
				if (line == null) /* EOF */
					break;
				line = line.trim();
				if ((line.length() == 0) || line.startsWith("#") || line.startsWith("//")) /* comments *///$NON-NLS-1$ //$NON-NLS-2$
					continue;

				try {
					permissions.add(new PermissionInfo(line));
				} catch (IllegalArgumentException iae) {
					/* incorrectly encoded permission */
					// TODO used to publish an event here
				}
			}
			int size = permissions.size();
			if (size > 0)
				info = permissions.toArray(new PermissionInfo[size]);
		} catch (IOException e) {
			// do nothing
		}
		return info;
	}

}
