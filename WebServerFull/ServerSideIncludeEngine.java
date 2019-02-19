/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Jibble Web Server / WebServerLite.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: ServerSideIncludeEngine.java,v 1.2 2004/02/01 13:37:35 pjm2 Exp $

*/


import java.io.*;
import java.util.HashSet;


/**
 * Provides static methods to offer limited support for simple SSI
 * command directives.
 * 
 * @author Copyright Paul Mutton, http://www.jibble.org/
 */
public class ServerSideIncludeEngine {
    
    private ServerSideIncludeEngine() {
        // Prevent this class from being constructed.
    }
    
    // Deliver the fully processed SSI page to the client
    public static void deliverDocument(BufferedOutputStream out, File file) throws IOException {
        HashSet visited = new HashSet();
        parse(out, visited, file);
        out.flush();        
    }
    
    // Oooh scary recursion
    private static void parse(BufferedOutputStream out, HashSet visited, File file) throws IOException {
        
        if (!file.exists() || file.isDirectory()) {
            out.write(("[SSI include not found: " + file.getCanonicalPath() + "]").getBytes());
            return;
        }
        
        if (visited.contains(file)) {
            out.write(("[SSI circular inclusion rejected: " + file.getCanonicalPath() + "]").getBytes());
            return;
        }

        visited.add(file);
        
        // Work out the filename extension.  If there isn't one, we keep
        // it as the empty string ("").
        String extension = WebServerConfig.getExtension(file);
        
        if (WebServerConfig.SSI_EXTENSIONS.contains(extension)) {
            // process this ssi page line by line
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                int startIndex;
                int endIndex;
                while ((startIndex = line.indexOf("<!--#include file=\"")) >= 0) {
                    if ((endIndex = line.indexOf("\" -->", startIndex)) > startIndex) {
                        out.write(line.substring(0, startIndex).getBytes());
                        String filename = line.substring(startIndex + 19, endIndex);
                        parse(out, visited, new File(file.getParentFile(), filename));
                        line = line.substring(endIndex + 5, line.length());
                    }
                    else {
                        out.write(line.substring(0, 19).getBytes());
                        line = line.substring(19, line.length());
                    }
                }
                out.write(line.getBytes());
                out.write(WebServerConfig.LINE_SEPARATOR);
            }
        }
        else {
            // just dish out the bytes
            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer, 0, 4096)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        visited.remove(file);
        
    }
    
}