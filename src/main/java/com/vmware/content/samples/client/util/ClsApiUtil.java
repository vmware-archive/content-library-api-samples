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
package com.vmware.content.samples.client.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.vmware.content.Library;
import com.vmware.content.LibraryModel;
import com.vmware.content.LibraryTypes;
import com.vmware.content.library.Item;
import com.vmware.content.library.ItemModel;
import com.vmware.content.library.StorageBacking;
import com.vmware.content.library.item.DownloadSessionModel;
import com.vmware.content.library.item.TransferEndpoint;
import com.vmware.content.library.item.UpdateSessionModel;
import com.vmware.content.samples.client.ClsApiClient;
import com.vmware.vcloud.suite.samples.common.ServiceManager;
import com.vmware.vcloud.suite.samples.vim.helpers.VimUtil;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;

/**
 * Helper class to perform commonly used operations using Content Library API.
 */
public class ClsApiUtil {

    private ClsApiUtil() {
    }

    /**
     * Generates random client tokens for vApi calls.
     *
     * @return randomly generated client token.
     */
    public static String getRandomClientToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create item in the given library.
     *
     * @param client authenticated ClsApiClient.
     * @param libraryId identifier of a library where
     *                  the new item needs to be created.
     * @param itemName name of the item to be created.
     * @param type type of the item to be created.
     * @return identifier of the newly created item.
     */
    public static String createItem(ClsApiClient client, String libraryId, String itemName, String type) {
        ItemModel item = new ItemModel();
        item.setName(itemName);
        item.setLibraryId(libraryId);
        item.setType(type);
        return client.item().create(getRandomClientToken(), item);
    }

    /**
     * Creates a local library backed by file/NFS uris.
     *
     * @param client authenticated ClsApiClient.
     * @param name name of the library to be created.
     * @param storageUri URI of the {@link com.vmware.content.library.StorageBacking}
     *                   to be used when creating this new library.
     * @return identifier of the newly created library.
     */
    public static String createLocalLibraryOnFileBacking(ClsApiClient client, String name, URI storageUri) {
        // build storageBacking
        StorageBacking libraryBacking = new StorageBacking();
        libraryBacking.setType(StorageBacking.Type.OTHER);
        libraryBacking.setStorageUri(storageUri);

        // build libraryModel for the library to be created
        LibraryModel libraryModel = new LibraryModel();
        libraryModel.setName(name);
        libraryModel.setType(LibraryModel.LibraryType.LOCAL);
        libraryModel.setStorageBackings(Collections
                .singletonList(libraryBacking));

        return client.localLibrary().create(getRandomClientToken(), libraryModel);
    }

    /**
     * Gets the identifier of the first library which matches the given name.
     *
     * @param client authenticated ClsApiClient.
     * @param libraryName name of the library to find identifier for.
     * @return identifier of the first library matching the given name.
     */
    public static String getLibraryByName(ClsApiClient client, String libraryName) {
        LibraryTypes.FindSpec findSpec = new LibraryTypes.FindSpec();
        findSpec.setName(libraryName);
        List<String> result = client.library().find(findSpec);
        if (result.size() >= 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    /**
     * Downloads files from a given item in a given folder.
     *
     * @param client authenticated ClsApiClient.
     * @param libraryItemId identifier of the item to be downloaded.
     * @param folderToDownloadFiles path to a directory on the local storage
     *                              to store the downloaded files from
     *                              the given item.
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws IOException
     */
    public static void downloadFilesFromItem(ClsApiClient client,
            String libraryItemId, String folderToDownloadFiles) throws InterruptedException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // create a download session to download files from item
        DownloadSessionModel downloadSessionModel = new DownloadSessionModel();
        downloadSessionModel.setLibraryItemId(libraryItemId);
        String downloadSessionId = client.downloadSession().create(getRandomClientToken(), downloadSessionModel);

        for (com.vmware.content.library.item.downloadsession.FileTypes.Info downloadInfo : client
                .downloadSessionFile().list(downloadSessionId)) {
            // prepare the file for download
            client.downloadSessionFile().prepare(downloadSessionId, downloadInfo.getName(),
                    com.vmware.content.library.item.downloadsession.FileTypes.EndpointType.HTTPS);
            do {
                Thread.sleep(1000);
            } while (client.downloadSessionFile()
                    .get(downloadSessionId, downloadInfo.getName())
                    .getStatus() != com.vmware.content.library.item.downloadsession.FileTypes.PrepareStatus.PREPARED);

            com.vmware.content.library.item.downloadsession.FileTypes.Info fileInfo =
                    client.downloadSessionFile().get(downloadSessionId, downloadInfo.getName());
            URI uri = fileInfo.getDownloadEndpoint().getUri();
            HttpUtil.downloadFileFromUri(uri, folderToDownloadFiles, downloadInfo.getName());
        }
    }

    /**
     * Imports a file from a URI in a given item.
     *
     * @param client authenticated ClsApiClient.
     * @param itemId identifier of the item to import the file into.
     * @param endpointUri HTTP URL of the file to be imported.
     * @param fileName name to use when importing the file.
     * @return identifier of the update session created to import the file.
     */
    public static String importFileFromHttpUriToItem(ClsApiClient client,
            String itemId, String endpointUri, String fileName) {
        // create update session for uploading the file
        UpdateSessionModel updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(itemId);
        String sessionId =
                client.updateSession().create(null, updateSessionModel);

        // add the http URL of the file to be imported to the session
        com.vmware.content.library.item.updatesession.FileTypes.AddSpec
                file = new com.vmware.content.library.item.updatesession.FileTypes.AddSpec();
        file.setName(fileName);
        file.setSourceType(com.vmware.content.library.item.updatesession.FileTypes.SourceType.PULL);
        TransferEndpoint endpoint = new TransferEndpoint();
        endpoint.setUri(URI.create(endpointUri));
        file.setSourceEndpoint(endpoint);
        client.updateSessionFile().add(sessionId, file);
        client.updateSession().complete(sessionId);
        return sessionId;
    }

    /**
     * Uploads a file from the local storage to a item specified by its identifier.
     *
     * @param client authenticated ClsApiClient.
     * @param itemId identifier of the item to upload the file to.
     * @param filePath local storage path to the file to be uploaded.
     * @param fileName name to use when uploading the file.
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws IOException
     */
    public static void uploadSingleFileFromLocalStorageToItem(ClsApiClient client,
            String itemId, String filePath, String fileName) throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException {
        // create an update session
        UpdateSessionModel updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(itemId);
        String sessionId =
                client.updateSession().create(getRandomClientToken(), updateSessionModel);

        // build the spec to upload the given file
        com.vmware.content.library.item.updatesession.FileTypes.AddSpec
                file = new com.vmware.content.library.item.updatesession.FileTypes.AddSpec();
        file.setName(fileName);
        file.setSourceType(com.vmware.content.library.item.updatesession.FileTypes.SourceType.PUSH);

        com.vmware.content.library.item.updatesession.FileTypes.Info
                fileInfo = client.updateSessionFile().add(sessionId, file);
        URI uploadUri = fileInfo.getUploadEndpoint().getUri();

        // push the bits
        HttpUtil.uploadFileToUri(new File(filePath), uploadUri);

        // complete the session
        client.updateSession().complete(sessionId);
    }

    /**
     * Gets resource pool from cluster name.
     *
     * @param client authenticated ClsApiClient.
     * @param clusterName name of the cluster to find
     *                    the {@link com.vmware.vim25.ManagedObjectReference} for.
     * @return {@link com.vmware.vim25.ManagedObjectReference} for the given cluster name.
     */
    public static ManagedObjectReference getResourcePoolFromClusterName(ClsApiClient client, String clusterName) {
        ServiceManager serviceManager;
        try {
            serviceManager = VimServiceManager.getServiceManager(client.getPlatformServiceController(),
                    client.getManagementNodeId());
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to VMware APIs of " +
                    "the vCenter Server instance", e);
        }

        ManagedObjectReference clusterMoRef;
        try {
            clusterMoRef =
                    VimUtil.getCluster(serviceManager.getVimPortType(),
                            serviceManager.getServiceContent(), clusterName);
            assert clusterMoRef != null;
        } catch (Exception e) {
            throw new RuntimeException("Cannot get managed object ID for the given cluster", e);
        }

        List<DynamicProperty> dynamicProps;
        try {
            // find the cluster's root resource pool
            dynamicProps =
                    VimUtil.getProperties(serviceManager.getVimPortType(),
                            serviceManager.getServiceContent(), clusterMoRef, clusterMoRef.getType(),
                            Arrays.asList("resourcePool"));
            assert dynamicProps != null && dynamicProps.size() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Cannot find root resource pool for the given cluster", e);
        }
        return (ManagedObjectReference) dynamicProps.get(0).getVal();
    }

    /**
     * Prints all local libraries in the vCenter Server instance client is logged in to.
     *
     * @param client authenticated ClsApiClient.
     */
    public static void printAllLocalLibraries(ClsApiClient client) {
        Library library = client.library();
        IOUtil.print("\nList of local libraries in this vCenter Server instance:");
        List<String> libraryIds = library.list();
        for (String libraryId : libraryIds) {
            LibraryModel libraryModel = library.get(libraryId);
            if (libraryModel.getType() == LibraryModel.LibraryType.LOCAL){
                IOUtil.print("    " + libraryModel.getName() + " (Id: " + libraryId + ")");
            }
        }
        IOUtil.print("");
    }

    /**
     * Prints the items from all the libraries in the given vCenter Server instance.
     *
     * @param client authenticated ClsApiClient.
     */
    public static void printAllLibraryItems(ClsApiClient client) {
        Library library = client.library();
        IOUtil.print("\nList of items in all content libraries:");
        // get all content libraries
        List<String> libraryIds = library.list();
        for (String libraryId : libraryIds) {
            LibraryModel libraryModel = library.get(libraryId);
            IOUtil.print(libraryModel.getName() + " (Id: " + libraryId + ")");
            // list all items in each content library
            printItemsFromLibrary(client, libraryId);
            IOUtil.print("");
        }
    }

    /**
     * Prints items from the given library.
     *
     * @param client authenticated ClsApiClient.
     * @param libraryId identifier of a library to print items from.
     */
    public static void printItemsFromLibrary(ClsApiClient client, String libraryId) {
        Item item = client.item();
        List<String> itemIds = item.list(libraryId);
        for (String itemId : itemIds) {
            ItemModel itemModel = item.get(itemId);
            IOUtil.print(itemModel.getName()
                    + " - Id: " + itemModel.getId()
                    + " (type=" + itemModel.getType() + ")");
        }
    }
}
