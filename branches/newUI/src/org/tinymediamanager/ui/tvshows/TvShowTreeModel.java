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
package org.tinymediamanager.ui.tvshows;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSeason;

/**
 * The Class TvShowTreeModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeModel implements TreeModel {
  private TvShowRootTreeNode      root       = new TvShowRootTreeNode();
  private List<TreeModelListener> listeners  = new ArrayList<TreeModelListener>();
  private Map<Object, TreeNode>   nodeMap    = Collections.synchronizedMap(new HashMap<Object, TreeNode>());
  private PropertyChangeListener  propertyChangeListener;
  private TvShowList              tvShowList = TvShowList.getInstance();

  /**
   * Instantiates a new tv show tree model.
   * 
   * @param tvShows
   *          the tv shows
   */
  public TvShowTreeModel(List<TvShow> tvShows) {
    // create the listener
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // added a tv show
        if (ADDED_TV_SHOW.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShow) {
          TvShow tvShow = (TvShow) evt.getNewValue();
          addTvShow(tvShow);
        }

        // removed a tv show
        if (REMOVED_TV_SHOW.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShow) {
          TvShow tvShow = (TvShow) evt.getNewValue();
          removeTvShow(tvShow);
        }

        // added a season
        if (ADDED_SEASON.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowSeason) {
          TvShowSeason season = (TvShowSeason) evt.getNewValue();
          addTvShowSeason(season, season.getTvShow());
        }

        // added an episode
        if (ADDED_EPISODE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getNewValue();
          addTvShowEpisode(episode, episode.getTvShow().getSeasonForEpisode(episode));
        }

        // removed an episode
        if (REMOVED_EPISODE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getNewValue();
          removeTvShowEpisode(episode);
        }

        // changed the season of an episode
        if (SEASON.equals(evt.getPropertyName()) && evt.getSource() instanceof TvShowEpisode) {
          // simply remove it from the tree and readd it
          TvShowEpisode episode = (TvShowEpisode) evt.getSource();
          removeTvShowEpisode(episode);
          addTvShowEpisode(episode, episode.getTvShow().getSeasonForEpisode(episode));
        }

        // update on changes of tv show
        if (evt.getSource() instanceof TvShow
            && (TITLE.equals(evt.getPropertyName()) || HAS_NFO_FILE.equals(evt.getPropertyName()) || HAS_IMAGES.equals(evt.getPropertyName()))) {
          // inform listeners (root - to update the sum)
          TreeModelEvent event = new TreeModelEvent(this, root.getPath(), null, null);
          for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(event);
          }
        }
        // update on changes of episode
        if (evt.getSource() instanceof TvShowEpisode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeMap.get(evt.getSource());
          if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            int index = parent.getIndex(node);
            TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { node });
            for (TreeModelListener listener : listeners) {
              listener.treeNodesChanged(event);
            }
          }
        }
      }
    };

    // install a property change listener to the TvShowList
    tvShowList.addPropertyChangeListener(propertyChangeListener);

    // build initial tree
    for (TvShow tvShow : tvShows) {
      addTvShow(tvShow);
    }
  }

  /**
   * Adds the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private void addTvShow(TvShow tvShow) {
    synchronized (root) {
      DefaultMutableTreeNode tvShowNode = new TvShowTreeNode(tvShow);
      root.add(tvShowNode);
      nodeMap.put(tvShow, tvShowNode);

      for (TvShowSeason season : tvShow.getSeasons()) {
        // check if there is a node for its season
        TvShowSeasonTreeNode seasonNode = (TvShowSeasonTreeNode) nodeMap.get(season);
        if (seasonNode == null) {
          addTvShowSeason(season, tvShow);
        }

        for (TvShowEpisode episode : season.getEpisodes()) {
          addTvShowEpisode(episode, season);
        }
      }

      int index = root.getIndex(tvShowNode);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { tvShow });

      for (TreeModelListener listener : listeners) {
        listener.treeNodesInserted(event);
      }

    }
    tvShow.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private void removeTvShow(TvShow tvShow) {
    synchronized (root) {
      TvShowTreeNode child = (TvShowTreeNode) nodeMap.get(tvShow);
      DefaultMutableTreeNode parent = root;
      if (parent != null && child != null) {
        int index = parent.getIndex(child);

        nodeMap.remove(tvShow);
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          nodeMap.remove(episode);
          episode.removePropertyChangeListener(propertyChangeListener);
        }

        tvShow.removePropertyChangeListener(propertyChangeListener);

        child.removeAllChildren();
        child.removeFromParent();

        // inform listeners
        TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesRemoved(event);
        }
      }
    }
  }

  /**
   * Adds the tv show season.
   * 
   * @param season
   *          the season
   * @param tvShow
   *          the tv show
   */
  private void addTvShowSeason(TvShowSeason season, TvShow tvShow) {
    synchronized (root) {
      // get the tv show node
      TvShowTreeNode parent = (TvShowTreeNode) nodeMap.get(tvShow);
      TvShowSeasonTreeNode child = new TvShowSeasonTreeNode(season);
      if (parent != null) {
        parent.add(child);
        nodeMap.put(season, child);

        int index = parent.getIndex(child);

        // inform listeners (tv show)
        TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesInserted(event);
        }

        // inform listeners (root - to update the sum)
        event = new TreeModelEvent(this, root.getPath(), null, null);
        for (TreeModelListener listener : listeners) {
          listener.treeNodesChanged(event);
        }
      }
    }
  }

  /**
   * Adds the tv show episode.
   * 
   * @param episode
   *          the episode
   * @param season
   *          the season
   */
  private void addTvShowEpisode(TvShowEpisode episode, TvShowSeason season) {
    synchronized (root) {
      // get the tv show season node
      TvShowSeasonTreeNode parent = (TvShowSeasonTreeNode) nodeMap.get(season);
      TvShowEpisodeTreeNode child = new TvShowEpisodeTreeNode(episode);
      if (parent != null) {
        parent.add(child);
        nodeMap.put(episode, child);

        int index = parent.getIndex(child);

        // inform listeners
        TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesInserted(event);
        }

        // inform listeners (root - to update the sum)
        event = new TreeModelEvent(this, root.getPath(), null, null);
        for (TreeModelListener listener : listeners) {
          listener.treeNodesChanged(event);
        }
      }

    }
    episode.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes the tv show episode.
   * 
   * @param episode
   *          the episode
   * @param season
   *          the season
   */
  private void removeTvShowEpisode(TvShowEpisode episode) {
    synchronized (root) {
      // get the tv show season node
      TvShowEpisodeTreeNode child = (TvShowEpisodeTreeNode) nodeMap.get(episode);
      TvShowSeasonTreeNode parent = null;
      if (child != null) {
        parent = (TvShowSeasonTreeNode) child.getParent();
      }

      if (parent != null && child != null) {
        int index = parent.getIndex(child);
        parent.remove(child);
        nodeMap.remove(episode);
        episode.removePropertyChangeListener(propertyChangeListener);

        // inform listeners
        TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesRemoved(event);
        }

        // remove tv show if there is no more episode in it
        if (parent.getChildCount() == 0) {
          TvShowSeason season = null;
          for (Entry<Object, TreeNode> entry : nodeMap.entrySet()) {
            if (entry.getValue() == parent) {
              season = (TvShowSeason) entry.getKey();
            }
          }
          if (season != null) {
            removeTvShowSeason(season);
          }
        }
      }
    }
  }

  /**
   * Removes the tv show season.
   * 
   * @param season
   *          the season
   */
  private void removeTvShowSeason(TvShowSeason season) {
    synchronized (root) {
      TvShowSeasonTreeNode child = (TvShowSeasonTreeNode) nodeMap.get(season);
      TvShowTreeNode parent = null;
      if (child != null) {
        parent = (TvShowTreeNode) child.getParent();
      }

      if (parent != null && child != null) {
        int index = parent.getIndex(child);
        parent.remove(child);
        nodeMap.remove(season);

        // inform listeners
        TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesRemoved(event);
        }
      }
    }
  }

  @Override
  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  @Override
  public Object getChild(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  @Override
  public int getChildCount(Object parent) {
    return ((TreeNode) parent).getChildCount();
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    TreeNode childNode = null;
    if (child instanceof TreeNode) {
      childNode = (TreeNode) child;
    }
    else {
      childNode = nodeMap.get(child);
    }
    return ((TreeNode) parent).getIndex(childNode);
  }

  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public boolean isLeaf(Object node) {
    // root is never a leaf
    if (node == root) {
      return false;
    }

    if (node instanceof TvShowTreeNode || node instanceof TvShowSeasonTreeNode) {
      return false;
    }

    if (node instanceof TvShowEpisodeTreeNode) {
      return true;
    }

    return getChildCount(node) == 0;
  }

  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void valueForPathChanged(TreePath arg0, Object arg1) {
  }
}