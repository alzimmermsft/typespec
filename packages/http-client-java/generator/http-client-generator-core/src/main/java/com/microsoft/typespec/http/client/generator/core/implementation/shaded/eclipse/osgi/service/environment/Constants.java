/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.environment;

/**
 * Constants used with the {@link EnvironmentInfo} service.
 * @since 3.0
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface Constants {
	//TODO These constants need to be aligned with the OSGi ones. See page 64-588 of the spec

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
	 */
    String OS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "linux") indicating the platform is running on a
	 * Linux-based operating system.
	 */
    String OS_LINUX = "linux";//$NON-NLS-1$

	/**
	 * Constant string (value "aix") indicating the platform is running on an
	 * AIX-based operating system.
	 */
    String OS_AIX = "aix";//$NON-NLS-1$

	/**
	 * Constant string (value "solaris") indicating the platform is running on a
	 * Solaris-based operating system.
	 */
    String OS_SOLARIS = "solaris";//$NON-NLS-1$

	/**
	 * Constant string (value "hpux") indicating the platform is running on an
	 * HP/UX-based operating system.
	 */
    String OS_HPUX = "hpux";//$NON-NLS-1$

	/**
	 * Constant string (value "qnx") indicating the platform is running on a
	 * QNX-based operating system.
	 */
    String OS_QNX = "qnx";//$NON-NLS-1$

	/**
	 * Constant string (value "macosx") indicating the platform is running on a
	 * Mac OS X operating system.
	 */
    String OS_MACOSX = "macosx";//$NON-NLS-1$

    /**
	 * Constant string (value "os/400") indicating the platform is running on a
	 * OS/400 operating system.
	 * @since 3.5
	 */
    String OS_OS400 = "os/400"; //$NON-NLS-1$

	/**
	 * Constant string (value "os/390") indicating the platform is running on a
	 * OS/390 operating system.
	 * @since 3.5
	 */
    String OS_OS390 = "os/390"; //$NON-NLS-1$

	/**
	 * Constant string (value "z/os") indicating the platform is running on a
	 * z/OS operating system.
	 * @since 3.5
	 */
    String OS_ZOS = "z/os"; //$NON-NLS-1$

	/**
	 * Constant string (value "freebsd") indicating the platform is running on a
	 * FreeBSD-based operating system.
	 * @since 3.15
	 */
    String OS_FREEBSD = "freebsd";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown operating system.
	 */
    String OS_UNKNOWN = "unknown";//$NON-NLS-1$

	/**
	 * Constant string (value "x86") indicating the platform is running on an
	 * x86-based architecture.
	 */
    String ARCH_X86 = "x86";//$NON-NLS-1$

    /**
	 * Constant string (value "sparc") indicating the platform is running on an
	 * Sparc-based architecture.
	 */
    String ARCH_SPARC = "sparc";//$NON-NLS-1$

	/**
	 * Constant string (value "x86_64") indicating the platform is running on an
	 * x86 64bit-based architecture.
	 *
	 * @since 3.1
	 */
    String ARCH_X86_64 = "x86_64";//$NON-NLS-1$

    /**
	 * Constant string (value "win32") indicating the platform is running on a
	 * machine using the Windows windowing system.
	 */
    String WS_WIN32 = "win32";//$NON-NLS-1$

    /**
	 * Constant string (value "motif") indicating the platform is running on a
	 * machine using the Motif windowing system.
	 */
    String WS_MOTIF = "motif";//$NON-NLS-1$

	/**
	 * Constant string (value "gtk") indicating the platform is running on a
	 * machine using the GTK windowing system.
	 */
    String WS_GTK = "gtk";//$NON-NLS-1$

	/**
	 * Constant string (value "photon") indicating the platform is running on a
	 * machine using the Photon windowing system.
	 */
    String WS_PHOTON = "photon";//$NON-NLS-1$

    /**
	 * Constant string (value "cocoa") indicating the platform is running on a
	 * machine using the Cocoa windowing system.
	 * @since 3.5
	 */
    String WS_COCOA = "cocoa";//$NON-NLS-1$

    /**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown windowing system.
	 */
    String WS_UNKNOWN = "unknown";//$NON-NLS-1$
}
