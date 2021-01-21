/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
 * ================================================================================
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.computation;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File read utils.
 *
 * @author Kai Lu
 */
public class FileUtils {

    /**
     * Reads contents of files inside the eventBodyDirectory, combines contents with
     * metadata to make an Event Object. Fails test in the event of failure to read
     * a file.
     * 
     * @param targetDirectory Path to directory with files.
     * @return List of Strings containing the body as acquired from files inside
     *         targetDirectory.
     * @throws IOException in the event it fails to read from the files.
     */
    public static List<String> getFilesFromDirectory(Path targetDirectory) throws IOException {
        try (Stream<Path> eventFileStream = Files.walk(targetDirectory)) {
            return eventFileStream.filter(Files::isRegularFile).filter(Files::isReadable)
                    .map(FileUtils::getFileContents).collect(Collectors.toList());
        }
    }

    /**
     * reads contents of file into a string. fails a tests in the event failure
     * occurs.
     * 
     * @param path path to file.
     * @return string containing files contents
     */
    public static String getFileContents(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException exception) {
            fail("IOException occurred while reading file.");
        }
        return null;
    }

    /**
     * Reads contents of resource. fails a test in the event failure occurs.
     * 
     * @param fileName of file in resources to be read.
     * @return contents of file
     */
    public static String getFileContents(String fileName) {
        try {
            Path path = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
            return getFileContents(path);
        } catch (URISyntaxException exception) {
            fail("Exception occurred, failed to acquire resource URI.");
        }
        return null;
    }
}
