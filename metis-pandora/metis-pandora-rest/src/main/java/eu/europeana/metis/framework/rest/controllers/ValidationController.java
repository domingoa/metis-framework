package eu.europeana.metis.framework.rest.controllers;

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.validation.Flag;
import eu.europeana.metis.mapping.validation.FlagType;
import eu.europeana.metis.service.ValidationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * A Controller exposing a REST API for performing flagging of field values
 * Created by ymamakis on 6/16/16.
 */
@Controller
@Api(value = "/",description = "Flagging REST API")
public class ValidationController {

    @Autowired
    private ValidationService service;


    /**
     * Validate an attribute
     * @param mappingId The mapping id
     * @param attr The attribute to validate
     * @return A List of flags
     */
    @ApiOperation(value = "Validate an attribute and retrieve all the flags", response = List.class)
    @RequestMapping(value = "/validation/{mappingId}/attribute", method = RequestMethod.POST,
            consumes = "application/json",produces = "application/json")
    @ResponseBody
    public List<Flag> validateAttribute(@ApiParam("mappingId") @PathVariable(value = "mappingId") String mappingId,
                                        @ApiParam("attribute") @RequestParam("attribute") Attribute attr){
        return service.validateField(mappingId,attr);
    }

    /**
     * Validate an element
     * @param mappingId The mapping id
     * @param elem The element to validate
     * @return A List of Flags
     */
    @RequestMapping(value = "/validation/{mappingId}/element", method = RequestMethod.POST,
            consumes = "application/json",produces = "application/json")
    @ApiOperation(value = "Validate an element and retrieve all its flags", response = List.class)
    @ResponseBody
    public List<Flag> validateElement(@ApiParam("mappingId") @PathVariable("mappingId") String mappingId,
                                      @ApiParam("element") @RequestParam("element") Element elem){
        return service.validateField(mappingId,elem);
    }

    /**
     * Manually flag an attribute
     * @param mappingId The mapping id
     * @param flagType The type of flag the user wants to apply
     * @param value The value on which to apply the flag
     * @param message The message that accompanies the flag
     * @param attr The attribute to flag
     * @return The generated flag
     */
    @RequestMapping(value = "/validation/{mappingId}/attribute/create/{value}/{flagType}", method = RequestMethod.POST,
            consumes = "application/json",produces = "application/json")
    @ApiOperation(value = "Manually flag an attribute", response = Flag.class)
    @ResponseBody
    public Flag createAttributeFlag(@ApiParam("mappingId") @PathVariable("mappingId") String mappingId,
                                    @ApiParam("flagType") @PathVariable("flagType") String flagType,
                                    @ApiParam("value") @PathVariable("value") String value,
                                    @ApiParam("message") @RequestParam("message") String message,
                                    @ApiParam("attribute") @RequestParam("attribute") Attribute attr){
        return service.createFlagForField(attr, FlagType.valueOf(flagType),value,message,mappingId);
    }

    /**
     * Manually flag an element
     * @param mappingId The mapping id
     * @param flagType The type of flag the user wants to apply
     * @param value The value on which to apply the flag
     * @param message The message that accompanies the flag
     * @param elem The element to flag
     * @return The generated flag
     */
    @RequestMapping(value = "/validation/{mappingId}/element/create/{value}/{flagType}", method = RequestMethod.POST,
            consumes = "application/json",produces = "application/json")
    @ApiOperation(value = "Create a flag for an element", response = Flag.class)
    public Flag createElementFlag(@ApiParam("mappingId") @PathVariable("mappingId") String mappingId,
                                  @ApiParam("flagType") @PathVariable("flagType") String flagType,
                                  @ApiParam("value") @PathVariable("value") String value,
                                  @ApiParam("message") @RequestParam("message") String message,
                                  @ApiParam("attribute") @RequestParam("element") Element elem){
        return service.createFlagForField(elem, FlagType.valueOf(flagType),value,message,mappingId);
    }
    /**
     * Delete a flag from an attribute
     * @param mappingId The mapping id
     * @param value The value from which to delete the flag
     * @param attr The attribute from which to delete the flag
     */
    @RequestMapping(value = "/validation/{mappingId}/attribute/{value}", method = RequestMethod.DELETE, consumes = "application/json")
    @ApiOperation(value = "Delete a flag for an attribute")
    public void deleteAttributeFlag(@ApiParam("attribute") @RequestParam("attribute") Attribute attr,
                                    @ApiParam("mappingId") @PathVariable("mappingId") String mappingId,
                                    @ApiParam("value") @PathVariable("value") String value){
        service.deleteFlagForField(attr, mappingId, value);
    }
    /**
     * Delete a flag from an element
     * @param mappingId The mapping id
     * @param value The value from which to delete the flag
     * @param elem The element from which to delete the flag
     */
    @RequestMapping(value = "/validation/{mappingId}/element/{value}", method = RequestMethod.DELETE, consumes = "application/json")
    @ApiOperation(value = "Delete a flag for an element")
    public void deleteElementFlag(@ApiParam("element") @RequestParam("element") Element elem,
                                  @ApiParam("mappingId") @PathVariable("mappingId") String mappingId,
                                  @ApiParam("value") @PathVariable("value") String value){
        service.deleteFlagForField(elem, mappingId, value);
    }

    /**
     * Validate a mapping
     * @param mapping The mapping to validate
     * @return The mapping with all the flags populated
     */
    @RequestMapping(value = "/validation/validate", method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Validate a mapping against its flags")
    @ResponseBody
    public Mapping validateMapping(@ApiParam @RequestBody Mapping mapping){
        return service.validateMapping(mapping);
    }
}