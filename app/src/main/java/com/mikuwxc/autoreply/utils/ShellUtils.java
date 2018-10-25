package com.mikuwxc.autoreply.utils;

import com.mikuwxc.autoreply.wcutil.OtherUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public class ShellUtils {
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_SU = "su";

    public static class CommandResult {
        public String errorMsg;
        public int result;
        public String successMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

    public static boolean upgradeRootPermission() {
        Throwable th;
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "touch /data/roottest.txt";
            process = Runtime.getRuntime().exec("su");
            DataOutputStream os2 = new DataOutputStream(process.getOutputStream());
            try {
                os2.writeBytes(cmd + "/n");
                os2.writeBytes("exit/n");
                os2.flush();
                if (os2 != null) {
                    try {
                        os2.close();
                    } catch (Exception e) {
                    }
                }
                process.destroy();
                os = os2;
                return true;
            } catch (Exception e2) {
                os = os2;
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e3) {
                        return false;
                    }
                }
                process.destroy();
                return false;
            } catch (Throwable th2) {
                th = th2;
                os = os2;
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e4) {
                        throw th;
                    }
                }
                process.destroy();
                throw th;
            }
        } catch (Exception e5) {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e32) {
                    return false;
                }
            }
            process.destroy();
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e42) {
                }
            }
            process.destroy();
        }

        return false;
    }

    public static boolean checkRootPermission() throws IOException {
        return execCommand("echo root", true, false).result == 0;
    }

    public static CommandResult execCommand(String command, boolean isRoot) throws IOException {
        return execCommand(new String[]{command}, isRoot, true);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot) throws IOException {
        String[] strArr;
        if (commands == null) {
            strArr = null;
        } else {
            strArr = (String[]) commands.toArray(new String[0]);
        }
        return execCommand(strArr, isRoot, true);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot) throws IOException {
        return execCommand(commands, isRoot, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) throws IOException {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) throws IOException {
        String[] strArr;
        if (commands == null) {
            strArr = null;
        } else {
            strArr = (String[]) commands.toArray(new String[0]);
        }
        return execCommand(strArr, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) throws IOException {
        IOException e;
        String stringBuilder;
        String stringBuilder2;
        Throwable th;
        Exception e2;
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(-1, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            DataOutputStream os2 = new DataOutputStream(process.getOutputStream());
            try {
                for (String command : commands) {
                    if (command != null) {
                        os2.write(command.getBytes());
                        os2.writeBytes("\n");
                        os2.flush();
                    }
                }
                os2.writeBytes("exit\n");
                os2.flush();
                result = process.waitFor();
                if (isNeedResultMsg) {
                    StringBuilder successMsg2 = new StringBuilder();
                    try {
                        BufferedReader successResult2;
                        StringBuilder errorMsg2 = new StringBuilder();
                        try {
                            successResult2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        } catch (Exception e5) {
                            e2 = e5;
                            os = os2;
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            e2.printStackTrace();
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e422) {
                                    e422.printStackTrace();
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    if (successMsg != null) {
                                        stringBuilder = successMsg.toString();
                                    } else {
                                        stringBuilder = null;
                                    }
                                    if (errorMsg != null) {
                                        stringBuilder2 = errorMsg.toString();
                                    } else {
                                        stringBuilder2 = null;
                                    }
                                    return new CommandResult(result, stringBuilder, stringBuilder2);
                                }
                            }
                            if (successResult != null) {
                                successResult.close();
                            }
                            if (errorResult != null) {
                                errorResult.close();
                            }
                            if (process != null) {
                                process.destroy();
                            }
                            if (successMsg != null) {
                                stringBuilder = successMsg.toString();
                            } else {
                                stringBuilder = null;
                            }
                            if (errorMsg != null) {
                                stringBuilder2 = errorMsg.toString();
                            } else {
                                stringBuilder2 = null;
                            }
                            return new CommandResult(result, stringBuilder, stringBuilder2);
                        } catch (Throwable th3) {
                            th = th3;
                            os = os2;
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e4222) {
                                    e4222.printStackTrace();
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    throw th;
                                }
                            }
                            if (successResult != null) {
                                successResult.close();
                            }
                            if (errorResult != null) {
                                errorResult.close();
                            }
                            if (process != null) {
                                process.destroy();
                            }
                            throw th;
                        }
                        try {
                            String s;
                            BufferedReader errorResult2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            while (true) {
                                try {
                                    s = successResult2.readLine();
                                    if (s == null) {
                                        break;
                                    }
                                    successMsg2.append(s);
                                } catch (IOException e6) {
                                    os = os2;
                                    errorMsg = errorMsg2;
                                    successMsg = successMsg2;
                                    errorResult = errorResult2;
                                    successResult = successResult2;
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (IOException e42222) {
                                            e42222.printStackTrace();
                                            if (process != null) {
                                                process.destroy();
                                            }
                                            if (successMsg != null) {
                                                stringBuilder = successMsg.toString();
                                            } else {
                                                stringBuilder = null;
                                            }
                                            if (errorMsg != null) {
                                                stringBuilder2 = errorMsg.toString();
                                            } else {
                                                stringBuilder2 = null;
                                            }
                                            return new CommandResult(result, stringBuilder, stringBuilder2);
                                        }
                                    }
                                    if (successResult != null) {
                                        successResult.close();
                                    }
                                    if (errorResult != null) {
                                        errorResult.close();
                                    }
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    if (successMsg != null) {
                                        stringBuilder = null;
                                    } else {
                                        stringBuilder = successMsg.toString();
                                    }
                                    if (errorMsg != null) {
                                        stringBuilder2 = null;
                                    } else {
                                        stringBuilder2 = errorMsg.toString();
                                    }
                                    return new CommandResult(result, stringBuilder, stringBuilder2);
                                } catch (Exception e7) {
                                    e2 = e7;
                                    os = os2;
                                    errorMsg = errorMsg2;
                                    successMsg = successMsg2;
                                    errorResult = errorResult2;
                                    successResult = successResult2;
                                    e2.printStackTrace();
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (IOException e422222) {
                                            e422222.printStackTrace();
                                            if (process != null) {
                                                process.destroy();
                                            }
                                            if (successMsg != null) {
                                                stringBuilder = successMsg.toString();
                                            } else {
                                                stringBuilder = null;
                                            }
                                            if (errorMsg != null) {
                                                stringBuilder2 = errorMsg.toString();
                                            } else {
                                                stringBuilder2 = null;
                                            }
                                            return new CommandResult(result, stringBuilder, stringBuilder2);
                                        }
                                    }
                                    if (successResult != null) {
                                        successResult.close();
                                    }
                                    if (errorResult != null) {
                                        errorResult.close();
                                    }
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    if (successMsg != null) {
                                        stringBuilder = successMsg.toString();
                                    } else {
                                        stringBuilder = null;
                                    }
                                    if (errorMsg != null) {
                                        stringBuilder2 = errorMsg.toString();
                                    } else {
                                        stringBuilder2 = null;
                                    }
                                    return new CommandResult(result, stringBuilder, stringBuilder2);
                                } catch (Throwable th4) {
                                    th = th4;
                                    os = os2;
                                    errorMsg = errorMsg2;
                                    successMsg = successMsg2;
                                    errorResult = errorResult2;
                                    successResult = successResult2;
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (IOException e4222222) {
                                            e4222222.printStackTrace();
                                            if (process != null) {
                                                process.destroy();
                                            }
                                            throw th;
                                        }
                                    }
                                    if (successResult != null) {
                                        successResult.close();
                                    }
                                    if (errorResult != null) {
                                        errorResult.close();
                                    }
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    throw th;
                                }
                            }
                            while (true) {
                                s = errorResult2.readLine();
                                if (s == null) {
                                    break;
                                }
                                errorMsg2.append(s);
                            }
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            errorResult = errorResult2;
                            successResult = successResult2;
                        } catch (IOException e8) {
                            os = os2;
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            successResult = successResult2;
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e42222222) {
                                    e42222222.printStackTrace();
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    if (successMsg != null) {
                                        stringBuilder = successMsg.toString();
                                    } else {
                                        stringBuilder = null;
                                    }
                                    if (errorMsg != null) {
                                        stringBuilder2 = errorMsg.toString();
                                    } else {
                                        stringBuilder2 = null;
                                    }
                                    return new CommandResult(result, stringBuilder, stringBuilder2);
                                }
                            }
                            if (successResult != null) {
                                successResult.close();
                            }
                            if (errorResult != null) {
                                errorResult.close();
                            }
                            if (process != null) {
                                process.destroy();
                            }
                            if (successMsg != null) {
                                stringBuilder = successMsg.toString();
                            } else {
                                stringBuilder = null;
                            }
                            if (errorMsg != null) {
                                stringBuilder2 = errorMsg.toString();
                            } else {
                                stringBuilder2 = null;
                            }
                            return new CommandResult(result, stringBuilder, stringBuilder2);
                        } catch (Exception e9) {
                            e2 = e9;
                            os = os2;
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            successResult = successResult2;
                            e2.printStackTrace();
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e422222222) {
                                    e422222222.printStackTrace();
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    if (successMsg != null) {
                                        stringBuilder = successMsg.toString();
                                    } else {
                                        stringBuilder = null;
                                    }
                                    if (errorMsg != null) {
                                        stringBuilder2 = errorMsg.toString();
                                    } else {
                                        stringBuilder2 = null;
                                    }
                                    return new CommandResult(result, stringBuilder, stringBuilder2);
                                }
                            }
                            if (successResult != null) {
                                successResult.close();
                            }
                            if (errorResult != null) {
                                errorResult.close();
                            }
                            if (process != null) {
                                process.destroy();
                            }
                            if (successMsg != null) {
                                stringBuilder = null;
                            } else {
                                stringBuilder = successMsg.toString();
                            }
                            if (errorMsg != null) {
                                stringBuilder2 = null;
                            } else {
                                stringBuilder2 = errorMsg.toString();
                            }
                            return new CommandResult(result, stringBuilder, stringBuilder2);
                        } catch (Throwable th5) {
                            th = th5;
                            os = os2;
                            errorMsg = errorMsg2;
                            successMsg = successMsg2;
                            successResult = successResult2;
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e4222222222) {
                                    e4222222222.printStackTrace();
                                    if (process != null) {
                                        process.destroy();
                                    }
                                    throw th;
                                }
                            }
                            if (successResult != null) {
                                successResult.close();
                            }
                            if (errorResult != null) {
                                errorResult.close();
                            }
                            if (process != null) {
                                process.destroy();
                            }
                            throw th;
                        }
                    } catch (IOException e10) {
                        os = os2;
                        successMsg = successMsg2;
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e42222222222) {
                                e42222222222.printStackTrace();
                                if (process != null) {
                                    process.destroy();
                                }
                                if (successMsg != null) {
                                    stringBuilder = successMsg.toString();
                                } else {
                                    stringBuilder = null;
                                }
                                if (errorMsg != null) {
                                    stringBuilder2 = errorMsg.toString();
                                } else {
                                    stringBuilder2 = null;
                                }
                                return new CommandResult(result, stringBuilder, stringBuilder2);
                            }
                        }
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                        if (process != null) {
                            process.destroy();
                        }
                        if (successMsg != null) {
                            stringBuilder = successMsg.toString();
                        } else {
                            stringBuilder = null;
                        }
                        if (errorMsg != null) {
                            stringBuilder2 = errorMsg.toString();
                        } else {
                            stringBuilder2 = null;
                        }
                        return new CommandResult(result, stringBuilder, stringBuilder2);
                    } catch (Exception e11) {
                        e2 = e11;
                        os = os2;
                        successMsg = successMsg2;
                        e2.printStackTrace();
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e422222222222) {
                                e422222222222.printStackTrace();
                                if (process != null) {
                                    process.destroy();
                                }
                                if (successMsg != null) {
                                    stringBuilder = successMsg.toString();
                                } else {
                                    stringBuilder = null;
                                }
                                if (errorMsg != null) {
                                    stringBuilder2 = errorMsg.toString();
                                } else {
                                    stringBuilder2 = null;
                                }
                                return new CommandResult(result, stringBuilder, stringBuilder2);
                            }
                        }
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                        if (process != null) {
                            process.destroy();
                        }
                        if (successMsg != null) {
                            stringBuilder = null;
                        } else {
                            stringBuilder = successMsg.toString();
                        }
                        if (errorMsg != null) {
                            stringBuilder2 = null;
                        } else {
                            stringBuilder2 = errorMsg.toString();
                        }
                        return new CommandResult(result, stringBuilder, stringBuilder2);
                    } catch (Throwable th6) {
                        th = th6;
                        os = os2;
                        successMsg = successMsg2;
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e4222222222222) {
                                e4222222222222.printStackTrace();
                                if (process != null) {
                                    process.destroy();
                                }
                                throw th;
                            }
                        }
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                        if (process != null) {
                            process.destroy();
                        }
                        throw th;
                    }
                }
                if (os2 != null) {
                    try {
                        os2.close();
                    } catch (IOException e42222222222222) {
                        e42222222222222.printStackTrace();
                    }
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
                if (process != null) {
                    process.destroy();
                    os = os2;
                }
            } catch (IOException e12) {
                os = os2;
            } catch (Exception e13) {
                e2 = e13;
                os = os2;
            } catch (Throwable th7) {
                th = th7;
                os = os2;
            }
        } catch (IOException e14) {
        } catch (Exception e15) {
            e2 = e15;
            e2.printStackTrace();
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e422222222222222) {
                    e422222222222222.printStackTrace();
                    if (process != null) {
                        process.destroy();
                    }
                    if (successMsg != null) {
                        stringBuilder = successMsg.toString();
                    } else {
                        stringBuilder = null;
                    }
                    if (errorMsg != null) {
                        stringBuilder2 = errorMsg.toString();
                    } else {
                        stringBuilder2 = null;
                    }
                    return new CommandResult(result, stringBuilder, stringBuilder2);
                }
            }
            if (successResult != null) {
                successResult.close();
            }
            if (errorResult != null) {
                errorResult.close();
            }
            if (process != null) {
                process.destroy();
            }
            if (successMsg != null) {
                stringBuilder = null;
            } else {
                stringBuilder = successMsg.toString();
            }
            if (errorMsg != null) {
                stringBuilder2 = null;
            } else {
                stringBuilder2 = errorMsg.toString();
            }
            return new CommandResult(result, stringBuilder, stringBuilder2);
        }
        if (successMsg != null) {
            stringBuilder = null;
        } else {
            stringBuilder = successMsg.toString();
        }
        if (errorMsg != null) {
            stringBuilder2 = null;
        } else {
            stringBuilder2 = errorMsg.toString();
        }
        return new CommandResult(result, stringBuilder, stringBuilder2);
    }

    public static void stopApp(String pkgName) throws IOException {
        execCommand("am force-stop " + pkgName, true);
    }

    public static void frozenApp(String pkgName) throws IOException {
        execCommand("pm disable " + pkgName, true);
    }

    public static void unFrozenApp(String pkgName) throws IOException {
        execCommand("pm enable " + pkgName, true);
    }

    public static void unFrozenAllApp() throws IOException {
        int i = 0;
        String successMsg = execCommand("pm list packages -d", true).successMsg;
        String[] packages;
        int length;
        if (OtherUtils.isEmpty(successMsg)) {
            successMsg = execCommand("pm list packages -d", false).successMsg;
            if (!OtherUtils.isEmpty(successMsg) && successMsg.contains("package:")) {
                packages = successMsg.substring(successMsg.indexOf("package:") + "package:".length()).split("package:");
                length = packages.length;
                while (i < length) {
                    unFrozenApp(packages[i]);
                    i++;
                }
            }
        } else if (successMsg.contains("package:")) {
            packages = successMsg.substring(successMsg.indexOf("package:") + "package:".length()).split("package:");
            length = packages.length;
            while (i < length) {
                unFrozenApp(packages[i]);
                i++;
            }
        }
    }

    public static void killPid(String processName) throws IOException {
        String pid = getPid(processName);
        if (!"0".equals(pid)) {
            execCommand("kill " + pid, true);
        }
    }

    public static String getPid(String processName) throws IOException {
        String successMsg = execCommand("ps | grep " + processName, true).successMsg;
        if (OtherUtils.isEmpty(successMsg)) {
            return "0";
        }
        return Pattern.compile("\\s+").matcher(successMsg).replaceAll(" ").split(" ")[1];
    }

    public static void closeFireWalls() throws IOException {
        execCommand("setenforce 0", true);
    }

    public static void chmod777(String path) throws IOException {
        execCommand("chmod -R 777 " + path, true);
    }

    public static void chmod000(String path) throws IOException {
        execCommand("chmod -R 000 " + path, true);
    }
}
