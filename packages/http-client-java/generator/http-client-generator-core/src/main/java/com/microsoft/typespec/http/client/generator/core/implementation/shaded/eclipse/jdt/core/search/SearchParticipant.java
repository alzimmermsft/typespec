/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * A search participant describes a particular extension to a generic search
 * mechanism, permitting combined search actions which will involve all required
 * participants.
 * <p>
 * A search participant is involved in the indexing phase and in the search phase.
 * The indexing phase consists in taking one or more search documents, parse them, and
 * add index entries in an index chosen by the participant. An index is identified by a
 * path on disk.
 * The search phase consists in selecting the indexes corresponding to a search pattern
 * and a search scope, from these indexes the search infrastructure extracts the document paths
 * that match the search pattern asking the search participant for the corresponding document,
 * finally the search participant is asked to locate the matches precisely in these search documents.
 * </p>
 * <p>
 * This class is intended to be subclassed by clients. During the indexing phase,
 * a subclass will be called with the following requests in order:
 * <ul>
 * <li>{@link #scheduleDocumentIndexing(SearchDocument, IPath)}</li>
 * <li>{@link #indexDocument(SearchDocument, IPath)}</li>
 * </ul>
 * <p>
 *
 * @since 3.0
 */
public abstract class SearchParticipant {

    private IPath lastIndexLocation;

    /**
     * Creates a new search participant.
     */
    protected SearchParticipant() {
        // do nothing
    }

    /**
     * Notification that this participant's help is needed in a search.
     * <p>
     * This method should be re-implemented in subclasses that need to do something
     * when the participant is needed in a search.
     * </p>
     */
    public void beginSearching() {
        // do nothing
    }

    /**
     * Notification that this participant's help is no longer needed.
     * <p>
     * This method should be re-implemented in subclasses that need to do something
     * when the participant is no longer needed in a search.
     * </p>
     */
    public void doneSearching() {
        // do nothing
    }

    /**
     * Returns a displayable name of this search participant.
     * <p>
     * This method should be re-implemented in subclasses that need to
     * display a meaningful name.
     * </p>
     *
     * @return the displayable name of this search participant
     */
    public String getDescription() {
        return "Search participant"; //$NON-NLS-1$
    }

    /**
     * Returns a search document for the given path.
     * The given document path is a string that uniquely identifies the document.
     * Most of the time it is a workspace-relative path, but it can also be a file system path, or a path inside a zip
     * file.
     * <p>
     * Implementors of this method can either create an instance of their own subclass of
     * {@link SearchDocument} or return an existing instance of such a subclass.
     * </p>
     *
     * @param documentPath the path of the document.
     * @return a search document
     */
    public abstract SearchDocument getDocument(String documentPath);

    /**
     * Indexes the given document in the given index. A search participant
     * asked to index a document should parse it and call
     * {@link SearchDocument#addIndexEntry(char[], char[])} as many times as
     * needed to add index entries to the index. If delegating to another
     * participant, it should use the original index location (and not the
     * delegatee's one). In the particular case of delegating to the default
     * search participant (see {@link SearchEngine#getDefaultSearchParticipant()}),
     * the provided document's path must be a path ending with one of the
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore#getJavaLikeExtensions()
     * Java-like extensions}
     * or with '.class'.
     * <p>
     * The given index location must represent a path in the file system to a file that
     * either already exists or is going to be created. If it exists, it must be an index file,
     * otherwise its data might be overwritten.
     * </p><p>
     * Clients are not expected to call this method.
     * </p>
     *
     * @param document the document to index
     * @param indexLocation the location in the file system to the index
     */
    public abstract void indexDocument(SearchDocument document, IPath indexLocation);

    /**
     * Indexes the given resolved document in the given index. A search participant
     * asked to index a resolved document should process it and call
     * {@link SearchDocument#addIndexEntry(char[], char[])} as many times as
     * needed to add only those additional index entries which could not have been originally added
     * to the index during a call to {@link SearchParticipant#indexDocument}. If delegating to another
     * participant, it should use the original index location (and not the
     * delegatee's one). In the particular case of delegating to the default
     * search participant (see {@link SearchEngine#getDefaultSearchParticipant()}),
     * the provided document's path must be a path ending with one of the
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore#getJavaLikeExtensions()
     * Java-like extensions}
     * or with '.class'.
     * <p>
     * The given index location must represent a path in the file system to a file that
     * either already exists or is going to be created. If it exists, it must be an index file,
     * otherwise its data might be overwritten.
     * </p><p>
     * Clients are not expected to call this method.
     * </p>
     *
     * @param document the document to index
     * @param indexLocation the location in the file system to the index
     *
     * @since 3.10
     */
    public void indexResolvedDocument(SearchDocument document, IPath indexLocation) {
        // do nothing, subtypes should do the "appropriate thing"
    }

    /**
     * Resolves the given document. A search participant asked to resolve a document should parse it and
     * resolve the types and preserve enough state to be able to tend to a indexResolvedDocument call
     * subsequently. This API is invoked without holding any index related locks or monitors.
     * <p>
     * Clients are not expected to call this method.
     * </p>
     *
     * @param document the document to resolve
     * @since 3.10
     * @see SearchParticipant#indexResolvedDocument
     * @see SearchDocument#requireIndexingResolvedDocument
     */
    public void resolveDocument(SearchDocument document) {
        // do nothing, subtypes should do the "appropriate thing"
    }

    /**
     * Returns the collection of index locations to consider when performing the
     * given search query in the given scope. The search engine calls this
     * method before locating matches.
     * <p>
     * An index location represents a path in the file system to a file that holds index information.
     * </p><p>
     * Clients are not expected to call this method.
     * </p>
     *
     * @param query the search pattern to consider
     * @param scope the given search scope
     * @return the collection of index paths to consider
     */
    public abstract IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope);
}
