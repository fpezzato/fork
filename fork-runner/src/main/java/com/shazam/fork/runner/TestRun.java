/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runner;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.shazam.fork.model.TestCaseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

class TestRun {
	private static final Logger logger = LoggerFactory.getLogger(TestRun.class);
    private final String poolName;
	private final TestRunParameters testRunParameters;
	private final List<ITestRunListener> testRunListeners;

    public TestRun(String poolName,
				   TestRunParameters testRunParameters,
				   List<ITestRunListener> testRunListeners) {
        this.poolName = poolName;
		this.testRunParameters = testRunParameters;
		this.testRunListeners = testRunListeners;
	}

	public void execute() {
		RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				testRunParameters.getTestPackage(),
				testRunParameters.getTestRunner(),
				testRunParameters.getDeviceInterface());

		TestCaseEvent test = testRunParameters.getTest();
		String testClassName = test.getTestClass();
		String testMethodName = test.getTestMethod();
		IRemoteAndroidTestRunner.TestSize testSize = testRunParameters.getTestSize();
		if (testSize != null) {
			runner.setTestSize(testSize);
		}
		runner.setRunName(poolName);
		runner.setMethodName(testClassName, testMethodName);
		runner.setMaxtimeToOutputResponse(testRunParameters.getTestOutputTimeout());
		try {
			runner.run(testRunListeners);
		} catch (ShellCommandUnresponsiveException | TimeoutException e) {
			logger.warn("Test: " + testClassName + " got stuck. You can increase the timeout in settings if it's too strict");
		} catch (AdbCommandRejectedException | IOException e) {
			throw new RuntimeException(format("Error while running test class %s", test), e);
		}
	}
}
