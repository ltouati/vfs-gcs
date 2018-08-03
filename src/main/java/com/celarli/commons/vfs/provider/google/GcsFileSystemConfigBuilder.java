package com.celarli.commons.vfs.provider.google;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.InputStream;


public class GcsFileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static final GcsFileSystemConfigBuilder BUILDER = new GcsFileSystemConfigBuilder();


    private GcsFileSystemConfigBuilder() {

        super("gcs.");
    }


    public static GcsFileSystemConfigBuilder getInstance() {

        return BUILDER;
    }


    @Override
    protected Class<? extends FileSystem> getConfigClass() {

        return GCSFileSystem.class;
    }


    /**
     * Set the input stream for json key file to access GCS
     */
    public void setKeyStream(FileSystemOptions opts, InputStream fis) {

        setParam(opts, "jsonKey", fis);
    }


    /**
     * Get the input stream set by file system to access GCS
     */
    public InputStream getKeyStream(FileSystemOptions opts) {

        return (InputStream) getParam(opts, "jsonKey");
    }

}