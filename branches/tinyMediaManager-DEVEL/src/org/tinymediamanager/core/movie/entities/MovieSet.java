/*
 * Copyright 2012 - 2014 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class MovieSet. This class is used to represent a movie set (which means a "collection" of n movies)
 * 
 * @author Manuel Laggner
 */
@Entity
public class MovieSet extends MediaEntity {

  private static final Logger            LOGGER               = LoggerFactory.getLogger(MovieSet.class);
  private static final Comparator<Movie> MOVIE_SET_COMPARATOR = new MovieInMovieSetComparator();

  @OneToMany(fetch = FetchType.EAGER)
  private List<Movie>                    movies               = new ArrayList<Movie>(0);

  @Transient
  private String                         titleSortable        = "";

  static {
    mediaFileComparator = new MovieMediaFileComparator();
  }

  /**
   * Instantiates a new movieset. To initialize the propertychangesupport after loading
   */
  public MovieSet() {
  }

  @Override
  public void setTitle(String newValue) {
    super.setTitle(newValue);

    synchronized (movies) {
      for (Movie movie : movies) {
        movie.movieSetTitleChanged();
      }
    }
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Terminator Collection" -> "Terminator Collection, The".
   * 
   * @return the title in its sortable format
   */
  public String getTitleSortable() {
    if (StringUtils.isEmpty(titleSortable)) {
      titleSortable = Utils.getSortableName(this.getTitle());
    }
    return titleSortable;
  }

  public MovieSet(String title) {
    setTitle(title);
  }

  public int getTmdbId() {
    int id = 0;
    try {
      id = (Integer) ids.get(TMDBID);
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put(TMDBID, newValue);
    firePropertyChange(TMDBID, oldValue, newValue);
  }

  @Override
  public void setPosterUrl(String newValue) {
    super.setPosterUrl(newValue);
    boolean written = false;
    String posterFilename = "movieset-poster.jpg";

    // write new poster
    writeImageToMovieFolder(movies, posterFilename, posterUrl);
    if (movies.size() > 0) {
      written = true;
    }

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, false);
      written = true;
    }

    if (written) {
      firePropertyChange(POSTER, false, true);
    }
    else {
      // at least cache it
      if (StringUtils.isNotEmpty(posterUrl) && movies.size() == 0) {
        ImageFetcher task = new ImageFetcher("poster", posterUrl);
        TmmTaskManager.getInstance().addImageDownloadTask(task);
      }
    }

  }

  @Override
  public void setFanartUrl(String newValue) {
    super.setFanartUrl(newValue);
    boolean written = false;
    String fanartFilename = "movieset-fanart.jpg";

    // write new fanart
    writeImageToMovieFolder(movies, fanartFilename, fanartUrl);
    if (movies.size() > 0) {
      written = true;
    }

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(false, true);
      written = true;
    }

    if (written) {
      firePropertyChange(FANART, false, true);
    }
    else {
      // at least cache it
      if (StringUtils.isNotEmpty(fanartUrl) && movies.size() == 0) {
        ImageFetcher task = new ImageFetcher("fanart", fanartUrl);
        TmmTaskManager.getInstance().addImageDownloadTask(task);
      }
    }
  }

  @Override
  public String getFanart() {
    String fanart = "";

    // try to get from the artwork folder if enabled
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()) {
      String filename = Globals.settings.getMovieSettings().getMovieSetArtworkFolder() + File.separator + getTitle() + "-fanart.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

    // try to get a fanart from one movie
    List<Movie> movies = new ArrayList<Movie>(this.movies);
    for (Movie movie : movies) {
      String filename = movie.getPath() + File.separator + "movieset-fanart.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(fanartUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    return fanart;
  }

  @Override
  public String getPoster() {
    String poster = "";

    // try to get from the artwork folder if enabled
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()) {
      String filename = Globals.settings.getMovieSettings().getMovieSetArtworkFolder() + File.separator + getTitle() + "-poster.jpg";
      File posterFile = new File(filename);
      if (posterFile.exists()) {
        return filename;
      }
      filename = Globals.settings.getMovieSettings().getMovieSetArtworkFolder() + File.separator + getTitle() + "-folder.jpg";
      posterFile = new File(filename);
      if (posterFile.exists()) {
        return filename;
      }
    }

    // try to get a fanart from one movie
    List<Movie> movies = new ArrayList<Movie>(this.movies);
    for (Movie movie : movies) {
      String filename = movie.getPath() + File.separator + "movieset-poster.jpg";
      File posterFile = new File(filename);
      if (posterFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(posterUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    return poster;
  }

  /**
   * Adds the movie to the end of the list
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    synchronized (movies) {
      if (movies.contains(movie)) {
        return;
      }
      movies.add(movie);
      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImageToMovieFolder(movies, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(movies, "movieset-poster.jpg", posterUrl);

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, movies);
  }

  /**
   * Inserts the movie into the right position of the list
   * 
   * @param movie
   */
  public void insertMovie(Movie movie) {
    synchronized (movies) {
      if (movies.contains(movie)) {
        return;
      }

      int index = Collections.binarySearch(movies, movie, MOVIE_SET_COMPARATOR);
      if (index < 0) {
        movies.add(-index - 1, movie);
      }
      else if (index >= 0) {
        movies.add(index, movie);
      }

      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImageToMovieFolder(movies, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(movies, "movieset-poster.jpg", posterUrl);

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, movies);
  }

  /**
   * Removes the movie from the list.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    // remove images from movie folder
    File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }
    imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }
    if (movie.getMovieSet() != null) {
      movie.setMovieSet(null);
      movie.saveToDb();
    }

    synchronized (movies) {
      movies.remove(movie);
      saveToDb();
    }

    firePropertyChange("movies", null, movies);
    firePropertyChange("removedMovie", null, movie);
  }

  public List<Movie> getMovies() {
    return movies;
  }

  /**
   * Sort movies inside this movie set by using either the sort title, release date or year.
   */
  public void sortMovies() {
    synchronized (movies) {
      Collections.sort(movies, MOVIE_SET_COMPARATOR);
    }
    firePropertyChange("movies", null, movies);
  }

  /**
   * Removes the all movies from this movie set.
   */
  public void removeAllMovies() {
    // store all old movies to remove the nodes in the tree
    List<Movie> oldValue = new ArrayList<Movie>(movies);
    // remove images from movie folder
    synchronized (movies) {
      for (Movie movie : movies) {
        File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
        if (imageFile.exists()) {
          imageFile.delete();
        }
        imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
        if (imageFile.exists()) {
          imageFile.delete();
        }

        if (movie.getMovieSet() != null) {
          movie.setMovieSet(null);
          movie.saveToDb();
        }
      }
      movies.clear();
      saveToDb();
    }

    firePropertyChange("movies", null, movies);
    firePropertyChange("removedAllMovies", oldValue, movies);
  }

  /**
   * toString. used for JComboBox in movie editor
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getTitle();
  }

  public int getMovieIndex(Movie movie) {
    return movies.indexOf(movie);
  }

  private void writeImageToMovieFolder(List<Movie> movies, String filename, String url) {
    // check for empty strings or movies
    if (movies == null || movies.size() == 0 || StringUtils.isEmpty(filename) || StringUtils.isEmpty(url)) {
      return;
    }

    // write image for all movies
    for (Movie movie : movies) {
      try {
        if (!movie.isMultiMovieDir()) {
          writeImage(url, movie.getPath() + File.separator + filename);
        }
      }
      catch (InterruptedException e) {
        LOGGER.warn("interrupted image download");
      }
      catch (IOException e) {
        LOGGER.warn("could not write files", e);
      }
    }
  }

  public void rewriteAllImages() {
    writeImageToMovieFolder(movies, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(movies, "movieset-poster.jpg", posterUrl);

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, true);
    }
  }

  private void writeImagesToArtworkFolder(boolean poster, boolean fanart) {
    // write images to artwork folder
    File artworkFolder = new File(Globals.settings.getMovieSettings().getMovieSetArtworkFolder());

    // check if folder exists
    if (!artworkFolder.exists()) {
      artworkFolder.mkdirs();
    }

    // write files
    try {
      // poster
      if (poster && StringUtils.isNotBlank(posterUrl)) {
        String providedFiletype = FilenameUtils.getExtension(posterUrl);
        writeImage(posterUrl, artworkFolder.getPath() + File.separator + getTitle() + "-folder." + providedFiletype);
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted image download");
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }

    try {
      // fanart
      if (fanart && StringUtils.isNotBlank(fanartUrl)) {
        String providedFiletype = FilenameUtils.getExtension(fanartUrl);
        writeImage(fanartUrl, artworkFolder.getPath() + File.separator + getTitle() + "-fanart." + providedFiletype);
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted image download");
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }
  }

  private void writeImage(String url, String pathAndFilename) throws IOException, InterruptedException {
    Url url1 = new Url(url);
    FileOutputStream outputStream = new FileOutputStream(pathAndFilename);
    InputStream is = url1.getInputStream();
    IOUtils.copy(is, outputStream);
    outputStream.flush();
    try {
      outputStream.getFD().sync(); // wait until file has been completely
                                   // written
    }
    catch (Exception e) {
      // empty here -> just not let the thread crash
    }
    outputStream.close();
    is.close();

    ImageCache.invalidateCachedImage(pathAndFilename);
  }

  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(getPoster()) && !StringUtils.isEmpty(getFanart())) {
      return true;
    }
    return false;
  }

  public List<File> getImagesToCache() {
    // get files to cache
    List<File> filesToCache = new ArrayList<File>();

    if (StringUtils.isNotBlank(getPoster())) {
      filesToCache.add(new File(getPoster()));
    }

    if (StringUtils.isNotBlank(getFanart())) {
      filesToCache.add(new File(getFanart()));
    }

    return filesToCache;
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  @Override
  public void saveToDb() {
    // update/insert this movie set to the database
    final EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
    synchronized (entityManager) {
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
        entityManager.persist(this);
        entityManager.getTransaction().commit();
      }
      else {
        entityManager.persist(this);
      }
    }
  }

  @Override
  public void deleteFromDb() {
    // delete this movie set from the database
    final EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
    synchronized (entityManager) {
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
        entityManager.remove(this);
        entityManager.getTransaction().commit();
      }
      else {
        entityManager.remove(this);
      }
    }
  }

  /**
   * recalculate all movie sorttitles
   */
  public void updateMovieSorttitle() {
    for (Movie movie : new ArrayList<Movie>(movies)) {
      movie.setSortTitleFromMovieSet();
      movie.saveToDb();
      movie.writeNFO();
    }
  }

  /**
   * clean movies from this movieset if there are any inconsistances
   */
  public void cleanMovieSet() {
    MovieList movieList = MovieList.getInstance();
    boolean dirty = false;

    for (Movie movie : new ArrayList<Movie>(movies)) {
      if (!movieList.getMovies().contains(movie)) {
        movies.remove(movie);
        dirty = true;
      }
    }

    if (dirty) {
      saveToDb();
    }
  }

  /*******************************************************************************
   * helper classses
   *******************************************************************************/
  private class ImageFetcher implements Runnable {
    private String propertyName = "";
    private String imageUrl     = "";

    public ImageFetcher(String propertyName, String url) {
      this.propertyName = propertyName;
      this.imageUrl = url;
    }

    @Override
    public void run() {
      String filename = ImageCache.getCachedFileName(imageUrl);
      File outputFile = new File(ImageCache.getCacheDir(), filename + ".jpg");

      try {
        Url url = new Url(imageUrl);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely
                                       // written
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();
        is.close();

        firePropertyChange(propertyName, "", outputFile);
      }
      catch (InterruptedException e) {
        LOGGER.warn("interrupted image download");
      }
      catch (IOException e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }
  }

  private static class MovieInMovieSetComparator implements Comparator<Movie> {
    @Override
    public int compare(Movie o1, Movie o2) {
      Collator collator = null;

      if (o1 == null || o2 == null) {
        return 0;
      }

      // sort with sorttitle if available
      if (StringUtils.isNotBlank(o1.getSortTitle()) && StringUtils.isNotBlank(o2.getSortTitle())) {
        collator = Collator.getInstance();
        return collator.compare(o1.getSortTitle(), o2.getSortTitle());
      }

      // sort with release date if available
      if (o1.getReleaseDate() != null && o2.getReleaseDate() != null) {
        return o1.getReleaseDate().compareTo(o2.getReleaseDate());
      }

      // sort with year if available
      if (StringUtils.isNotBlank(o1.getYear()) && StringUtils.isNotBlank(o2.getYear())) {
        try {
          int year1 = Integer.parseInt(o1.getYear());
          int year2 = Integer.parseInt(o2.getYear());
          return year1 - year2;
        }
        catch (Exception e) {
        }
      }

      // fallback
      return 0;
    }
  }
}
