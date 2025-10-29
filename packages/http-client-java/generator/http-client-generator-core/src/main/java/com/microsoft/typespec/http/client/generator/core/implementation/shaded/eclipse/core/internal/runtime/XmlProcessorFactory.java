/*******************************************************************************
 *  Copyright (c) 2023 Joerg Kubitz and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;

/**
 * XML processing which prohibits external entities.
 *
 * @see <a href="https://rules.sonarsource.com/java/RSPEC-2755/">RSPEC-2755</a>
 */
public class XmlProcessorFactory {
    private XmlProcessorFactory() {
        // static Utility only
    }

    // using these factories is synchronized with creating & configuring them
    // potentially concurrently in another thread:
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE
        = createDocumentBuilderFactoryWithErrorOnDOCTYPE();

    /**
     * Creates TransformerFactory which throws TransformerException when detecting
     * external entities.
     *
     * @return javax.xml.transform.TransformerFactory
     */
    public static TransformerFactory createTransformerFactoryWithErrorOnDOCTYPE() {
        TransformerFactory factory = TransformerFactory.newInstance();
        // prohibit the use of all protocols by external entities:
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); //$NON-NLS-1$
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); //$NON-NLS-1$
        return factory;
    }

    /**
     * Creates DocumentBuilderFactory which throws SAXParseException when detecting
     * external entities. It's magnitudes faster to call
     * {@link #createDocumentBuilderWithErrorOnDOCTYPE()}.
     *
     * @return javax.xml.parsers.DocumentBuilderFactory
     */
    public static synchronized DocumentBuilderFactory createDocumentBuilderFactoryWithErrorOnDOCTYPE() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // completely disable DOCTYPE declaration:
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return factory;
    }

    /**
     * Creates DocumentBuilder which throws SAXParseException when detecting
     * external entities. The builder is not thread safe.
     *
     * @return javax.xml.parsers.DocumentBuilder
     */
    public static synchronized DocumentBuilder createDocumentBuilderWithErrorOnDOCTYPE()
        throws ParserConfigurationException {
        return DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE.newDocumentBuilder();
    }

    /**
     * Obtain a new instance of a DOM {@link Document} object to build a DOM tree
     * with.
     *
     * @return A new instance of a DOM Document object.
     * @see javax.xml.parsers.DocumentBuilder#newDocument()
     */
    public static Document newDocumentWithErrorOnDOCTYPE() throws ParserConfigurationException {
        return createDocumentBuilderWithErrorOnDOCTYPE().newDocument();
    }

}
