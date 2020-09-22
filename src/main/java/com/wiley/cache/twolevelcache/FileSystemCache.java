package com.wiley.cache.twolevelcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(com.wiley.cache.twolevelcache.FileSystemCache.class);

  private final Map<K, String> objectsRepository;
  private Path tempFolder;
  private int repositorySize;

  FileSystemCache(int repositorySize) {
    try {
      this.tempFolder = Files.createTempDirectory("cache");
      this.tempFolder.toFile().deleteOnExit();
    } catch (IOException e) {
      LOG.error(format("Can not create a temp directory. %s", e.getMessage()));
    }
    this.repositorySize = repositorySize;
    this.objectsRepository = new ConcurrentHashMap<>(repositorySize);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized V getFromCache(K key) {
    if (isObjectContained(key)) {
      String fileName = objectsRepository.get(key);
      try (InputStream fileInputStream = new FileInputStream(new File(tempFolder + File.separator + fileName));
           ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
        V objectFromCache = (V) objectInputStream.readObject();
        LOG.debug(format("Get an object with key '%s' from file system cache", key));
        return objectFromCache;
      } catch (ClassNotFoundException | IOException e) {
        LOG.error(format("Can not read a file '%s': %s", fileName, e.getMessage()));
      }
    }
    LOG.debug(format("An object with key '%s' does not exist", key));
    return null;
  }

  @Override
  public synchronized void putIntoCache(K key, V value) {
    File tmpFile = null;
    try {
      tmpFile = Files.createTempFile(tempFolder, "file_", ".cache").toFile();
    } catch (IOException e) {
      LOG.error(format("Can not create a temp file. %s", e.getMessage()));
    }

    if (tmpFile != null) {
      try (OutputStream fileOutputStream = new FileOutputStream(tmpFile);
           ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream)) {
        outputStream.writeObject(value);
        outputStream.flush();
        objectsRepository.put(key, tmpFile.getName());
        LOG.debug(format("Put an object with key '%s' into file system cache", key));
      } catch (IOException e) {
        LOG.error(format("Can not write an object to a file '%s': %s", tmpFile.getName(), e.getMessage()));
      }
    }
  }

  @Override
  public synchronized void deleteFromCache(K key) {
    String fileName = objectsRepository.get(key);
    File fileToDelete = new File(tempFolder + File.separator + fileName);
    if (fileToDelete.delete()) {
      LOG.debug(format("Delete an object with key '%s' and filename '%s' from file system cache", key, fileName));
    } else {
      LOG.debug(format("Can not delete a file '%s'", fileName));
    }
    objectsRepository.remove(key);
  }

  @Override
  public int getCacheSize() {
    return objectsRepository.size();
  }

  @Override
  public boolean isObjectContained(K key) {
    return objectsRepository.containsKey(key);
  }

  @Override
  public boolean hasFreeSpace() {
    return getCacheSize() < repositorySize;
  }

  @Override
  public void clearCache() {
    try {
      Files.walk(tempFolder)
              .filter(Files::isRegularFile)
              .map(Path::toFile)
              .forEach(file -> {
                if (file.delete()) {
                  LOG.debug(format("Delete an object '%s' from file system cache", file.getName()));
                } else {
                  LOG.error(format("Can not delete a file '%s'", file.getName()));
                }
              });
    } catch (IOException e) {
      LOG.error(format("Can not access the cache directory. %s", e.getMessage()));
    }
    objectsRepository.clear();
  }
}
