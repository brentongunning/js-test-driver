/*
 * Copyright 2009 Google Inc.
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
package com.google.jstestdriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.jstestdriver.action.ConfigureGatewayAction;
import com.google.jstestdriver.action.ConfigureGatewayAction.Factory;
import com.google.jstestdriver.action.UploadAction;
import com.google.jstestdriver.browser.BrowserActionExecutorAction;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.browser.BrowserRunner;
import com.google.jstestdriver.browser.CommandLineBrowserRunner;
import com.google.jstestdriver.hooks.ActionListProcessor;
import com.google.jstestdriver.hooks.TestsPreProcessor;
import com.google.jstestdriver.output.FileNameFormatter;
import com.google.jstestdriver.output.PrintXmlTestResultsAction;
import com.google.jstestdriver.output.XmlPrinter;
import com.google.jstestdriver.output.XmlPrinterImpl;
import com.google.jstestdriver.util.NullStopWatch;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 * @author corysmith@google.com (Cory Smith)
 */
public class DefaultActionListProviderTest extends TestCase {
  Set<BrowserRunner> browsers =
    Sets.<BrowserRunner>newHashSet(
        new CommandLineBrowserRunner("browser", "", null));

  public void testParseFlagsAndCreateActionQueue() throws Exception {
    DefaultActionListProvider provider =
        createProvider(9876, 9877, false, Collections.<String> emptyList(), Collections
            .<ActionListProcessor> emptySet(), "", null);
    List<Action> actions = provider.get();

    ArrayList<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserStartupAction.class);
    assertSequence(expectedActions, actions);
  }

  private DefaultActionListProvider createProvider(int port,
                                                   int sslPort,
                                                   boolean reset,
                                                   List<String> tests,
                                                   Set<ActionListProcessor> processors,
                                                   String testOutput,
                                                   XmlPrinter xmlPrinter) {
    ActionFactory actionFactory =
        new ActionFactory(null, Collections.<TestsPreProcessor>emptySet(), false,
            null, null, new NullStopWatch());
    return new DefaultActionListProvider(
        tests,
        Collections.<String>emptyList(),
        reset,
        Collections.<String>emptyList(),
        port,
        sslPort,
        testOutput,
        processors,
        xmlPrinter,
        new ActionSequenceBuilder(
            actionFactory,
            new BrowserActionExecutorAction(
                null,
                null,
                null,
                null,
                null,
                -1,
                null,
                null, null),
            new FailureCheckerAction(null, null),
            new UploadAction(null),
            new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0))),
            null,
            newConfigureGatewayActionFactory(),
            new BrowserStartupAction(null, null, null, null, null), null),
        true);
  }

  private Factory newConfigureGatewayActionFactory() {
    return new Factory() {
      public ConfigureGatewayAction create(JsonArray gatewayConfig) {
        return new ConfigureGatewayAction(null, null, null, null, gatewayConfig);
      }
    };
  }

  public void testParseWithServerAndReset() throws Exception {
    String serverAddress = "http://otherserver:8989";
    DefaultActionListProvider parser =
        createProvider(-1, -1, true, Collections
            .<String> emptyList(), Collections.<ActionListProcessor>emptySet(), "", null);

    FlagsImpl flags = new FlagsImpl();
    flags.setServer(serverAddress);
    flags.setBrowser(Arrays.asList("browser1"));
    flags.setReset(true);

    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);

    List<Action> actions = parser.get();
    assertSequence(expectedActions, actions);
  }


  public void testParseFlagsAndCreateTestActions() throws Exception {
    List<String> tests = Arrays.asList("foo.testBar");
    DefaultActionListProvider parser =
        createProvider(9876, 9877, false, tests, Collections
            .<ActionListProcessor> emptySet(), "", null);

    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);
    expectedActions.add(ServerShutdownAction.class);
    expectedActions.add(FailureCheckerAction.class);

    List<Action> actions = parser.get();
    assertSequence(expectedActions, actions);
  }

  public void testXmlTestResultsActionIsAddedIfTestOutputFolderIsSet() throws Exception {
    List<String> tests = Arrays.asList("foo.testBar");
    DefaultActionListProvider parser =
        createProvider(9876, 9877, false, tests, Collections
            .<ActionListProcessor> emptySet(), ".", new XmlPrinterImpl(null, null, new FileNameFormatter()));

    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);
    expectedActions.add(PrintXmlTestResultsAction.class);
    expectedActions.add(ServerShutdownAction.class);
    expectedActions.add(FailureCheckerAction.class);

    List<Action> actions = parser.get();
    assertSequence(expectedActions, actions);
  }

  private void assertSequence(List<Class<? extends Action>> expectedActions,
      List<Action> actions) {
    assertNotNull(actions);
    List<Class<? extends Action>> actual = new ArrayList<Class<? extends Action>>();
    for (Action action : actions) {
      actual.add(action != null ? action.getClass() : null);
    }
    assertEquals(expectedActions, actual);
  }
}
