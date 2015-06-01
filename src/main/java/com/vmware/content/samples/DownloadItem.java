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

/**
 * This API sample demonstrates download of the files from a given item.
 * <p>
 * This sample assumes that the connected vCenter Server instance has at least
 * one item available to download in the available content libraries.
 */
public class DownloadItem extends AbstractSample {
    public static void main(String[] args) {
        DownloadItem sample = new DownloadItem();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        // list the available items
        ClsApiHelper.printAllLibraryItems(client);
        String itemId = IOUtil.read("Enter item ID to download: ");
        String path = IOUtil.read("Enter directory to store the " +
                "downloaded files; for example, /tmp: ");
        ClsApiHelper.downloadFilesFromItem(client, itemId, path);
    }
}
