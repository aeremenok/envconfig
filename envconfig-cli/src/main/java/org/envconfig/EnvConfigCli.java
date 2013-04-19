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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Wraps {@link EnvConfig} with command-line interface.
 *
 * @author AYeremenok
 */
public class EnvConfigCli {
    private static final String ENVIRONMENT = "env";
    private static final String CONFIG_PATH = "cp";
    private static final String HELP = "h";
    private static final String VERBOSE = "v";

    private static final Formatter PLAIN_LOG_FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord record) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            printWriter.println(record.getMessage());
            if (record.getThrown() != null) {
                record.getThrown().printStackTrace(printWriter);
                printWriter.println();
            }
            return stringWriter.toString();
        }
    };

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(ENVIRONMENT, "environment", true, "Environment catalog name to use overriding property files from");
        options.addOption(CONFIG_PATH, "config-path", true, "Config path, where destination property files reside");
        options.addOption(HELP, "help", false, "Prints help");
        options.addOption(VERBOSE, "verbose", false, "Prints detalied messages");

        CommandLineParser parser = new BasicParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption(HELP)) {
                printHelp(options);
                return;
            }

            tuneLoggers(commandLine.hasOption(VERBOSE) ? Level.FINE : Level.INFO);

            String environment = commandLine.getOptionValue(ENVIRONMENT);
            String configPath = commandLine.getOptionValue(CONFIG_PATH);

            EnvConfig envConfig = new EnvConfig(new JarSourceFilePreprocessor());
            envConfig.configureEnvironment(environment, configPath);
        } catch (ParseException e) {
            error("Cannot parse command line arguments", e);
            printHelp(options);
        } catch (RuntimeException e) {
            error("Cannot perform config update", e);
            printHelp(options);
        } catch (IOException e) {
            error("Cannot perform config update", e);
        } catch (ConfigurationException e) {
            error("Cannot perform config update", e);
        }
    }

    private static void tuneLoggers(Level newLevel) {
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(newLevel);
            handler.setFormatter(PLAIN_LOG_FORMATTER);
        }
    }

    private static void error(String message, Throwable e) {
        Logger.getLogger(EnvConfigCli.class.getName()).log(Level.SEVERE, message, e);
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("envconfig", options);
    }
}
