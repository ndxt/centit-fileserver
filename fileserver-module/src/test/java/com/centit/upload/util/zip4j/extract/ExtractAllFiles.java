/*
* Copyright 2010 Srikanth Reddy Lingala  
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
* http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, 
* software distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License. 
*/

package com.centit.upload.util.zip4j.extract;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates extracting all files from a zip file
 * 
 * @author Srikanth Reddy Lingala
 *
 */
public class ExtractAllFiles {

    private Logger logger = LoggerFactory.getLogger(ExtractAllFiles.class);

    public ExtractAllFiles() {

        try {
            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile("c:\\ZipTest\\ExtractAllFiles.zip");

            // Extracts all files to the path specified
            zipFile.extractAll("c:\\ZipTest");

        } catch (ZipException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ExtractAllFiles();
    }

}
