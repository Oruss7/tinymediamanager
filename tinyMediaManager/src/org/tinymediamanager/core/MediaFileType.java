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

package org.tinymediamanager.core;

/**
 * various MediaFileTypes
 * 
 * @author Myron Boyle
 */
public enum MediaFileType {

  // NOTE: do not change the sort order or insert any new inbetween!
  // this renumerates the internal ID for the types and ALL saved values are wrong!
  // when you change that, we need to delete the whole DB !!!

  // @formatter:off
  VIDEO, 
  AUDIO, 
  SUBTITLE, 
  NFO, 
  POSTER, 
  FANART, 
  BANNER, 
  THUMB, 
  TRAILER, 
  EXTRAFANART, 
  GRAPHIC, 
  UNKNOWN,
  VIDEO_EXTRA; // bonus/extra videos
  // @formatter:on
}
