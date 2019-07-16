/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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


const setAlertRef = jest.fn();
const showInfoMessage = jest.fn();
const showSuccessMessage = jest.fn();
const showServerError = jest.fn();
const showServerErrorMessage = jest.fn();
const showErrorMessage = jest.fn();
const showMessage = jest.fn();

export {
  setAlertRef,
  showInfoMessage,
  showSuccessMessage,
  showServerError,
  showServerErrorMessage,
  showErrorMessage,
  showMessage
};