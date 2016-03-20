/*
 * Copyright 2012-2015 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * @author Steve Ch aloner (steve@objectify.be)
 */
@Singleton
public class ExecutionContextProvider implements Supplier<DeadboltExecutionContextProvider>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContextProvider.class);

    private final DeadboltExecutionContextProvider ecProvider;

    @Inject
    public ExecutionContextProvider(final Configuration config,
                                    final Application application)
    {
        boolean customEcEnabled = config.getBoolean(ConfigKeys.CUSTOM_EC_DEFAULT._1,
                                                    ConfigKeys.CUSTOM_EC_DEFAULT._2);
        DeadboltExecutionContextProvider defaultProvider = new DefaultDeadboltExecutionContextProvider();
        DeadboltExecutionContextProvider ecp = defaultProvider;
        if (customEcEnabled)
        {
            try
            {
                ecp = application.injector().instanceOf(DeadboltExecutionContextProvider.class);
                LOGGER.debug("Custom execution context provider found");
            }
            catch (Exception e)
            {
                LOGGER.debug("No custom execution context found.");
            }
            this.ecProvider = ecp;
        }
        else
        {
            ecProvider = defaultProvider;
        }
    }

    @Override
    public DeadboltExecutionContextProvider get()
    {
        return ecProvider;
    }
}
