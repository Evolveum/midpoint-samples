/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.samples.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.common.validator.EventHandler;
import com.evolveum.midpoint.common.validator.EventResult;
import com.evolveum.midpoint.common.validator.LegacyValidator;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ScriptingExpressionType;

/**
 * Test validity of the samples in the trunk/samples directory.
 *
 * @author Radovan Semancik
 */
public class TestSamples extends AbstractSampleTest {

    public static final String[] IGNORE_PATTERNS = new String[] {
            "META-INF", "experimental", "json", "misc", "rest", "samples-test", "model-.*", "bulk-actions", "bulk", "legacy", "audit"
    };
    public static final String[] CHECK_PATTERNS = new String[] { ".*.xml" };
    public static final String OBJECT_RESULT_OPERATION_NAME = TestSamples.class.getName() + ".validateObject";
    private static final String RESULT_OPERATION_NAME = TestSamples.class.getName() + ".validateFile";

    @BeforeSuite
    public void setup() throws SchemaException, SAXException, IOException {
        PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
        PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
    }

    @Test
    public void testSamples() throws Exception {
        testSamplesDirectory(SAMPLES_DIRECTORY);
    }

    private void testSamplesDirectory(File directory) throws Exception {
        for (String fileName : Objects.requireNonNull(directory.list())) {
            if (matches(fileName, IGNORE_PATTERNS)) {
                // Ignore. Don't even dive inside
                continue;
            }
            File file = new File(directory, fileName);
            if (file.isFile() && matches(fileName, CHECK_PATTERNS)) {
                parse(file);
                // TODO: any plans with validate?
//                validate(file);
            }
            if (file.isDirectory()) {
                // Descend inside
                testSamplesDirectory(file);
            }
        }
    }

    private boolean matches(String s, String[] patterns) {
        for (String pattern : patterns) {
            if (s.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    private void parse(File file) throws SchemaException {
        System.out.println("===> DOM Parsing file " + file.getPath());
        Document dom = DOMUtil.parseFile(file);
        Element firstChildElement = DOMUtil.getFirstChildElement(dom);
        if (firstChildElement.getLocalName().equals("objects")) {
            parseObjectsElements(file, firstChildElement);
        } else {
            parseObjectFile(file);
        }
    }

    // TODO: This is rather primitive now, as there is no indication of which of the objects is wrong.
    // Possibly something like DomLexicalProcessor.readObjects could be utilized, but we need to know
    // the language in advance or have the source where detection is possible (e.g. String).
    // This is all possible, which leads us to another question:
    // Why only XML samples are checked? (At least it seems so from CHECK_PATTERNS constant above.)
    private void parseObjectsElements(File file, Element topElement) throws SchemaException {
        try {
            Item<?, ?> item = prismContext.parserFor(file).parseItem();
            if (item instanceof PrismProperty
                    && (item.getRealValue() instanceof ScriptingExpressionType
                    || item.getRealValue() instanceof ExecuteScriptType)) {
                return;
            }

            prismContext.parserFor(file)
                    .strict()
                    .parseObjects();
        } catch (Exception e) {
            throw new SchemaException("Error parsing " + file.getPath() + ": " + e.getMessage(), e);
        }
        /* Original code that did not apply namespaces from the root <objects> to sub-objects:
        List<Element> objectElements = DOMUtil.listChildElements(topElement);
        for (int i = 0; i < objectElements.size(); i++) {
            try {
                //noinspection unused
                PrismObject<Objectable> object = prismContext
                        .parserFor(objectElements.get(i))
                        .strict()
                        .parse();
            } catch (Exception e) {
                throw new SchemaException("Error parsing " + file.getPath() + ", element " + objectElements.get(i).getLocalName() + " (#" + (i + 1) + "): " + e.getMessage(), e);
            }
        }
        */
    }

    private void parseObjectFile(File file) throws SchemaException {
        System.out.println("===> Parsing file " + file.getPath());
        try {
            //noinspection unused
            PrismObject<Objectable> object = prismContext
                    .parserFor(file)
                    .strict()
                    .parse();
        } catch (Exception e) {
            throw new SchemaException("Error parsing " + file.getPath() + ": " + e.getMessage(), e);
        }
    }

    private void validate(File file) throws FileNotFoundException {
        System.out.println("===> Validating file " + file.getPath());

        EventHandler<Objectable> handler = new EventHandler<>() {

            @Override
            public EventResult preMarshall(Element objectElement, Node postValidationTree,
                    OperationResult objectResult) {
                return EventResult.cont();
            }

            @Override
            public EventResult postMarshall(Objectable object, Element objectElement,
                    OperationResult objectResult) {

                // Try to marshall it back. This may detect some JAXB misconfiguration problems.
                try {
                    //noinspection unchecked
                    String serializedString =
                            PrismTestUtil.serializeObjectToString(
                                    object.asPrismObject(), PrismContext.LANG_XML);
                } catch (SchemaException e) {
                    objectResult.recordFatalError("Object serialization failed", e);
                }

                return EventResult.cont();
            }

            @Override
            public void handleGlobalError(OperationResult currentResult) {
                // no reaction
            }

        };

        LegacyValidator<Objectable> validator = new LegacyValidator<>(PrismTestUtil.getPrismContext());
        validator.setVerbose(false);
        validator.setAllowAnyType(true);
        validator.setHandler(handler);
        FileInputStream fis = new FileInputStream(file);
        OperationResult result = new OperationResult(RESULT_OPERATION_NAME);

        validator.validate(fis, result, OBJECT_RESULT_OPERATION_NAME);

        if (!result.isSuccess()) {
            // The error is most likely the first inner result. Therefore let's pull it out for convenience
            String errorMessage = result.getMessage();
            if (!result.getSubresults().isEmpty()) {
                if (result.getSubresults().get(0).getMessage() != null) {
                    errorMessage = result.getSubresults().get(0).getMessage();
                }
            }
            System.out.println("ERROR: " + errorMessage);
            System.out.println(result.debugDump());
            Assert.fail(file.getPath() + ": " + errorMessage);
        } else {
            System.out.println("OK");
            //System.out.println(result.dump());
        }

        System.out.println();
    }
}
