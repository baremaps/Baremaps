/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/

import fs from 'fs';
import path from 'path';
import { Test } from './test';
import { TestsLogger } from './testsLogger';

/** TestManager is responsible for discovering and creating tests */
export class TestManager {
  private testsPath: string;
  private tests: Test[];

  /**
   * Create a new TestManager
   *
   * @param testsFolder path to the tests folder
   * @param integrationFolder path to the integration folder
   * @param styleUrl URL to the style
   * @param refStyleUrl URL to the reference style
   * @param testLogger logger for tests
   * @param threshold threshold for image comparison
   * @returns a new TestManager
   * @throws if the test is not setup correctly
   */
  constructor(
    testsFolder: string,
    integrationFolder: string,
    styleUrl: string,
    refStyleUrl: string,
    testLogger: TestsLogger,
    threshold: number,
  ) {
    this.testsPath = path.join(testsFolder, integrationFolder);
    this.tests = [] as Test[];
    const testNames = this.discoverTests();
    for (const testName of testNames) {
      try {
        this.tests.push(
          new Test(
            path.join(process.cwd(), this.testsPath, testName),
            styleUrl,
            refStyleUrl,
            testLogger,
            threshold,
          ),
        );
      } catch (e) {
        testLogger.logError(testName, e);
      }
    }
  }

  public getTests(): Test[] {
    return this.tests;
  }

  public discoverTests() {
    let testNames: string[];
    try {
      const basePath = path.join(process.cwd(), this.testsPath);
      // filter by directories
      testNames = fs
        .readdirSync(basePath)
        .filter((file) => fs.statSync(path.join(basePath, file)).isDirectory());
    } catch (e) {
      console.error(
        `ERROR: Folder '${this.testsPath}' could not be found in current directory`,
      );
      process.exit(1);
    }
    return testNames;
  }
}
