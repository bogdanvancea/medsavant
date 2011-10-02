package org.ut.biolab.medsavant.server.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.server.mail.Mail;

/**
 *
 * @author mfiume
 */
public class ServerLogger {

    private static String logPath = "server.log";
    private static String emailaddress;
    private static BufferedWriter writer;
    private static Logger logger;
    private static boolean logOpen;

    public static void log(Class c,String string) {
        log(c,string, Level.INFO);
    }

    private static void openLogFile() throws IOException {
        FileHandler handler = new FileHandler(logPath, true);
        handler.setFormatter(new BriefLogFormatter());
        logger = Logger.getLogger("org.ut.biolab.medsavant.server");
        logger.addHandler(handler);
        logOpen = true;
    }

    public static void setMailRecipient(String eaddress) {
        emailaddress = eaddress;
    }

    public static void logByEmail(Class c,String subject, String message) {
        logByEmail(c,subject,message,Level.INFO);
    }
    
    public static void logByEmail(Class c,String subject, String message, Level l) {
        message += "\n\nMedSavant Server Utility";
        if (emailaddress != null) {
            Mail.sendEmail(emailaddress, "[" + l + "] " + subject, message);
            log(c,"(Also emailed to " + emailaddress + "): \"" + message.replace("\n", "") + "\"",l);
        } else {
            log(c,"(No email address configured to sent to): \"" + message.replace("\n", "") + "\"",l);
        }
    }

    public static void log(Class c,String msg, Level level) {
        try {
            if (!logOpen) {
                openLogFile();
            }
            logger.log(level, "'{'{0}'} '{1}", new Object[]{c.toString(), msg});
        } catch (IOException ex) {
        }
    }

    public static void setLogStatus(boolean b) {
        if (b) {
        } else {
            try {
                writer.close();
            } catch (IOException ex) {
            }
            writer = null;
        }
    }
}