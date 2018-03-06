package com.example.xk.xdhook.backend;

import java.util.Map;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import com.example.xk.xdhook.util.HexDumper;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Main implements IXposedHookLoadPackage
{
    private static final String CONFIG = "//data//local//tmp//config.txt";

    public Class string_to_class(String type)
    {
        switch(type)
        {
            case "int":     return int.class;
            case "short":   return short.class;
            case "long":    return long.class;
            case "float":   return float.class;
            case "double":  return double.class;

            case "byte":    return byte.class;
            case "byte[]":  return byte[].class;

            case "Map":     return Map.class;
            case "char":    return char.class;
            case "String":  return String.class;
            case "boolean": return boolean.class;
            case "String[]":return String[].class;

            case "Context": return Context.class;
            case "Bundle":  return Bundle.class;

            default:        return null;
        }
    }

    public XC_MethodHook callback_fun = new XC_MethodHook()
    {
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
            for (int i = 0; i < param.args.length; i++)
            {
                if(param.args[i].getClass().getSimpleName() == "byte[]")
                {
                    XposedBridge.log("[arg" + i + "]:\r\n" + param.args[i].toString());
                    XposedBridge.log("[arg" + i + "]:\r\n" + HexDumper.toHexString(param.args[i].toString().getBytes()));
                    XposedBridge.log("[arg" + i + "]:\r\n" + HexDumper.dumpHexString(param.args[i].toString().getBytes()));
                    continue;
                }

                XposedBridge.log("[arg" + i + "]:\r\n" + param.args[i].toString());
            }

//            RuntimeException e = new RuntimeException("run is here");
//            e.fillInStackTrace();
//            XposedBridge.log("> StackTrace:\r\n" + e);
        }

        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
//            for (int i = 0; i < param.args.length; i++)
//            {
//                if(param.args[i].getClass().getSimpleName() == "byte[]")
//                {
//                    XposedBridge.log("[arg" + i + "]:\r\n" + param.args[i].toString());
//                    XposedBridge.log("[arg" + i + "]:\r\n" + HexDumper.toHexString(param.args[i].toString().getBytes()));
//                    XposedBridge.log("[arg" + i + "]:\r\n" + HexDumper.dumpHexString(param.args[i].toString().getBytes()));
//                    continue;
//                }
//
//                XposedBridge.log("[arg" + i + "]:\r\n" + param.args[i].toString());
//            }

            XposedBridge.log("[Ret]:" + param.getResult());
        }
    };

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
    {
        if(lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
            return;

        String package_name = null;
        String class_name = null;
        String function_name = null;
        String args_type = null;

        Object[] args_obj = null;

        File file = new File(CONFIG);

        if (file.isDirectory())
        {
            XposedBridge.log("config.txt open failed!");
        }
        else
        {
            try
            {
                InputStream instream = new FileInputStream(file);

                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);

                    String line;

                    while (( line = buffreader.readLine()) != null)
                    {
                        if(line.indexOf("package_name") != -1)
                            package_name = line.substring(line.indexOf(":") + 1);
                        else if(line.indexOf("class_name") != -1)
                            class_name = line.substring(line.indexOf(":") + 1);
                        else if(line.indexOf("function_name") != -1)
                            function_name = line.substring(line.indexOf(":") + 1);
                        else if(line.indexOf("args_type") != -1)
                            args_type = line.substring(line.indexOf(":") + 1);
                    }

                    instream.close();
                }
            }
            catch (Exception e)
            {
                XposedBridge.log(e.getMessage());
            }
        }

        int pos = 0;
        if((args_type != null) && (args_type.length() != 0))
        {
            String args_array[] = args_type.split(",");
            args_obj = new Object[args_array.length + 1];

            for (String tmp:args_array)
            {
                if(string_to_class(tmp) != null)
                    args_obj[pos] = string_to_class(tmp);
                else
                    args_obj[pos] = lpparam.classLoader.loadClass(tmp);
                pos++;
            }
        }
        else
        {
            args_obj = new Object[1];
        }

        args_obj[pos] = callback_fun;

        XposedBridge.log("````````````````````````````````````````````````````````````````````````````````");
        XposedBridge.log("package_name:" + package_name);
        XposedBridge.log("class_name:" + class_name);
        XposedBridge.log("function_name:" + function_name);
        XposedBridge.log("args_type:" + args_type);

        if (!lpparam.packageName.equals(package_name))
            return;

        XposedBridge.log("start hook ~");

        findAndHookMethod(class_name, lpparam.classLoader, function_name, args_obj);
    }
}





