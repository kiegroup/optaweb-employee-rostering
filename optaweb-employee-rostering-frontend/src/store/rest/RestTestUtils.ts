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
import RestServiceClient from './RestServiceClient';

export const getAnswers: Map<string, any> = new Map();
export const postAnswers: Map<string, any> = new Map();
export const putAnswers: Map<string, any> = new Map();
export const deleteAnswers: Map<string, any> = new Map();

export function onGet(url: string, answer: any) { getAnswers.set(url, answer); }
export function onPost(url: string, params: any, answer: any) { postAnswers.set(url + JSON.stringify(params), answer); }
export function onUploadFile(url: string, file: any, answer: any) {
  postAnswers.set(url + JSON.stringify(file), answer);
}
export function onPut(url: string, params: any, answer: any) { putAnswers.set(url + JSON.stringify(params), answer); }
export function onDelete(url: string, answer: any) { deleteAnswers.set(url, answer); }

export function resetRestClientMock(mock: jest.Mocked<RestServiceClient>) {
  getAnswers.clear();
  postAnswers.clear();
  putAnswers.clear();
  deleteAnswers.clear();
  mock.get.mockClear();
  mock.post.mockClear();
  mock.put.mockClear();
  mock.delete.mockClear();
}
