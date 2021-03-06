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
package eu.europeana.enrichment.rest.client.main;

import eu.europeana.enrichment.api.external.UriList;
import org.apache.commons.io.FileUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymamakis
 */
public class EntityRemover {


    public static void main(String... args) {
        if (args == null || args.length < 2) {
            System.out.println("No targetUrl/file provided");
            System.out.println("Syntax is remove.sh <enrichment_endpoint> <csv_location>");
            return;
        }

        if (!new File(args[1]).exists() || !new File(args[1]).isFile()) {
            System.out.println("File does not exist or is not a file");
            System.out.println("Syntax is remove.sh <enrichment_endpoint> <csv_location>");
            return;
        }
        EntityRemover remover = new EntityRemover();
        remover.delete(args[1], args[0]);
    }

    private void delete(String filePath, String urlPath) {
        try {
            List<String> urls = FileUtils.readLines(new File(filePath));
            UriList lst = new UriList();
            lst.setUris(urls);

            RestTemplate template = new RestTemplate();
            template.delete(urlPath,lst);


        } catch (IOException ex) {
            Logger.getLogger(EntityRemover.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
