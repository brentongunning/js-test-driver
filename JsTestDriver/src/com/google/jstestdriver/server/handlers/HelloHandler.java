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
package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.annotations.ResponseWriter;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class HelloHandler implements RequestHandler {

  private final PrintWriter writer;

  @Inject
  public HelloHandler(@ResponseWriter PrintWriter writer) {
    this.writer = writer;
  }

  public void handleIt() throws IOException {
    writer.write("hello");
    writer.flush();
  }
}
