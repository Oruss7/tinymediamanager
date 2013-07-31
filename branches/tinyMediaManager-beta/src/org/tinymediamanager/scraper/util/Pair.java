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
package org.tinymediamanager.scraper.util;

/**
 * A Pair is simple container containing 2 paired values and types.
 * 
 * @param <First>
 *          the generic type
 * @param <Second>
 *          the generic type
 * @author seans
 */
public class Pair<First, Second> {

  /** The first. */
  private First  first;

  /** The second. */
  private Second second;

  /**
   * Instantiates a new pair.
   * 
   * @param f
   *          the f
   * @param s
   *          the s
   */
  public Pair(First f, Second s) {
    this.first = f;
    this.second = s;
  }

  /**
   * First.
   * 
   * @return the first
   */
  public First first() {
    return first;
  }

  /**
   * Second.
   * 
   * @return the second
   */
  public Second second() {
    return second;
  }
}