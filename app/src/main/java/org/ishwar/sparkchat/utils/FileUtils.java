/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.utils;

import java.io.File;

public class FileUtils {

    public static boolean deleteDir(File...files){
        for(int i = 0; files != null && i < files.length; ++i){
            if(files[i].isDirectory() && (!deleteDir(files[i].listFiles()) || !files[i].delete())){
                return false;
            }else if(files[i].isFile() && !files[i].delete()){
                return false;
            }
        }
        return true;
    }

}
