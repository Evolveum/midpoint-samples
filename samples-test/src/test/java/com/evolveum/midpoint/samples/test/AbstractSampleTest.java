/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.samples.test;

import java.io.File;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.evolveum.midpoint.model.test.AbstractModelIntegrationTest;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * Base class for sample testing.
 * Samples are not testing by starting the application (Spring context) with all
 * samples imported during the startup, as they are mutually exclusive.
 */
@ContextConfiguration(locations = { "classpath:ctx-samples-test.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractSampleTest extends AbstractModelIntegrationTest {

    // the tests are run in "sample-test" project directory, we can use samples (sources),
    // as there is no further preprocessing (e.g. Maven filtering) involved
    protected static final File SAMPLES_DIRECTORY = new File("../samples");

    protected static final File USER_ADMINISTRATOR_FILE =
            new File("src/test/resources/user-administrator.xml");

    @Override
    public void initSystem(Task initTask, OperationResult initResult) throws Exception {
        super.initSystem(initTask, initResult);

        // This should discover the connectors
        logger.trace("initSystem: trying modelService.postInit()");
        modelService.postInit(initResult);
        logger.trace("initSystem: modelService.postInit() done");

        PrismObject<UserType> userAdministrator =
                repoAddObjectFromFile(USER_ADMINISTRATOR_FILE, initResult);
        loginSuperUser(userAdministrator);
    }
}
