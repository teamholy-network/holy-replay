package de.teamholy.replay.filesystem.saving;


import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.data.ReplayData;
import de.teamholy.replay.utils.LogUtils;
import de.teamholy.replay.utils.fetcher.Acceptor;
import de.teamholy.replay.utils.fetcher.Consumer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

// TODO: Use NIO for better performance
public class DefaultReplaySaver implements IReplaySaver {

    public final static File DIR = new File(ReplaySystem.getInstance().getDataFolder() + "/replays/");

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private boolean reformatting;

    private ExecutorService pool = Executors.newCachedThreadPool();

    public static boolean isValidName(String replayName) {
        return NAME_PATTERN.matcher(replayName).matches();
    }

    @Override
    public void saveReplay(Replay replay) {

        if (!DIR.exists()) DIR.mkdirs();

        File file = new File(DIR, replay.getId() + ".replay");


        try {
            if (!file.exists()) file.createNewFile();


            try (FileOutputStream fileOut = new FileOutputStream(file);
                 GZIPOutputStream gOut = new GZIPOutputStream(fileOut);
                 ObjectOutputStream objectOut = new ObjectOutputStream(gOut)) {

                objectOut.writeObject(replay.getData());
                objectOut.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void loadReplay(String replayName, Consumer<Replay> consumer) {

        this.pool.execute(new Acceptor<Replay>(consumer) {

            @Override
            public Replay getValue() {

                File file = new File(DIR, replayName + ".replay");

                try (FileInputStream fileIn = new FileInputStream(file);
                     GZIPInputStream gIn = new GZIPInputStream(fileIn);
                     ObjectInputStream objectIn = new ObjectInputStream(gIn)) {

                    ReplayData data = (ReplayData) objectIn.readObject();

                    return new Replay(replayName, data);

                } catch (ClassNotFoundException | IOException e) {
                    if (!reformatting) e.printStackTrace();
                }

                return null;
            }
        });
    }

    @Override
    public boolean replayExists(String replayName) {
        if (!isValidName(replayName)) return false;

        File file = new File(DIR, replayName + ".replay");

        return file.exists();
    }

    @Override
    public void deleteReplay(String replayName) {
        File file = new File(DIR, replayName + ".replay");

        if (file.exists()) file.delete();
    }

    public void reformatAll() {
        this.reformatting = true;
        if (DIR.exists()) {
            Arrays.stream(DIR.listFiles())
                    .filter(file -> (file.isFile() && file.getName().endsWith(".replay")))
                    .map(File::getName)
                    .collect(Collectors.toList())
                    .forEach(file -> reformat(file.replaceAll("\\.replay", "")));
        }

        this.reformatting = false;
    }

    private void reformat(String replayName) {
        loadReplay(replayName, old -> {

            if (old == null) {
                LogUtils.log("Reformatting: " + replayName);

                try {
                    File file = new File(DIR, replayName + ".replay");

                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                    ReplayData data = (ReplayData) objectIn.readObject();

                    objectIn.close();
                    fileIn.close();

                    deleteReplay(replayName);
                    saveReplay(new Replay(replayName, data));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    public List<String> getReplays() {
        List<String> files = new ArrayList<>();

        if (DIR.exists()) {
            for (File file : DIR.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".replay")) {
                    files.add(file.getName().replaceAll("\\.replay", ""));
                }
            }
        }
        return files;
    }

}
