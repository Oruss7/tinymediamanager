/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.hdtrailersnet.HDTrailersNet;
import org.tinymediamanager.scraper.imdb.ImdbMetadataProvider;
import org.tinymediamanager.scraper.ofdb.OfdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.zelluloid.ZelluloidMetadataProvider;
import org.tinymediamanager.ui.moviesets.MovieSetTreeModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

/**
 * The Class MovieList.
 */
public class MovieList extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger          LOGGER         = Logger.getLogger(MovieList.class);

  /** The instance. */
  private static MovieList             instance;

  /** The settings. */
  private final Settings               settings       = Settings.getInstance();

  /** The movie list. */
  private ObservableElementList<Movie> movieList;

  /** The movie set list. */
  private List<MovieSet>               movieSetList;

  /** The tag listener. */
  private PropertyChangeListener       tagListener;

  /** The tags observable. */
  private List<String>                 tagsObservable = ObservableCollections.observableList(new ArrayList<String>());

  /** The movie set tree model. */
  private MovieSetTreeModel            movieSetTreeModel;

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {
    // the tag listener: its used to always have a full list of all tags used in
    // tmm
    tagListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // listen to changes of tags
        if ("tag".equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateTags(movie);
        }
      }
    };
  }

  /**
   * Gets the single instance of MovieList.
   * 
   * @return single instance of MovieList
   */
  public static MovieList getInstance() {
    if (MovieList.instance == null) {
      MovieList.instance = new MovieList();
    }
    return MovieList.instance;
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    int oldValue = movieList.size();
    movieList.add(movie);
    updateTags(movie);
    movie.addPropertyChangeListener(tagListener);
    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Removes the datasource.
   * 
   * @param path
   *          the path
   */
  public void removeDatasource(String path) {
    if (StringUtils.isEmpty(path)) {
      return;
    }

    for (int i = movieList.size() - 1; i >= 0; i--) {
      Movie movie = movieList.get(i);
      if (path.equals(movie.getDataSource())) {
        removeMovie(movie);
      }
    }
  }

  /**
   * Gets the unscraped movies.
   * 
   * @return the unscraped movies
   */
  public List<Movie> getUnscrapedMovies() {
    List<Movie> unscrapedMovies = new ArrayList<Movie>();

    for (Movie movie : movieList) {
      if (!movie.isScraped()) {
        unscrapedMovies.add(movie);
      }
    }

    return unscrapedMovies;
  }

  /**
   * Removes the movie.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    int oldValue = movieList.size();
    movieList.remove(movie);
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.remove(movie);
    Globals.entityManager.getTransaction().commit();
    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Remove all movies.
   * 
   */
  public void removeMovies() {
    for (int i = movieList.size() - 1; i >= 0; i--) {
      Movie movie = movieList.get(i);
      removeMovie(movie);
    }
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public ObservableElementList<Movie> getMovies() {
    if (movieList == null) {
      movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));
    }
    return movieList;
  }

  /**
   * Load movies from database.
   */
  public void loadMoviesFromDatabase() {
    List<Movie> movies = null;
    List<MovieSet> movieSets = null;
    try {
      // load movies
      TypedQuery<Movie> query = Globals.entityManager.createQuery("SELECT movie FROM Movie movie", Movie.class);
      movies = query.getResultList();
      if (movies != null) {
        LOGGER.debug("found " + movies.size() + " movies in database");
        movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>(movies.size())),
            GlazedLists.beanConnector(Movie.class));

        for (Object obj : movies) {
          if (obj instanceof Movie) {
            Movie movie = (Movie) obj;
            // movie.setObservables();
            movie.initializeAfterLoading();
            addMovie(movie);
          }
          else {
            LOGGER.error("retrieved no movie: " + obj);
          }
        }

      }
      else {
        LOGGER.debug("found no movies in database");
      }

      // load movie sets
      TypedQuery<MovieSet> querySets = Globals.entityManager.createQuery("SELECT movieSet FROM MovieSet movieSet", MovieSet.class);
      movieSets = querySets.getResultList();
      if (movieSets != null) {
        LOGGER.debug("found " + movieSets.size() + " movieSets in database");
        movieSetList = ObservableCollections.observableList(new ArrayList<MovieSet>(movieSets.size()));

        // load movie sets
        for (Object obj : movieSets) {
          if (obj instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) obj;
            movieSet.setObservables();
            addMovieSet(movieSet);
          }
        }
      }
      else {
        LOGGER.debug("found no movieSets in database");
      }

    }
    catch (PersistenceException e) {
      LOGGER.error("loadMoviesFromDatabase", e);
    }
    catch (Exception e) {
      LOGGER.error("loadMoviesFromDatabase", e);
    }
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public synchronized Movie getMovieByPath(String path) {

    for (Movie movie : movieList) {
      if (movie.getPath().compareTo(path) == 0) {
        return movie;
      }
    }

    return null;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @param ImdbId
   *          the imdb id
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, String ImdbId, IMediaMetadataProvider metadataProvider) {
    List<MediaSearchResult> sr = null;
    if (ImdbId != null && !ImdbId.isEmpty()) {
      sr = searchMovieByImdbId(ImdbId, metadataProvider);
    }
    if (sr == null || sr.size() == 0) {
      sr = searchMovie(searchTerm, metadataProvider);
    }

    return sr;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  private List<MediaSearchResult> searchMovie(String searchTerm, IMediaMetadataProvider metadataProvider) {
    // format searchstring
    searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      IMediaMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      searchResult = provider.search(new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, searchTerm));
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  /**
   * Search movie.
   * 
   * @param imdbId
   *          the imdb id
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  private List<MediaSearchResult> searchMovieByImdbId(String imdbId, IMediaMetadataProvider metadataProvider) {

    List<MediaSearchResult> searchResult = null;
    MediaSearchOptions options = new MediaSearchOptions();
    options.setMediaType(MediaType.MOVIE);
    options.set(SearchParam.IMDBID, imdbId);

    try {
      IMediaMetadataProvider provider = metadataProvider;
      // get a new metadataProvider if no one is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      searchResult = provider.search(options);
    }
    catch (Exception e) {
      LOGGER.warn("failed to search movie with imdbid", e);
      searchResult = new ArrayList<MediaSearchResult>();
    }

    return searchResult;
  }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider() {
    MovieScrapers scraper = Globals.settings.getMovieScraper();
    return getMetadataProvider(scraper);
  }

  /**
   * Gets the metadata provider.
   * 
   * @param scraper
   *          the scraper
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider(MovieScrapers scraper) {
    IMediaMetadataProvider metadataProvider = null;
    switch (scraper) {
      case OFDB:
        LOGGER.debug("get instance of OfdbMetadataProvider");
        metadataProvider = new OfdbMetadataProvider();
        break;

      case ZELLULOID:
        LOGGER.debug("get instance of ZelluloidMetadataProvider");
        metadataProvider = new ZelluloidMetadataProvider();
        break;

      case IMDB:
        LOGGER.debug("get instance of ImdbMetadataProvider");
        metadataProvider = new ImdbMetadataProvider(Globals.settings.getImdbSite());
        break;

      case TMDB:
      default:
        LOGGER.debug("get instance of TmdbMetadataProvider");
        try {
          metadataProvider = new TmdbMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
        }
    }

    //
    // try {
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.com/imdb.xml")));
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.de/imdb_de.xml")));
    // } catch (Exception e) {
    // LOGGER.error("tried to get xmbc scraper", e);
    // }

    // }

    return metadataProvider;
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    // List<IMediaArtworkProvider> artworkProviders = new
    // ArrayList<IMediaArtworkProvider>();
    // // if (artworkProvider == null) {
    // IMediaArtworkProvider artworkProvider = null;
    //
    // try {
    // if (Globals.settings.isImageScraperTmdb()) {
    // LOGGER.debug("get instance of TmdbMetadataProvider");
    // artworkProvider = new TmdbMetadataProvider();
    // artworkProviders.add(artworkProvider);
    // }
    // }
    // catch (Exception e) {
    // LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
    // }
    // try {
    // if (Globals.settings.isImageScraperFanartTv()) {
    // LOGGER.debug("get instance of FanartTvMetadataProvider");
    // artworkProvider = new FanartTvMetadataProvider();
    // artworkProviders.add(artworkProvider);
    // }
    // }
    // catch (Exception e) {
    // LOGGER.warn("failed to get instance of FanartTvMetadataProvider", e);
    // }
    // // }

    List<MovieArtworkScrapers> scrapers = new ArrayList<MovieArtworkScrapers>();
    if (Globals.settings.isImageScraperTmdb()) {
      scrapers.add(MovieArtworkScrapers.TMDB);
    }

    if (Globals.settings.isImageScraperFanartTv()) {
      scrapers.add(MovieArtworkScrapers.FANART_TV);
    }

    return getArtworkProviders(scrapers);
  }

  /**
   * Gets the artwork providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the artwork providers
   */
  public List<IMediaArtworkProvider> getArtworkProviders(List<MovieArtworkScrapers> scrapers) {
    List<IMediaArtworkProvider> artworkProviders = new ArrayList<IMediaArtworkProvider>();

    IMediaArtworkProvider artworkProvider = null;

    // tmdb
    if (scrapers.contains(MovieArtworkScrapers.TMDB)) {
      try {
        if (Globals.settings.isImageScraperTmdb()) {
          LOGGER.debug("get instance of TmdbMetadataProvider");
          artworkProvider = new TmdbMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // fanart.tv
    if (scrapers.contains(MovieArtworkScrapers.FANART_TV)) {
      try {
        if (Globals.settings.isImageScraperFanartTv()) {
          LOGGER.debug("get instance of FanartTvMetadataProvider");
          artworkProvider = new FanartTvMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of FanartTvMetadataProvider", e);
      }
    }

    return artworkProviders;
  }

  /**
   * Gets the trailer providers.
   * 
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders() {
    // List<IMediaTrailerProvider> trailerProviders = new
    // ArrayList<IMediaTrailerProvider>();
    // LOGGER.debug("get instances of IMediaTrailerProviders");
    //
    // // tmdb
    // if (Globals.settings.isTrailerScraperTmdb()) {
    // try {
    // IMediaTrailerProvider trailerProvider = new TmdbMetadataProvider();
    // trailerProviders.add(trailerProvider);
    // }
    // catch (Exception e) {
    // LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
    // }
    // }
    //
    // // hd-trailers.net
    // if (Globals.settings.isTrailerScraperHdTrailers()) {
    // IMediaTrailerProvider trailerProvider = new HDTrailersNet();
    // trailerProviders.add(trailerProvider);
    // }
    //
    // // ofdb.de
    // if (Globals.settings.isTrailerScraperOfdb()) {
    // IMediaTrailerProvider trailerProvider = new OfdbMetadataProvider();
    // trailerProviders.add(trailerProvider);
    // }
    //
    // return trailerProviders;
    List<MovieTrailerScrapers> scrapers = new ArrayList<MovieTrailerScrapers>();

    if (Globals.settings.isTrailerScraperTmdb()) {
      scrapers.add(MovieTrailerScrapers.TMDB);
    }

    if (Globals.settings.isTrailerScraperHdTrailers()) {
      scrapers.add(MovieTrailerScrapers.HDTRAILERS);
    }

    if (Globals.settings.isTrailerScraperOfdb()) {
      scrapers.add(MovieTrailerScrapers.OFDB);
    }

    return getTrailerProviders(scrapers);
  }

  /**
   * Gets the trailer providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders(List<MovieTrailerScrapers> scrapers) {
    List<IMediaTrailerProvider> trailerProviders = new ArrayList<IMediaTrailerProvider>();

    // tmdb
    if (scrapers.contains(MovieTrailerScrapers.TMDB)) {
      try {
        IMediaTrailerProvider trailerProvider = new TmdbMetadataProvider();
        trailerProviders.add(trailerProvider);
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // hd-trailer.net
    if (scrapers.contains(MovieTrailerScrapers.HDTRAILERS)) {
      IMediaTrailerProvider trailerProvider = new HDTrailersNet();
      trailerProviders.add(trailerProvider);
    }

    // ofdb.de
    if (scrapers.contains(MovieTrailerScrapers.OFDB)) {
      IMediaTrailerProvider trailerProvider = new OfdbMetadataProvider();
      trailerProviders.add(trailerProvider);
    }

    return trailerProviders;
  }

  /**
   * Gets the movie count.
   * 
   * @return the movie count
   */
  public int getMovieCount() {
    int size = movieList.size();
    return size;
  }

  /**
   * Gets the movie set count.
   * 
   * @return the movie set count
   */
  public int getMovieSetCount() {
    int size = movieSetList.size();
    return size;
  }

  /**
   * Gets the tags in movies.
   * 
   * @return the tags in movies
   */
  public List<String> getTagsInMovies() {
    return tagsObservable;
  }

  /**
   * Update tags used in movies.
   * 
   * @param movie
   *          the movie
   */
  private void updateTags(Movie movie) {
    for (String tagInMovie : movie.getTags()) {
      boolean tagFound = false;
      for (String tag : tagsObservable) {
        if (tagInMovie.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addTag(tagInMovie);
      }
    }
  }

  /**
   * Adds the tag.
   * 
   * @param newTag
   *          the new tag
   */
  private void addTag(String newTag) {
    for (String tag : tagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tagsObservable.add(newTag);
    firePropertyChange("tag", null, tagsObservable);
  }

  /**
   * Search duplicates.
   */
  public void searchDuplicates() {
    Map<String, Movie> imdbDuplicates = new HashMap<String, Movie>();
    Map<Integer, Movie> tmdbDuplicates = new HashMap<Integer, Movie>();

    for (Movie movie : movieList) {
      movie.clearDuplicate();

      // imdb duplicate search only works with given imdbid
      if (StringUtils.isNotEmpty(movie.getImdbId())) {
        // is there a movie with this imdbid sotred?
        if (imdbDuplicates.containsKey(movie.getImdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = imdbDuplicates.get(movie.getImdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          imdbDuplicates.put(movie.getImdbId(), movie);
        }
      }

      // tmdb duplicate search only works with with given tmdb id
      if (movie.getTmdbId() > 0) {
        // is there a movie with this tmdbid sotred?
        if (tmdbDuplicates.containsKey(movie.getTmdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = tmdbDuplicates.get(movie.getTmdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          tmdbDuplicates.put(movie.getTmdbId(), movie);
        }
      }
    }
  }

  /**
   * Gets the movie set list.
   * 
   * @return the movieSetList
   */
  public List<MovieSet> getMovieSetList() {
    if (movieSetList == null) {
      movieSetList = ObservableCollections.observableList(new ArrayList<MovieSet>());
    }
    return movieSetList;
  }

  /**
   * Sets the movie set list.
   * 
   * @param movieSetList
   *          the movieSetList to set
   */
  public void setMovieSetList(ObservableElementList<MovieSet> movieSetList) {
    this.movieSetList = movieSetList;
  }

  /**
   * Adds the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void addMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    this.movieSetList.add(movieSet);
    firePropertyChange("movieSets", null, movieSetList);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  /**
   * Removes the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void removeMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    movieSetList.remove(movieSet);
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.remove(movieSet);
    Globals.entityManager.getTransaction().commit();
    firePropertyChange("movieSets", null, movieSetList);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  /**
   * Remove all movieSets.
   * 
   */
  public void removeMovieSets() {
    for (int i = movieSetList.size() - 1; i >= 0; i--) {
      MovieSet movieSet = movieSetList.get(i);
      // remove it via the tree model to ensure the tree is redrawn right
      movieSetTreeModel.removeMovieSet(movieSet);
    }
  }

  /**
   * Gets the movie set tree model.
   * 
   * @return the movie set tree model
   */
  public MovieSetTreeModel getMovieSetTreeModel() {
    return movieSetTreeModel;
  }

  /**
   * Sets the movie set tree model.
   * 
   * @param movieSetTreeModel
   *          the new movie set tree model
   */
  public void setMovieSetTreeModel(MovieSetTreeModel movieSetTreeModel) {
    this.movieSetTreeModel = movieSetTreeModel;
  }

  /**
   * Find movie set.
   * 
   * @param name
   *          the name
   * @return the movie set
   */
  public MovieSet findMovieSet(String name) {
    // search for the movieset by name
    for (MovieSet movieSet : movieSetList) {
      if (movieSet.getName().equals(name)) {
        return movieSet;
      }
    }

    return null;
  }

  public void sortMoviesInMovieSet(MovieSet movieSet) {
    movieSet.sortMovies();
    movieSetTreeModel.sortMoviesInMovieSet(movieSet);
  }
}