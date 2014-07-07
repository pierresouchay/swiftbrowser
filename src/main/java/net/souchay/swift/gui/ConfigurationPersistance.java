package net.souchay.swift.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.souchay.swift.net.SwiftConfiguration;

public class ConfigurationPersistance {

    private final File configs;

    private final static Logger LOG = Logger.getLogger(ConfigurationPersistance.class.getName());

    /**
     * Get the configuration directory
     * 
     * @return
     */
    public File getConfigurationDirectory() {
        return rootDir;
    }

    /**
     * Get the instance factory
     * 
     * @return the instance
     */
    public static synchronized ConfigurationPersistance getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ConfigurationPersistance();
        return INSTANCE;
    }

    private static ConfigurationPersistance INSTANCE;

    private final File rootDir;

    private ConfigurationPersistance() {
        rootDir = new File(new File(System.getProperty("user.home"), ".config"), "swiftBrowser"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        configs = new File(rootDir, "configurations"); //$NON-NLS-1$
        if (!configs.exists()) {
            configs.mkdirs();
        }

    }

    private final static Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    public List<SwiftConfiguration> loadConfigs() {
        String files[] = configs.list();
        ArrayList<SwiftConfiguration> daConfigs = new ArrayList<SwiftConfiguration>();
        for (String f : files) {
            File fx = new File(configs, f);
            if (f.endsWith(FILES_SUFFIX) && fx.isFile() && fx.canRead() && fx.length() > 0 && fx.length() < 81920) {
                Properties p = new Properties();
                try {
                    Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(fx), CHARSET));
                    try {
                        p.load(r);
                        SwiftConfiguration cfg = new SwiftConfiguration(p);
                        cfg.setId(f.substring(0, f.length() - FILES_SUFFIX.length()));
                        daConfigs.add(cfg);
                    } catch (Exception ignored) {
                        LOG.log(Level.WARNING, "Failed to load properties from file " + fx.getAbsolutePath(), ignored); //$NON-NLS-1$
                    } finally {
                        r.close();
                    }
                } catch (IOException notFound) {
                    LOG.log(Level.WARNING,
                            "Failed to load properties from NOT found file " + fx.getAbsolutePath(), notFound); //$NON-NLS-1$
                }
            }
        }
        return daConfigs;
    }

    private final static String FILES_SUFFIX = ".properties";//$NON-NLS-1$

    public void saveConfig(SwiftConfiguration configuration) throws IOException {
        Properties p = new Properties();
        configuration.saveProperties(p);
        File fx = new File(configs, configuration.getId() + FILES_SUFFIX);
        Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fx), CHARSET));
        try {
            p.store(w, "SwiftBrowser Configuration file"); //$NON-NLS-1$
        } finally {
            w.close();
        }

    }

    /**
     * Return true if configuration file has been deleted
     * 
     * @param configuration the configuration to delete
     * @return
     * @throws IOException
     */
    public boolean deleteConfiguration(SwiftConfiguration configuration) {
        File fx = new File(configs, configuration.getId() + FILES_SUFFIX);
        return fx.delete();
    }
}
