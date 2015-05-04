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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Helper class to deal with http upload and download.
 */
public class HttpUtil {

    private HttpUtil() {
    }

    /**
     * Downloads a file from a given HTTP URI in a given folder.
     *
     * @param uri HTTP URI to download file from.
     * @param folderToDownloadFiles path to a directory on the local storage
     *                              to store the download the files.
     * @param fileName name to use when creating the downloaded file on the local storage.
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.KeyStoreException
     * @throws java.security.KeyManagementException
     * @throws java.io.IOException
     */
    public static void downloadFileFromUri(URI uri, String folderToDownloadFiles, String fileName)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        HttpGet getRequest = new HttpGet(uri);
        CloseableHttpClient httpClient = getCloseableHttpClient();
        HttpResponse response = httpClient.execute(getRequest);
        File file = new File(folderToDownloadFiles, fileName);
        response.getEntity().writeTo(new FileOutputStream(file));
        IOUtil.print("Downloaded: " + file.getPath());
    }

    /**
     * Uploads a file from local storage to a given HTTP URI.
     *
     * @param localFile local storage path to the file to upload.
     * @param uploadUri HTTP URI where the file needs to be uploaded.
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.KeyStoreException
     * @throws java.security.KeyManagementException
     * @throws java.io.IOException
     */
    public static void uploadFileToUri(File localFile, URI uploadUri)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        CloseableHttpClient httpClient = getCloseableHttpClient();
        HttpPut request = new HttpPut(uploadUri);
        HttpEntity content = new FileEntity(localFile);
        request.setEntity(content);
        HttpResponse response = httpClient.execute(request);
        EntityUtils.consumeQuietly(response.getEntity());
    }

    private static CloseableHttpClient getCloseableHttpClient()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(builder.build());
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }
}
