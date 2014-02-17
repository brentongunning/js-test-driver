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

import com.google.common.collect.Lists;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FakeJsTestDriverClient;
import com.google.jstestdriver.FileUploader;
import com.google.jstestdriver.ResponseStream;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.util.NullStopWatch;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Collections;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class BrowserManagedRunnerTest extends TestCase {

  public void testCall() throws Exception {
    String browserId = "5";
    String serverAddress = "http://foo";
    
    final BrowserInfo browserInfo = new BrowserInfo();
    browserInfo.setId(Long.parseLong(browserId));
    browserInfo.setServerReceivedHeartbeat(true);
    browserInfo.setReady(true);
    final FakeJsTestDriverClient client =
        new FakeJsTestDriverClient(Lists.newArrayList(browserInfo));
    final FakeBrowserRunner runner = new FakeBrowserRunner();
    final FakeBrowserActionRunner browserActionRunner = new FakeBrowserActionRunner();
    int browserTimeout = 70000;
    runCaptureUrlTest(browserId, serverAddress, client, runner, browserActionRunner, browserTimeout);
    assertTrue(runner.serverAddress,
      runner.serverAddress.contains(String.valueOf(browserTimeout)));
    runCaptureUrlTest(browserId, serverAddress, client, runner, browserActionRunner, 1);
    assertTrue(runner.serverAddress,
      runner.serverAddress.contains(String.valueOf(SlaveBrowser.TIMEOUT)));
  }

  /**
   * @param browserId
   * @param serverAddress
   * @param client
   * @param runner
   * @param browserActionRunner
   * @param browserTimeout
   * @throws Exception
   */
  private void runCaptureUrlTest(String browserId, String serverAddress,
      final FakeJsTestDriverClient client, final FakeBrowserRunner runner,
      final FakeBrowserActionRunner browserActionRunner, int browserTimeout) throws Exception {
    final BrowserCallable<Collection<ResponseStream>> browserRunner =
        new BrowserCallable<Collection<ResponseStream>>(browserActionRunner, browserId,
            new BrowserControl(runner, serverAddress, new NullStopWatch(), client,
                Collections.<JstdTestCase>emptyList(), browserTimeout));
    browserRunner.call();
  }

  private static final class FakeBrowserActionRunner extends BrowserActionRunner {
    public FakeBrowserActionRunner() {
      super(null, null, null, new NullStopWatch(), null, null);
    }
    
    @Override
    public Collection<ResponseStream> call() {
      return null;
    }
  }

  private static final class FakeBrowserRunner implements BrowserRunner {
    private String serverAddress;

    public void stopBrowser() {
      
    }

    public void startBrowser(String serverAddress) {
      this.serverAddress = serverAddress;
    }

    public int getTimeout() {
      return 0;
    }

    public int getNumStartupTries() {
      return 1;
    }

    public long getHeartbeatTimeout() {
      return SlaveBrowser.TIMEOUT;
    }

    public int getUploadSize() {
      return FileUploader.CHUNK_SIZE;
    }
  }
}
