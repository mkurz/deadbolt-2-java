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
import play.Play;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Looks for custom implementations of {@link TemplateFailureListener} in the injector.  Provides a no-op version
 * if nothing else can be found.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class TemplateFailureListenerProvider implements Provider<TemplateFailureListener>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateFailureListenerProvider.class);

    @Override
    public TemplateFailureListener get()
    {
        TemplateFailureListener listener;
        try
        {
            listener = Play.application().injector().instanceOf(TemplateFailureListener.class);
            LOGGER.info("Custom TemplateFailureListener found: [{}]", listener.getClass());
        }
        catch (Exception e)
        {
            LOGGER.info("No custom TemplateFailureListener found, falling back to no-op implementation.  Don't worry, this is a feature and not a bug.");
            listener = new NoOpTemplateFailureListener();
        }
        return listener;
    }
}
