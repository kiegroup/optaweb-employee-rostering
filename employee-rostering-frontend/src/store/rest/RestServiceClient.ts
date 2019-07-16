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

import { AxiosInstance, AxiosResponse, AxiosStatic } from 'axios';

import { showServerError, showServerErrorMessage } from 'ui/Alerts';
import { ServerSideExceptionInfo } from 'types';

export default class RestServiceClient {

  restClient: AxiosInstance;

  constructor(baseURL: string, axios: AxiosStatic) {
    this.restClient = axios.create({
      baseURL: baseURL,
      validateStatus: () => true
    });
    this.handleResponse = this.handleResponse.bind(this);
  }

  get<T>(url: string): Promise<T> {
    return this.restClient.get<T>(url).then(this.handleResponse);
  }

  post<T>(url: string, params: any): Promise<T> {
    return this.restClient.post<T>(url, params).then(this.handleResponse);
  }

  put<T>(url: string, params: any): Promise<T> {
    return this.restClient.put<T>(url, params).then(this.handleResponse);
  }

  delete<T>(url: string): Promise<T> {
    return this.restClient.delete<T>(url)
      .then(this.handleResponse);
  }

  handleResponse<T>(res: AxiosResponse<T>): Promise<T> {
    if (200 <= res.status && res.status <= 300) {
      return Promise.resolve(res.data);
    }
    else {
      if (res.headers["content-type"] === "application/json") {
        showServerError(res.data as unknown as ServerSideExceptionInfo)
      }
      else {
        showServerErrorMessage(res.statusText);
      }
      return Promise.reject(res.status);
    }
  }
}
