package com.github.SnowFlakes.unit;

import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by snowf on 2019/2/17.
 */
public class Parameter {

    public static boolean DependenceCheck() {
        boolean Satisfied = true;
        return Satisfied;
    }

    public static int GetInt(Object o, int d) {
        if (o != null) {
            try {
                int i = Integer.parseInt(o.toString().trim());
                if (i == 0) {
                    return d;
                } else {
                    return i;
                }
            } catch (NumberFormatException e) {
                return d;
            }
        }
        return d;
    }

    public static int[] GetInts(Object o, int[] d) {
        if (o != null) {
            try {
                int[] i = StringArrays.toInteger(o.toString().trim().split("\\s+"));
                if (i.length == 0) {
                    return d;
                } else {
                    return i;
                }
            } catch (NumberFormatException e) {
                return d;
            }
        }
        return d;
    }

    public static String GetStringOpt(CommandLine commandLine, String opt_string, String default_string) {
        return commandLine.hasOption(opt_string) ? commandLine.getOptionValue(opt_string) : default_string;
    }

    public static File GetFileOpt(CommandLine commandLine, String opt_string, File default_file) {
        return commandLine.hasOption(opt_string) ? new File(commandLine.getOptionValue(opt_string)) : default_file;
    }

    public static int GetIntOpt(CommandLine commandLine, String opt_string, int default_int) {
        return commandLine.hasOption(opt_string) ? Integer.parseInt(commandLine.getOptionValue(opt_string))
                : default_int;
    }

    public static float GetFloatOpt(CommandLine commandLine, String opt_string, float default_float) {
        return commandLine.hasOption(opt_string) ? Float.parseFloat(commandLine.getOptionValue(opt_string))
                : default_float;
    }

    public static String[] GetStringOpts(CommandLine commandLine, String opt_string, String[] default_string) {
        return commandLine.hasOption(opt_string) ? commandLine.getOptionValues(opt_string) : default_string;
    }

    public static File[] GetFileOpts(CommandLine commandLine, String opt_string, File[] default_file) {
        if (commandLine.hasOption(opt_string)) {
            return StringArrays.toFile(commandLine.getOptionValues(opt_string));
        } else {
            return default_file;
        }
    }

    public static int[] GetIntOpts(CommandLine commandLine, String opt_string, int[] default_string) {
        if (commandLine.hasOption(opt_string)) {
            return StringArrays.toInteger(commandLine.getOptionValues(opt_string));
        } else {
            return default_string;
        }
    }

}
