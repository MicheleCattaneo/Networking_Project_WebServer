package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {

    private final static String serverPath = Path.of("").toAbsolutePath().toString();

    /**
     * Given the host and the url, get the CANONICAL path (unique, removed '../' ),
     * and ensure that it starts with the same sub-path Care must be given, if the
     * given parameters to not form a valid url, the method returns false, but not
     * because of a permission problem but because the object does not exist. Use
     * this method AFTER having checked the validity of the host + url
     * 
     * @param host the host
     * @param url  the url
     * @return True if the user has the permission to perform the action, false
     *         otherwise
     */
    public static boolean hasPermissions(String host, String url) {

        try {
            String canonicalObjectPath = new File(serverPath, "/" + host + "/" + url).getCanonicalPath();
            String canonicalHostPath = new File(serverPath, "/" + host).getCanonicalPath();
            if (canonicalObjectPath.startsWith(canonicalHostPath)) {
                return true;
            }
            return false;

        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }

    }

    /**
     * Retrieve from the given url the file extension.
     * 
     * @param url the url
     * @return a String containing the file extension.
     */
    public static String getFileType(String url) {
        String[] trimmedUrl = url.split("\\.");
        return url.equals("/") ? "text/html" : trimmedUrl[trimmedUrl.length - 1];
    }

    /**
     * Check whether the file located at host + url exists.
     * 
     * @param host the host, first part of the path
     * @param url  the url, second part of the host
     * @return true if the path correspond to an actual file. Might be a folder or
     *         normal file.
     */
    public static boolean isValidFile(String host, String url) {
        File f = new File(serverPath, host + url);
        return f.exists();
    }

    /**
     * Retrieve the content of a file, located at host + url, relative to the server
     * path
     * 
     * @param host the host, the first part of the path
     * @param url  the url, the rest of the path
     * @return a String containing the content of the file
     * @throws IOException
     */
    public static byte[] getBody(String host, String url) throws IOException {
        String fileName = serverPath + "/" + host + url;
        return Files.readAllBytes(Paths.get(fileName));
    }

    /**
     * Create the file with the body.
     * 
     * @param host the host
     * @param url  the url
     * @param body the body of the file
     * @return True if the file was created, False if the file was edited
     * @throws IOException error creating the file or writing to it
     */
    public static boolean createFile(String host, String url, byte[] body) throws IOException {
        File newFile = new File(host, url);
        boolean created = newFile.createNewFile();
        FileOutputStream stream = new FileOutputStream(newFile);
        stream.write(body);
        stream.close();
        return created;
    }

    /**
     * Delete a file.
     * 
     * @param host the host
     * @param url  the url
     * @throws FileNotFoundException file can't be found
     */
    public static void deleteFile(String host, String url) throws FileNotFoundException {
        File file = new File(host, url);
        if (file.exists()) {
            file.delete();
        } else {
            throw new FileNotFoundException();
        }
    }

    /**
     * Maps a file extension to the corresponding MIME Type ( Conetent-Type ).
     * 
     * @param ext the extension String
     * @return the correct Content-Type header to return for the given extension
     */
    public static String mapExtensionsToContentType(final String ext) {
        if (ext.equalsIgnoreCase("txt")) {
            return "text/plain";
        }
        if (ext.equalsIgnoreCase("pdf")) {
            return "application/pdf";
        }
        if (ext.equalsIgnoreCase("css")) {
            return "text/css";
        }
        if (ext.equalsIgnoreCase("png")) {
            return "image/png";
        }
        if (ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("jpg")) {
            return "image/jpeg";
        }
        if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")) {
            return "text/html";
        }
        // default one
        return "text/plain";
    }

}
