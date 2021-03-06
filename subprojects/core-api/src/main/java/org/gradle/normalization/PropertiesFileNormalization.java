/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.normalization;

import org.gradle.api.Incubating;

/**
 * Specifies how properties files should be normalized.
 *
 * @since 6.8
 */
@Incubating
public interface PropertiesFileNormalization {
    /**
     * Specifies that the value of a certain property should be ignored when normalizing properties files.  {@code propertyName} is matched case-sensitively with the property key.
     * This method can be called multiple times to declare additional properties to be ignored.
     * @param propertyName - the name of the property to ignore
     */
    void ignoreProperty(String propertyName);
}
