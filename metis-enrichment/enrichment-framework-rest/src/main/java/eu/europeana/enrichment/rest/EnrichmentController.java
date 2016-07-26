package eu.europeana.enrichment.rest;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.*;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.EntityRemover;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


@Api("/")
@Controller(value = "/")
public class EnrichmentController {
	Logger log = Logger.getLogger(this.getClass().getName());
	@Autowired
	private  Enricher enricher;



    @ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/delete",method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete a list of URIs")
	public void delete(@ApiParam @RequestBody UriList values) throws Exception {
        EntityRemover remover = new EntityRemover(enricher.getEnricher());
        remover.remove(values.getUris(), null);
    }


	@RequestMapping(value = "/getByUri", method = RequestMethod.POST)
	@ApiOperation(value = "Retrieve an entity by URI or its sameAs", response = EntityWrapper.class)
	@ResponseBody
	public String getByUri(@ApiParam("uri") @RequestParam("uri") String uri,
						   @ApiParam("toXml") @RequestParam("toXml") boolean toEdm) throws IOException {

		EntityWrapper wrapper = enricher.getByUri(uri);
		if(wrapper!=null) {
			ObjectMapper objIdMapper = new ObjectMapper();
			try {
			if (toEdm) {
					EntityWrapper newWrapper = new EntityWrapper(wrapper.getClassName(),
                            wrapper.getOriginalField(),wrapper.getUrl(),wrapper.getOriginalValue(), convertEntity(wrapper));
				return objIdMapper.writeValueAsString(newWrapper);
			} else {
				SimpleModule sm = new SimpleModule("objId",
						Version.unknownVersion());
				sm.addSerializer(new ObjectIdSerializer());
				objIdMapper.registerModule(sm);
				return objIdMapper.writeValueAsString(wrapper);
			}

			} catch (IOException e) {
				throw e;
			}
		}
		return "";
	}


	@RequestMapping("/enrich")
	@ResponseBody
	@ApiOperation(value = "Enrich a series of field value pairs", response = EntityWrapperList.class)
    public String enrich(@ApiParam("input")@RequestParam("input") String input,
            @ApiParam("toXml")@RequestParam("toXml") String toXML) throws Exception {
        try{
			ObjectMapper mapper = new ObjectMapper();
			InputValueList values = mapper.readValue(input,
					InputValueList.class);
			EntityWrapperList response = new EntityWrapperList();List<EntityWrapper> wrapperList = enricher.tagExternal(values
					.getInputValueList());

			ObjectMapper objIdMapper = new ObjectMapper();
			if (!Boolean.parseBoolean(toXML)) {
				SimpleModule sm = new SimpleModule("objId",
						Version.unknownVersion());
				sm.addSerializer(new ObjectIdSerializer());
				objIdMapper.registerModule(sm);
				response.setWrapperList(wrapperList);
			} else {

				response.setWrapperList(convertToXml(wrapperList));
			}

			return objIdMapper.writeValueAsString(response);

		} catch (JsonParseException e) {
			throw e;
		} catch (JsonMappingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}

	}

	private List<EntityWrapper> convertToXml(List<EntityWrapper> wrapperList)
			throws JsonParseException, JsonMappingException, IOException {
		List<EntityWrapper> entityWrapperList = new ArrayList<EntityWrapper>();
		for (EntityWrapper wrapper : wrapperList) {
			entityWrapperList.add(new EntityWrapper(wrapper.getClassName(),
					wrapper.getOriginalField(),wrapper.getUrl(),wrapper.getOriginalValue(), convertEntity(wrapper)));
		}
		return entityWrapperList;
	}

	private String convertEntity(EntityWrapper wrapper)
			throws JsonParseException, JsonMappingException, IOException {
		if (wrapper.getClassName().equals(ConceptImpl.class.getName())) {
			return convertConcept(wrapper.getContextualEntity());
		} else if (wrapper.getClassName().equals(AgentImpl.class.getName())) {
			return convertAgent(wrapper.getContextualEntity());
		} else if (wrapper.getClassName().equals(PlaceImpl.class.getName())) {
			return convertPlace(wrapper.getContextualEntity());
		} else {
			return convertTimespan(wrapper.getContextualEntity());
		}
	}

	private String convertTimespan(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		TimespanImpl ts = new ObjectMapper().readValue(contextualEntity,
				TimespanImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Timespan rdf:about=\"");
		sb.append(ts.getAbout());
		sb.append("\">\n");
		addMap(sb, ts.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, ts.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, ts.getBegin(), "edm:begin", "xml:lang", false);
		addMap(sb, ts.getEnd(), "edm:end", "xml:lang", false);
		addMap(sb, ts.getDctermsHasPart(), "dcterms:hasPart", "rdf:resource",
				true);
		addMap(sb, ts.getHiddenLabel(), "skos:hiddenLabel", "xml:lang", false);
		addMap(sb, ts.getIsPartOf(), "dcterms:isPartOf", "rdf:resource", true);
		addMap(sb, ts.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, ts.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		sb.append("</edm:Timespan>");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));

		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private void addArray(StringBuilder sb, String[] arrValues, String element,
			String attribute) {
		if (arrValues != null) {
			for (String str : arrValues) {
				sb.append("<");
				sb.append(element);
				sb.append(" ");
				sb.append(attribute);
				sb.append("\"=");
				sb.append(str);
				sb.append("\"/>\n");
			}
		}
	}

	private void addMap(StringBuilder sb, Map<String, List<String>> values,
			String elementName, String attributeName, boolean isResource) {
		if (values != null) {
			for (Entry<String, List<String>> entry : values.entrySet()) {
				for (String str : entry.getValue()) {
					sb.append("<");
					sb.append(elementName);
					sb.append(" ");
					sb.append(attributeName);
					sb.append("=\"");
					if (!isResource) {
						sb.append(entry.getKey());
						sb.append("\">");
						sb.append(str);
						sb.append("</");
						sb.append(elementName);
						sb.append(">\n");
					} else {
						sb.append(str);
						sb.append("\"/>\n");
					}
				}
			}
		}
	}

	private String convertPlace(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		PlaceImpl place = new ObjectMapper().readValue(contextualEntity,
				PlaceImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Place rdf:about=\"");
		sb.append(place.getAbout());
		sb.append("\">\n");
		addMap(sb, place.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, place.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, place.getDcTermsHasPart(), "dcterms:hasPart",
				"rdf:resource", true);
		addMap(sb, place.getIsPartOf(), "dcterms:isPartOf", "rdf:resource",
				true);
		addMap(sb, place.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, place.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		if ((place.getLatitude()!=null&& place.getLatitude()!= 0) && (place.getLongitude()!=null && place.getLongitude() != 0)) {
			sb.append("<wgs84_pos:long>");
			sb.append(place.getLongitude());
			sb.append("</wgs84_pos:long>\n");
			sb.append("<wgs84_pos:lat>");
			sb.append(place.getLatitude());
			sb.append("</wgs84_pos:lat>\n");
		}
		if (place.getAltitude()!=null && place.getAltitude()!= 0) {
			sb.append("<wgs84_pos:alt>");
			sb.append(place.getAltitude());
			sb.append("</wgs84_pos:alt>\n");
		}
		sb.append("</edm:Place>\n");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private String convertAgent(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		AgentImpl agent = new ObjectMapper().readValue(contextualEntity,
				AgentImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<edm:Agent rdf:about=\"");
		sb.append(agent.getAbout());
		sb.append("\">");
		addMap(sb, agent.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, agent.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, agent.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
				false);
		addMap(sb, agent.getFoafName(), "foaf:name", "xml:lang", false);
		addMap(sb, agent.getNote(), "skos:note", "xml:lang", false);
		addMap(sb, agent.getBegin(), "edm:begin", "xml:lang", false);
		addMap(sb, agent.getEnd(), "edm:end", "xml:lang", false);
		addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);
		addMap(sb, agent.getEdmHasMet(), "edm:hasMet", "xml:lang", false);
		addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);
		addMap(sb, agent.getRdaGr2BiographicalInformation(),
				"rdaGr2:biographicaInformation", "xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfBirth(), "rdaGr2:dateOfBirth",
				"xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfDeath(), "rdaGr2:dateOfDeath",
				"xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfEstablishment(),
				"rdaGr2:dateOfEstablishment", "xml:lang", false);
		addMap(sb, agent.getRdaGr2DateOfTermination(),
				"rdaGr2:dateOfTermination", "xml:lang", false);
		addMap(sb, agent.getRdaGr2Gender(), "rdaGr2:gender", "xml:lang", false);
		addMapResourceOrLiteral(sb, agent.getDcDate(), "dc:date");
		addMapResourceOrLiteral(sb, agent.getEdmIsRelatedTo(),
				"edm:isRelatedTo");
		addMapResourceOrLiteral(sb, agent.getRdaGr2ProfessionOrOccupation(),
				"rdaGr2:professionOrOccupation");
		addArray(sb, agent.getEdmWasPresentAt(), "edm:wasPresentAt",
				"rdf:resource");
		addArray(sb, agent.getOwlSameAs(), "owl:sameAs", "rdf:resource");
		sb.append("</edm:Agent>\n");
		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}

	private void addMapResourceOrLiteral(StringBuilder sb,
			Map<String, List<String>> values, String element) {

		if (values != null) {
			for (Entry<String, List<String>> entry : values.entrySet()) {
				for (String str : entry.getValue()) {
					sb.append("<");
					sb.append(element);
					sb.append(" ");
					if(isUri(str)){
						sb.append("rdf:resource=\"");
						sb.append(str);
						sb.append("\"/>\n");
					} else {
						sb.append("xml:lang=\"");
						sb.append(entry.getKey());
						sb.append("\">");
						sb.append(str);
						sb.append("</");
						sb.append(element);
						sb.append(">\n");
					}
				}
			}
		}
	}

	private boolean isUri(String str) {
		return str.startsWith("http://");
	}

	private String convertConcept(String contextualEntity)
			throws JsonParseException, JsonMappingException, IOException {
		ConceptImpl concept = new ObjectMapper().readValue(contextualEntity,
				ConceptImpl.class);
		StringBuilder sb = new StringBuilder();
		sb.append("<skos:Concept rdf:about=\"");
		sb.append(concept.getAbout());
		sb.append("\"/>\n");
		addMap(sb, concept.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
		addMap(sb, concept.getAltLabel(), "skos:altLabel", "xml:lang", false);
		addMap(sb, concept.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
				false);
		addMap(sb, concept.getNotation(), "skos:notation", "xml:lang", false);
		addMap(sb, concept.getNote(), "skos:note", "xml:lang", false);
		addArray(sb, concept.getBroader(), "skos:broader", "rdf:resource");
		addArray(sb, concept.getBroadMatch(), "skos:broadMatch", "rdf:resource");
		addArray(sb, concept.getCloseMatch(), "skos:closeMatch", "rdf:resource");
		addArray(sb, concept.getExactMatch(), "skos:exactMatch", "rdf:resource");
		addArray(sb, concept.getInScheme(), "skos:inScheme", "rdf:resource");
		addArray(sb, concept.getNarrower(), "skos:narrower", "rdf:resource");
		addArray(sb, concept.getNarrowMatch(), "skos:narrowMatch",
				"rdf:resource");
		addArray(sb, concept.getRelated(), "skos:related", "rdf:resource");
		addArray(sb, concept.getRelatedMatch(), "skos:relatedMatch",
				"rdf:resource");
		sb.append("</skos:Concept>\n");

		log.info(StringEscapeUtils.escapeXml(sb.toString()));
		return StringEscapeUtils.escapeHtml3(sb.toString());
	}
}