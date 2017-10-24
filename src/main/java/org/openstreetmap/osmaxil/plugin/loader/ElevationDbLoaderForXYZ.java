package org.openstreetmap.osmaxil.plugin.loader;

import java.io.File;
import java.io.FilenameFilter;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("ElevationDbLoaderForXYZ")
@Lazy
@Scope("prototype")
public class ElevationDbLoaderForXYZ extends AbstractElevationDbLoader {

	protected void loadData(String source) {
		File xyzFolder = new File(this.folderPath);
		File[] xyzFiles = xyzFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xyz");
			}
		});
		for (int i = 0; i < xyzFiles.length; i++) {
			File xyzFile = xyzFiles[i];
			LOGGER.info("Loading file " + xyzFile);
			this.copyPointCloudFromXYZFile(source, xyzFile.getAbsolutePath());
		}
	}

}
