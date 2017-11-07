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
package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public interface BeforeAuthCheckCache extends Function3<DeadboltHandler, Http.Context, Optional<String>, CompletionStage<Optional<Result>>>
{
}

// Can be replaced with scala.Function3 when dropping support for Scala 2.11 (probably in Play 2.7)
interface Function3<A,B,C,R> {
    R apply(A a, B b, C c);
}