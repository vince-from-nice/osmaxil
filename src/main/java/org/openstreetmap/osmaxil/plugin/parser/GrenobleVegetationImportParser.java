package org.openstreetmap.osmaxil.plugin.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.VegetationImport;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component("GrenobleVegetationImportParser")
@Lazy
public class GrenobleVegetationImportParser extends AbstractImportParser<VegetationImport> {
	
	private int count;
	
	File json;

	class File {
		String name;
		String type;
		Feature[] features;
		class Feature {
			public Feature() {}
			String type;
			Geometry geometry;
			class Geometry {
				public Geometry() {};
				String type;
				double[] coordinates;
			}
			Properties properties;
			class Properties {
				public Properties() {};
				int ELEM_POINT_ID;
				String CODE;
				String NOM;
				String GENRE;
				String GENRE_DESC;
				String CATEGORIE;
				String CATEGORIE_DESC;
				String SOUS_CATEGORIE;
				String SOUS_CATEGORIE_DESC;
				String CODE_PARENT;
				String CODE_PARENT_DESC;
				String ADR_SECTEUR;
				String BIEN_REFERENCE;
				String GENRE_BOTA;
				String ESPECE;
				String VARIETE;
				String STADEDEDEVELOPPEMENT;
				String EQUIPE;
				String REMARQUES;
				int ANNEEDEPLANTATION;
				String RAISONDEPLANTATION;
				String TRAITEMENTCHENILLES;
				float DIAMETREARBRE;
			}
		}
	}

	@PostConstruct
	public void init() throws IOException {
		LOGGER.info("Init of GrenobleVegetationImportParser");
		String fileContent = new String(Files.readAllBytes(Paths.get(this.filePath)), Charset.forName("UTF-8"));
		Gson gson = new Gson();
		this.json = gson.fromJson(fileContent, File.class);
		LOGGER.info("Ok " + json.features.length + " has been loaded");
	}

	@Override
	public boolean hasNext() {
		return count < this.json.features.length;
	}

	@Override
	public VegetationImport next() {
		VegetationImport result = new VegetationImport();
		File.Feature f = this.json.features[this.count];
		File.Feature.Properties p = f.properties;
		result.setId(this.count + 1);
		result.setReference(String.valueOf(p.ELEM_POINT_ID));
		result.setLongitude(f.geometry.coordinates[0]);
		result.setLatitude(f.geometry.coordinates[1]);
		result.setGenus(p.GENRE_BOTA);
		if (p.ESPECE != null) {
			if (p.GENRE_BOTA != null) {
				result.setSpecies(p.GENRE_BOTA + " " + p.ESPECE);
			} else {
				result.setSpecies(p.ESPECE);
			}
		}		
		if (p.DIAMETREARBRE > 0) {
			result.setCircumference(p.DIAMETREARBRE);
		}
		if (p.ANNEEDEPLANTATION > 0) {
			result.setPlantingYear(p.ANNEEDEPLANTATION);
		}
		this.count++;
		return result;
	}

}
