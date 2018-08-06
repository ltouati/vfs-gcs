package com.celarli.commons.vfs.provider.google;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.URLFileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;


/**
 * This class is in charge of bridging the gap between commons vfs and GCS
 */
public class GCSFileObject extends AbstractFileObject {

    private static final Logger log = LoggerFactory.getLogger(GCSFileObject.class);
    /**
     * The GCS client
     */
    private final Storage storage;
    /**
     * The current blob object
     */
    private Blob currentBlob = null;


    /**
     * Constructor
     *
     * @param name    the file name
     * @param fs      the file system object
     * @param storage the GCS client
     */
    GCSFileObject(@Nonnull AbstractFileName name, @Nonnull GCSFileSystem fs, @Nonnull Storage storage) {

        super(name, fs);
        this.storage = storage;
    }


    /**
     * We must override this method because the parent one throws exception.
     *
     * @param modtime the last modified time.
     * @return true if successfully modified, false otherwise.
     * @throws Exception in case an error happens.
     */
    @Override
    protected boolean doSetLastModifiedTime(long modtime) throws Exception {

        return true;
    }


    @Nonnull
    @Override
    protected FileType doGetType() throws Exception {

        log.debug("Trying to get file type for:" + this.getName());
        URLFileName urlFileName = (URLFileName) this.getName();

        if (urlFileName != null && urlFileName.getType() == FileType.FOLDER) {
            return FileType.FOLDER;
        }

        Bucket bucket = this.storage.get(urlFileName.getHostName());
        if (bucket == null || !bucket.exists()) {
            throw new IllegalArgumentException(format("Bucket %s does not exists", urlFileName.getHostName()));
        }

        String path = urlFileName.getPath();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Blob blob = bucket.get(path);
        if (blob != null && blob.exists()) {
            log.debug(format("File :%s exists on bucket", this.getName()));
            return FileType.FILE;
        }
        else {
            // GCS does not have folders.  Just files with path separators in their names.

            // Here's the trick for folders.
            //
            // Do a listing on that prefix.  If it returns anything, after not existing, then it's a folder.
            String prefix = computePrefix(urlFileName);
            log.debug(format("File does not :%s exists on bucket try to see if it's a directory", this.getName()));
            Page<Blob> blobs;
            if (prefix.equals("/")) {
                // Special root path case. List the root blobs with no prefix
                return FileType.FOLDER;
            }
            else {
                log.debug(format("listing directory :%s", prefix));
                blobs = bucket.list(Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(prefix));
            }
            if (blobs.getValues().iterator().hasNext()) {
                return FileType.FOLDER;
            }
        }
        return FileType.IMAGINARY;

    }


    @Nonnull
    @Override
    protected String[] doListChildren() throws Exception {

        log.debug(format("Listing directory below:%s", this.getName().toString()));
        URLFileName urlFileName = (URLFileName) this.getName();
        Bucket bucket = this.storage.get(urlFileName.getHostName());
        if (bucket == null || !bucket.exists()) {
            throw new IllegalArgumentException(format("Bucket %s does not exists", urlFileName.getHostName()));
        }

        String prefix = computePrefix(urlFileName);

        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }

        Page<Blob> blobs = bucket.list(Storage.BlobListOption.currentDirectory(),
                Storage.BlobListOption.prefix(prefix));

        List<String> childrenList = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
            String name = blob.getName();
            if (!name.equalsIgnoreCase(prefix)) {
                childrenList.add("/" + name);
            }
        }
        String[] ret = new String[childrenList.size()];
        childrenList.toArray(ret);
        return ret;
    }


    @Nonnull
    private String computePrefix(@Nonnull URLFileName urlFileName) {

        String prefix = urlFileName.getPath();
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        return prefix;
    }


    @Override
    protected long doGetContentSize() throws Exception {

        return this.currentBlob.getSize();
    }


    @Nonnull
    @Override
    protected InputStream doGetInputStream() throws Exception {

        final ReadChannel readChannel = this.storage.reader(this.currentBlob.getBlobId());
        return Channels.newInputStream(readChannel);
    }


    /**
     * Callback for handling create folder requests.  Since there are no folders
     * in GCS this call is ingored.
     *
     * @throws Exception ignored
     */
    @Override
    protected void doCreateFolder() throws Exception {

        log.info("doCreateFolder() called.");
    }


    /**
     * Used for creating folders.  It's not used since GCS does not have
     * the concept of folders.
     *
     * @throws FileSystemException ignored
     */
    @Override
    public void createFolder() throws FileSystemException {

        log.debug("createFolder() called.");
    }


    @Override
    protected void doAttach() throws Exception {

        URLFileName urlFileName = (URLFileName) this.getName();
        Bucket bucket = this.storage.get(urlFileName.getHostName());
        if (bucket == null || !bucket.exists()) {
            throw new IllegalArgumentException(format("Bucket %s does not exists", urlFileName.getHostName()));
        }

        String path = urlFileName.getPath();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        this.currentBlob = bucket.get(path);
    }


    @Override
    protected void doDelete() throws Exception {

        getCurrentBlob();
        this.currentBlob.delete();
    }


    @Nonnull
    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {

        getCurrentBlob();
        return Channels.newOutputStream(this.currentBlob.writer());
    }


    private void getCurrentBlob() {

        URLFileName urlFileName = (URLFileName) this.getName();
        String path = urlFileName.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        this.currentBlob = storage.create(BlobInfo.newBuilder(urlFileName.getHostName(), path).build());
    }


    /**
     * Returns the file's list of children.
     *
     * @return The list of children
     * @throws FileSystemException If there was a problem listing children
     * @see AbstractFileObject#getChildren()
     */
    @Override
    public FileObject[] getChildren() throws FileSystemException {

        try {
            // Folders which are copied from other folders, have type = IMAGINARY. We can not throw exception based on folder
            // type only and so we have check here for content.
            if (getType().hasContent()) {
                throw new FileNotFolderException(getName());
            }
        }
        catch (Exception ex) {
            throw new FileNotFolderException(getName(), ex);
        }

        return super.getChildren();
    }


    /**
     * Callback for handling the <code>getLastModifiedTime()</code> Commons VFS API call.
     *
     * @return Time since the file has last been modified
     * @throws Exception
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {

        if (currentBlob != null) {
            return currentBlob.getUpdateTime();
        }

        return super.doGetLastModifiedTime();
    }

}
