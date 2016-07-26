package eu.europeana.enrichment.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import eu.europeana.corelib.solr.entity.TimespanImpl;

/**
 * @author hgeorgiadis
 *
 */
public class TimespanRepresentationMigration extends AbstractRepresentationMigration<TimespanImpl> {


	public TimespanRepresentationMigration(Map<String, String> lookupCodeUri,
			Map<String, String> lookupOriginalCodeUri) {
		super(lookupCodeUri, lookupOriginalCodeUri);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void migrateRepresentation(String codeURI, String originalCodeURI, TimespanImpl representation) {

		representation.setAbout(codeURI);
		Map<String, List<String>> isPartOfNew = new HashMap<String, List<String>>();
		Map<String, List<String>> isPartOf = representation.getIsPartOf();
		if (isPartOf != null && !isPartOf.isEmpty()) {
			for (String lang : isPartOf.keySet()) {
				List<String> isPartOrUrisNew = new ArrayList<String>();
				for (String isPartOfURI : isPartOf.get(lang)) {
					

					String lookupCodeUri = lookupOriginalCodeUri(isPartOfURI);
					if (lookupCodeUri != null) {

						isPartOrUrisNew.add(lookupCodeUri);
					}
				}
				if (!isPartOrUrisNew.isEmpty()) {
					isPartOfNew.put(lang, isPartOrUrisNew);
				}
			}
			representation.setIsPartOf(isPartOfNew.isEmpty() ? null : isPartOfNew);
		}

		Map<String, List<String>> hasPartNew = new HashMap<String, List<String>>();
		Map<String, List<String>> hasPart = representation.getDctermsHasPart();
		if (hasPart != null && !hasPart.isEmpty()) {
			for (String lang : hasPart.keySet()) {
				List<String> hasPartUrisNew = new ArrayList<String>();
				for (String hasPartURI : hasPart.get(lang)) {
										
					String lookupCodeUri = lookupOriginalCodeUri(hasPartURI);
					if (lookupCodeUri != null) {

						hasPartUrisNew.add(lookupCodeUri);
					}
				}
				if (!hasPartUrisNew.isEmpty()) {
					hasPartNew.put(lang, hasPartUrisNew);
				}
			}
			representation.setDctermsHasPart(hasPartNew.isEmpty() ? null : hasPartNew);
		}

		String[] sameAsURIs = representation.getOwlSameAs();
		sameAsURIs = ArrayUtils.addAll(sameAsURIs, originalCodeURI);
		representation.setOwlSameAs(sameAsURIs);
	}

}