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

import axios, { AxiosInstance } from 'axios';

export default class RestServiceClient {

  restClient: AxiosInstance;

  constructor(baseURL: string) {
    this.restClient = axios.create({
      baseURL: baseURL
    });
  }

  get<T>(url: string): Promise<T> {
    return this.restClient.get<T>(url).then(res => res.data);
  }

  post<T>(url: string, params: any): Promise<T> {
    return this.restClient.post<T>(url, params).then(res => res.data);
  }

  put<T>(url: string, params: any): Promise<T> {
    return this.restClient.put<T>(url, params).then(res => res.data);
  }

  delete<T>(url: string): Promise<T> {
    return this.restClient.delete<T>(url).then(res => res.data);
  }
}
