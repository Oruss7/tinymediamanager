/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.anidb.AniDBMetadataProvider;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.thetvdb.TheTvDbMetadataProvider;

/**
 * The Class TvShowList.
 * 
 * @author Manuel Laggner
 */
public class TvShowList extends AbstractModelObject {
  private static final Logger    LOGGER         = LoggerFactory.getLogger(TvShowList.class);
  private static TvShowList      instance       = null;

  private List<TvShow>           tvShowList     = ObservableCollections.observableList(new ArrayList<TvShow>());
  private List<String>           tagsObservable = ObservableCollections.observableList(new ArrayList<String>());
  private PropertyChangeListener tagListener;

  /**
   * Instantiates a new TvShowList.
   */
  private TvShowList() {
    // the tag listener: its used to always have a full list of all tags used in tmm
    tagListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // listen to changes of tags
        if ("tag".equals(evt.getPropertyName())) {
          TvShow tvShow = (TvShow) evt.getSource();
          updateTags(tvShow);
        }
        if (EPISODE_COUNT.equals(evt.getPropertyName())) {
          firePropertyChange(EPISODE_COUNT, 0, 1);
        }
      }
    };
  }

  /**
   * Gets the single instance of TvShowList.
   * 
   * @return single instance of TvShowList
   */
  public static TvShowList getInstance() {
    if (instance == null) {
      instance = new TvShowList();
    }

    return instance;
  }

  /**
   * Gets the tv shows.
   * 
   * @return the tv shows
   */
  public List<TvShow> getTvShows() {
    return tvShowList;
  }

  /**
   * Adds the tv show.
   * 
   * @param newValue
   *          the new value
   */
  public void addTvShow(TvShow newValue) {
    int oldValue = tvShowList.size();

    tvShowList.add(newValue);
    newValue.addPropertyChangeListener(tagListener);
    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(ADDED_TV_SHOW, null, newValue);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
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

    for (int i = tvShowList.size() - 1; i >= 0; i--) {
      TvShow tvShow = tvShowList.get(i);
      if (new File(path).equals(new File(tvShow.getDataSource()))) {
        removeTvShow(tvShow);
      }
    }
  }

  /**
   * Removes the tv show.
   * 
   * @param tvShow
   *          the tvShow
   */
  public void removeTvShow(TvShow tvShow) {
    int oldValue = tvShowList.size();
    tvShow.removeAllEpisodes();
    tvShowList.remove(tvShow);
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.remove(tvShow);
    Globals.entityManager.getTransaction().commit();
    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(REMOVED_TV_SHOW, null, tvShow);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
  }

  /**
   * Gets the tv show count.
   * 
   * @return the tv show count
   */
  public int getTvShowCount() {
    return tvShowList.size();
  }

  /**
   * Gets the episode count.
   * 
   * @return the episode count
   */
  public int getEpisodeCount() {
    int count = 0;
    for (int i = 0; i < tvShowList.size(); i++) {
      TvShow tvShow = tvShowList.get(i);
      count += tvShow.getEpisodeCount();
    }

    return count;
  }

  /**
   * Load tv shows from database.
   */
  public void loadTvShowsFromDatabase() {
    List<TvShow> tvShows = null;
    try {
      // load tv shows
      TypedQuery<TvShow> query = Globals.entityManager.createQuery("SELECT tvShow FROM TvShow tvShow", TvShow.class);
      tvShows = query.getResultList();
      if (tvShows != null) {
        LOGGER.info("found " + tvShows.size() + " tv shows in database");
        for (Object obj : tvShows) {
          if (obj instanceof TvShow) {
            TvShow tvShow = (TvShow) obj;
            tvShow.initializeAfterLoading();
            for (TvShowEpisode episode : tvShow.getEpisodes()) {
              episode.initializeAfterLoading();
            }

            // for performance reasons we add tv shows directly
            tvShowList.add(tvShow);
          }
          else {
            LOGGER.error("retrieved no tv show: " + obj);
          }
        }

      }
      else {
        LOGGER.debug("found no movies in database");
      }
    }
    catch (Exception e) {
      LOGGER.error("loadTvShowsFromDatabase", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "", "message.database.loadtvshows"));
    }
  }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public ITvShowMetadataProvider getMetadataProvider() {
    TvShowScrapers scraper = Globals.settings.getTvShowSettings().getTvShowScraper();
    return getMetadataProvider(scraper);
  }

  /**
   * Gets the metadata provider.
   * 
   * @param scraper
   *          the scraper
   * @return the metadata provider
   */
  public ITvShowMetadataProvider getMetadataProvider(TvShowScrapers scraper) {
    ITvShowMetadataProvider metadataProvider = null;
    switch (scraper) {
      case ANIDB:
        LOGGER.debug("get instance of AniDbMetadataProvider");
        metadataProvider = new AniDBMetadataProvider();
        break;
      case TVDB:
      default:
        LOGGER.debug("get instance of TheTvDbMetadataProvider");
        metadataProvider = new TheTvDbMetadataProvider();
        break;

    }

    return metadataProvider;
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    List<TvShowArtworkScrapers> scrapers = new ArrayList<TvShowArtworkScrapers>();
    scrapers.add(TvShowArtworkScrapers.TVDB);
    scrapers.add(TvShowArtworkScrapers.ANIDB);
    scrapers.add(TvShowArtworkScrapers.FANART_TV);
    return getArtworkProviders(scrapers);
  }

  /**
   * Gets the artwork providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the artwork providers
   */
  public List<IMediaArtworkProvider> getArtworkProviders(List<TvShowArtworkScrapers> scrapers) {
    List<IMediaArtworkProvider> artworkProviders = new ArrayList<IMediaArtworkProvider>();

    IMediaArtworkProvider artworkProvider = null;

    // the tv db
    if (scrapers.contains(TvShowArtworkScrapers.TVDB)) {
      try {
        if (Globals.settings.getTvShowSettings().isImageScraperTvdb()) {
          LOGGER.debug("get instance of TheTvDbMetadataProvider");
          artworkProvider = new TheTvDbMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TheTvDbMetadataProvider", e);
      }
    }

    // anidb
    if (scrapers.contains(TvShowArtworkScrapers.ANIDB)) {
      artworkProviders.add(new AniDBMetadataProvider());
    }

    // fanart.tv
    if (scrapers.contains(TvShowArtworkScrapers.FANART_TV)) {
      try {
        if (Globals.settings.getTvShowSettings().isImageScraperFanartTv()) {
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
   * Search tv show.
   * 
   * @param searchTerm
   *          the search term
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, ITvShowMetadataProvider metadataProvider) {
    // format searchstring
    // searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      ITvShowMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      searchResult = provider.search(new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, searchTerm));
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  /**
   * Update tags.
   * 
   * @param tvShow
   *          the tv show
   */
  private void updateTags(TvShow tvShow) {
    for (String tagInTvShow : tvShow.getTags()) {
      boolean tagFound = false;
      for (String tag : tagsObservable) {
        if (tagInTvShow.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addTag(tagInTvShow);
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
   * Gets the tags in tv shows.
   * 
   * @return the tags in tv shows
   */
  public List<String> getTagsInTvShows() {
    return tagsObservable;
  }

  /**
   * Gets the TV show by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public TvShow getTvShowByPath(File path) {
    ArrayList<TvShow> tvShows = new ArrayList<TvShow>(tvShowList);
    // iterate over all tv shows and check whether this path is being owned by one
    for (TvShow tvShow : tvShows) {
      if (new File(tvShow.getPath()).compareTo(path) == 0) {
        return tvShow;
      }
    }

    return null;
  }

  /**
   * Gets the episodes by file. Filter out all episodes from the Database which are part of this file
   * 
   * @param file
   *          the file
   * @return the tv episodes by file
   */
  public List<TvShowEpisode> getTvEpisodesByFile(TvShow tvShow, File file) {
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(1);
    // validy check
    if (file == null) {
      return episodes;
    }

    // check if that file is in this tv show/episode (iterating thread safe)
    for (int j = 0; j < tvShow.getEpisodes().size(); j++) {
      TvShowEpisode episode = tvShow.getEpisodes().get(j);
      for (int k = 0; k < episode.getMediaFiles().size(); k++) {
        MediaFile mediaFile = episode.getMediaFiles().get(k);
        if (file.equals(mediaFile.getFile())) {
          episodes.add(episode);
        }
      }
    }
    return episodes;
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (TvShow tvShow : new ArrayList<TvShow>(tvShowList)) {
      tvShow.clearTitleSortable();
    }
  }

  /**
   * Gets the new TvShows or TvShows with new episodes
   * 
   * @return the new TvShows
   */
  public List<TvShow> getNewTvShows() {
    List<TvShow> newShows = new ArrayList<TvShow>();
    for (TvShow show : tvShowList) {
      if (show.isNewlyAdded()) {
        newShows.add(show);
      }
    }
    return newShows;
  }

  /**
   * Gets the new episodes
   * 
   * @return the new episodes
   */
  public List<TvShowEpisode> getNewEpisodes() {
    List<TvShowEpisode> newEp = new ArrayList<TvShowEpisode>();
    for (TvShow show : tvShowList) {
      for (TvShowEpisode ep : show.getEpisodes()) {
        if (ep.isNewlyAdded()) {
          newEp.add(ep);
        }
      }
    }
    return newEp;
  }

}
