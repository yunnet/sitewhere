/*
 * NetworksController.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.web.rest.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sitewhere.device.marshaling.DeviceGroupElementMarshalHelper;
import com.sitewhere.rest.model.device.group.DeviceGroup;
import com.sitewhere.rest.model.device.request.DeviceGroupCreateRequest;
import com.sitewhere.rest.model.search.SearchCriteria;
import com.sitewhere.rest.model.search.SearchResults;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.device.group.IDeviceGroup;
import com.sitewhere.spi.device.group.IDeviceGroupElement;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.ISearchResults;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Controller for device group operations.
 * 
 * @author Derek Adams
 */
@Controller
@RequestMapping(value = "/devicegroups")
@Api(value = "", description = "Operations related to SiteWhere device groups.")
public class DeviceGroupsController extends SiteWhereController {

	/**
	 * Create a device group.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "Create a new device group")
	public IDeviceGroup createDeviceGroup(@RequestBody DeviceGroupCreateRequest request)
			throws SiteWhereException {
		IDeviceGroup result = SiteWhereServer.getInstance().getDeviceManagement().createDeviceGroup(request);
		return DeviceGroup.copy(result);
	}

	/**
	 * Get a device group by unique token.
	 * 
	 * @param groupToken
	 * @return
	 * @throws SiteWhereException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "Get a device group by unique token")
	public IDeviceGroup getDeviceGroupByToken(
			@ApiParam(value = "Unique token that identifies group", required = true) @PathVariable String groupToken)
			throws SiteWhereException {
		IDeviceGroup group = SiteWhereServer.getInstance().getDeviceManagement().getDeviceGroup(groupToken);
		if (group == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidDeviceGroupToken, ErrorLevel.ERROR);
		}
		return DeviceGroup.copy(group);
	}

	/**
	 * Update an existing device group.
	 * 
	 * @param groupToken
	 * @param request
	 * @return
	 * @throws SiteWhereException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.PUT)
	@ResponseBody
	@ApiOperation(value = "Update an existing device group")
	public IDeviceGroup updateDeviceGroup(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@RequestBody DeviceGroupCreateRequest request) throws SiteWhereException {
		IDeviceGroup group =
				SiteWhereServer.getInstance().getDeviceManagement().updateDeviceGroup(groupToken, request);
		return DeviceGroup.copy(group);
	}

	/**
	 * Delete an existing device group.
	 * 
	 * @param groupToken
	 * @param force
	 * @return
	 * @throws SiteWhereException
	 */
	@RequestMapping(value = "/{groupToken}", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation(value = "Delete a device group by unique token")
	public IDeviceGroup deleteDeviceGroup(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@ApiParam(value = "Delete permanently", required = false) @RequestParam(defaultValue = "false") boolean force)
			throws SiteWhereException {
		IDeviceGroup group =
				SiteWhereServer.getInstance().getDeviceManagement().deleteDeviceGroup(groupToken, force);
		return DeviceGroup.copy(group);
	}

	/**
	 * List all device groups.
	 * 
	 * @param includeDeleted
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws SiteWhereException
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List all device groups")
	public ISearchResults<IDeviceGroup> listDeviceGroups(
			@ApiParam(value = "Include deleted", required = false) @RequestParam(defaultValue = "false") boolean includeDeleted,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws SiteWhereException {
		SearchCriteria criteria = new SearchCriteria(page, pageSize);
		ISearchResults<IDeviceGroup> results =
				SiteWhereServer.getInstance().getDeviceManagement().listDeviceGroups(includeDeleted, criteria);
		List<IDeviceGroup> groupsConv = new ArrayList<IDeviceGroup>();
		for (IDeviceGroup group : results.getResults()) {
			groupsConv.add(DeviceGroup.copy(group));
		}
		return new SearchResults<IDeviceGroup>(groupsConv, results.getNumResults());
	}

	/**
	 * List elements from a device group that meet the given criteria.
	 * 
	 * @param groupToken
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws SiteWhereException
	 */
	@RequestMapping(value = "/{groupToken}/elements", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value = "List elements from a device group")
	public ISearchResults<IDeviceGroupElement> listDeviceGroupElements(
			@ApiParam(value = "Unique token that identifies device group", required = true) @PathVariable String groupToken,
			@ApiParam(value = "Include detailed element information", required = false) @RequestParam(defaultValue = "false") boolean includeDetails,
			@ApiParam(value = "Page Number (First page is 1)", required = false) @RequestParam(defaultValue = "1") int page,
			@ApiParam(value = "Page size", required = false) @RequestParam(defaultValue = "100") int pageSize)
			throws SiteWhereException {
		DeviceGroupElementMarshalHelper helper =
				new DeviceGroupElementMarshalHelper().setIncludeDetails(includeDetails);
		SearchCriteria criteria = new SearchCriteria(page, pageSize);
		ISearchResults<IDeviceGroupElement> results =
				SiteWhereServer.getInstance().getDeviceManagement().listDeviceGroupElements(groupToken,
						criteria);
		List<IDeviceGroupElement> elmConv = new ArrayList<IDeviceGroupElement>();
		for (IDeviceGroupElement elm : results.getResults()) {
			elmConv.add(helper.convert(elm, SiteWhereServer.getInstance().getAssetModuleManager()));
		}
		return new SearchResults<IDeviceGroupElement>(elmConv, results.getNumResults());
	}
}