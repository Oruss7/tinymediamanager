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
package org.tinymediamanager.ui.gamesets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.game.Game;
import org.tinymediamanager.core.game.GameList;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.giantbomb.giantbombMetadataProvider;
import org.tinymediamanager.ui.UTF8Control;

import com.omertron.themoviedbapi.model.Collection;
import com.omertron.themoviedbapi.model.CollectionInfo;

/**
 * The Class GameSetChooserModel.
 */
public class GameSetChooserModel extends AbstractModelObject {

  private static final ResourceBundle     BUNDLE      = ResourceBundle.getBundle("messages", new UTF8Control());         //$NON-NLS-1$

  /** The static LOGGER. */
  private static final Logger             LOGGER      = LoggerFactory.getLogger(GameSetChooserModel.class);

  /** The Constant emptyResult. */
  public static final GameSetChooserModel emptyResult = new GameSetChooserModel();

  /** The name. */
  private String                          name        = "";

  /** The poster url. */
  private String                          posterUrl   = "";

  /** The fanart url. */
  private String                          fanartUrl   = "";

  /** The tmdb id. */
  private int                             tmdbId      = 0;

  /** The collection. */
  private Collection                      collection;

  /** The info. */
  private CollectionInfo                  info;

  /** The games. */
  private List<GameInSet>                 games       = ObservableCollections.observableList(new ArrayList<GameInSet>());

  /** The mp. */
  private giantbombMetadataProvider       mp;

  /** The scraped. */
  private boolean                         scraped;

  /**
   * Instantiates a new game set chooser model.
   * 
   * @param collection
   *          the collection
   */
  public GameSetChooserModel(Collection collection) {
    this.collection = collection;

    setName(collection.getName());
    setTmdbId(collection.getId());
    setPosterUrl(collection.getPosterPath());
    setFanartUrl(collection.getBackdropPath());

    try {
      mp = new giantbombMetadataProvider();
    }
    catch (Exception e) {
      mp = null;
    }
  }

  /**
   * create the empty search result.
   */
  private GameSetChooserModel() {
    setName(BUNDLE.getString("chooser.nothingfound")); //$NON-NLS-1$
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName(String name) {
    this.name = name;
    firePropertyChange("name", "", name);
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param tmdbId
   *          the new tmdb id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Sets the poster url.
   * 
   * @param posterUrl
   *          the new poster url
   */
  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
    firePropertyChange("posterUrl", "", posterUrl);
  }

  /**
   * Sets the fanart url.
   * 
   * @param fanartUrl
   *          the new fanart url
   */
  public void setFanartUrl(String fanartUrl) {
    this.fanartUrl = fanartUrl;
    firePropertyChange("fanartUrl", "", fanartUrl);
  }

  /**
   * Checks if is scraped.
   * 
   * @return true, if is scraped
   */
  public boolean isScraped() {
    return scraped;
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Gets the fanart url.
   * 
   * @return the fanart url
   */
  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Match with existing games.
   */
  public void matchWithExistingGames() {
    List<Game> gamesFromGameList = GameList.getInstance().getGames();
    for (GameInSet mis : games) {

      // try to match via imdbid if nothing has been found
      if (mis.game == null) {
        if (StringUtils.isEmpty(mis.imdbId)) {
          // get imdbid for this tmdbid
          if (mp != null) {
            MediaScrapeOptions options = new MediaScrapeOptions();
            options.setTmdbId(mis.tmdbId);
            options.setLanguage(Globals.settings.getGameSettings().getScraperLanguage());
            options.setCountry(Globals.settings.getGameSettings().getCertificationCountry());
            try {
              MediaMetadata md = mp.getMetadata(options);
              // FIXME
              // mis.imdbId = md.getImdbId();
            }
            catch (Exception e) {
              LOGGER.warn(e.getMessage());
            }
          }
        }

        if (StringUtils.isNotEmpty(mis.imdbId)) {
          for (Game game : gamesFromGameList) {
            // FIXME
            // if (mis.imdbId.equals(game.getGameId("default"))) {
            // mis.setGame(game);
            // break;
            // }
          }
        }
      }
    }
  }

  /**
   * Scrape metadata.
   */
  public void scrapeMetadata() {
    try {
      if (mp != null) {
        MediaScrapeOptions options = new MediaScrapeOptions();
        options.setTmdbId(collection.getId());
        options.setLanguage(Globals.settings.getGameSettings().getScraperLanguage());
        options.setCountry(Globals.settings.getGameSettings().getCertificationCountry());

        CollectionInfo info = null;
        if (info != null) {
          this.info = info;
          for (Collection collection : info.getParts()) {
            GameInSet game = new GameInSet(collection.getName());
            game.setTmdbId(collection.getId());
            game.setReleaseDate(collection.getReleaseDate());
            games.add(game);
          }

          Collections.sort(games);

          // try to match games
          matchWithExistingGames();

          this.scraped = true;
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("error while scraping metadata", e);
    }

  }

  /**
   * Gets the info.
   * 
   * @return the info
   */
  public CollectionInfo getInfo() {
    return info;
  }

  /**
   * Gets the games.
   * 
   * @return the games
   */
  public List<GameInSet> getGames() {
    return games;
  }

  /**
   * The Class GameInSet.
   */
  public static class GameInSet extends AbstractModelObject implements Comparable<GameInSet> {

    /** The name. */
    private String name        = "";

    /** The tmdb id. */
    private int    tmdbId      = 0;

    /** The imdb id. */
    private String imdbId      = "";

    /** The release date. */
    private String releaseDate = "";

    /** The game. */
    private Game   game        = null;

    /**
     * Instantiates a new game in set.
     * 
     * @param name
     *          the name
     */
    public GameInSet(String name) {
      this.name = name;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * Gets the tmdb id.
     * 
     * @return the tmdb id
     */
    public int getTmdbId() {
      return tmdbId;
    }

    /**
     * Gets the imdb id.
     * 
     * @return the imdb id
     */
    public String getImdbId() {
      return imdbId;
    }

    /**
     * Gets the release date.
     * 
     * @return the release date
     */
    public String getReleaseDate() {
      return releaseDate;
    }

    /**
     * Gets the game.
     * 
     * @return the game
     */
    public Game getGame() {
      return game;
    }

    /**
     * Sets the tmdb id.
     * 
     * @param tmdbId
     *          the new tmdb id
     */
    public void setTmdbId(int tmdbId) {
      this.tmdbId = tmdbId;
    }

    /**
     * Sets the imdb id.
     * 
     * @param imdbId
     *          the new imdb id
     */
    public void setImdbId(String imdbId) {
      this.imdbId = imdbId;
    }

    /**
     * Sets the release date.
     * 
     * @param releaseDate
     *          the new release date
     */
    public void setReleaseDate(String releaseDate) {
      this.releaseDate = releaseDate;
    }

    /**
     * Sets the game.
     * 
     * @param game
     *          the new game
     */
    public void setGame(Game game) {
      this.game = game;
      firePropertyChange("game", null, game);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(GameInSet o) {
      return releaseDate.compareTo(o.releaseDate);
    }
  }

  public Object getGiantbombId() {
    // TODO Auto-generated method stub
    return null;
  }
}
