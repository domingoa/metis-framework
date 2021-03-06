package eu.europeana.normalization.service.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.normalization.common.model.NormalizedBatchResult;
import eu.europeana.normalization.common.model.NormalizedRecordResult;
import eu.europeana.normalization.service.NormalizationService;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Produces({MediaType.APPLICATION_JSON + "; charset=UTF-8"})
@RestController
public class NormalizationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationController.class);
  private final NormalizationService normalizationService;

  @Autowired
  public NormalizationController(NormalizationService normalizationService) {
    this.normalizationService = normalizationService;
  }

  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = RestEndpoints.NORMALIZATION, method = RequestMethod.POST)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
  public NormalizedBatchResult normalizeEdmInternal(@RequestBody List<String> records)
      throws Exception {
    try {
      List<NormalizedRecordResult> result = new ArrayList<>();
      for (String edmRec : records) {
        result.add(normalize(edmRec));
      }
      return new NormalizedBatchResult(result);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new InternalServerErrorException(e);
    }
  }

  private NormalizedRecordResult normalize(String edmRec) {
    try {
      return normalizationService.processNormalize(edmRec);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return new NormalizedRecordResult(e.getMessage(), edmRec);
    }
  }

}

