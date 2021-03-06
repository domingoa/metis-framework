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
package eu.europeana.metis.mapping.statistics;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Statistics wrapper class containing the values of all the fields encountered in a dataset
 * Created by ymamakis on 6/15/16.
 */
@Entity
@XmlRootElement
public class DatasetStatistics {

    @Id
    private ObjectId id;

    @Indexed
    private String datasetId;

    @Reference
    private Map<String,Statistics> statistics;

    /**
     * The id of the wrapper class
     * @return the id of the wrapper class
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the wrapper class
     * @param id
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * The id of the dataset
     * @return The id of the datasaet
     */
    @XmlElement
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * The id of the dataset
     * @param datasetId The id of the dataset
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * A map of statistics per encountered field in the dataset.
     * The field is expressed using its XPath
     * @return A map with all the statistics of field values for a dataset
     */
    @XmlElement
    public Map<String,Statistics> getStatistics() {
        return statistics;
    }

    /**
     * Set the statistics for a dataset
     * @see Statistics
     * @param statistics The statistics for a dataset
     */
    public void setStatistics(Map<String,Statistics> statistics) {
        this.statistics = statistics;
    }
}
