package pl.mymc;

import com.eternalcode.gitcheck.GitCheck;
import com.eternalcode.gitcheck.GitCheckResult;
import com.eternalcode.gitcheck.git.GitRelease;
import com.eternalcode.gitcheck.git.GitRepository;
import com.eternalcode.gitcheck.git.GitTag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

public class BotVersionChecker {
    private static final Logger logger = Logger.getLogger(BotVersionChecker.class.getName());

    public static void checkVersion(String botName, String currentVersion, boolean checkForUpdates, boolean autoDownloadUpdates) {
        if (!checkForUpdates) {
            return;
        }
        logger.info("Checking for newer version...");
        GitCheck gitCheck = new GitCheck();
        GitRepository repository = GitRepository.of("WieszczY85", botName);

        GitCheckResult result = gitCheck.checkRelease(repository, GitTag.of(currentVersion));

        if (!result.isUpToDate()) {
            GitRelease release = result.getLatestRelease();
            GitTag tag = release.getTag();

            logger.warning("Found newer version of " + botName + ": " + tag.getTag());
            if (autoDownloadUpdates) {
                File oldJar = new File(botName + ".jar");
                File backupJar = new File("_" + botName + ".jar");
                if (oldJar.exists()) {
                    oldJar.renameTo(backupJar);
                }

                File newJar = new File(botName + ".jar");
                try {
                    String jarUrl = "https://github.com/WieszczY85/" + botName + "/releases/download/" + tag.getTag() + "/" + botName + "-" + tag.getTag() + ".jar";
                    URL website = new URL(jarUrl);
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());

                    FileOutputStream fos = new FileOutputStream(newJar);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    rbc.close();

                    logger.info("Starting to download the bot...");
                    logger.info("Downloaded and installed the latest version, which will be used after the next server restart.");

                    // Delete the backup file if the update was successful
                    if (backupJar.exists()) {
                        backupJar.delete();
                    }
                } catch (IOException e) {
                    logger.severe("Failed to download the new version: " + e.getMessage());

                    // Delete the new file and restore the old file if the update failed
                    if (newJar.exists()) {
                        newJar.delete();
                    }
                    if (backupJar.exists()) {
                        backupJar.renameTo(oldJar);
                    }
                }
            } else {
                logger.warning("You can download the latest version from: " + release.getPageUrl());
            }
        } else {
            logger.info("You already have the latest version of " + botName + ": " + currentVersion);
        }
    }
}
