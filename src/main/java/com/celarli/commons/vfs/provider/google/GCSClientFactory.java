package com.celarli.commons.vfs.provider.google;

import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;


public class GCSClientFactory {

    private static final String INVALID_CLIENT_TYPE = "No suitable client type found to create storage client";
    private static final String CREDENTIAL_NOT_FOUND = "Credential not found";


    public static Storage getClient(FileSystemOptions fileSystemOptions) {

        Integer type = GcsFileSystemConfigBuilder.getInstance().getClientType(fileSystemOptions);

        Optional<ClientType> optional = ClientType.getByType(type);

        ClientType clientType = optional.orElseThrow(() -> new RuntimeException(INVALID_CLIENT_TYPE));
        switch (clientType) {
        case STORAGE_ACCOUNT:
            InputStream inputStream = GcsFileSystemConfigBuilder.getInstance().getKeyStream(fileSystemOptions);

            if (inputStream == null) {
                throw new RuntimeException(CREDENTIAL_NOT_FOUND);
            }

            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);

                String hostname = GcsFileSystemConfigBuilder.getInstance().getHostname(fileSystemOptions);
                if (hostname != null) {
                    return StorageOptions.newBuilder().setCredentials(credentials).setHost(hostname).build().getService();
                }
                else {
                    return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                }
            }
            catch (IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }

        case COMPUTE_ENGINE:
            // Explicitly request service account credentials from the compute engine instance.
            GoogleCredentials computeEngineCredentials = ComputeEngineCredentials.create();
            return StorageOptions.newBuilder().setCredentials(computeEngineCredentials).build().getService();

        case APPLICATION:
            return StorageOptions.getDefaultInstance().getService();
        }

        throw new RuntimeException(INVALID_CLIENT_TYPE);
    }
}
