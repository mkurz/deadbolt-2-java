/*
 * Copyright 2010-2016 Steve Chaloner
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
package be.objectify.deadbolt.java.filters;

import akka.stream.Materializer;
import play.core.j.JavaContextComponents;
import play.mvc.Filter;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltFilter extends Filter
{
    private final JavaContextComponents javaContextComponents;

    public AbstractDeadboltFilter(final Materializer mat,
                                  final JavaContextComponents javaContextComponents)
    {
        super(mat);
        this.javaContextComponents = javaContextComponents;
    }

    Http.Context context(final Http.RequestHeader requestHeader)
    {
        final Http.RequestBuilder requestBuilder = new Http.RequestBuilder().headers(requestHeader.headers())
                                                                            .host(requestHeader.host())
                                                                            .method(requestHeader.method())
                                                                            .path(requestHeader.path())
                                                                            .remoteAddress(requestHeader.remoteAddress())
                                                                            .secure(requestHeader.secure())
                                                                            .attrs(requestHeader.attrs())
                                                                            .tags(requestHeader.tags())
                                                                            .host(requestHeader.host())
                                                                            .uri(requestHeader.uri())
                                                                            .version(requestHeader.version());
        requestHeader.clientCertificateChain().ifPresent(requestBuilder::clientCertificateChain);
        for (Http.Cookie cookie : requestHeader.cookies())
        {
            requestBuilder.cookie(cookie);
        }
        return new Http.Context(requestBuilder,
                                javaContextComponents);
    }
}
