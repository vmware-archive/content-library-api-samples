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

import com.vmware.content.samples.client.util.IOUtil;
import com.vmware.content.samples.client.util.OvfUtil;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateResult;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateSpec;
import com.vmware.vcenter.ovf.LibraryItemTypes.CreateTarget;
import com.vmware.vcenter.ovf.LibraryItemTypes.DeployableIdentity;

/**
 * This API sample demonstrates capture of a VM/VApp to a content library.
 */
public class CreateOvfFromVm extends AbstractSample {

    public static void main(String[] args) {
        CreateOvfFromVm sample = new CreateOvfFromVm();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        String entityType = IOUtil.read("Enter entity type (VirtualMachine or VirtualApp) " +
                "to capture in a library: ");
        String entityId = IOUtil.read("Enter the managed object ID for the entity (eg. vm-13): ");

        DeployableIdentity deployable = new DeployableIdentity();
        deployable.setType(entityType);
        deployable.setId(entityId);

        // display available local libraries where the VM/ VApp can be captured to
        ClsApiHelper.printAllLocalLibraries(client);
        String libraryId = IOUtil.read("Enter target library ID: ");
        CreateTarget target = new CreateTarget();
        target.setLibraryId(libraryId);

        // build a spec to create item in the library
        String name = IOUtil.read("Enter library item name to set: (press enter for none): ");
        String desc = IOUtil.read("Enter library item description to set: (press enter for none): ");
        CreateSpec spec = new CreateSpec();
        spec.setName(name);
        spec.setDescription(desc);

        // create OVF library item
        CreateResult result =
                client.ovfLibraryItem().create(null, deployable, target, spec);

        // display operation result
        String messageOnSuccess = "OVF item created successfully." +
                "\nLibrary item ID: " + result.getOvfLibraryItemId();
        String messageOnFailure = "OVF item creation failed.";
        OvfUtil.displayOperationResult(result.getSucceeded(),
                result.getError(), messageOnSuccess, messageOnFailure);

        // list the items from the library, to see the created OVF item
        ClsApiHelper.printItemsFromLibrary(client, libraryId);
    }
}
