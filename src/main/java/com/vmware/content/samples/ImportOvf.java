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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import com.vmware.content.TypeTypes.Info;
import com.vmware.content.library.item.UpdateSessionModel;
import com.vmware.content.library.item.updatesession.FileTypes;
import com.vmware.content.library.item.updatesession.FileTypes.AddSpec;
import com.vmware.content.library.item.updatesession.FileTypes.SourceType;
import com.vmware.content.library.item.updatesession.FileTypes.ValidationResult;
import com.vmware.content.samples.client.util.ClsApiUtil;
import com.vmware.content.samples.client.util.HttpUtil;
import com.vmware.content.samples.client.util.IOUtil;

/**
 * This API sample demonstrates import/upload of
 * an OVF package to an item in a content library.
 * <p>
 * Workflows demonstrated:
 * <ul>
 *     <li> Importing an OVF from a given HTTP URL </li>
 *     <li> Uploading an OVF from local storage </li>
 * </ul>
 */
public class ImportOvf extends AbstractSample {

    public static void main(String[] args) {
        ImportOvf sample = new ImportOvf();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        // list available types
        for (Info type : client.type().list()) {
            IOUtil.print(type.getName());
        }

        // create a local library
        IOUtil.print("\nCreating a library");
        String fileUri = IOUtil.read("Enter the library storage backing URI; for example, file:///tmp: ");
        String libraryId = ClsApiUtil.createLocalLibraryOnFileBacking(client, "local library", URI.create(fileUri));

        String itemId = ClsApiUtil.createItem(client, libraryId, "ttylinux-from-url", "ovf");
        IOUtil.print("Created OVF item in the library. ItemId: " + itemId);

        String ovfUrl = IOUtil.read("Enter an OVF URL: ");
        ClsApiUtil.importFileFromHttpUriToItem(client, itemId, ovfUrl, "ttylinux.ovf");
        IOUtil.print("Importing OVF from given URL");

        // upload an OVF from the local filesystem
        String secondItemId = ClsApiUtil.createItem(client, libraryId, "ttylinux-from-local", "ovf");
        IOUtil.print("Created another OVF item in the library. ItemId: "+ secondItemId);
        IOUtil.print("Let's upload OVF file from local storage to it.");
        uploadOvfFromLocalStorageToItem(secondItemId);
        IOUtil.print("Uploaded files from local storage");

        // list all the items from the library
        IOUtil.print("\nList of items from the library:");
        ClsApiUtil.printItemsFromLibrary(client, libraryId);
    }

    private void uploadOvfFromLocalStorageToItem(String itemId)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        UpdateSessionModel updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(itemId);
        String sessionId =
                client.updateSession().create(null, updateSessionModel);

        // add the OVF file to the session
        String ovfFilePath = IOUtil.read("Enter path to the OVF file on the local storage: ");
        File ovfFile = new File(ovfFilePath);
        AddSpec fileSpec = new AddSpec();
        fileSpec.setName("ttylinux.ovf");
        fileSpec.setSourceType(SourceType.PUSH);
        FileTypes.Info file = client.updateSessionFile().add(sessionId, fileSpec);

        IOUtil.print("OVF file from local storage is being uploaded");
        HttpUtil.uploadFileToUri(new File(ovfFilePath), file.getUploadEndpoint().getUri());

        IOUtil.print("Validating OVF upload");
        // validate the OVF file
        ValidationResult result =
                client.updateSessionFile().validate(sessionId);

        for (String missingFile : result.getMissingFiles()) {
            IOUtil.print("Uploading missing file: " + missingFile);
            fileSpec = new AddSpec();
            fileSpec.setName(missingFile);
            fileSpec.setSourceType(SourceType.PUSH);
            file = client.updateSessionFile().add(sessionId, fileSpec);

            HttpUtil.uploadFileToUri(new File(ovfFile.getParentFile(), missingFile),
                    file.getUploadEndpoint().getUri());
        }
        // complete the session
        client.updateSession().complete(sessionId);
    }
}
