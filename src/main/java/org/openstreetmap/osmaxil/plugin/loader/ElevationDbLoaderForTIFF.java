package org.openstreetmap.osmaxil.plugin.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("ElevationDbLoaderForTIFF")
@Lazy
@Scope("prototype")
public class ElevationDbLoaderForTIFF extends AbstractElevationDbLoader {

	protected void loadData(String source) throws IOException {
		File folder = new File(this.folderPath);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".tiff") || name.toLowerCase().endsWith(".tif");
			}
		});
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			LOGGER.info("Converting " + file + " to the XYZ format");
			String xyzFilePath = file.getParentFile().getAbsolutePath() + File.separator + file.getName().substring(0, file.getName().length() - 4)
					+ ".xyz";
			String cmd = "gdal_translate.exe -of XYZ " + file.getAbsolutePath() + " " + xyzFilePath;
			executeCommand(cmd);
			LOGGER.info("Loading " + xyzFilePath);
			this.copyPointCloudFromXYZFile(source, xyzFilePath);
		}
	}

}
