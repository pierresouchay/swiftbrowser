package net.souchay.swift.downloads;

import java.io.File;

public interface CompareStrategy {

    boolean areEqual(RemoteSpec spec, File file);

}
