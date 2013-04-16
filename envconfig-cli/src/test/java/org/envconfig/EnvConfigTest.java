package org.envconfig;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author AYeremenok
 */
public class EnvConfigTest {
    @Test
    public void testConfigure() throws Exception {
        SourceFilePreprocessor classPathPreprocessor = new SourceFilePreprocessor() {
            public File prepareSourceFiles() throws IOException {
                return new File(getClass().getClassLoader().getResource("example1").getFile());
            }
        };
        EnvConfig envConfig = new EnvConfig(classPathPreprocessor);

        File configDir = new File(FileUtils.getTempDirectory(), "example1" + UUID.randomUUID());
        envConfig.configureEnvironment("alpha", configDir.getAbsolutePath());

        File conf1 = new File(configDir, "conf1.properties");
        File conf2 = new File(configDir, "conf2.properties");

        assertThat(configDir.exists(), is(true));
        assertThat(conf1.exists(), is(true));
        assertThat(conf2.exists(), is(true));
        assertThat(new File(configDir, "trash.properties").exists(), is(false));

        assertThat(FileUtils.readFileToString(conf1), is(equalTo("z.y.x = test1\r\n" +
                                                                         "z.y = test1\r\n" +
                                                                         "z = test1\r\n" +
                                                                         "\r\n" +
                                                                         "a.b = test\r\n" +
                                                                         "a.b.c = test\r\n" +
                                                                         "a.b.c.d = test\r\n")));
    }
}
