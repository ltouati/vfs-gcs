# Dalet fork
The purpose of this fork is to maintain any changes made by Dalet, until those changes are merged upstream.
From this fork we publish artifacts to JCenter; instead of `com.celarli.commons:vfs-gcs` the artifacts are named
`com.dalet.celarli.commons:vfs-gcs` to make clear their different origin.

## Versioning model
The upstream repository owner seems reasonably responsive to PRs, so we derive our versioning model from the upstream
version, as follows:
-  Every push to master gets built and published
-  The version number we publish (simply using git describe) is:
      `<upstream version>-<number-of-commits-since>-g<hash-of-HEAD>`
   For example, version `1.0.8-4-g604ac6d` implies a build of commit `604ac6d`, which is 4 commits on from the upstream
   published version `1.0.8`.


# vfs-gcs
Google Cloud Storage provider for Apache Commons VFS - http://commons.apache.org/proper/commons-vfs/

From the website...
"Commons VFS provides a single API for accessing various different file systems. It presents a uniform view of the files from various different sources, such as the files on local disk, on an HTTP server, or inside a Zip archive."

Now Apache Commons VFS can now add Google Cloud Storage to the list.

The system will use your GCP credentials in the following way

1. `GOOGLE_APPLICATION_CREDENTIALS` environment variable, pointing to a service account key JSON file path.
2. Cloud SDK credentials `gcloud auth application-default login`
3. App Engine standard environment credentials.
4. Compute Engine credentials.

Here is an example using the API
```java
 String currFileNameStr;

        // Now let's create a temp file just for upload
        File temp = File.createTempFile("uploadFile01", ".tmp");
        try (FileWriter fw = new FileWriter(temp)) {
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append("testing...");
            bw.flush();
        }

        DefaultFileSystemManager currMan = new DefaultFileSystemManager();
        currMan.addProvider("gcs", new GCSFileProvider());
        currMan.addProvider("file", new DefaultLocalFileProvider());
        currMan.init();

        // Create a URL for creating this remote file
        currFileNameStr = "test01.tmp";
        String bucket = "gcp-ltouati-2";
        String currUriStr = String.format("%s://%s/%s",
                                          "gcs", bucket, currFileNameStr);

// Resolve the imaginary file remotely.  So we have a file object
        FileObject currFile = currMan.resolveFile(currUriStr);

// Resolve the local file for upload
        FileObject currFile2 = currMan.resolveFile(
                String.format("file://%s", temp.getAbsolutePath()));

// Use the API to copy from one local file to the remote file
        currFile.copyFrom(currFile2, Selectors.SELECT_SELF);

// Delete the temp we don't need anymore
        temp.delete();
        currFile.delete();
```

