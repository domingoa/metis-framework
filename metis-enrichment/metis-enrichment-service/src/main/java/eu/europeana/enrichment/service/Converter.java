package eu.europeana.enrichment.service;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class Converter {

  public EnrichmentResultList convert(List<EntityWrapper> wrapperList) throws IOException {
    EnrichmentResultList list = new EnrichmentResultList();

    for (EntityWrapper wrapper : wrapperList) {
      list.getResult().add(convert(wrapper));
    }
    return list;

  }

  public EnrichmentBase convert(EntityWrapper wrapper) throws IOException {
    if (wrapper.getClassName().equals(ConceptImpl.class.getName())) {
      return convertConcept(wrapper.getContextualEntity());
    } else if (wrapper.getClassName().equals(AgentImpl.class.getName())) {
      return convertAgent(wrapper.getContextualEntity());
    } else if (wrapper.getClassName().equals(PlaceImpl.class.getName())) {
      return convertPlace(wrapper.getContextualEntity());
    } else if (wrapper.getClassName().equals(TimespanImpl.class.getName())) {
      return convertTimespan(wrapper.getContextualEntity());
    }
    return null;
  }

  private Timespan convertTimespan(String contextualEntity)
      throws IOException {
    TimespanImpl ts = new ObjectMapper().readValue(contextualEntity,
        TimespanImpl.class);

    Timespan output = new Timespan();

    output.setAbout(ts.getAbout());
    output.setPrefLabelList(convert(ts.getPrefLabel()));
    output.setAltLabelList(convert(ts.getAltLabel()));
    output.setBeginList(convert(ts.getBegin()));
    output.setEndList(convert(ts.getEnd()));
    output.setHasPartsList(convertPart(ts.getDctermsHasPart()));
    output.setHiddenLabel(convert(ts.getHiddenLabel()));
    output.setIsPartOfList(convertPart(ts.getIsPartOf()));
    output.setNotes(convert(ts.getNote()));
    output.setSameAs(convertToPartsList(ts.getOwlSameAs()));

    return output;
  }

  private Concept convertConcept(String contextualEntity)
      throws IOException {
    ConceptImpl concept = new ObjectMapper().readValue(contextualEntity, ConceptImpl.class);
    Concept output = new Concept();

    output.setAbout(concept.getAbout());
    output.setPrefLabelList(convert(concept.getPrefLabel()));
    output.setAltLabelList(convert(concept.getAltLabel()));
    output.setHiddenLabel(convert(concept.getHiddenLabel()));
    output.setNotation(convert(concept.getNotation()));
    output.setNotes(convert(concept.getNote()));
    output.setBroader(convertToResourceList(concept.getBroader()));
    output.setBroadMatch(convertToResourceList(concept.getBroadMatch()));
    output.setCloseMatch(convertToResourceList(concept.getCloseMatch()));
    output.setExactMatch(convertToResourceList(concept.getExactMatch()));
    output.setInScheme(convertToResourceList(concept.getInScheme()));
    output.setNarrower(convertToResourceList(concept.getNarrower()));
    output.setNarrowMatch(convertToResourceList(concept.getNarrowMatch()));
    output.setRelated(convertToResourceList(concept.getRelated()));
    output.setRelatedMatch(convertToResourceList(concept.getRelatedMatch()));

    return output;
  }


  private Place convertPlace(String contextualEntity)
      throws IOException {
    PlaceImpl place = new ObjectMapper().readValue(contextualEntity,
        PlaceImpl.class);

    Place output = new Place();

    output.setAbout(place.getAbout());
    output.setPrefLabelList(convert(place.getPrefLabel()));
    output.setAltLabelList(convert(place.getAltLabel()));

    output.setHasPartsList(convertPart(place.getDcTermsHasPart()));
    output.setIsPartOfList(convertPart(place.getIsPartOf()));
    output.setNotes(convert(place.getNote()));
    output.setSameAs(convertToPartsList(place.getOwlSameAs()));

    if ((place.getLatitude() != null && place.getLatitude() != 0) &&
        (place.getLongitude() != null && place.getLongitude() != 0)) {
      output.setLat(place.getLatitude().toString());
      output.setLon(place.getLongitude().toString());
    }

    if (place.getAltitude() != null && place.getAltitude() != 0) {
      output.setAlt(place.getAltitude().toString());
    }
    return output;
  }

  private Agent convertAgent(String contextualEntity)
      throws IOException {
    AgentImpl agent = new ObjectMapper().readValue(contextualEntity,
        AgentImpl.class);

    Agent output = new Agent();

    output.setAbout(agent.getAbout());
    output.setPrefLabelList(convert(agent.getPrefLabel()));
    output.setAltLabelList(convert(agent.getAltLabel()));
    output.setHiddenLabel(convert(agent.getHiddenLabel()));
    output.setFoafName(convert(agent.getFoafName()));
    output.setNotes(convert(agent.getNote()));

    output.setBeginList(convert(agent.getBegin()));
    output.setEndList(convert(agent.getEnd()));

    output.setIdentifier(convert(agent.getDcIdentifier()));
    output.setHasMet(convert(agent.getEdmHasMet()));
    output.setBiographicaInformation(convert(agent.getRdaGr2BiographicalInformation()));
    output.setDateOfBirth(convert(agent.getRdaGr2DateOfBirth()));
    output.setDateOfDeath(convert(agent.getRdaGr2DateOfDeath()));
    output.setDateOfEstablishment(convert(agent.getRdaGr2DateOfEstablishment()));
    output.setDateOfTermination(convert(agent.getRdaGr2DateOfTermination()));
    output.setGender(convert(agent.getRdaGr2Gender()));

    output.setDate(convertResourceOrLiteral(agent.getDcDate()));
    output.setProfessionOrOccupation(
        convertResourceOrLiteral(agent.getRdaGr2ProfessionOrOccupation()));

    output.setWasPresentAt(convertToResourceList(agent.getEdmWasPresentAt()));
    output.setSameAs(convertToPartsList(agent.getOwlSameAs()));

    return output;
  }

  private List<Label> convert(Map<String, List<String>> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach((key, entry) ->
        entry.stream().map(
            value -> new Label(key, value)
        ).forEach(labels::add)
    );
    return labels;
  }

  private List<Part> convertPart(Map<String, List<String>> map) {
    List<Part> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) ->
        entry.stream().map(
            value -> new Part(key)
        ).forEach(parts::add)
    );
    return parts;
  }

  private List<LabelResource> convertResourceOrLiteral(Map<String, List<String>> map) {
    List<LabelResource> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) ->
        entry.stream().map(
            value ->
                (isUri(key) ? new LabelResource(key): new LabelResource(key, value))
        ).forEach(parts::add)
    );
    return parts;
  }

  private List<Resource> convertToResourceList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Resource::new).collect(Collectors.toList());
  }

  private List<Part> convertToPartsList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Part::new).collect(Collectors.toList());
  }

  private boolean isUri(String str) {
    return str.startsWith("http://");
  }
}
