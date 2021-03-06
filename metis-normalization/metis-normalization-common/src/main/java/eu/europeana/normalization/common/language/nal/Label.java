/* LanguageName.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common.language.nal;

/**
 * A label assigned to a language represented in NAL
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class Label {

  /**
   * String label
   */
  private String label;
  /**
   * String language
   */
  private String language;
  /**
   * String script
   */
  private String script;

  /**
   * Creates a new instance of this class.
   */
  public Label(String label) {
    super();
    this.label = label;
  }

  /**
   * Creates a new instance of this class.
   */
  public Label(String label, String language) {
    super();
    this.label = label;
    this.language = language;
  }

  /**
   * Creates a new instance of this class.
   */
  public Label(String label, String language, String script) {
    super();
    this.label = label;
    this.language = language;
    this.script = script;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  @Override
  public String toString() {
    return "Label [label=" + label + ", language=" + language + ", script=" + script + "]";
  }

}
