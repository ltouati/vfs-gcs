package com.celarli.commons.vfs.provider.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.URLFileNameParser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * A Commons VFS provider that allow connecting to the Google Cloud Storage
 */
public class GCSFileProvider extends AbstractOriginatingFileProvider {

    static final Collection<Capability> capabilities =
            Collections.unmodifiableCollection(Arrays.asList(Capability.GET_TYPE,
                    Capability.READ_CONTENT,
                    Capability.APPEND_CONTENT,
                    Capability.URI,
                    Capability.ATTRIBUTES,
                    Capability.RANDOM_ACCESS_READ,
                    Capability.DIRECTORY_READ_CONTENT,
                    Capability.LIST_CHILDREN,
                    Capability.LAST_MODIFIED,
                    Capability.GET_LAST_MODIFIED,
                    Capability.CREATE,
                    Capability.DELETE)
            );


    @Override
    protected FileSystem doCreateFileSystem(FileName fileName, FileSystemOptions fileSystemOptions) {

        try {

            InputStream fis = GcsFileSystemConfigBuilder.getInstance().getKeyStream(fileSystemOptions);

            Storage storage;
            if (fis == null) {
                storage = StorageOptions.getDefaultInstance().getService();
            }
            else {
                GoogleCredentials credentials = GoogleCredentials.fromStream(fis);

                storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            }

            return new GCSFileSystem(fileName, fileSystemOptions, storage);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public Collection<Capability> getCapabilities() {

        return capabilities;
    }


    public GCSFileProvider() {

        setFileNameParser(new URLFileNameParser(80));
    }
}
