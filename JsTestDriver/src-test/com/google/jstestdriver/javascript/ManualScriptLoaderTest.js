/*
 * Copyright 2011 Google Inc.
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


var ManualScriptLoaderTest = TestCase("ManualScriptLoaderTest");


ManualScriptLoaderTest.prototype.setUp = function() {
  this.src_ = new jstestdriver.FileSource("/test/foo.js", -1, "/test/foo.js");
  this.win_ = {};
  this.testCaseManager_ = {
    removed : null,
    updated : null,
    removeTestCaseForFilename : function(src) {
      this.removed = src;
    },
    updateLatestTestCase : function(src) {
      this.updated = src;
    }
  };

  function now() {
    return now.currentTime;
  }
  now.currentTime = 0;
  this.now_ = now;

  function onFinish(fileResult) {
    onFinish.result = fileResult;
  }
  onFinish.result = null;
  this.onFinish_ = onFinish;
  
  this.loader_ = new jstestdriver.ManualScriptLoader(this.win_,
      this.testCaseManager_,
      now);
  
};

ManualScriptLoaderTest.prototype.testBeginLoad = function() {
  this.loader_.beginLoad(this.src_, this.onFinish_);
  this.now_.currentTime++;

  assertEquals(this.src_.fileSrc, this.testCaseManager_.removed);
  assertTrue(this.win_.onerror instanceof Function);
};


ManualScriptLoaderTest.prototype.testBeginLoadAndError = function() {
  this.loader_.beginLoad(this.src_, this.onFinish_);
  var msg = 'error';
  this.now_.currentTime++;
  this.loader_.endLoad(this.src_);
  // executes after the script loads
  this.win_.onerror(msg, 'http://localhost/test/foo.js', 10);
  
  assertEquals(jstestdriver.EMPTY_FUNC, this.win_.onerror);
  var result = new jstestdriver.FileResult(
      this.src_,
      false,
      [
       'error loading file: ', this.src_.fileSrc,
       ':', 10,
       ': ', msg
      ].join(''),
      1);

  assertEquals(result, this.onFinish_.result);
  assertEquals(this.src_.fileSrc, this.testCaseManager_.removed);
};


ManualScriptLoaderTest.prototype.testBeginLoadAndComplete = function() {
  this.loader_.beginLoad(this.src_, this.onFinish_);
  this.now_.currentTime++;
  this.loader_.endLoad(this.src_);

  var result = new jstestdriver.FileResult(
      this.src_,
      true,
      "",
      1);
  
  assertEquals(result, this.onFinish_.result);
  assertEquals(this.src_.fileSrc, this.testCaseManager_.removed);
  assertEquals(this.src_.fileSrc, this.testCaseManager_.updated);
};
