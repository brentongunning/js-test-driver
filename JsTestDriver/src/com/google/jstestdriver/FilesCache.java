/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class FilesCache {


  // TODO(corysmith): replace this with a synchronized collection
  // When the map semantics are clean.
  private final Map<String, FileInfo> files;

  /**
   * @param files Files is expected to be an ordered map. Seriously.
   */
  public FilesCache(Map<String, FileInfo> files) {
    this.files = files;
  }

  public synchronized String getFileContent(String fileName) {
    FileInfo info = files.get(fileName);
    if (info == null) {
      throw new MissingFileException();
    }
    return info.getData();
  }
  
  public synchronized byte[] getFileRawData(String fileName) {
    FileInfo info = files.get(fileName);
    if (info == null) {
      throw new MissingFileException();
    }
    return info.getRawData();
  }

  public synchronized void clear() {
    files.clear();
  }

  public synchronized void addFile(FileInfo fileInfo) {
    files.put(fileInfo.getDisplayPath(), fileInfo);
  }

  public int getFilesNumber() {
    return files.size();
  }

  public synchronized Set<String> getAllFileNames() {
    return files.keySet();
  }
  
  /**
   * Returns all files in order.
   */
  public synchronized Collection<FileInfo> getAllFileInfos() {
    return files.values();
  }

  @Override
  public String toString() {
    return "FilesCache [files=" + files + "]";
  }
  
  public static class MissingFileException extends RuntimeException {
    private static final long serialVersionUID = -552403239267162285L;
  }

  public FileInfo getFile(String filePath) {
    return files.get(filePath);
  }
}
