package org.envconfig;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;

import static org.apache.commons.io.filefilter.FileFilterUtils.nameFileFilter;

/**
 * Performs environment configuration update.
 *
 * @author AYeremenok
 */
public class EnvConfig {
    private static final Logger LOG = Logger.getLogger(EnvConfig.class.getName());

    private static final String DEFAULT_DIRECTORY_NAME = "defaults";

    private final SourceFilePreprocessor sourceFilePreprocessor;

    /**
     * Injects the given source file preprocessing implementation.
     *
     * @param sourceFilePreprocessor source file preprocessor
     */
    public EnvConfig(SourceFilePreprocessor sourceFilePreprocessor) {
        this.sourceFilePreprocessor = sourceFilePreprocessor;
    }

    /**
     * Updates the configuration files of the current host. </br>
     * First, the files of /default directory are copied to the config path, if they are missing. </br>
     * Second, the values from files in /"environment" directory replace the values in corresponding files in the config path.
     *
     * @param environment environment name
     * @param configPath  config path
     * @throws IOException              if failed to read or write some file
     * @throws IllegalArgumentException if environment or configPath is empty
     * @throws IllegalStateException    if some directory of config file is in inconsistent state
     */
    public void configureEnvironment(String environment, String configPath) throws IOException, ConfigurationException {
        if (StringUtils.isBlank(environment)) {
            throw new IllegalArgumentException("Environment must not be empty");
        }
        if (StringUtils.isBlank(configPath)) {
            throw new IllegalArgumentException("Config path must not be empty");
        }

        LOG.info(String.format("Updating configs %s on environment %s", environment, configPath));

        File destRootDir = getDestRootDir(configPath);
        File sourceRootDir = sourceFilePreprocessor.prepareSourceFiles();

        IOFileFilter acceptDefaultDir = nameFileFilter(DEFAULT_DIRECTORY_NAME);
        for (File defaults : FileUtils.listFiles(sourceRootDir, new ParentDirectoryFilter(acceptDefaultDir), acceptDefaultDir)) {
            copyDefaultPropertyFile(destRootDir, defaults);
        }

        IOFileFilter acceptEnvironmentDir = nameFileFilter(environment);
        for (File overrides : FileUtils.listFiles(sourceRootDir, new ParentDirectoryFilter(acceptEnvironmentDir), acceptEnvironmentDir)) {
            updateConfigFile(overrides, destRootDir);
        }

        LOG.info(String.format("Configs of [%s]//%s updated", environment, configPath));
    }

    private File getDestRootDir(String configPath) {
        File destRootDir = new File(configPath);
        if (!destRootDir.exists()) {
            destRootDir.mkdirs();
        } else if (!destRootDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a directory", destRootDir));
        }
        return destRootDir;
    }

    private void updateConfigFile(File fileWithOverrides, File destRootDir) throws IOException, ConfigurationException {
        PropertiesConfiguration sourceConfig = new PropertiesConfiguration(fileWithOverrides);
        sourceConfig.setEncoding(Charsets.UTF_8.name());

        File destFile = new File(destRootDir, fileWithOverrides.getName());
        PropertiesConfiguration destConfig = new PropertiesConfiguration(destFile);
        destConfig.setEncoding(Charsets.UTF_8.name());
        destConfig.setAutoSave(false);

        int updates = 0;
        for (Iterator sourceKeys = sourceConfig.getKeys(); sourceKeys.hasNext(); ) {
            String key = (String) sourceKeys.next();
            Object value = sourceConfig.getProperty(key);

            LOG.fine(String.format("%s=%s will be copied to %s", key, value, destFile));

            if (destConfig.containsKey(key)) {
                destConfig.setProperty(key, value);
            } else {
                destConfig.addProperty(key, value);
            }
            updates++;
        }

        destConfig.save();
        LOG.info(String.format("%s updated with %s overriden keys", destFile, updates));
    }

    private void copyDefaultPropertyFile(File destRootDir, File defaultFile) throws IOException {
        File destFile = new File(destRootDir, defaultFile.getName());

        if (destFile.exists()) {
            LOG.info(String.format("%s will be replaced with the fresh one", destFile));
            FileUtils.forceDelete(destFile);
        } else {
            LOG.info(String.format("%s will be copied at the first time to %s", defaultFile.getName(), destRootDir));
        }

        FileUtils.copyFile(defaultFile, destFile);
    }

    /**
     * Applies a delegate filter to a parent directory of an examined file.
     */
    private static class ParentDirectoryFilter extends AbstractFileFilter {
        private final IOFileFilter parentDirFilter;

        private ParentDirectoryFilter(IOFileFilter parentDirFilter) {
            this.parentDirFilter = parentDirFilter;
        }

        @Override
        public boolean accept(File dir, String name) {
            return parentDirFilter.accept(dir);
        }
    }
}
