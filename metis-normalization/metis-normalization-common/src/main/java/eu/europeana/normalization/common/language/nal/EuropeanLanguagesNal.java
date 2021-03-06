/* EuropaEuLanguagesNal.java - created on 15/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common.language.nal;

import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.common.language.LanguagesVocabulary;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Holds data from the European Languages NAL dump, which is used to support the normalization of
 * language values.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 15/03/2016
 */
public class EuropeanLanguagesNal {

  private static final Logger LOGGER = LoggerFactory.getLogger(EuropeanLanguagesNal.class);

  private final List<NalLanguage> languages = new ArrayList<>();
  private final List<NalLanguage> deprecatedLanguages = new ArrayList<>();

  private LanguagesVocabulary targetVocabulary;

  private Map<String, NalLanguage> normalizedIndex;
  private Map<String, NalLanguage> isoCodeIndex;

  /**
   * Creates a new instance of this class.
   */
  public EuropeanLanguagesNal(File xmlSourceFile) {
    Document langNalDom = XmlUtil.parseDomFromFile(xmlSourceFile);
    processDom(langNalDom);
  }

  /**
   * Creates a new instance of this class.
   */
  public EuropeanLanguagesNal() {
    InputStream nalFileIn = getClass().getClassLoader().getResourceAsStream("languages.xml");
    Document langNalDom;
    try {
      langNalDom = XmlUtil.parseDom(new InputStreamReader(nalFileIn, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Unsupported encoding", e);
      throw new RuntimeException(e.getMessage(), e);
    }
    processDom(langNalDom);
    initIsoCodeIndex();
  }

  /**
   * @param langNalDom
   */
  private void processDom(Document langNalDom) {
    Iterable<Element> records = XmlUtil.elements(langNalDom.getDocumentElement(), "record");
    for (Element recordEl : records) {
      NalLanguage l = new NalLanguage(recordEl.getAttribute("id"));
      l.setIso6391(XmlUtil.getElementTextByTagName(recordEl, "iso-639-1"));
      l.setIso6392b(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2b"));
      l.setIso6392t(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2t"));
      l.setIso6393(XmlUtil.getElementTextByTagName(recordEl, "iso-639-3"));
      Element nameEl = XmlUtil.getElementByTagName(recordEl, "name");

      for (Element nameSubEl : XmlUtil.elements(nameEl)) {
        if (nameSubEl.getNodeName().equals("original.name")) {
          l.getOriginalNames().addAll(createLabels(nameSubEl));
        } else if (nameSubEl.getNodeName().equals("alternative.name")) {
          l.getAlternativeNames().addAll(createLabels(nameSubEl));
        }
      }
      Element labelEl = XmlUtil.getElementByTagName(recordEl, "label");
      l.getLabels().addAll(createLabels(labelEl));

      if (!recordEl.getAttribute("deprecated").equals("true")) {
        languages.add(l);
      } else {
        deprecatedLanguages.add(l);
      }
// log.info("Missing language code for: "+recordEl.getAttribute("id"));
    }
  }

  private List<Label> createLabels(Element labelEl) {
    List<Label> list = new ArrayList<>();
    for (Element nameVersionEl : XmlUtil.elements(labelEl, "lg.version")) {
      list.add(createLabel(nameVersionEl));
    }
    return list;
  }

  private Label createLabel(Element nameVersionEl) {
    return new Label(nameVersionEl.getTextContent(),
        nameVersionEl.getAttribute("lg"),
        nameVersionEl.getAttribute("script"));
  }

  public List<NalLanguage> getLanguages() {
    return languages;
  }

  public List<NalLanguage> getDeprecatedLanguages() {
    return deprecatedLanguages;
  }

  public synchronized void initNormalizedIndex() {
    if (normalizedIndex == null) {
      normalizedIndex = new Hashtable<>();
      for (NalLanguage l : getLanguages()) {
        String normalizedLanguageId = l.getNormalizedLanguageId(targetVocabulary);
        if (normalizedLanguageId != null) {
          normalizedIndex.put(normalizedLanguageId, l);
        }
      }
    }
  }

  private void initIsoCodeIndex() {
    isoCodeIndex = new Hashtable<>();
    for (NalLanguage l : getLanguages()) {
      if (l.getIso6391() != null) {
        isoCodeIndex.put(l.getIso6391(), l);
      }
      if (l.getIso6392b() != null) {
        isoCodeIndex.put(l.getIso6392b(), l);
      }
      if (l.getIso6392t() != null) {
        isoCodeIndex.put(l.getIso6392t(), l);
      }
      if (l.getIso6393() != null) {
        isoCodeIndex.put(l.getIso6393(), l);
      }
    }
  }

  public NalLanguage lookupNormalizedLanguageId(String normalizedLanguageId) {
    return normalizedIndex.get(normalizedLanguageId);
  }

  public LanguagesVocabulary getTargetVocabulary() {
    return targetVocabulary;
  }

  public synchronized void setTargetVocabulary(LanguagesVocabulary target) {
    targetVocabulary = target;
  }

  public NalLanguage lookupIsoCode(String code) {
    return isoCodeIndex.get(code);
  }

}
