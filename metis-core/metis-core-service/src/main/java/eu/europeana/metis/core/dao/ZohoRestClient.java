/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.core.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.core.crm.Row;
import eu.europeana.metis.core.crm.ZohoContactResponse;
import eu.europeana.metis.core.crm.ZohoOrganizationResponse;
import eu.europeana.metis.core.organization.Organization;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Rest Client for Zoho CRM
 * Created by ymamakis on 2/23/16.
 */
@Component
public class ZohoRestClient extends ZohoClient{

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
    public Contact getContactByEmail(String email) throws IOException {
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


}