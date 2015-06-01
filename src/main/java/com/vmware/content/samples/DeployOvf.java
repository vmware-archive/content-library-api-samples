/*******************************************************************************
 * Copyright (c) 2015 VMware, Inc.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.vmware.content.samples;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.vmware.content.samples.client.util.IOUtil;
import com.vmware.content.samples.client.util.OvfUtil;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentResult;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeploymentTarget;
import com.vmware.vcenter.ovf.LibraryItemTypes.OvfSummary;
import com.vmware.vcenter.ovf.LibraryItemTypes.ResourcePoolDeploymentSpec;
import com.vmware.vcenter.ovf.OvfParams;
import com.vmware.vim25.ManagedObjectReference;

/**
 * This API sample demonstrates deployment of an OVF item to a given resource pool.
 * <p>
 * This sample assumes that connected vCenter Server instance already has
 * at least one OVF item in the available content libraries.
 */
public class DeployOvf extends AbstractSample {

    public static void main(String[] args) {
        DeployOvf sample = new DeployOvf();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        // display existing items
        ClsApiHelper.printAllLibraryItems(client);
        String itemId = IOUtil.read("Enter item ID of an OVF to deploy: ");

        String clusterName = IOUtil.read("Enter the name of the cluster to deploy the OVF: ");
        ManagedObjectReference resourcePool =
                ClsApiHelper.getResourcePoolFromClusterName(client, clusterName);

        DeploymentTarget target = new DeploymentTarget();
        target.setResourcePoolId(resourcePool.getValue());

        // filter additional parameters for deployment against the deployment target
        Map<String, String> networkMap = displayOvfParamsAndGetNetworkSettings(itemId, target);

        // create a resource pool deployment spec
        String entityName = "Sample OVF VM" + System.currentTimeMillis() / 1000;
        ResourcePoolDeploymentSpec spec = createResourcePoolDeploymentSpec(entityName, true, networkMap);

        // deploy the OVF library item with the spec, on the target
        DeploymentResult result =
                client.ovfLibraryItem().deploy(null, itemId, target, spec);

        // display result of the operation
        String messageOnSuccess = "OVF item deployment succeeded." +
                "\nDeployment information:" + " " + result.getResourceId().getType()
                + " " + result.getResourceId().getId();
        String messageOnFailure = "OVF item deployment failed.";
        OvfUtil.displayOperationResult(result.getSucceeded(),
                result.getError(), messageOnSuccess, messageOnFailure);
    }

    private Map<String, String> displayOvfParamsAndGetNetworkSettings(String itemId, DeploymentTarget target) {
        OvfSummary summary = client.ovfLibraryItem().filter(itemId, target);
        IOUtil.print("OVF name: " + summary.getName());
        IOUtil.print("OVF description: " + summary.getAnnotation());
        // display network sections and get the network choice from user
        List<String> networks = summary.getNetworks();
        Map<String, String> networkMap = null;
        if (networks != null && !networks.isEmpty()) {
            networkMap = new HashMap<>();
            IOUtil.print("Networks (ovf:NetworkSection):");
            for (ListIterator<String> iterator = networks.listIterator(); iterator.hasNext(); ) {
                String network = iterator.next();
                String networkId = IOUtil.read("ID for " + iterator.previousIndex() + ": " +
                        network + " (press enter for default):");
                if (!networkId.isEmpty()) {
                    networkMap.put(network, networkId);
                }
            }
        }
        // display storage groups section
        List<String> storages = summary.getStorageGroups();
        if (storages != null && !storages.isEmpty()) {
            IOUtil.print("Storage groups (vmw:StorageGroupSection):");
            for (ListIterator<String> iterator = storages.listIterator(); iterator.hasNext(); ) {
                String storage = iterator.next();
                IOUtil.print(iterator.previousIndex() + ": " + storage);
            }
        }
        // display additional parameters
        List<Structure> params = summary.getAdditionalParams();
        if (params != null && !params.isEmpty()) {
            IOUtil.print("Additional parameters:");
            for (ListIterator<Structure> iterator = params.listIterator(); iterator.hasNext(); ) {
                OvfParams param =
                        iterator.next()._convertTo(OvfParams.class);
                IOUtil.print(iterator.previousIndex() + ": " + param.getType());
            }
        }
        return networkMap;
    }

    private ResourcePoolDeploymentSpec createResourcePoolDeploymentSpec(String entityName,
            boolean acceptAllEULA, Map<String, String> networkMap) {
        ResourcePoolDeploymentSpec spec = new ResourcePoolDeploymentSpec();
        spec.setAcceptAllEULA(acceptAllEULA); /* only this field is mandatory in the spec */
        spec.setName(entityName);
        spec.setAnnotation("This is a VM created using LibraryItem interface");
        // If specific network ID were specified, use it now
        if (networkMap != null) {
            spec.setNetworkMappings(networkMap);
        }
        // If specific datastore ID was specified, use it now
        String datastoreId =
                IOUtil.read("Enter datastore ID (press enter for default): ");
        if (!datastoreId.isEmpty()) {
            spec.setDefaultDatastoreId(datastoreId);
        }
        return spec;
    }
}
