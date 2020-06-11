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
import { alert } from 'store/alert';
import { AxiosStatic } from 'axios';
import { ServerSideExceptionInfo, BasicObject } from 'types';
import RestServiceClient from './RestServiceClient';

const mockGet = jest.fn();
const mockPost = jest.fn();
const mockPut = jest.fn();
const mockDelete = jest.fn();
const mockCreate = jest.fn(() => ({
  get: mockGet,
  post: mockPost,
  put: mockPut,
  delete: mockDelete,
}));

const axios: AxiosStatic = {
  create: mockCreate,
} as any;

beforeEach(() => {
  mockGet.mockClear();
  mockPost.mockClear();
  mockPut.mockClear();
  mockDelete.mockClear();
  mockCreate.mockClear();
});

describe('Rest Service Client', () => {
  it('Should call axios constructor with correct arguments', () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    expect(mockCreate).toBeCalled();
    expect(mockCreate).toBeCalledWith({
      baseURL,
      validateStatus: expect.any(Function),
    });
    expect(restServiceClient).toEqual(expect.any(RestServiceClient));
  });

  it('Should call axios.get on get', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const handleResponseSpy = jest.spyOn(restServiceClient, 'handleResponse');
    const targetURL = '/endpoint';
    const response = {
      status: 200,
      data: {},
      statusText: 'Ok',
      headers: {},
      config: {},
    };
    mockGet.mockReturnValue(Promise.resolve(response));
    await restServiceClient.get(targetURL);

    expect(mockGet).toBeCalled();
    expect(mockGet).toBeCalledWith('/endpoint');
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it('Should call axios.post on post', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const handleResponseSpy = jest.spyOn(restServiceClient, 'handleResponse');
    const targetURL = '/endpoint';
    const data = {
      a: 'Hello',
      b: 2,
    };
    const response = {
      status: 200,
      data: {},
      statusText: 'Ok',
      headers: {},
      config: {},
    };
    mockPost.mockReturnValue(Promise.resolve(response));
    await restServiceClient.post(targetURL, data);

    expect(mockPost).toBeCalled();
    expect(mockPost).toBeCalledWith('/endpoint', data);
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it('Should call axios.post with form data on uploadFile', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const handleResponseSpy = jest.spyOn(restServiceClient, 'handleResponse');
    const targetURL = '/endpoint';
    const data = 'myfile.txt';
    const response = {
      status: 200,
      data: {},
      statusText: 'Ok',
      headers: {},
      config: {},
    };
    mockPost.mockReturnValue(Promise.resolve(response));
    await restServiceClient.uploadFile(targetURL, data as unknown as File);

    const expectedParam = new FormData();
    expectedParam.append('file', data);
    expect(mockPost).toBeCalled();
    expect(mockPost).toBeCalledWith('/endpoint', expectedParam);
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it('Should call axios.put on put', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const handleResponseSpy = jest.spyOn(restServiceClient, 'handleResponse');
    const targetURL = '/endpoint';
    const data = {
      a: 'Hello',
      b: 2,
    };
    const response = {
      status: 200,
      data: {},
      statusText: 'Ok',
      headers: {},
      config: {},
    };
    mockPut.mockReturnValue(Promise.resolve(response));
    await restServiceClient.put(targetURL, data);

    expect(mockPut).toBeCalled();
    expect(mockPut).toBeCalledWith('/endpoint', data);
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it('Should call axios.delete on delete', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const handleResponseSpy = jest.spyOn(restServiceClient, 'handleResponse');
    const targetURL = '/endpoint';
    const response = {
      status: 200,
      data: {},
      statusText: 'Ok',
      headers: {},
      config: {},
    };
    mockDelete.mockReturnValue(Promise.resolve(response));
    await restServiceClient.delete(targetURL);

    expect(mockDelete).toBeCalled();
    expect(mockDelete).toBeCalledWith('/endpoint');
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it('Should resolves to the value on success', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const data = {
      a: 'Test',
      b: 2,
    };

    const response = {
      status: 200,
      data,
      statusText: 'Ok',
      headers: {},
      config: {},
    };

    expect(restServiceClient.handleResponse(response)).resolves.toEqual(data);
  });

  it('Should reject the promise on failure and show an alert with text if not JSON', async () => {
    const dispatch = jest.fn();

    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const data = 'Error';
    restServiceClient.setDispatch(dispatch);
    const errorStatus = 'I am a teapot';
    const response = {
      status: 404,
      data,
      statusText: errorStatus,
      headers: {
        'content-type': 'text/plain',
      },
      config: {},
    };
    await expect(restServiceClient.handleResponse(response)).rejects.toEqual(404);
    expect(dispatch).toBeCalledWith(alert.showServerErrorMessage('I am a teapot'));
  });

  it('Should reject the promise on failure and show an alert of exception if JSON', async () => {
    const dispatch = jest.fn();

    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const data: ServerSideExceptionInfo & BasicObject = {
      i18nKey: 'key',
      stackTrace: [],
      exceptionMessage: 'Hi',
      messageParameters: [],
      exceptionClass: 'Clazz',
      exceptionCause: null,
    };
    restServiceClient.setDispatch(dispatch);
    const errorStatus = 'I am a teapot';
    const response = {
      status: 404,
      data,
      statusText: errorStatus,
      headers: {
        'content-type': 'application/json;charset=utf-8',
      },
      config: {},
    };
    await expect(restServiceClient.handleResponse(response)).rejects.toEqual(404);
    expect(dispatch).toBeCalledWith(alert.showServerError(data));
  });

  it('Should throw an Error if dispatch is not set', async () => {
    const baseURL = '/rest';
    const restServiceClient = new RestServiceClient(baseURL, axios);
    const data = {
      a: 'Test',
      b: 2,
    };

    const errorStatus = 'I am a teapot';
    const response = {
      status: 404,
      data,
      statusText: errorStatus,
      headers: {},
      config: {},
    };
    await expect(() => restServiceClient.handleResponse(response)).toThrow();
  });
});
