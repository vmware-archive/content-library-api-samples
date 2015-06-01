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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.vmware.content.library.item.UpdateSessionModel;
import com.vmware.content.library.item.updatesession.FileTypes.AddSpec;
import com.vmware.content.library.item.updatesession.FileTypes.Info;
import com.vmware.content.library.item.updatesession.FileTypes.SourceType;
import com.vmware.content.library.item.updatesession.FileTypes.ValidationResult;
import com.vmware.content.samples.client.util.IOUtil;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * This API sample demonstrates upload of an OVA package
 * from local storage into an OVF item in a content library.
 */
public class ImportOva extends AbstractSample {

    public static void main(String[] args) throws Exception {
        ImportOva sample = new ImportOva();
        sample.run(args);
    }

    @Override
    public void runSample(String[] args) throws Exception {
        IOUtil.print("Creating a library");
        String libraryName = "my-lib";
        String libraryId = ClsApiHelper.getLibraryByName(client, libraryName);
        if (libraryId == null) {
            String fileUri = IOUtil.read("Enter the library storage backing URI; " +
                    "for example, file:///tmp: ");
            libraryId = ClsApiHelper.createLocalLibraryOnFileBacking(client, libraryName, URI.create(fileUri));
        }

        IOUtil.print("Creating an item");
        String itemId = ClsApiHelper.createItem(client, libraryId, "my-item", "ovf");
        UpdateSessionModel updateSessionModel = new UpdateSessionModel();
        updateSessionModel.setLibraryItemId(itemId);
        String sessionId = client.updateSession().create(ClsApiHelper.getRandomClientToken(),
                updateSessionModel);

        String ovaPath = IOUtil.read("Enter OVA file path: ");
        try {
            uploadOva(ovaPath, sessionId);
            // use the validation to figure out the missing files
            ValidationResult result = client.updateSessionFile().validate(sessionId);
            if (result.getHasErrors()) {
                throw new RuntimeException("Invalid OVA file " + result);
            }
            // mark the session as completed
            client.updateSession().complete(sessionId);
            IOUtil.print("Upload of OVA file finished successfully");
        } catch (Exception e) {
            // if anything goes wrong, try to cancel the session
            client.updateSession().cancel(sessionId);
            throw e;
        } finally {
            // cleanup the session
            client.updateSession().delete(sessionId);
        }
        ClsApiHelper.printItemsFromLibrary(client, libraryId);
    }

    private URI generateUploadUri(String sessionId, String fileName) {
        // add the OVF file
        AddSpec fileSpec = new AddSpec();
        fileSpec.setName(fileName);
        fileSpec.setSourceType(SourceType.PUSH);
        Info fileInfo = client.updateSessionFile().add(sessionId, fileSpec);
        URI uploadUri = fileInfo.getUploadEndpoint().getUri();
        return uploadUri;
    }

    private void uploadOva(String ovaPath, String sessionId) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient =
                HttpClients.custom().setSSLSocketFactory(sslsf).build();
        IOUtil.print("Streaming OVF to update session " + sessionId);
        try (TarArchiveInputStream tar =
                new TarArchiveInputStream(new FileInputStream(ovaPath))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                long bytes = entry.getSize();
                IOUtil.print("Uploading " + entry.getName() + " (" + entry.getSize() + " bytes)");
                URI uploadUri = generateUploadUri(sessionId, entry.getName());
                HttpPut request = new HttpPut(uploadUri);
                HttpEntity content = new TarBasedInputStreamEntity(tar, bytes);
                request.setEntity(content);
                HttpResponse response = httpclient.execute(request);
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private static class TarBasedInputStreamEntity extends AbstractHttpEntity {
        private final static int BUFFER_SIZE = 2048;

        private final InputStream content;
        private final long length;
        private boolean consumed = false;

        public TarBasedInputStreamEntity(final InputStream instream, long length) {
            super();
            if (instream == null) {
                throw new IllegalArgumentException(
                        "Source input stream may not be null");
            }
            this.content = instream;
            this.length = length;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public long getContentLength() {
            return this.length;
        }

        @Override
        public InputStream getContent() throws IOException {
            return this.content;
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException {
            if (outstream == null) {
                throw new IllegalArgumentException(
                        "Output stream may not be null");
            }
            InputStream instream = this.content;
            byte[] buffer = new byte[BUFFER_SIZE];
            int l;
            if (this.length < 0) {
                // consume until EOF
                while ((l = instream.read(buffer)) != -1) {
                    outstream.write(buffer, 0, l);
                }
            } else {
                // consume no more than length
                long remaining = this.length;
                while (remaining > 0) {
                    l =
                            instream.read(buffer, 0,
                                    (int) Math.min(BUFFER_SIZE, remaining));
                    if (l == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l);
                    remaining -= l;
                }
            }
            this.consumed = true;
        }

        @Override
        public boolean isStreaming() {
            return !this.consumed;
        }

        @Override
        @Deprecated
        public void consumeContent() throws IOException {
            this.consumed = true;
            // If the input stream is from a connection, closing it will read to
            // the end of the content. Otherwise, we don't care what it does.
            this.content.close();
        }
    }
}
