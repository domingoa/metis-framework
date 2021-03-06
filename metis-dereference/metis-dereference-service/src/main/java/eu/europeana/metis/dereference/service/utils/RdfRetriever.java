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
package eu.europeana.metis.dereference.service.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Helper class to retrieve a remote unmapped entity
 * Created by ymamakis on 2/11/16.
 */

@Service
public class RdfRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdfRetriever.class);
     /**
     * Retrieve a remote entity from a resource as a String
     * @param resource The remote entity to retrieve
     * @return The string representation of the remote entity
     */
    public String retrieve(String resource){
        URLConnection urlConnection;
        if (resource != null) {
            try {

                urlConnection = new URL(resource).openConnection();
                urlConnection
                        .setRequestProperty("accept",
                                "application/rdf+xml");
                InputStream inputStream = urlConnection.getInputStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                return writer.toString();

            } catch (IOException e) {
                LOGGER.error("Failed to retrieve: " + resource + " with message: " +e.getMessage());
            }
        }
        return "";
    }
}
