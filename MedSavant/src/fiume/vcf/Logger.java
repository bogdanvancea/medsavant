/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.vcf;

import java.util.Date;

/**
 *
 * @author mfiume
 */
public class Logger {

    private static boolean logOn = false;

    public static void log(Class c, String msg) {
        if (logOn)
            System.out.println("[" + c.getSimpleName() + "] " + (new Date()).toString() + " " + msg);
    }
}
