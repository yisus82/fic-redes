/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Jibble Web Server / WebServerLite.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: ServerSideScriptEngine.java,v 1.4 2004/02/01 13:37:35 pjm2 Exp $

*/


import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Provides limited support for running server side scripts.
 * The HashMap of server variables are sent to the process
 * when it is executed.  While the process is outputting
 * data to standard output, this will be issued to the connecting
 * client.
 * 
 * @author Copyright Paul Mutton, http://www.jibble.org/
 */
public class ServerSideScriptEngine {

    // This could be a lot better.  Consider server side scripting a beta feature
    // for now.
    
    public static void execute(BufferedOutputStream out, HashMap serverVars, File file, String path) throws Throwable {
        
        // Place server variables into a String array.
        String[] envp = new String[serverVars.size()];
        Iterator varIt = serverVars.keySet().iterator();
        for (int i = 0; i < serverVars.size(); i++) {
            String key = (String)varIt.next();
            String value = (String)serverVars.get(key);
            envp[i] = key + "=" + value;
        }
        
        // Execute the external command
        String filename = file.toString();
        String[] cmdarray = null;
        
        if (filename.toLowerCase().endsWith(".pl")) {
            cmdarray = new String[]{"perl", filename};
        }
        else if (filename.toLowerCase().endsWith(".php")) {
            cmdarray = new String[]{"php", filename};
        }
        else {
            cmdarray = new String[]{filename};
        }
        Process process = Runtime.getRuntime().exec(cmdarray, envp, file.getParentFile());
        
        // Send the process output to the connecting client.
        InputStream in = process.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        
    }
    
}