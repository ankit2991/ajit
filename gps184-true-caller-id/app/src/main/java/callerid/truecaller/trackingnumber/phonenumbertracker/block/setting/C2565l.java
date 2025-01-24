package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class C2565l implements FileFilter {
    public boolean accept(File file) {
        return Pattern.matches("cpu[0-9]+", file.getName());
    }
}
