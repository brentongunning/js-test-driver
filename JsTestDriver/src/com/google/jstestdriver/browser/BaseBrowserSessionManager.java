/*
 * Copyright 2010 Google Inc.
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

package com.google.jstestdriver.browser;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.HeartBeatManager;
import com.google.jstestdriver.Server;
import com.google.jstestdriver.util.Sleeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the client portion of session for a given captured browser.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class BaseBrowserSessionManager implements BrowserSessionManager {
  private static final long WAIT_INTERVAL = 1000L;
  private static final Logger logger = LoggerFactory.getLogger(BaseBrowserSessionManager.class);
  private final Server server;
  private final String baseUrl;
  private final HeartBeatManager heartBeatManager;
  private final Sleeper sleeper;

  @Inject
  public BaseBrowserSessionManager(Server server, @Named("server") String baseUrl,
      HeartBeatManager heartBeatManager, Sleeper sleeper) {
    this.server = server;
    this.baseUrl = baseUrl;
    this.heartBeatManager = heartBeatManager;
    this.sleeper = sleeper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String startSession(String browserId) {
    String sessionId = server.startSession(baseUrl, browserId);

    if ("FAILED".equals(sessionId)) {
      while ("FAILED".equals(sessionId)) {
        try {
          logger.error("Currently waiting for browser: {}, with is currently in use. ", browserId);
          sleeper.sleep(WAIT_INTERVAL);
        } catch (InterruptedException e) {
          logger.error("Could not create session for browser: " + browserId);
          throw new RuntimeException("Can't start a session on the server!" + browserId);
        }
        sessionId = server.startSession(baseUrl, browserId);
      }
    }

    heartBeatManager.startTimer();
    heartBeatManager.startHeartBeat(baseUrl, browserId, sessionId);
    return sessionId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stopSession(String sessionId, String browserId) {
    heartBeatManager.cancelTimer();
    server.stopSession(baseUrl, browserId, sessionId);
  }
}