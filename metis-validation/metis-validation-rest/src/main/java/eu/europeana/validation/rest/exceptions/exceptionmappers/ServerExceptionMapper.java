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
package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.rest.exceptions.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/24/16.
 */
@ControllerAdvice
public class ServerExceptionMapper {

    /**
     * Handles specified exception
     *
     * @param e exception to be handled
     * @return
     */
    @ExceptionHandler(ServerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(ServerException e) {
        return "Server exception: [" + e.getMessage() + "]";
    }
}
