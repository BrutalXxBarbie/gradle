/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.internal.resource.transfer

import org.gradle.internal.logging.progress.ProgressLogger
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.internal.resource.transfer.ResourceOperation.Type
import static org.gradle.internal.resource.transfer.ResourceOperation.toHumanReadableFormat

class ResourceOperationTest extends Specification {

    ProgressLogger progressLogger = Mock()

    def "no progress event is logged for files < 1024 bytes"() {
        given:
        def operation = new ResourceOperation(progressLogger, Type.download, 1023)
        when:
        operation.logProcessedBytes(1023)
        then:
        0 * progressLogger.progress(_)
    }

    def "logs processed bytes in kbyte intervals"() {
        given:
        def operation = new ResourceOperation(progressLogger, Type.download, 1024 * 10)
        when:
        operation.logProcessedBytes(512 * 0)
        operation.logProcessedBytes(512 * 1)
        then:
        0 * progressLogger.progress(_)

        when:
        operation.logProcessedBytes(512 * 1)
        operation.logProcessedBytes(512 * 2)
        then:
        1 * progressLogger.progress("1 KiB/10 KiB downloaded")
        1 * progressLogger.progress("2 KiB/10 KiB downloaded")
        0 * progressLogger.progress(_)
    }

    def "last chunk of bytes <1k is not logged"() {
        given:
        def operation = new ResourceOperation(progressLogger, Type.download, 2000)
        when:
        operation.logProcessedBytes(1000)
        operation.logProcessedBytes(1000)
        then:
        1 * progressLogger.progress("1 KiB/1 KiB downloaded")
        0 * progressLogger.progress(_)
    }

    def "adds operationtype information in progress output"() {
        given:
        def operation = new ResourceOperation(progressLogger, type, 1024 * 10)
        when:
        operation.logProcessedBytes(1024)
        then:
        1 * progressLogger.progress(message)
        where:
        type          | message
        Type.download | "1 KiB/10 KiB downloaded"
        Type.upload   | "1 KiB/10 KiB uploaded"
    }

    def "completed completes progressLogger"() {
        given:
        def operation = new ResourceOperation(progressLogger, Type.upload, 1)
        when:
        operation.completed()
        then:
        1 * progressLogger.completed()
    }

    def "handles unknown content length"() {
        given:
        def operation = new ResourceOperation(progressLogger, Type.upload, 0)
        when:
        operation.logProcessedBytes(1024)
        then:
        1 * progressLogger.progress("1 KiB/unknown size uploaded")
    }

    @Unroll
    def "converts to 1024 based human readable format (#bytes -> #humanReadableString)"() {
        expect:
        toHumanReadableFormat(bytes) == humanReadableString

        where:
        bytes || humanReadableString
        0     || '0 B'
        null  || 'unknown size'
    }

    @Unroll
    def "converts 2^10 based human readable format (±#bytes -> ±#humanReadableString)"(long bytes, String humanReadableString) {
        expect:
        toHumanReadableFormat(bytes) == humanReadableString
        toHumanReadableFormat(-bytes) == "-$humanReadableString"

        where:
        bytes             || humanReadableString
        1                 || '1 B'
        10                || '10 B'
        11                || '11 B'
        111               || '111 B'
        512               || '512 B'
        1023              || '1023 B'
        1024              || '1 KiB'
        1025              || '1 KiB'
        1024**1 - 1       || '1023 B'

        1024**1           || '1 KiB'
        1024**1 + 1       || '1 KiB'
        1024**2 - 1       || '1023 KiB'

        1024**2           || '1 MiB'
        1024**2 + 1       || '1 MiB'
        1024**3 - 1       || '1023 MiB'

        1024**3           || '1 GiB'
        1024**3 + 1       || '1 GiB'
        1024**4 - 1       || '1023 GiB'

        1024**4           || '1 TiB'
        1024**4 + 1       || '1 TiB'
        1024**5 - 1       || '1023 TiB'

        1024**5           || '1 PiB'
        1024**5 + 1       || '1 PiB'
        1024**6 - 1       || '1023 PiB'

        1024**6           || '1 EiB'
        1024**6 + 1       || '1 EiB'

        Integer.MAX_VALUE || '1 GiB'
        Long.MAX_VALUE    || '7 EiB'
    }

    @Unroll
    def "converts random values to 2^10 based human readable format"() {
        expect:
        def rand = new Random(0)
        (0..1_000_000)
            .collect { rand.nextLong() }
            .each { long bytes ->
                assert toHumanReadableFormat(bytes) == toHumanReadableFormatSlow(bytes)
            }
    }

    private static String toHumanReadableFormatSlow(long bytes) {
        switch (Math.abs(bytes)) {
            case { it < 1024 }: return "${bytes} B"
            case { it < 1024**2 }: return "${(bytes / 1024**1).longValue()} KiB"
            case { it < 1024**3 }: return "${(bytes / 1024**2).longValue()} MiB"
            case { it < 1024**4 }: return "${(bytes / 1024**3).longValue()} GiB"
            case { it < 1024**5 }: return "${(bytes / 1024**4).longValue()} TiB"
            case { it < 1024**6 }: return "${(bytes / 1024**5).longValue()} PiB"
            case { it < 1024**7 }: return "${(bytes / 1024**6).longValue()} EiB"
        }
    }
}

