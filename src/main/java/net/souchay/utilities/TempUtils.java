package net.souchay.utilities;

import java.io.File;
import java.io.IOException;

public class TempUtils {

    private static volatile File tempDir;

    public static File getTempDir() throws IOException {
        if (tempDir != null)
            return tempDir;
        File f = File.createTempFile("xxx", ".dat"); //$NON-NLS-1$//$NON-NLS-2$
        f.delete();
        File fx = f.getParentFile();
        tempDir = fx;
        return fx;
    }
}
