package com.celarli.commons.vfs.provider.google;

import com.google.cloud.storage.Storage;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;


/**
 * Implementation of a filesystem backed by a GCS bucket
 */
public class GCSFileSystem extends AbstractFileSystem {

    /**
     * The GCS client
     */
    private final Storage storage;


    /**
     * Constructor
     *
     * @param rootName          the file system root name
     * @param fileSystemOptions the file system options
     * @param storage           the GCS client
     */
    GCSFileSystem(@Nonnull FileName rootName, @Nullable FileSystemOptions fileSystemOptions, @Nonnull Storage storage) {

        super(rootName, null, fileSystemOptions);
        this.storage = storage;
    }


    /**
     * Creates a file object so we can interact with
     *
     * @param abstractFileName the file name of the object to interact with
     * @return the file object
     * @throws Exception if we can't create the file object
     */
    @Nonnull
    @Override
    protected FileObject createFile(@Nonnull AbstractFileName abstractFileName) {

        return new GCSFileObject(abstractFileName, this, storage);
    }


    /**
     * Adds capabilities to this driver
     *
     * @param caps the driver capabilities
     */
    @Override
    protected void addCapabilities(@Nonnull Collection<Capability> caps) {

        caps.addAll(GCSFileProvider.capabilities);
    }

}
