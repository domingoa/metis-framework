package eu.europeana.normalization.util.nlp;

import com.ibm.icu.text.Transliterator;
import java.util.Enumeration;

/**
 * The choice for Greek alphabet transliteration to Latin. Follows the standard UNGEGN, which is
 * based in the older standard ELOT 743, in use in Greek passports, for example. This transliterator
 * also removes accents and transforms to lowercase (after the transliteration)
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 21 de Jun de 2012
 */
public class CyrillicTransliterator {

  private Transliterator translit;

  /**
   * Creates a new instance of this class.
   */
  public CyrillicTransliterator() {
    translit = Transliterator
        .getInstance("Cyrillic-Latin; nfd; [:nonspacing mark:] remove; nfc; Lower");
  }

  /**
   * @param args
   * @throws Exception
   */
  @SuppressWarnings("rawtypes")
  public static void main(String[] args) {
    String source = "Топ-10 туристических объектов Латвии.";
    System.out.println(source);

    Transliterator translit = Transliterator.getInstance("Cyrillic-Latin");
    String res = translit.transliterate(source);
    System.out.println(res);

    translit = Transliterator
        .getInstance("Cyrillic-Latin; nfd; [:nonspacing mark:] remove; nfc; Lower");
    res = translit.transliterate(source);
    System.out.println(res);

    Enumeration availableIDs = Transliterator.getAvailableIDs();
    while (availableIDs.hasMoreElements()) {
      System.out.println(availableIDs.nextElement());
    }

// translit = Transliterator.getInstance("Greek-Latin");
// res = translit.transliterate(source);
//
// System.out.println(res);
// translit = Transliterator.getInstance("Greek-Latin/BGN");
// res = translit.transliterate(source);
//
// System.out.println(res);
// translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// System.out.println(res);
//
// translit =
// Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// System.out.println(res);
//
// // translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");
//
// // Choice. UNGEGN is based in ELOT 743
// translit =
// Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
//
// System.out.println("Greek-Latin/UNGEGN; nfc");
// translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd");
// res = translit.transliterate(source);
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
//
// res = translit.transliterate("Is this removin the latin 1 accents? á é ã ê");
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
  }

  /**
   * @return transliterated text
   */
  public String transliterate(String sourceText) {
    return translit.transliterate(sourceText);
  }
}
