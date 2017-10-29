package org.openstreetmap.osmaxil;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.springframework.stereotype.Component;

@Component
public class StatsGenerator {

	static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

	public void displayRepartitionOfMatchingScore(Collection<AbstractElement> elements) {
		LOGGER_FOR_STATS.info("Repartitions of elements by matching scores:");
		int[] matchedElementsNbrByScore = new int[10];
		for (AbstractElement element : elements) {
			Float score = element.getMatchingScore();
			for (int i = 0; i < 10; i++) {
				if (score <= (i + 1) * 0.1) {
					matchedElementsNbrByScore[i]++;
					break;
				}
			}
		}
		for (int i = 0; i < 10; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(" - score between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
			sb.append(matchedElementsNbrByScore[i]);
			if (elements.size() > 0) {
				sb.append(" (" + 100 * matchedElementsNbrByScore[i] / elements.size() + "%)");
			}
			sb.append(" elements");
			LOGGER_FOR_STATS.info(sb);
		}
	}

	public void displayRepartitionByValues(Collection<AbstractElement> elements) {
		LOGGER_FOR_STATS.info("Repartitions of elements by values:");
		// TODO
	}
}
