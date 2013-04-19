/*
 * Copyright (C) 2013 Andrey Yeremenok (eav1986__at__gmail__com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.envconfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import static java.util.Arrays.asList;

/**
 * Extracts property files from the current working jar to a subdirectory of ${java.io.tmpdir}, which is deleted on JVM exit.
 *
 * @author AYeremenok
 */
public class JarSourceFilePreprocessor implements SourceFilePreprocessor {
    // Can unjar and copy default files, but patching is supported only for "properties".
    private static final Set<String> CONFIG_EXTENSIONS = new HashSet<String>(asList("properties", "xml", "conf"));

    public File prepareSourceFiles() throws IOException {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IOException("Unable to read jar content");
        }

        File destinationDir = new File(FileUtils.getTempDirectory(), String.format("envconfig-%s", System.currentTimeMillis()));
        destinationDir.deleteOnExit();

        URL location = codeSource.getLocation();
        ZipFile zipFile = new ZipFile(location.getFile());

        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry zipEntry = (ZipEntry) e.nextElement();
            File destFile = new File(destinationDir, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                destFile.mkdirs();
            } else if (CONFIG_EXTENSIONS.contains(FilenameUtils.getExtension(zipEntry.getName()).toLowerCase())) {
                unzipFile(zipFile, zipEntry, destFile);
            }
        }

        return destinationDir;
    }

    private void unzipFile(ZipFile zipFile, ZipEntry zipEntry, File destFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            out = new BufferedOutputStream(new FileOutputStream(destFile));
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
