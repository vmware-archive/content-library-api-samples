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

import java.net.URI;

import com.vmware.content.LibraryModel;
import com.vmware.content.samples.client.util.ClsApiUtil;
import com.vmware.content.samples.client.util.IOUtil;

/**
 * This API sample demonstrates basic workflows from the Content Library Service.
 * <p>
 * Workflows demonstrated on a content library:
 * <ul>
 *     <li> Create a library backed by NFS eg. nfs://my-nfs-server:/exported_share or
 *          the local file system of the connected vCenter Server instance (eg. file:///tmp in case of VCSA 6.0) </li>
 *     <li> Update the name of a library </li>
 *     <li> Delete a library </li>
 * </ul>
 * <p>
 * Workflows demonstrated on an item in a content library:
 * <ul>
 *     <li> Create an item in a library </li>
 *     <li> Import an ISO file from a HTTP URL into an item </li>
 *     <li> Upload an ISO file from local storage to an item </li>
 *     <li> Download the specified item from a library </li>
 * </ul>
 *
 * @see https://blogs.vmware.com/developer/2015/05/api-tutorial-basic-life-cycle-content-library.html
 */
public class BasicLibraryFlow extends AbstractSample {

    public static void main(String[] args) throws Exception {
        BasicLibraryFlow sample = new BasicLibraryFlow();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        // create a local library backed by file/NFS URI
        String fileUri = IOUtil.read("Enter the library storage backing URI; " +
                "for example, file:///tmp: ");
        String libraryName = "Demo library for basic workflow!";
        String libraryId = ClsApiUtil.createLocalLibraryOnFileBacking(client, libraryName, URI.create(fileUri));
        IOUtil.print("Created library backed by file URI. LibraryId: " + libraryId);

        // update library name
        updateLibraryName(libraryId, libraryName + "updated");
        IOUtil.print("Updated library name.");

        // create item in this library
        String firstItemId = ClsApiUtil.createItem(client, libraryId, "cdrom", "iso");
        IOUtil.print("Created ISO item. ItemId: " + firstItemId);

        // upload an iso file from URL to this library item
        String endpointUri = IOUtil.read("Enter the URL of the remote ISO image to upload; " +
                "for example, http://www.example.com/cdrom.iso: ");
        String fileName = "cdrom.iso";
        ClsApiUtil.importFileFromHttpUriToItem(client, firstItemId, endpointUri, fileName);
        IOUtil.print("File import to URL started. FileName:" + fileName);

        // create another item in the same library of type iso
        String secondItemId = ClsApiUtil.createItem(client, libraryId, "another_cdrom", "iso");

        // upload file from local storage to this library item
        String isoFilePathOnLocalStorage = IOUtil.read("Enter the file path " +
                "of the local ISO image to upload; for example, /tmp/cdrom.iso: ");
        ClsApiUtil.uploadSingleFileFromLocalStorageToItem(client, secondItemId,
                isoFilePathOnLocalStorage, "another_cdrom.iso");
        IOUtil.print("Uploaded ISO file from local storage");

        // downloading a library item
        String downloadsDirectory = IOUtil.read("Enter the folder path to store " +
                "the downloaded file; for example, /tmp: ");
        ClsApiUtil.downloadFilesFromItem(client, firstItemId, downloadsDirectory);
        IOUtil.print("Downloaded item files to " + downloadsDirectory);

        // cleanup
        client.localLibrary().delete(libraryId);
    }

    private void updateLibraryName(String libraryId, String updatedName) {
        LibraryModel libraryModel = new LibraryModel();
        libraryModel.setName(updatedName);
        client.localLibrary().update(libraryId, libraryModel);
    }
}
