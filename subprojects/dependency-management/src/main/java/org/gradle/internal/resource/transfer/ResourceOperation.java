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

package org.gradle.internal.resource.transfer;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.gradle.internal.logging.progress.ProgressLogger;

import javax.annotation.Nullable;

public class ResourceOperation {

    private static final int BIT_GROUP_COUNT = 10;
    private static final int KiB_BASE = 1 << BIT_GROUP_COUNT;
    private static final String[] UNITS = new String[]{" B", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB"};

    public enum Type {
        download,
        upload;

        public String getCapitalized() {
            return StringUtils.capitalize(toString());
        }
    }

    private final ProgressLogger progressLogger;
    private final Type operationType;
    private final String contentLengthString;

    private long loggedKBytes;
    private long totalProcessedBytes;

    public ResourceOperation(ProgressLogger progressLogger, Type type, long contentLength) {
        this.progressLogger = progressLogger;
        this.operationType = type;
        this.contentLengthString = toHumanReadableFormat(contentLength == 0 ? null : contentLength);
    }

    @VisibleForTesting
    static String toHumanReadableFormat(@Nullable Long bytes) {
        if (bytes == null) {
            return "unknown size";
        } else if (bytes < 0) {
            return "-" + toHumanReadableFormat(-bytes);
        } else {
            int baseExponent = ((Long.SIZE - 1) - Long.numberOfLeadingZeros(bytes)) / BIT_GROUP_COUNT;
            long l = 1L << (baseExponent * BIT_GROUP_COUNT);

            long result = bytes / l;
            String unit = UNITS[baseExponent];
            return result + unit;
        }
    }

    public void logProcessedBytes(long processedBytes) {
        totalProcessedBytes += processedBytes;
        long processedKiB = totalProcessedBytes / KiB_BASE;
        if (processedKiB > loggedKBytes) {
            loggedKBytes = processedKiB;
            String progressMessage = String.format("%s/%s %sed", toHumanReadableFormat(totalProcessedBytes), contentLengthString, operationType);
            progressLogger.progress(progressMessage);
        }
    }

    public void completed() {
        this.progressLogger.completed();
    }
}
