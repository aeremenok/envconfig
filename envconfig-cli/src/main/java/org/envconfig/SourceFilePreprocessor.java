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
