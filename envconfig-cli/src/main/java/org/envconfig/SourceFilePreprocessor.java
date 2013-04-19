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

import java.io.File;
import java.io.IOException;

/**
 * Performs preparation of source config files from a bunle. Different implementations should handle different bundle types. </br>
 * At the present time the only one is {@link JarSourceFilePreprocessor}, which extracts property files from the current working jar.
 *
 * @author AYeremenok
 */
public interface SourceFilePreprocessor {
    /**
     * Prepares source config files for being accessible while config update.
     *
     * @return the prepared source directory
     * @throws IOException if preparation failed
     */
    File prepareSourceFiles() throws IOException;
}
