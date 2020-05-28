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

import { alert } from 'store/alert';
import { ServerSideExceptionInfo, BasicObject } from 'types';
import { ThunkDispatch } from 'redux-thunk';
import { AppState } from 'store/types';

const typeJsonRegex = new RegExp('application/json.*');
export default class RestServiceClient {
  restClient: AxiosInstance;

  dispatch: ThunkDispatch<AppState, any, any> | null;

  constructor(baseURL: string, axios: AxiosStatic) {
    this.restClient = axios.create({
      baseURL,
      validateStatus: () => true,
    });
    this.dispatch = null;
    this.handleResponse = this.handleResponse.bind(this);
  }

  setDispatch(dispatch: ThunkDispatch<AppState, any, any>) {
    this.dispatch = dispatch;
  }

  get<T>(url: string): Promise<T> {
    return this.restClient.get<T>(url).then(this.handleResponse);
  }

  post<T>(url: string, params: any): Promise<T> {
    return this.restClient.post<T>(url, params).then(this.handleResponse);
  }

  uploadFile<T>(url: string, file: File): Promise<T> {
    const data = new FormData();
    data.append('file', file);
    return this.restClient.post<T>(url, data).then(this.handleResponse);
  }

  put<T>(url: string, params: any): Promise<T> {
    return this.restClient.put<T>(url, params).then(this.handleResponse);
  }

  delete<T>(url: string): Promise<T> {
    return this.restClient.delete<T>(url)
      .then(this.handleResponse);
  }

  handleResponse<T>(res: AxiosResponse<T>): Promise<T> {
    if (res.status >= 200 && res.status <= 300) {
      return Promise.resolve(res.data);
    }
    if (this.dispatch !== null) {
      if (typeJsonRegex.test(res.headers['content-type'])) {
        this.dispatch(alert.showServerError(res.data as unknown as ServerSideExceptionInfo & BasicObject));
      } else {
        this.dispatch(alert.showServerErrorMessage(res.statusText));
      }
      return Promise.reject(res.status);
    }

    throw Error('Dispatch was not passed to RestServiceClient');
  }
}
