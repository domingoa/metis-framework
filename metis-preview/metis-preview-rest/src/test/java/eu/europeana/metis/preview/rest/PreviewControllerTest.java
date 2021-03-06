package eu.europeana.metis.preview.rest;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.preview.common.exception.PreviewServiceException;
import eu.europeana.metis.preview.common.exception.ZipFileException;
import eu.europeana.metis.preview.exceptions.handler.ValidationExceptionHandler;
import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.metis.preview.service.ZipService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class PreviewControllerTest {
  public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
      MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));


  private PreviewService previewService;
  private MockMvc datasetControllerMock;
  private ZipService zipService;


  @Before
  public void setUp() throws Exception {
    previewService = mock(PreviewService.class);
    zipService = mock(ZipService.class);

    PreviewController previewController = new PreviewController(previewService, zipService);
    datasetControllerMock = MockMvcBuilders
        .standaloneSetup(previewController)
        .setControllerAdvice(new ValidationExceptionHandler())
        .build();
  }

  @Test
  public void previewUpload_withOkzipfile_returnsResults() throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();
    List<String> list = new ArrayList<>();

    ExtendedValidationResult result = getExtendedValidationResult();

    when(zipService.readFileToStringList(any(InputStream.class))).thenReturn(list);
    when(previewService.createRecords(eq(list), eq("myNewId"), any(boolean.class), any(String.class), anyBoolean())).thenReturn(result);

    datasetControllerMock.perform(fileUpload("/upload")
        .file(fileMock)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(APPLICATION_JSON_UTF8)
        .param("collectionId", "myNewId")
        .param("organizationId", "myOrg"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.resultList", is((String)null)))
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.portalUrl", is("myUri")))
        .andExpect(jsonPath("$.records[0]", is("myRecord")));

    verify(zipService, times(1)).readFileToStringList(any(InputStream.class));
    verify(previewService, times(1)).createRecords(anyList(), eq("myNewId"), eq(true),
        eq("EDM_external2internal_v2.xsl"), eq(true)  );
    verifyNoMoreInteractions(previewService, zipService);
  }

  @Test
  public void previewUpload_zipServiceFails_throwsZipException()
      throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();
    List<String> list = new ArrayList<>();

    when(zipService.readFileToStringList(any(InputStream.class))).thenThrow(new ZipFileException("myZipException"));

    datasetControllerMock.perform(fileUpload("/upload")
        .file(fileMock)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(APPLICATION_JSON_UTF8)
        .param("collectionId", "myNewId")
        .param("organizationId", "myOrg"))
        .andExpect(status().is(400))
        .andExpect(jsonPath("$.errorMessage", is("myZipException")));
  }

  @Test
  public void previewUpload_previewServiceFails_throwsValidationService()
      throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();
    List<String> list = new ArrayList<>();

    when(zipService.readFileToStringList(any(InputStream.class))).thenReturn(list);
    when(previewService.createRecords(eq(list), eq("myNewId"), any(boolean.class), any(String.class), anyBoolean()))
        .thenThrow(new PreviewServiceException("myException"));

    datasetControllerMock.perform(fileUpload("/upload")
        .file(fileMock)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(APPLICATION_JSON_UTF8)
        .param("collectionId", "myNewId")
        .param("organizationId", "myOrg"))
        .andExpect(status().is(500))
        .andExpect(jsonPath("$.errorMessage", is("myException")));
  }

  @NotNull
  private MockMultipartFile createMockMultipartFile() throws IOException {
    Resource resource = new ClassPathResource("ValidExternalOk.zip");
    return new MockMultipartFile("file", resource.getInputStream());
  }

  @NotNull
  private ExtendedValidationResult getExtendedValidationResult() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(0);
    cal.set(2017, 7, 12);
    Date date = cal.getTime();

    ExtendedValidationResult result = new ExtendedValidationResult();
    result.setSuccess(true);
    result.setPortalUrl("myUri");
    result.setDate(date);
    List<String> records = new ArrayList<>();
    result.setRecords(records);
    records.add("myRecord");
    return result;
  }
}