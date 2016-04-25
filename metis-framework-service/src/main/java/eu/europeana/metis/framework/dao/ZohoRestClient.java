package eu.europeana.metis.framework.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.framework.common.*;
import eu.europeana.metis.framework.crm.*;
import eu.europeana.metis.framework.organization.Organization;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rest Client for Zoho CRM
 * Created by ymamakis on 2/23/16.
 */
@Component
public class ZohoRestClient implements ZohoClient{

    private RestTemplate template = new RestTemplate();
    private String baseUrl;
    private String authorizationToken;
    private String scope;
    private final static String GETALLPROVIDERS = "getRecords";
    private final static String GETPROVIDERBYID = "getRecordById";
    private final static String GETBYEMAIL = "getSearchRecordsByPDC";
    private final static String ORGANIZATIONMODULE ="CustomModule1";
    private final static String USERSMODULE = "Contacts";

    /**
     * Constructor for Zoho Rest Client
     * @param baseUrl The URL of Zoho
     * @param authorizationToken The authorization token
     * @param scope The scope
     */

    public ZohoRestClient(String baseUrl, String authorizationToken, String scope) {
        this.authorizationToken = authorizationToken;

        this.baseUrl = baseUrl;
        this.scope = scope;
        List<HttpMessageConverter<?>> converters = template.getMessageConverters();
        converters.add(new MappingJackson2HttpMessageConverter());
        template.setMessageConverters(converters);
    }

    /**
     * Retrieve all the organizations from Zoho
     * @return A list of all the registered organizations in Zoho
     * @throws ParseException
     * @throws IOException
     */
    @Override
    public List<Organization> getAllOrganizations() throws ParseException, IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ZohoOrganizationResponse resp = new ZohoOrganizationResponse();
        HttpEntity<ZohoOrganizationResponse> entity = new HttpEntity<>(resp, httpHeaders);
        ResponseEntity<String> ts = template.exchange(baseUrl+"/"+ORGANIZATIONMODULE +"/"+ GETALLPROVIDERS + "?authtoken=" + authorizationToken + "&scope=" + scope, HttpMethod.GET, entity, String.class);
        ObjectMapper om = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ZohoOrganizationResponse ret = om.readValue(ts.getBody(), ZohoOrganizationResponse.class);
        return fromListResponse(ret);
    }

    /**
     * Get an organization by a specific id from the CRM
     * @param id The id of the organization to search for
     * @return The Organization representation for that Id
     * @throws ParseException
     * @throws IOException
     */
    @Override
    public Organization getOrganizationById(String id) throws ParseException, IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ZohoOrganizationResponse resp = new ZohoOrganizationResponse();
        HttpEntity<ZohoOrganizationResponse> entity = new HttpEntity<>(resp, httpHeaders);
        ResponseEntity<String>  ts = template.exchange(baseUrl + "/"+ORGANIZATIONMODULE+"/"+GETPROVIDERBYID + "?authtoken=" + authorizationToken + "&scope=" + scope + "&id=" + id, HttpMethod.GET, entity, String.class);
        ObjectMapper om = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ZohoOrganizationResponse ret = om.readValue(ts.getBody(), ZohoOrganizationResponse.class);
        return fromOneResponse(ret);
    }

    @Override
    public Contact getContactByEmail(String email) throws ParseException,IOException{
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ZohoContactResponse resp = new ZohoContactResponse();
        HttpEntity<ZohoContactResponse> entity = new HttpEntity<>(resp, httpHeaders);
        ResponseEntity<String>  ts = template.exchange(baseUrl + "/"+USERSMODULE +"/" + GETBYEMAIL+ "?authtoken=" + authorizationToken + "&scope=" + scope + "&searchColumn=email&searchValue=" + email, HttpMethod.GET, entity, String.class);
        ObjectMapper om = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ZohoContactResponse ret = om.readValue(ts.getBody(), ZohoContactResponse.class);
        return fromOneResponse(ret);
    }

    private Contact fromOneResponse(ZohoContactResponse ret) {
        if(ret.getContactResponse().getContactResult().getModule().getRows().size()>0) {
            Row row = ret.getContactResponse().getContactResult().getModule().getRows().get(0);
            return readResponseToContact(row);
        }
        return null;
    }

    private Contact readResponseToContact(Row row) {
        Contact contact = new Contact();
        for (Field field : row.getFields()) {
            switch (field.getVal()) {
                case ZohoFields.EMAIL:
                    contact.setEmail(field.getContent());
                    break;
                case ZohoFields.FIRSTNAME:
                    contact.setFirstName(field.getContent());
                    break;
                case ZohoFields.LASTNAME:
                    contact.setLastName(field.getContent());
                    break;
            }
        }
        return contact;
    }

    private List<Organization> fromListResponse(ZohoOrganizationResponse resp) throws ParseException, MalformedURLException {
        List<Row> rows = resp.getOrganizationResponse().getOrganizationResult().getModule().getRows();
        List<Organization> orgs = new ArrayList<>();
        for (Row row : rows) {
            orgs.add(readResponsetoOrganization(row));
        }
        return orgs;
    }

    private Organization fromOneResponse(ZohoOrganizationResponse resp) throws ParseException, MalformedURLException {
        Row row = resp.getOrganizationResponse().getOrganizationResult().getModule().getRows().get(0);

        return readResponsetoOrganization(row);
    }

    private Organization readResponsetoOrganization(Row row) throws ParseException, MalformedURLException {
        Organization org = new Organization();
        DateFormat fd = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        for (Field field : row.getFields()) {

            switch (field.getVal()){
                case ZohoFields.ID:
                    org.setOrganizationId(field.getContent());
                    break;
                case ZohoFields.ACRONYM:
                    org.setAcronym(field.getContent());
                    break;
                case  ZohoFields.NAME:
                    org.setName(field.getContent());
                    break;
                case ZohoFields.CREATEDTIME:
                    org.setCreated(fd.parse(field.getContent()));
                    break;
                case ZohoFields.MODIFIEDTIME:
                    org.setModified(fd.parse(field.getContent()));
                    break;
                case ZohoFields.ROLE:
                    List<String> roles = Arrays.asList(field.getContent().split(";"));
                    List<Role> metisRoles = new ArrayList<>();
                    for (String role:roles){
                        metisRoles.add(Role.valueOf(role));
                    }
                    org.setRoles(metisRoles);
                    break;
                case ZohoFields.COUNTRY:
                    org.setCountry(Country.valueOf(field.getContent()));
                    break;
                case ZohoFields.DOMAIN:
                    org.setDomain(Domain.valueOf(field.getContent()));
                    break;
                case ZohoFields.GEOGRAPHICLEVEL:
                    org.setGeographicLevel(GeographicLevel.valueOf(field.getContent()));
                    break;
                case ZohoFields.WEBSITE:
                    org.setWebsite(new URL(field.getContent()));
                    break;
                case ZohoFields.SECTOR:
                    org.setSector(Sector.valueOf(field.getContent()));
            }

        }


        return org;
    }
}
